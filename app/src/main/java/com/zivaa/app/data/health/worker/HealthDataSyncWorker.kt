package com.zivaa.app.data.health.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.logging.HealthSyncLogger
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.sensors.SensorDataStore
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthDataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "HealthDataSyncWorker"
        private const val CHUNK_SIZE = 800
        private const val DELETION_CHUNK_SIZE = 50
        private val syncMutex = kotlinx.coroutines.sync.Mutex()
    }

    private var activePatientId: String? = null

    private fun logWorkerI(message: String) {
        android.util.Log.i(TAG, message)
        HealthSyncLogger.recordRawLogLine(applicationContext, activePatientId, TAG, "I", message)
    }

    private fun logWorkerW(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.w(TAG, message, throwable)
        } else {
            android.util.Log.w(TAG, message)
        }
        HealthSyncLogger.recordRawLogLine(applicationContext, activePatientId, TAG, "W", message, throwable)
    }

    private fun logWorkerE(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.e(TAG, message, throwable)
        } else {
            android.util.Log.e(TAG, message)
        }
        HealthSyncLogger.recordRawLogLine(applicationContext, activePatientId, TAG, "E", message, throwable)
    }

    override suspend fun doWork(): Result {
        if (!syncMutex.tryLock()) {
            android.util.Log.i(TAG, "Another HealthDataSyncWorker is currently running. Skipping duplicate execution.")
            return Result.success()
        }
        return try {
            doWorkInternal()
        } finally {
            syncMutex.unlock()
        }
    }

    private suspend fun doWorkInternal(): Result {
        val authManager = com.zivaa.app.data.remote.AuthManager.getInstance(applicationContext)
        com.zivaa.app.data.remote.RetrofitClient.initialize(applicationContext)
        val patientId = authManager.getUserId() ?: inputData.getString("patient_id")
        activePatientId = patientId

        val healthConnectManager = HealthConnectManager(applicationContext)
        healthConnectManager.activeUserId = patientId
        val syncPrefsManager = SyncPrefsManager(applicationContext)
        val sessionStartTime = System.currentTimeMillis()
        val syncTypeStr = inputData.getString("sync_type") ?: "Background"
        val syncType = if (syncTypeStr.equals("Foreground", ignoreCase = true)) {
            HealthSyncLogger.SyncType.FOREGROUND
        } else {
            HealthSyncLogger.SyncType.BACKGROUND
        }

        // Ensure Health Connect is available
        if (!healthConnectManager.isSdkAvailable()) {
            logWorkerE("Health Connect SDK is not available on this device. Aborting sync.")
            HealthSyncLogger.log(
                context = applicationContext,
                userId = patientId,
                syncType = syncType,
                stage = HealthSyncLogger.Stage.PERMISSION_AUDIT,
                status = HealthSyncLogger.Status.FAILURE,
                details = "Health Connect SDK is not available on this device. Aborting sync."
            )
            return Result.failure()
        }

        // Adaptive background read feature check (Android 14+ specific)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (syncType == HealthSyncLogger.SyncType.BACKGROUND && !healthConnectManager.hasBackgroundReadPermission()) {
                logWorkerI("Health Connect background read permission not granted on Android 14+. Skipping background sync until user grants or opens app.")
                HealthSyncLogger.log(
                    context = applicationContext,
                    userId = patientId,
                    syncType = syncType,
                    stage = HealthSyncLogger.Stage.PERMISSION_AUDIT,
                    status = HealthSyncLogger.Status.WARNING,
                    details = "Health Connect background read permission not granted on Android 14+. Skipping background sync."
                )
                return Result.success()
            }
        }

        // Ensure at least one health metric permission is granted so we can sync available data
        if (!healthConnectManager.hasAnyPermissions()) {
            logWorkerW("No Health Connect permissions granted. Skipping background sync.")
            HealthSyncLogger.log(
                context = applicationContext,
                userId = patientId,
                syncType = syncType,
                stage = HealthSyncLogger.Stage.PERMISSION_AUDIT,
                status = HealthSyncLogger.Status.WARNING,
                details = "No Health Connect permissions granted. Skipping sync."
            )
            return Result.success()
        }

        if (patientId == null) {
            logWorkerE("No authenticated user found (JWT/patientId is null). Cannot sync health data.")
            HealthSyncLogger.log(
                context = applicationContext,
                userId = null,
                syncType = syncType,
                stage = HealthSyncLogger.Stage.SYNC_START,
                status = HealthSyncLogger.Status.FAILURE,
                details = "No authenticated user found (JWT/patientId is null). Cannot sync health data."
            )
            return Result.failure()
        }

        HealthSyncLogger.logSyncStart(
            context = applicationContext,
            userId = patientId,
            syncType = syncType,
            trigger = syncTypeStr,
            details = "Health Connect sync session started. Health Connect SDK is available."
        )

        return try {
            val lastSyncedPatient = syncPrefsManager.getLastSyncedPatientId()
            val forceBackfill = inputData.getBoolean("force_backfill", false)
            
            if (lastSyncedPatient != patientId) {
                logWorkerI("Switched patient detected ($patientId vs $lastSyncedPatient). Updating active patient reference.")
                syncPrefsManager.setLastSyncedPatientId(patientId)
                // INVARIANT: Do NOT clear patientId's token! Each patient owns their scoped token.
            }

            try {
                val prioritiesRes = com.zivaa.app.data.remote.RetrofitClient.apiService.getSourcePriorities()
                if (prioritiesRes.isSuccessful && !prioritiesRes.body().isNullOrEmpty()) {
                    syncPrefsManager.saveSourcePriorities(prioritiesRes.body()!!)
                } else {
                    logWorkerW("Source priorities returned non-success HTTP ${prioritiesRes.code()}")
                }
            } catch (e: Exception) {
                logWorkerW("Non-blocking fetch of source priorities failed: ${e.message}", e)
            }

            val currentGranted = healthConnectManager.getGrantedPermissions()
            val tokenPermissions = syncPrefsManager.getTokenPermissions(patientId)
            val newlyGranted = currentGranted - tokenPermissions

            val permissionsExpanded = tokenPermissions.isNotEmpty() && newlyGranted.isNotEmpty()

            if (permissionsExpanded) {
                logWorkerI("Health Connect permissions expanded (new: $newlyGranted). Resetting ChangesToken to backfill new metrics.")
                HealthSyncLogger.log(
                    context = applicationContext,
                    userId = patientId,
                    syncType = syncType,
                    stage = HealthSyncLogger.Stage.PERMISSION_AUDIT,
                    status = HealthSyncLogger.Status.SUCCESS,
                    details = "Health Connect permissions expanded (new: $newlyGranted). Resetting ChangesToken to backfill new metrics."
                )
                syncPrefsManager.clearChangesToken(patientId)
            }

            val currentToken = if (forceBackfill || permissionsExpanded) null else syncPrefsManager.getChangesToken(patientId)
            val payload = mutableListOf<com.zivaa.app.data.remote.SupabaseVitalRecord>()
            var nextToken: String? = null

            if (currentToken == null) {
                // TOKEN-FIRST "SNAPSHOT-AND-TAIL" INITIALIZATION:
                // Step 1: Freeze change-feed cursor FIRST to guarantee mathematical zero gap.
                val startToken = healthConnectManager.getChangesToken()
                val now = Instant.now()
                val priorityStart = now.minus(7, ChronoUnit.DAYS)

                logWorkerI("Initializing zero-gap sync. Priority hydration window [$priorityStart to $now]")
                HealthSyncLogger.log(
                    context = applicationContext,
                    userId = patientId,
                    syncType = syncType,
                    stage = HealthSyncLogger.Stage.METRIC_QUERY,
                    details = "Priority hydration window [$priorityStart to $now] with token-first freeze."
                )

                // Step 2: Fetch Priority Hydration Window (last 7 days)
                val priorityRecords = healthConnectManager.fetchAllAvailableMetrics(priorityStart, now)
                logWorkerI("Fetched ${priorityRecords.size} records for 7-day priority window.")

                val priorityBreakdown = priorityRecords.groupBy { it::class.simpleName ?: "Record" }
                for ((mName, mList) in priorityBreakdown) {
                    HealthSyncLogger.logMetricQuery(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        metricType = mName,
                        recordCount = mList.size,
                        details = "7-day priority window hydration [$priorityStart to $now]"
                    )
                }

                if (priorityRecords.isNotEmpty()) {
                    val priorityPayload = priorityRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) }
                    val prioritySuccess = uploadRecordsToSupabase(priorityPayload, patientId, syncType)
                    if (!prioritySuccess) return Result.retry()
                    payload.addAll(priorityPayload)

                    // Immediate orchestration ping so dashboard displays fresh data
                    try {
                        val pingStart = System.currentTimeMillis()
                        val pingRes = com.zivaa.app.data.remote.ZivaaBackendClient.apiService.syncComplete(
                            com.zivaa.app.data.remote.SyncCompletePayload(
                                patient_id = patientId, 
                                timezone = java.util.TimeZone.getDefault().id, 
                                sync_type = "PriorityHydration",
                                records_synced = priorityPayload.size,
                                metric_types = priorityPayload.map { it.metricType }.distinct()
                            )
                        )
                        val pingDuration = System.currentTimeMillis() - pingStart
                        HealthSyncLogger.log(
                            context = applicationContext,
                            userId = patientId,
                            syncType = syncType,
                            stage = HealthSyncLogger.Stage.ORCHESTRATION_TRIGGER,
                            status = if (pingRes.isSuccessful) HealthSyncLogger.Status.SUCCESS else HealthSyncLogger.Status.WARNING,
                            durationMs = pingDuration,
                            details = "Priority hydration orchestration ping (HTTP ${pingRes.code()})"
                        )
                    } catch (e: Exception) {
                        logWorkerW("Priority sync complete notice failed: ${e.message}")
                    }
                }

                // Step 3: Advance live token to the pre-frozen cursor
                nextToken = startToken
                syncPrefsManager.setLastSuccessfulSyncTimestamp(System.currentTimeMillis(), patientId)

                // Step 4: If baseline historical backfill (up to 30/90 days) is incomplete,
                // trigger dedicated decoupled worker. NEVER run deep backfill in live sync!
                if (!syncPrefsManager.isBaselineBackfillComplete(patientId)) {
                    logWorkerI("Enqueuing decoupled historical backfill for patient $patientId.")
                    HealthHistoricalBackfillWorker.enqueue(applicationContext, patientId)
                }
            } else {
                // SUBSEQUENT RUNS: Use ChangesToken API with official Google pagination loop.
                try {
                    var token: String = currentToken
                    var hasMore: Boolean
                    var totalUpsertions = 0
                    var pageIndex = 0
                    val deletedRecordIds = mutableListOf<String>()
                    do {
                        if (isStopped) {
                            logWorkerW("Worker stopped during ChangesToken pagination. Halting gracefully.")
                            return Result.retry()
                        }
                        pageIndex++
                        val changesResponse = try {
                            healthConnectManager.getChanges(token)
                        } catch (e: Exception) {
                            logWorkerE("Error reading changes from token [${token.take(12)}...]: ${e.javaClass.simpleName} - ${e.message}", e)
                            HealthSyncLogger.log(
                                context = applicationContext,
                                userId = patientId,
                                syncType = syncType,
                                stage = HealthSyncLogger.Stage.CHANGES_QUERY,
                                status = HealthSyncLogger.Status.FAILURE,
                                details = "Error reading changes from token [${token.take(12)}...]",
                                exception = e
                            )
                            null
                        }
                        if (changesResponse != null) {
                            val upsertions = changesResponse.changes.filterIsInstance<androidx.health.connect.client.changes.UpsertionChange>()
                            val deletions = changesResponse.changes.filterIsInstance<androidx.health.connect.client.changes.DeletionChange>()

                            totalUpsertions += upsertions.size
                            payload.addAll(upsertions.flatMap { healthConnectManager.mapRecordToSupabase(it.record, patientId) })
                            deletedRecordIds.addAll(deletions.map { it.recordId })

                            // Metric breakdown logging for ChangesToken
                            val upsertBreakdown = upsertions.groupBy { it.record::class.simpleName ?: "Record" }
                            if (upsertBreakdown.isNotEmpty()) {
                                for ((mName, mList) in upsertBreakdown) {
                                    val origins = mList.map { it.record.metadata.dataOrigin.packageName }.distinct()
                                    HealthSyncLogger.logChangesPage(
                                        context = applicationContext,
                                        userId = patientId,
                                        syncType = syncType,
                                        metricType = mName,
                                        recordCount = mList.size,
                                        pageIndex = pageIndex,
                                        hasMore = changesResponse.hasMore,
                                        details = "Origins: $origins"
                                    )
                                }
                            } else if (deletions.isEmpty()) {
                                HealthSyncLogger.log(
                                    context = applicationContext,
                                    userId = patientId,
                                    syncType = syncType,
                                    stage = HealthSyncLogger.Stage.CHANGES_QUERY,
                                    metricType = "ALL_METRICS",
                                    recordCount = 0,
                                    status = HealthSyncLogger.Status.SUCCESS,
                                    details = "Token page $pageIndex returned 0 changes (hasMore: ${changesResponse.hasMore})"
                                )
                            }

                            if (deletions.isNotEmpty()) {
                                HealthSyncLogger.log(
                                    context = applicationContext,
                                    userId = patientId,
                                    syncType = syncType,
                                    stage = HealthSyncLogger.Stage.DELETION_DETECTED,
                                    metricType = "DELETION_RECORDS",
                                    recordCount = deletions.size,
                                    status = HealthSyncLogger.Status.SUCCESS,
                                    details = "Deletions detected: ${deletions.map { it.recordId }.take(5)}"
                                )
                            }

                            token = changesResponse.nextChangesToken
                            hasMore = changesResponse.hasMore
                        } else {
                            if (nextToken == null) {
                                logWorkerW("Null changes response without active nextToken. Clearing token for $patientId.")
                                syncPrefsManager.clearChangesToken(patientId)
                            }
                            break
                        }
                    } while (hasMore)
                    nextToken = token
                    logWorkerI("ChangesToken pagination complete: $totalUpsertions upsertions, ${deletedRecordIds.size} deletions for patient $patientId.")

                    // Process deletions in Supabase
                    if (deletedRecordIds.isNotEmpty()) {
                        for (delChunk in deletedRecordIds.chunked(DELETION_CHUNK_SIZE)) {
                            val delStart = System.currentTimeMillis()
                            try {
                                val filterStr = "in.(${delChunk.joinToString(",")})"
                                val delRes = com.zivaa.app.data.remote.RetrofitClient.apiService.deleteRawVitalsByHealthConnectIds(
                                    patientIdQuery = "eq.$patientId",
                                    healthConnectIdInQuery = filterStr
                                )
                                val delDuration = System.currentTimeMillis() - delStart
                                val isSuccess = delRes.isSuccessful
                                HealthSyncLogger.logDeletions(
                                    context = applicationContext,
                                    userId = patientId,
                                    syncType = syncType,
                                    recordCount = delChunk.size,
                                    durationMs = delDuration,
                                    isSuccess = isSuccess,
                                    details = if (isSuccess) "Purged ${delChunk.size} deleted records from Supabase." else "HTTP ${delRes.code()} - ${delRes.errorBody()?.string()}"
                                )
                                if (isSuccess) {
                                    logWorkerI("Successfully purged ${delChunk.size} deleted Health Connect records from Supabase.")
                                } else {
                                    logWorkerW("Failed to purge deleted records from Supabase: HTTP ${delRes.code()} - ${delRes.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                val delDuration = System.currentTimeMillis() - delStart
                                HealthSyncLogger.logDeletions(
                                    context = applicationContext,
                                    userId = patientId,
                                    syncType = syncType,
                                    recordCount = delChunk.size,
                                    durationMs = delDuration,
                                    isSuccess = false,
                                    details = "Exception purging deletions: ${e.message}",
                                    exception = e
                                )
                                logWorkerE("Exception purging deleted Health Connect records from Supabase: ${e.message}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logWorkerE("Changes token expired or unrecoverable error: ${e.message}. Recovering via zero-gap token refresh.", e)
                    HealthSyncLogger.log(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        stage = HealthSyncLogger.Stage.CHANGES_QUERY,
                        status = HealthSyncLogger.Status.WARNING,
                        details = "Changes token expired. Recovering incrementally from last successful sync.",
                        exception = e
                    )
                    // Token-first recovery:
                    val recoverToken = healthConnectManager.getChangesToken()
                    val lastSyncTs = syncPrefsManager.getLastSuccessfulSyncTimestamp(patientId)
                    val sinceInstant = if (lastSyncTs > 0L) Instant.ofEpochMilli(lastSyncTs) else Instant.now().minus(7, ChronoUnit.DAYS)
                    val recoveredRecords = healthConnectManager.fetchAllAvailableMetrics(sinceInstant, Instant.now())
                    if (recoveredRecords.isNotEmpty()) {
                        payload.addAll(recoveredRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
                    }
                    nextToken = recoverToken
                }
            }

            // --- DETERMINISTIC SESSION VITALS ANCHOR ---
            // Local SQLite range query (<10ms) for low-frequency session metrics (Sleep, Resting HR).
            // Guarantees zero gaps and handles OEM batching / stage recalculations idempotently.
            val anchorNow = Instant.now()
            val anchorStart = anchorNow.minus(24, ChronoUnit.HOURS)

            val sessionSleep = healthConnectManager.readRecordsSafely(
                androidx.health.connect.client.records.SleepSessionRecord::class,
                anchorStart,
                anchorNow
            )
            if (sessionSleep.isNotEmpty()) {
                payload.addAll(sessionSleep.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
            }

            val sessionRestingHr = healthConnectManager.readRecordsSafely(
                androidx.health.connect.client.records.RestingHeartRateRecord::class,
                anchorStart,
                anchorNow
            )
            if (sessionRestingHr.isNotEmpty()) {
                payload.addAll(sessionRestingHr.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
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
                HealthSyncLogger.log(
                    context = applicationContext,
                    userId = patientId,
                    syncType = syncType,
                    stage = HealthSyncLogger.Stage.DEDUPLICATION_MAPPING,
                    metricType = "ALL_METRICS",
                    recordCount = payload.size,
                    status = HealthSyncLogger.Status.SUCCESS,
                    details = "Payload prepared for Supabase batch upload. Unique items: ${payload.size}"
                )

                val allSuccessful = uploadRecordsToSupabase(payload, patientId, syncType)

                if (isStopped) {
                    logWorkerW("Worker stopped during chunk uploads. Retrying.")
                    HealthSyncLogger.log(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        stage = HealthSyncLogger.Stage.SYNC_RETRY,
                        status = HealthSyncLogger.Status.RETRY_QUEUED,
                        details = "Worker stopped during chunk uploads. Retrying."
                    )
                    return Result.retry()
                }

                if (!allSuccessful) {
                    logWorkerW("One or more chunks failed to sync to Supabase. Scheduling retry without advancing ChangesToken.")
                    HealthSyncLogger.log(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        stage = HealthSyncLogger.Stage.SYNC_RETRY,
                        status = HealthSyncLogger.Status.RETRY_QUEUED,
                        details = "One or more chunks failed to sync to Supabase. Scheduling retry without advancing ChangesToken."
                    )
                    return Result.retry()
                } else {
                    syncPrefsManager.setLastSuccessfulSyncTimestamp(System.currentTimeMillis(), patientId)
                    // Ping the backend to orchestrate post-sync tasks (Tripwire, Morning Briefing, etc.)
                    try {
                        val totalRecords = payload.size
                        val metricTypes = payload.map { it.metricType }.distinct()
                        
                        val orchStart = System.currentTimeMillis()
                        val syncRes = com.zivaa.app.data.remote.ZivaaBackendClient.apiService.syncComplete(
                            com.zivaa.app.data.remote.SyncCompletePayload(
                                patient_id = patientId, 
                                timezone = java.util.TimeZone.getDefault().id, 
                                sync_type = syncTypeStr,
                                records_synced = totalRecords,
                                metric_types = metricTypes
                            )
                        )
                        val orchDuration = System.currentTimeMillis() - orchStart
                        HealthSyncLogger.log(
                            context = applicationContext,
                            userId = patientId,
                            syncType = syncType,
                            stage = HealthSyncLogger.Stage.ORCHESTRATION_TRIGGER,
                            status = if (syncRes.isSuccessful) HealthSyncLogger.Status.SUCCESS else HealthSyncLogger.Status.WARNING,
                            durationMs = orchDuration,
                            details = "Post-sync orchestration ping (HTTP ${syncRes.code()})"
                        )
                        if (syncRes.isSuccessful) {
                            logWorkerI("Sync complete orchestration triggered successfully ($syncTypeStr).")
                        } else {
                            logWorkerW("Sync complete orchestration returned HTTP ${syncRes.code()}: ${syncRes.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        logWorkerE("Failed to trigger sync complete orchestration: ${e.message}", e)
                    }
                }
            }

            // If everything succeeded (or there were 0 records to sync but we got a valid token), save the next token
            if (nextToken != null) {
                syncPrefsManager.saveChangesToken(nextToken, patientId)
                syncPrefsManager.saveTokenPermissions(currentGranted, patientId)
                logWorkerI("Saved next ChangesToken and footprint (${currentGranted.size} types) for patient $patientId.")
            }

            val sessionDuration = System.currentTimeMillis() - sessionStartTime
            val metricSummary = payload.groupBy { it.metricType }.mapValues { it.value.size }
            HealthSyncLogger.logSyncSuccess(
                context = applicationContext,
                userId = patientId,
                syncType = syncType,
                totalRecords = payload.size,
                durationMs = sessionDuration,
                metricSummary = metricSummary,
                details = "ChangesToken advanced. Next sync scheduled."
            )

            Result.success()
        } catch (e: SecurityException) {
            logWorkerE("SecurityException: Health Connect background read not permitted or permissions missing: ${e.message}", e)
            HealthSyncLogger.logSyncFailure(
                context = applicationContext,
                userId = patientId,
                syncType = syncType,
                stage = HealthSyncLogger.Stage.PERMISSION_AUDIT,
                exception = e,
                details = "Health Connect background read not permitted or permissions revoked."
            )
            Result.failure()
        } catch (e: Exception) {
            logWorkerE("Unexpected error in HealthDataSyncWorker execution: ${e.javaClass.simpleName} - ${e.message}", e)
            HealthSyncLogger.logSyncFailure(
                context = applicationContext,
                userId = patientId,
                syncType = syncType,
                stage = HealthSyncLogger.Stage.SYNC_FAILED,
                exception = e,
                details = "Unexpected error in HealthDataSyncWorker execution. Retrying.",
                willRetry = true
            )
            Result.retry()
        }
    }

    private suspend fun uploadRecordsToSupabase(
        records: List<com.zivaa.app.data.remote.SupabaseVitalRecord>,
        patientId: String,
        syncType: HealthSyncLogger.SyncType
    ): Boolean = withContext(Dispatchers.IO) {
        if (records.isEmpty()) return@withContext true
        val uniquePayload = records.distinctBy { 
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

        for ((index, chunk) in chunks.withIndex()) {
            if (isStopped) {
                logWorkerW("Worker stopped before uploading chunk ${index + 1}/${chunks.size}")
                HealthSyncLogger.log(
                    context = applicationContext,
                    userId = patientId,
                    syncType = syncType,
                    stage = HealthSyncLogger.Stage.BATCH_UPLOAD,
                    recordCount = chunk.size,
                    status = HealthSyncLogger.Status.WARNING,
                    details = "Worker stopped before uploading chunk ${index + 1}/${chunks.size}"
                )
                return@withContext false
            }
            val chunkMetrics = chunk.map { it.metricType }.distinct()
            val chunkStartTime = System.currentTimeMillis()
            logWorkerI("Syncing chunk ${index + 1}/${chunks.size} (${chunk.size} records) [sequential]...")
            
            var uploadException: Throwable? = null
            val response = try {
                com.zivaa.app.data.remote.RetrofitClient.apiService.insertRawVitals(chunk)
            } catch (e: Exception) {
                uploadException = e
                logWorkerE("Exception syncing chunk ${index + 1}/${chunks.size}: ${e.message}", e)
                null
            }
            val chunkDuration = System.currentTimeMillis() - chunkStartTime
            val isSuccess = response != null && response.isSuccessful
            val statusCode = response?.code()
            val errorStr = if (!isSuccess) {
                response?.errorBody()?.string() ?: (uploadException?.message ?: "Unknown network error or null response")
            } else null

            HealthSyncLogger.logUploadChunk(
                context = applicationContext,
                userId = patientId,
                syncType = syncType,
                chunkIndex = index + 1,
                totalChunks = chunks.size,
                recordCount = chunk.size,
                metricTypes = chunkMetrics,
                durationMs = chunkDuration,
                isSuccess = isSuccess,
                httpStatus = statusCode,
                errorBody = errorStr,
                exception = uploadException
            )

            if (!isSuccess) {
                logWorkerE("Failed to sync chunk ${index + 1}/${chunks.size}: HTTP $statusCode - $errorStr")
                return@withContext false
            }
        }
        true
    }
}
