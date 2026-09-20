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
        val authManager = com.zivaa.app.data.remote.AuthManager.getInstance(applicationContext)
        com.zivaa.app.data.remote.RetrofitClient.initialize(applicationContext)
        val patientId = authManager.getUserId()
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
            val forceBackfill = inputData.getBoolean("force_backfill", false) || (lastSyncedPatient != patientId)
            
            if (lastSyncedPatient != patientId) {
                logWorkerI("New or switched patient detected ($patientId vs $lastSyncedPatient). Clearing previous token.")
                HealthSyncLogger.log(
                    context = applicationContext,
                    userId = patientId,
                    syncType = syncType,
                    stage = HealthSyncLogger.Stage.SYNC_START,
                    details = "New or switched patient detected ($patientId vs $lastSyncedPatient). Clearing previous ChangesToken."
                )
                syncPrefsManager.clearChangesToken(patientId)
                syncPrefsManager.setLastSyncedPatientId(patientId)
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

            val permissionsExpanded = (tokenPermissions.isNotEmpty() && newlyGranted.isNotEmpty()) ||
                    (tokenPermissions.isEmpty() && syncPrefsManager.getChangesToken(patientId) != null)

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
                // FIRST RUN / FORCE BACKFILL / TOKEN RECOVERY:
                val lastSyncTs = syncPrefsManager.getLastSuccessfulSyncTimestamp(patientId)
                val now = Instant.now()
                var totalRecordsUploaded = 0

                if (lastSyncTs > 0L && !forceBackfill) {
                    // RESILIENT TOKEN EXPIRY RECOVERY:
                    // Recover incrementally from last successful sync timestamp instead of a full 90-day backfill
                    val sinceInstant = Instant.ofEpochMilli(lastSyncTs)
                    logWorkerI("ChangesToken expired/reset. Recovering incrementally from $sinceInstant to $now")
                    HealthSyncLogger.log(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        stage = HealthSyncLogger.Stage.METRIC_QUERY,
                        details = "ChangesToken expired/reset. Recovering incrementally from $sinceInstant to $now"
                    )
                    var windowEnd = now
                    while (windowEnd.isAfter(sinceInstant)) {
                        if (isStopped) return Result.retry()
                        val windowStart = if (windowEnd.minus(7, ChronoUnit.DAYS).isBefore(sinceInstant)) sinceInstant else windowEnd.minus(7, ChronoUnit.DAYS)
                        val windowRecords = healthConnectManager.fetchAllAvailableMetrics(windowStart, windowEnd)
                        if (windowRecords.isNotEmpty()) {
                            val windowBreakdown = windowRecords.groupBy { it::class.simpleName ?: "Record" }
                            for ((mName, mList) in windowBreakdown) {
                                HealthSyncLogger.logMetricQuery(
                                    context = applicationContext,
                                    userId = patientId,
                                    syncType = syncType,
                                    metricType = mName,
                                    recordCount = mList.size,
                                    details = "Incremental recovery window [$windowStart to $windowEnd]"
                                )
                            }
                            val windowPayload = windowRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) }
                            val success = uploadRecordsToSupabase(windowPayload, patientId, syncType)
                            if (!success) return Result.retry()
                            totalRecordsUploaded += windowPayload.size
                        }
                        windowEnd = windowStart
                    }
                } else {
                    // FIRST RUN / FULL BASELINE BACKFILL:
                    val savedCheckpoint = if (forceBackfill) 0L else syncPrefsManager.getHistoricalLookbackProgress(patientId)
                    val priorityStart = now.minus(7, ChronoUnit.DAYS)

                    if (savedCheckpoint == 0L) {
                        // 1. Fetch & UPLOAD Priority Window (last 7 days) FIRST for instant dashboard hydration
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
                            totalRecordsUploaded += priorityPayload.size

                            // Immediate orchestration ping so dashboard displays fresh data without waiting for deep history
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
                        syncPrefsManager.setHistoricalLookbackProgress(priorityStart.toEpochMilli(), patientId)
                    } else {
                        logWorkerI("Resuming deep history backfill from checkpoint: ${Instant.ofEpochMilli(savedCheckpoint)}")
                        HealthSyncLogger.log(
                            context = applicationContext,
                            userId = patientId,
                            syncType = syncType,
                            stage = HealthSyncLogger.Stage.METRIC_QUERY,
                            details = "Resuming deep history backfill from checkpoint: ${Instant.ofEpochMilli(savedCheckpoint)}"
                        )
                    }

                    // 2. Stream remaining deep history in rolling 7-day windows
                    // Health Connect enforces 30-day lookback limit unless READ_HEALTH_DATA_HISTORY is granted
                    val maxLookbackDays = if (healthConnectManager.hasHistoryReadPermission()) 90L else 30L
                    val maxLookbackInstant = now.minus(maxLookbackDays, ChronoUnit.DAYS)
                    var windowEnd = if (savedCheckpoint > 0L) Instant.ofEpochMilli(savedCheckpoint) else priorityStart

                    while (windowEnd.isAfter(maxLookbackInstant)) {
                        if (isStopped) return Result.retry()
                        val windowStart = if (windowEnd.minus(7, ChronoUnit.DAYS).isBefore(maxLookbackInstant)) maxLookbackInstant else windowEnd.minus(7, ChronoUnit.DAYS)
                        val chunkRecords = healthConnectManager.fetchAllAvailableMetrics(windowStart, windowEnd)
                        if (chunkRecords.isNotEmpty()) {
                            val chunkBreakdown = chunkRecords.groupBy { it::class.simpleName ?: "Record" }
                            for ((mName, mList) in chunkBreakdown) {
                                HealthSyncLogger.logMetricQuery(
                                    context = applicationContext,
                                    userId = patientId,
                                    syncType = syncType,
                                    metricType = mName,
                                    recordCount = mList.size,
                                    details = "Historical backfill window [$windowStart to $windowEnd]"
                                )
                            }
                            val chunkPayload = chunkRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) }
                            val chunkSuccess = uploadRecordsToSupabase(chunkPayload, patientId, syncType)
                            if (!chunkSuccess) return Result.retry()
                            totalRecordsUploaded += chunkPayload.size
                        }
                        windowEnd = windowStart
                        syncPrefsManager.setHistoricalLookbackProgress(windowEnd.toEpochMilli(), patientId)
                    }

                    syncPrefsManager.setBaselineBackfillComplete(true, patientId)
                    syncPrefsManager.clearHistoricalLookbackProgress(patientId)
                }

                // 3. Obtain a fresh ChangesToken for future incremental runs
                nextToken = healthConnectManager.getChangesToken()
                syncPrefsManager.setLastSuccessfulSyncTimestamp(System.currentTimeMillis(), patientId)
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
                    logWorkerE("Changes token expired or unrecoverable error: ${e.message}. Clearing token and scheduling retry.", e)
                    HealthSyncLogger.log(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        stage = HealthSyncLogger.Stage.CHANGES_QUERY,
                        status = HealthSyncLogger.Status.FAILURE,
                        details = "Changes token expired or unrecoverable error. Clearing token and scheduling retry.",
                        exception = e
                    )
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
                    syncPrefsManager.setLastSleepSyncDate(nowStr, patientId)
                }
                if (hasSteps) {
                    syncPrefsManager.setLastStepsSyncDate(nowStr, patientId)
                }
                if (hasHeartRate) {
                    syncPrefsManager.setLastHeartRateSyncTime(nowMs, patientId)
                }

                // --- SMART 2-STRIKE FALLBACK CHECK ---
                val lastSleepDate = syncPrefsManager.getLastSleepSyncDate(patientId)
                val isPast10AM = java.time.LocalTime.now().isAfter(java.time.LocalTime.of(10, 0))
                val sleepMissing = (lastSleepDate != nowStr) && isPast10AM

                val lastStepsDate = syncPrefsManager.getLastStepsSyncDate(patientId)
                val isPast11AM = java.time.LocalTime.now().isAfter(java.time.LocalTime.of(11, 0))
                val stepsMissing = (lastStepsDate != nowStr) && isPast11AM

                val lastHrTime = syncPrefsManager.getLastHeartRateSyncTime(patientId)
                val hrMissing = (lastHrTime == 0L) || ((nowMs - lastHrTime) > (4 * 60 * 60 * 1000L))

                val isForeground = syncType == HealthSyncLogger.SyncType.FOREGROUND
                val attemptsToday = syncPrefsManager.getFallbackAttemptsCount(nowStr, patientId)
                val lastAttemptTs = syncPrefsManager.getLastFallbackTimestamp(patientId)
                val cooldownPassed = (nowMs - lastAttemptTs) > (3 * 60 * 60 * 1000L)

                val shouldFallback = (sleepMissing || stepsMissing || hrMissing) &&
                        (isForeground || (attemptsToday < 2 && cooldownPassed))

                if (shouldFallback) {
                    logWorkerI("Fallback Triggered (Sleep: $sleepMissing, Steps: $stepsMissing, HR: $hrMissing, Attempt: ${attemptsToday + 1}/2, Foreground: $isForeground)")
                    HealthSyncLogger.log(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        stage = HealthSyncLogger.Stage.FALLBACK_TRIGGER,
                        status = HealthSyncLogger.Status.WARNING,
                        details = "Fallback Triggered (Sleep: $sleepMissing, Steps: $stepsMissing, HR: $hrMissing, Attempt: ${attemptsToday + 1}/2, Foreground: $isForeground)"
                    )
                    syncPrefsManager.recordFallbackAttempt(nowStr, patientId)
                    syncPrefsManager.setLastFallbackAttemptDate(nowStr, patientId)

                    val fallbackRecords = healthConnectManager.fetchAllAvailableMetrics(2)
                    val fallbackBreakdown = fallbackRecords.groupBy { it::class.simpleName ?: "Record" }
                    for ((mName, mList) in fallbackBreakdown) {
                        HealthSyncLogger.logMetricQuery(
                            context = applicationContext,
                            userId = patientId,
                            syncType = syncType,
                            metricType = mName,
                            recordCount = mList.size,
                            details = "Smart fallback scan (last 2 days)"
                        )
                    }

                    payload.addAll(fallbackRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })

                    if (fallbackRecords.any { it::class.simpleName == "SleepSessionRecord" }) {
                        syncPrefsManager.setLastSleepSyncDate(nowStr, patientId)
                    }
                    if (fallbackRecords.any { it::class.simpleName == "StepsRecord" }) {
                        syncPrefsManager.setLastStepsSyncDate(nowStr, patientId)
                    }
                    if (fallbackRecords.any { it::class.simpleName == "HeartRateRecord" }) {
                        syncPrefsManager.setLastHeartRateSyncTime(System.currentTimeMillis(), patientId)
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
                    HealthSyncLogger.logMetricQuery(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        metricType = "StepsRecord",
                        recordCount = todaySteps.size,
                        details = "Safety net: Hydrated authentic today steps."
                    )
                    payload.addAll(todaySteps.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
                    syncPrefsManager.setLastStepsSyncDate(todayLocalDate.toString(), patientId)
                }
            }

            // Safety net: Ensure today's Heart Rate is never missing if Health Connect has recorded heart rate
            val hasTodayHrInPayload = payload.any { record ->
                record.metricType == "HeartRateRecord" && try {
                    Instant.parse(record.recordedAt).isAfter(todayStart)
                } catch (e: Exception) {
                    false
                }
            }
            if (!hasTodayHrInPayload) {
                val todayHr = healthConnectManager.fetchTodayHeartRateRecords()
                if (todayHr.isNotEmpty()) {
                    HealthSyncLogger.logMetricQuery(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        metricType = "HeartRateRecord",
                        recordCount = todayHr.size,
                        details = "Safety net: Hydrated authentic today heart rate."
                    )
                    payload.addAll(todayHr.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
                    syncPrefsManager.setLastHeartRateSyncTime(System.currentTimeMillis(), patientId)
                }
            }

            // Safety net: Ensure recent Sleep is never missing if Health Connect has recorded sleep sessions
            val hasRecentSleepInPayload = payload.any { record ->
                record.metricType == "SleepSessionRecord"
            }
            if (!hasRecentSleepInPayload) {
                val recentSleep = healthConnectManager.fetchRecentSleepRecords()
                if (recentSleep.isNotEmpty()) {
                    HealthSyncLogger.logMetricQuery(
                        context = applicationContext,
                        userId = patientId,
                        syncType = syncType,
                        metricType = "SleepSessionRecord",
                        recordCount = recentSleep.size,
                        details = "Safety net: Hydrated authentic recent sleep sessions."
                    )
                    payload.addAll(recentSleep.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) })
                    syncPrefsManager.setLastSleepSyncDate(todayLocalDate.toString(), patientId)
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
