package com.zivaa.app.data.health.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.sensors.SensorDataStore
import java.time.Instant

class HealthDataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val healthConnectManager = HealthConnectManager(applicationContext)
        val syncPrefsManager = SyncPrefsManager(applicationContext)

        // Ensure Health Connect is available
        if (!healthConnectManager.isSdkAvailable()) {
            return Result.failure()
        }

        // Ensure the background read feature is available
        if (!healthConnectManager.isBackgroundReadAvailable()) {
            return Result.failure()
        }

        // Check if we have the permissions (including background read)
        if (!healthConnectManager.hasAllPermissions()) {
            return Result.failure()
        }

        // Initialize AuthManager so background syncs have the JWT token
        val authManager = com.zivaa.app.data.remote.AuthManager(applicationContext)
        com.zivaa.app.data.remote.RetrofitClient.authManager = authManager

        val patientId = authManager.getUserId()
        if (patientId == null) {
            println("No authenticated user found. Cannot sync health data.")
            return Result.failure()
        }

        return try {
            val lastSyncedPatient = syncPrefsManager.getLastSyncedPatientId()
            val forceBackfill = inputData.getBoolean("force_backfill", false) || (lastSyncedPatient != patientId)
            
            if (lastSyncedPatient != patientId) {
                println("New or switched patient detected ($patientId vs $lastSyncedPatient). Clearing previous token.")
                syncPrefsManager.clearChangesToken(patientId)
                syncPrefsManager.setLastSyncedPatientId(patientId)
            }

            val currentToken = if (forceBackfill) null else syncPrefsManager.getChangesToken(patientId)
            val payload = mutableListOf<com.zivaa.app.data.remote.SupabaseVitalRecord>()
            var nextToken: String? = null

            if (currentToken == null) {
                // FIRST RUN / FORCE BACKFILL: No token exists for this patient.
                // 1. Fetch 90 days of backfill data.
                val rawRecords = healthConnectManager.fetchAllAvailableMetrics()
                println("No token or force backfill for $patientId. Fetched ${rawRecords.size} records for backfill.")
                
                payload.addAll(rawRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })

                // 2. Obtain a new ChangesToken for future runs
                nextToken = healthConnectManager.getChangesToken()
            } else {
                // SUBSEQUENT RUNS: Use ChangesToken API as primary source.
                try {
                    val changesResponse = healthConnectManager.getChanges(currentToken)
                    if (changesResponse != null) {
                        val upsertions = changesResponse.changes.filterIsInstance<androidx.health.connect.client.changes.UpsertionChange>()
                        
                        println("[SYNC METHOD] Normal ChangesToken returned ${upsertions.size} upsertions for patient $patientId.")
                        
                        payload.addAll(upsertions.flatMap { healthConnectManager.mapRecordToSupabase(it.record, patientId) })
                        nextToken = changesResponse.nextChangesToken
                    } else {
                        return Result.retry()
                    }
                } catch (e: Exception) {
                    println("Changes token expired or error: ${e.message}. Clearing token.")
                    syncPrefsManager.clearChangesToken(patientId)
                    return Result.retry()
                }

                // --- ANOMALY DETECTION ---
                val nowStr = java.time.LocalDate.now().toString()
                
                // 1. Update trackers if we found data via ChangesToken
                val hasSleep = payload.any { it.metricType == "SleepSessionRecord" }
                val hasHeartRate = payload.any { it.metricType == "HeartRateRecord" }
                
                if (hasSleep) {
                    syncPrefsManager.setLastSleepSyncDate(nowStr)
                }
                if (hasHeartRate) {
                    syncPrefsManager.setLastHeartRateSyncTime(System.currentTimeMillis())
                }

                // 2. Check for Sleep Anomaly
                val lastSleepDate = syncPrefsManager.getLastSleepSyncDate()
                val isPast10AM = java.time.LocalTime.now().isAfter(java.time.LocalTime.of(10, 0))
                val lastFallbackAttempt = syncPrefsManager.getLastFallbackAttemptDate()
                
                val sleepMissing = (lastSleepDate != nowStr) && isPast10AM
                val canAttemptFallback = (lastFallbackAttempt != nowStr)
                
                // 3. Check for Heart Rate Anomaly (missing for > 4 hours)
                val lastHrTime = syncPrefsManager.getLastHeartRateSyncTime()
                val hrMissing = lastHrTime > 0 && (System.currentTimeMillis() - lastHrTime) > (4 * 60 * 60 * 1000L)

                if ((sleepMissing || hrMissing) && canAttemptFallback) {
                    println("[SYNC METHOD] Anomaly Detected (Sleep Missing: $sleepMissing, HR Missing: $hrMissing). Triggering 3-day Fallback.")
                    syncPrefsManager.setLastFallbackAttemptDate(nowStr) // Mark fallback attempt for today
                    
                    val fallbackRecords = healthConnectManager.fetchAllAvailableMetrics(3)
                    payload.addAll(fallbackRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
                    
                    if (fallbackRecords.any { it::class.simpleName == "SleepSessionRecord" }) {
                         syncPrefsManager.setLastSleepSyncDate(nowStr)
                    }
                    if (fallbackRecords.any { it::class.simpleName == "HeartRateRecord" }) {
                         syncPrefsManager.setLastHeartRateSyncTime(System.currentTimeMillis())
                    }
                }
            }

            // Sync to backend if we have records
            
            // Append phone sensor data
            val sensorData = com.zivaa.app.data.sensors.SensorDataStore(applicationContext).getAndClearDailyData()
            for ((key, value) in sensorData) {
                payload.add(
                    com.zivaa.app.data.remote.SupabaseVitalRecord(
                        patientId = patientId,
                        metricType = "PhoneSensorRecord",
                        recordedAt = Instant.now().toString(),
                        values = mapOf(key to value),
                        source = "zivaa_phone_sensors"
                    )
                )
            }

            if (payload.isNotEmpty()) {
                val uniquePayload = payload.distinctBy { 
                    it.patientId + "_" + it.metricType + "_" + it.recordedAt + "_" + it.source 
                }
                val chunks = uniquePayload.chunked(500)
                var allSuccessful = true
                for (chunk in chunks) {
                    val response = com.zivaa.app.data.remote.RetrofitClient.apiService.insertRawVitals(chunk)
                    if (!response.isSuccessful) {
                        println("Failed to sync chunk: ${response.code()} ${response.errorBody()?.string()}")
                        allSuccessful = false
                    }
                }

                if (!allSuccessful) {
                    // Don't save the new token if sync fails, so we retry fetching these changes later
                    return Result.retry()
                } else {
                    // Ping the backend to orchestrate post-sync tasks (Tripwire, Morning Briefing, etc.)
                    try {
                        val totalRecords = uniquePayload.size
                        val metricTypes = uniquePayload.map { it.metricType }.distinct()
                        
                        com.zivaa.app.data.remote.ZivaaBackendClient.apiService.syncComplete(
                            com.zivaa.app.data.remote.SyncCompletePayload(
                                patient_id = patientId, 
                                timezone = java.util.TimeZone.getDefault().id, 
                                sync_type = "Background",
                                records_synced = totalRecords,
                                metric_types = metricTypes
                            )
                        )
                        println("Sync complete orchestration triggered successfully.")
                    } catch (e: Exception) {
                        println("Failed to trigger sync complete orchestration: ${e.message}")
                    }
                }
            }

            // If everything succeeded (or there were 0 records to sync but we got a valid token), save the next token
            if (nextToken != null) {
                syncPrefsManager.saveChangesToken(nextToken, patientId)
                println("Saved next ChangesToken for patient $patientId.")
            }

            // Sync Daily Aggregations logic removed: Aggregation is now handled seamlessly by a Postgres trigger in Supabase!
            
            Result.success()
        } catch (e: SecurityException) {
            println("SecurityException: Background read not permitted or missing permissions. ${e.message}")
            Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
