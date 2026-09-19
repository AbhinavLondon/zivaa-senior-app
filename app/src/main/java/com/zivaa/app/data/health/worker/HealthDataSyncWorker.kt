package com.zivaa.app.data.health.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.sensors.SensorDataStore
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class HealthDataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "HealthDataSyncWorker"
        private const val CHUNK_SIZE = 800
        private const val MAX_CONCURRENT_UPLOADS = 3
        private const val DELETION_CHUNK_SIZE = 50
        private const val NOTIFICATION_ID = 40401
    }

    override suspend fun doWork(): Result {
        val healthConnectManager = HealthConnectManager(applicationContext)
        val syncPrefsManager = SyncPrefsManager(applicationContext)

        // Ensure Health Connect is available
        if (!healthConnectManager.isSdkAvailable()) {
            android.util.Log.e(TAG, "Health Connect SDK is not available on this device. Aborting sync.")
            return Result.failure()
        }

        // Adaptive background read feature check (Android 14+ specific)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val syncType = inputData.getString("sync_type") ?: "Background"
            if (syncType.equals("Background", ignoreCase = true) && !healthConnectManager.isBackgroundReadAvailable()) {
                android.util.Log.w(TAG, "Health Connect background read feature is unavailable on this Android 14+ device.")
            }
        }

        // Ensure at least one health metric permission is granted so we can sync available data
        if (!healthConnectManager.hasAnyPermissions()) {
            android.util.Log.w(TAG, "No Health Connect permissions granted. Skipping background sync.")
            return Result.success()
        }

        // Initialize AuthManager so background syncs have the JWT token thread-safely
        val authManager = com.zivaa.app.data.remote.AuthManager.getInstance(applicationContext)
        com.zivaa.app.data.remote.RetrofitClient.initialize(applicationContext)

        val patientId = authManager.getUserId()
        if (patientId == null) {
            android.util.Log.e(TAG, "No authenticated user found (JWT/patientId is null). Cannot sync health data.")
            return Result.failure()
        }

        return try {
            val lastSyncedPatient = syncPrefsManager.getLastSyncedPatientId()
            val syncType = inputData.getString("sync_type") ?: "Background"
            val forceBackfill = inputData.getBoolean("force_backfill", false) || (lastSyncedPatient != patientId)
            
            if (lastSyncedPatient != patientId) {
                android.util.Log.i(TAG, "New or switched patient detected ($patientId vs $lastSyncedPatient). Clearing previous token.")
                syncPrefsManager.clearChangesToken(patientId)
                syncPrefsManager.setLastSyncedPatientId(patientId)
            }

            try {
                val prioritiesRes = com.zivaa.app.data.remote.RetrofitClient.apiService.getSourcePriorities()
                if (prioritiesRes.isSuccessful && !prioritiesRes.body().isNullOrEmpty()) {
                    syncPrefsManager.saveSourcePriorities(prioritiesRes.body()!!)
                } else {
                    android.util.Log.w(TAG, "Source priorities returned non-success HTTP ${prioritiesRes.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Non-blocking fetch of source priorities failed: ${e.message}", e)
            }

            val currentGranted = healthConnectManager.getGrantedPermissions()
            val tokenPermissions = syncPrefsManager.getTokenPermissions(patientId)
            val newlyGranted = currentGranted - tokenPermissions

            val permissionsExpanded = (tokenPermissions.isNotEmpty() && newlyGranted.isNotEmpty()) ||
                    (tokenPermissions.isEmpty() && syncPrefsManager.getChangesToken(patientId) != null)

            if (permissionsExpanded) {
                android.util.Log.i(TAG, "Health Connect permissions expanded (new: $newlyGranted). Resetting ChangesToken to backfill new metrics.")
                syncPrefsManager.clearChangesToken(patientId)
            }

            val currentToken = if (forceBackfill || permissionsExpanded) null else syncPrefsManager.getChangesToken(patientId)
            val payload = mutableListOf<com.zivaa.app.data.remote.SupabaseVitalRecord>()
            var nextToken: String? = null

            if (currentToken == null) {
                // Elevate to Foreground Service for initial baseline backfill to lift 10-minute OS timeout
                try {
                    setForeground(createForegroundInfo())
                } catch (e: Exception) {
                    android.util.Log.w(TAG, "Could not set foreground info for baseline backfill: ${e.message}")
                }

                // FIRST RUN / FORCE BACKFILL: No token exists for this patient.
                // 1. Fetch Priority Window (last 7 days) first for instant dashboard hydration
                val now = Instant.now()
                val priorityStart = now.minus(7, ChronoUnit.DAYS)
                val priorityRecords = healthConnectManager.fetchAllAvailableMetrics(priorityStart, now)
                android.util.Log.i(TAG, "Fetched ${priorityRecords.size} records for 7-day priority window.")
                payload.addAll(priorityRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })

                // 2. Fetch remaining deep history (days 8 to 90)
                val deepHistoryStart = now.minus(90, ChronoUnit.DAYS)
                val deepHistoryRecords = healthConnectManager.fetchAllAvailableMetrics(deepHistoryStart, priorityStart)
                android.util.Log.i(TAG, "Fetched ${deepHistoryRecords.size} records for 8-90 day history window.")
                payload.addAll(deepHistoryRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })

                val allRecords = priorityRecords + deepHistoryRecords

                // 3. Initialize tracking dates so initial sync doesn't immediately trigger fallbacks
                val nowStr = java.time.LocalDate.now().toString()
                if (allRecords.any { it::class.simpleName == "SleepSessionRecord" }) {
                    syncPrefsManager.setLastSleepSyncDate(nowStr)
                }
                if (allRecords.any { it::class.simpleName == "StepsRecord" }) {
                    syncPrefsManager.setLastStepsSyncDate(nowStr)
                }
                if (allRecords.any { it::class.simpleName == "HeartRateRecord" }) {
                    syncPrefsManager.setLastHeartRateSyncTime(System.currentTimeMillis())
                }

                syncPrefsManager.setBaselineBackfillComplete(true, patientId)

                // 4. Obtain a new ChangesToken for future runs
                nextToken = healthConnectManager.getChangesToken()
            } else {
                // SUBSEQUENT RUNS: Use ChangesToken API with official Google pagination loop.
                try {
                    var token: String = currentToken
                    var hasMore: Boolean
                    var totalUpsertions = 0
                    val deletedRecordIds = mutableListOf<String>()
                    do {
                        if (isStopped) {
                            android.util.Log.w(TAG, "Worker stopped during ChangesToken pagination. Halting gracefully.")
                            return Result.retry()
                        }
                        val changesResponse = try {
                            healthConnectManager.getChanges(token)
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Error reading changes from token [${token.take(12)}...]: ${e.javaClass.simpleName} - ${e.message}", e)
                            null
                        }
                        if (changesResponse != null) {
                            val upsertions = changesResponse.changes.filterIsInstance<androidx.health.connect.client.changes.UpsertionChange>()
                            val deletions = changesResponse.changes.filterIsInstance<androidx.health.connect.client.changes.DeletionChange>()

                            totalUpsertions += upsertions.size
                            payload.addAll(upsertions.flatMap { healthConnectManager.mapRecordToSupabase(it.record, patientId) })
                            deletedRecordIds.addAll(deletions.map { it.recordId })

                            token = changesResponse.nextChangesToken
                            hasMore = changesResponse.hasMore
                        } else {
                            if (nextToken == null) {
                                android.util.Log.w(TAG, "Null changes response without active nextToken. Clearing token for $patientId.")
                                syncPrefsManager.clearChangesToken(patientId)
                            }
                            break
                        }
                    } while (hasMore)
                    nextToken = token
                    android.util.Log.i(TAG, "ChangesToken pagination complete: $totalUpsertions upsertions, ${deletedRecordIds.size} deletions for patient $patientId.")

                    // Process deletions in Supabase
                    if (deletedRecordIds.isNotEmpty()) {
                        for (delChunk in deletedRecordIds.chunked(DELETION_CHUNK_SIZE)) {
                            try {
                                val filterStr = "in.(${delChunk.joinToString(",")})"
                                val delRes = com.zivaa.app.data.remote.RetrofitClient.apiService.deleteRawVitalsByHealthConnectIds(
                                    patientIdQuery = "eq.$patientId",
                                    healthConnectIdInQuery = filterStr
                                )
                                if (delRes.isSuccessful) {
                                    android.util.Log.i(TAG, "Successfully purged ${delChunk.size} deleted Health Connect records from Supabase.")
                                } else {
                                    android.util.Log.w(TAG, "Failed to purge deleted records from Supabase: HTTP ${delRes.code()} - ${delRes.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e(TAG, "Exception purging deleted Health Connect records from Supabase: ${e.message}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Changes token expired or unrecoverable error: ${e.message}. Clearing token and scheduling retry.", e)
                    syncPrefsManager.clearChangesToken(patientId)
                    return Result.retry()
                }

                // --- METRIC STATE TRACKING ---
                val nowStr = java.time.LocalDate.now().toString()
                val nowMs = System.currentTimeMillis()

                val hasSleep = payload.any { it.metricType == "SleepSessionRecord" }
                val hasSteps = payload.any { it.metricType == "StepsRecord" }
                val hasHeartRate = payload.any { it.metricType == "HeartRateRecord" }

                if (hasSleep) {
                    syncPrefsManager.setLastSleepSyncDate(nowStr)
                }
                if (hasSteps) {
                    syncPrefsManager.setLastStepsSyncDate(nowStr)
                }
                if (hasHeartRate) {
                    syncPrefsManager.setLastHeartRateSyncTime(nowMs)
                }

                // --- SMART 2-STRIKE FALLBACK CHECK ---
                val lastSleepDate = syncPrefsManager.getLastSleepSyncDate()
                val isPast10AM = java.time.LocalTime.now().isAfter(java.time.LocalTime.of(10, 0))
                val sleepMissing = (lastSleepDate != nowStr) && isPast10AM

                val lastStepsDate = syncPrefsManager.getLastStepsSyncDate()
                val isPast11AM = java.time.LocalTime.now().isAfter(java.time.LocalTime.of(11, 0))
                val stepsMissing = (lastStepsDate != nowStr) && isPast11AM

                val lastHrTime = syncPrefsManager.getLastHeartRateSyncTime()
                val hrMissing = (lastHrTime == 0L) || ((nowMs - lastHrTime) > (4 * 60 * 60 * 1000L))

                val isForeground = syncType.equals("Foreground", ignoreCase = true)
                val attemptsToday = syncPrefsManager.getFallbackAttemptsCount(nowStr)
                val lastAttemptTs = syncPrefsManager.getLastFallbackTimestamp()
                val cooldownPassed = (nowMs - lastAttemptTs) > (3 * 60 * 60 * 1000L)

                val shouldFallback = (sleepMissing || stepsMissing || hrMissing) &&
                        (isForeground || (attemptsToday < 2 && cooldownPassed))

                if (shouldFallback) {
                    android.util.Log.i(TAG, "Fallback Triggered (Sleep: $sleepMissing, Steps: $stepsMissing, HR: $hrMissing, Attempt: ${attemptsToday + 1}/2, Foreground: $isForeground)")
                    syncPrefsManager.recordFallbackAttempt(nowStr)
                    syncPrefsManager.setLastFallbackAttemptDate(nowStr)

                    val fallbackRecords = healthConnectManager.fetchAllAvailableMetrics(2)
                    payload.addAll(fallbackRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })

                    if (fallbackRecords.any { it::class.simpleName == "SleepSessionRecord" }) {
                        syncPrefsManager.setLastSleepSyncDate(nowStr)
                    }
                    if (fallbackRecords.any { it::class.simpleName == "StepsRecord" }) {
                        syncPrefsManager.setLastStepsSyncDate(nowStr)
                    }
                    if (fallbackRecords.any { it::class.simpleName == "HeartRateRecord" }) {
                        syncPrefsManager.setLastHeartRateSyncTime(System.currentTimeMillis())
                    }
                }
            }

            // Safety net: Ensure today's steps are never missing if Health Connect has recorded steps
            val todayLocalDate = java.time.LocalDate.now()
            val todayStart = todayLocalDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
            val hasTodayStepsInPayload = payload.any { record ->
                record.metricType == "StepsRecord" && try {
                    Instant.parse(record.recordedAt).isAfter(todayStart)
                } catch (e: Exception) {
                    false
                }
            }
            if (!hasTodayStepsInPayload) {
                val todaySteps = healthConnectManager.fetchTodayStepsRecords()
                if (todaySteps.isNotEmpty()) {
                    payload.addAll(todaySteps.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
                    syncPrefsManager.setLastStepsSyncDate(todayLocalDate.toString())
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
                val metricPriority = mapOf(
                    "HeartRateRecord" to 1,
                    "SleepSessionRecord" to 2,
                    "StepsRecord" to 3,
                    "OxygenSaturationRecord" to 4,
                    "BloodPressureRecord" to 5,
                    "RestingHeartRateRecord" to 6,
                    "BloodGlucoseRecord" to 7,
                    "BodyTemperatureRecord" to 8
                )
                val sortedPayload = uniquePayload.sortedBy { metricPriority[it.metricType] ?: Int.MAX_VALUE }
                val chunks = sortedPayload.chunked(CHUNK_SIZE)
                var allSuccessful = true
                val semaphore = Semaphore(MAX_CONCURRENT_UPLOADS)
                coroutineScope {
                    val deferredUploads = chunks.mapIndexed { index, chunk ->
                        async(Dispatchers.IO) {
                            if (isStopped) {
                                android.util.Log.w(TAG, "Worker stopped before uploading chunk ${index + 1}/${chunks.size}")
                                return@async false
                            }
                            semaphore.withPermit {
                                if (isStopped) {
                                    android.util.Log.w(TAG, "Worker stopped before acquiring permit for chunk ${index + 1}/${chunks.size}")
                                    return@withPermit false
                                }
                                android.util.Log.i(TAG, "Syncing chunk ${index + 1}/${chunks.size} (${chunk.size} records) [concurrency active]...")
                                val response = try {
                                    com.zivaa.app.data.remote.RetrofitClient.apiService.insertRawVitals(chunk)
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "Exception syncing chunk ${index + 1}/${chunks.size}: ${e.message}", e)
                                    null
                                }
                                if (response == null || !response.isSuccessful) {
                                    val errorStr = response?.errorBody()?.string() ?: "Network error or null response"
                                    android.util.Log.e(TAG, "Failed to sync chunk ${index + 1}/${chunks.size}: HTTP ${response?.code()} - $errorStr")
                                    false
                                } else {
                                    true
                                }
                            }
                        }
                    }
                    allSuccessful = deferredUploads.awaitAll().all { it }
                }

                if (isStopped) {
                    android.util.Log.w(TAG, "Worker stopped during chunk uploads. Retrying.")
                    return Result.retry()
                }

                if (!allSuccessful) {
                    android.util.Log.w(TAG, "One or more chunks failed to sync to Supabase. Scheduling retry without advancing ChangesToken.")
                    // Don't save the new token if sync fails, so we retry fetching these changes later
                    return Result.retry()
                } else {
                    // Ping the backend to orchestrate post-sync tasks (Tripwire, Morning Briefing, etc.)
                    try {
                        val totalRecords = uniquePayload.size
                        val metricTypes = uniquePayload.map { it.metricType }.distinct()
                        
                        val syncRes = com.zivaa.app.data.remote.ZivaaBackendClient.apiService.syncComplete(
                            com.zivaa.app.data.remote.SyncCompletePayload(
                                patient_id = patientId, 
                                timezone = java.util.TimeZone.getDefault().id, 
                                sync_type = syncType,
                                records_synced = totalRecords,
                                metric_types = metricTypes
                            )
                        )
                        if (syncRes.isSuccessful) {
                            android.util.Log.i(TAG, "Sync complete orchestration triggered successfully ($syncType).")
                        } else {
                            android.util.Log.w(TAG, "Sync complete orchestration returned HTTP ${syncRes.code()}: ${syncRes.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Failed to trigger sync complete orchestration: ${e.message}", e)
                    }
                }
            }

            // If everything succeeded (or there were 0 records to sync but we got a valid token), save the next token
            if (nextToken != null) {
                syncPrefsManager.saveChangesToken(nextToken, patientId)
                syncPrefsManager.saveTokenPermissions(currentGranted, patientId)
                android.util.Log.i(TAG, "Saved next ChangesToken and footprint (${currentGranted.size} types) for patient $patientId.")
            }

            // Sync Daily Aggregations logic removed: Aggregation is now handled seamlessly by a Postgres trigger in Supabase!
            
            Result.success()
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException: Health Connect background read not permitted or permissions missing: ${e.message}", e)
            Result.failure()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Unexpected error in HealthDataSyncWorker execution: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.retry()
        }
    }

    private fun createForegroundInfo(): androidx.work.ForegroundInfo {
        val channelId = "zivaa_health_sync_channel"
        val notificationManager = applicationContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Health Data Synchronization",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress during initial or deep health baseline synchronization"
            }
            notificationManager?.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Zivaa Health Sync")
            .setContentText("Securing your health baseline...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .build()

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            androidx.work.ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            androidx.work.ForegroundInfo(NOTIFICATION_ID, notification, 0)
        } else {
            androidx.work.ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
