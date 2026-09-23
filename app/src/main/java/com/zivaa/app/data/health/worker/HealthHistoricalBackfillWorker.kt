package com.zivaa.app.data.health.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.logging.HealthSyncLogger
import com.zivaa.app.data.local.SyncPrefsManager
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dedicated worker for streaming deep historical health data (up to 30/90 days) into Supabase.
 *
 * ARCHITECTURAL INVARIANT:
 * This worker operates strictly backward into past time ranges and NEVER calls, clears,
 * or overwrites the live ChangesToken in SyncPrefsManager. Live incremental delta sync
 * is completely isolated in HealthDataSyncWorker.
 */
class HealthHistoricalBackfillWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "HealthHistoricalBackfill"
        private const val CHUNK_SIZE = 800
        const val WORK_NAME_PREFIX = "HistoricalBackfill_"

        fun enqueue(context: Context, patientId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = OneTimeWorkRequestBuilder<HealthHistoricalBackfillWorker>()
                .setConstraints(constraints)
                .setInputData(
                    workDataOf("patient_id" to patientId)
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "$WORK_NAME_PREFIX$patientId",
                    ExistingWorkPolicy.KEEP,
                    request
                )
        }
    }

    private var activePatientId: String? = null

    private fun logI(message: String) {
        android.util.Log.i(TAG, message)
        HealthSyncLogger.recordRawLogLine(applicationContext, activePatientId, TAG, "I", message)
    }

    private fun logW(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.w(TAG, message, throwable)
        } else {
            android.util.Log.w(TAG, message)
        }
        HealthSyncLogger.recordRawLogLine(applicationContext, activePatientId, TAG, "W", message, throwable)
    }

    private fun logE(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.e(TAG, message, throwable)
        } else {
            android.util.Log.e(TAG, message)
        }
        HealthSyncLogger.recordRawLogLine(applicationContext, activePatientId, TAG, "E", message, throwable)
    }

    override suspend fun doWork(): Result {
        if (!HealthSyncCoordinator.syncMutex.tryLock()) {
            android.util.Log.i(TAG, "Health sync or backfill is currently active. Skipping duplicate execution.")
            return Result.success()
        }
        return try {
            doWorkInternal()
        } finally {
            HealthSyncCoordinator.syncMutex.unlock()
        }
    }

    private suspend fun doWorkInternal(): Result {
        val authManager = com.zivaa.app.data.remote.AuthManager.getInstance(applicationContext)
        com.zivaa.app.data.remote.RetrofitClient.initialize(applicationContext)
        val patientId = inputData.getString("patient_id") ?: authManager.getUserId()

        if (patientId.isNullOrBlank()) {
            logE("No valid patient ID provided. Aborting historical backfill.")
            return Result.failure()
        }
        activePatientId = patientId

        val syncPrefsManager = SyncPrefsManager(applicationContext)
        val healthConnectManager = HealthConnectManager(applicationContext)
        healthConnectManager.activeUserId = patientId

        if (!healthConnectManager.isSdkAvailable() || !healthConnectManager.hasAnyPermissions()) {
            logW("Health Connect unavailable or no permissions granted. Postponing backfill.")
            return Result.retry()
        }

        if (syncPrefsManager.isBaselineBackfillComplete(patientId)) {
            logI("Baseline backfill already marked complete for patient $patientId. Exiting.")
            return Result.success()
        }

        val now = Instant.now()
        val maxLookbackDays = if (healthConnectManager.hasHistoryReadPermission()) 90L else 30L
        val maxLookbackInstant = now.minus(maxLookbackDays, ChronoUnit.DAYS)

        val savedCheckpoint = syncPrefsManager.getHistoricalLookbackProgress(patientId)
        val priorityStart = now.minus(7, ChronoUnit.DAYS)

        var windowEnd = if (savedCheckpoint > 0L) Instant.ofEpochMilli(savedCheckpoint) else priorityStart

        logI("Starting decoupled historical backfill for patient $patientId. WindowEnd: $windowEnd, MaxLookback: $maxLookbackInstant")
        HealthSyncLogger.log(
            context = applicationContext,
            userId = patientId,
            syncType = HealthSyncLogger.SyncType.BACKGROUND,
            stage = HealthSyncLogger.Stage.HISTORICAL_BACKFILL,
            status = HealthSyncLogger.Status.IN_PROGRESS,
            details = "Historical backfill starting from $windowEnd down to $maxLookbackInstant"
        )

        var totalRecordsUploaded = 0

        while (windowEnd.isAfter(maxLookbackInstant)) {
            if (isStopped) {
                logW("Historical backfill stopped by WorkManager. Checkpoint preserved at $windowEnd.")
                return Result.retry()
            }

            val targetStart = windowEnd.minus(7, ChronoUnit.DAYS)
            val windowStart = if (targetStart.isBefore(maxLookbackInstant)) maxLookbackInstant else targetStart

            logI("Backfill fetching window [$windowStart to $windowEnd]...")
            val chunkRecords = healthConnectManager.fetchAllAvailableMetrics(windowStart, windowEnd)

            if (chunkRecords.isNotEmpty()) {
                val chunkBreakdown = chunkRecords.groupBy { it::class.simpleName ?: "Record" }
                for ((mName, mList) in chunkBreakdown) {
                    HealthSyncLogger.logMetricQuery(
                        context = applicationContext,
                        userId = patientId,
                        syncType = HealthSyncLogger.SyncType.BACKGROUND,
                        metricType = mName,
                        recordCount = mList.size,
                        details = "Decoupled backfill window [$windowStart to $windowEnd]"
                    )
                }

                val chunkPayload = chunkRecords.flatMap { healthConnectManager.mapRecordToSupabase(it, patientId) }
                val uploadOk = uploadChunked(chunkPayload, patientId)
                if (!uploadOk) {
                    logW("Failed to upload backfill chunk [$windowStart to $windowEnd]. Retrying later.")
                    return Result.retry()
                }
                totalRecordsUploaded += chunkPayload.size
            }

            windowEnd = windowStart
            syncPrefsManager.setHistoricalLookbackProgress(windowEnd.toEpochMilli(), patientId)
        }

        syncPrefsManager.setBaselineBackfillComplete(true, patientId)
        syncPrefsManager.clearHistoricalLookbackProgress(patientId)

        logI("Historical backfill successfully completed for patient $patientId. Total records: $totalRecordsUploaded.")
        
        // Notify backend to trigger sliding-window 90-day baseline rollup
        try {
            com.zivaa.app.data.remote.ZivaaBackendClient.apiService.syncComplete(
                com.zivaa.app.data.remote.SyncCompletePayload(
                    patient_id = patientId,
                    timezone = java.util.TimeZone.getDefault().id,
                    sync_type = "HistoricalBackfill",
                    records_synced = totalRecordsUploaded
                )
            )
            logI("Historical backfill syncComplete notified backend successfully.")
        } catch (e: Exception) {
            logW("Failed to notify backend syncComplete for HistoricalBackfill: ${e.message}")
        }

        HealthSyncLogger.log(
            context = applicationContext,
            userId = patientId,
            syncType = HealthSyncLogger.SyncType.BACKGROUND,
            stage = HealthSyncLogger.Stage.HISTORICAL_BACKFILL,
            status = HealthSyncLogger.Status.SUCCESS,
            details = "Historical backfill completed successfully ($totalRecordsUploaded records)."
        )

        return Result.success()
    }

    private suspend fun uploadChunked(
        records: List<com.zivaa.app.data.remote.SupabaseVitalRecord>,
        patientId: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (records.isEmpty()) return@withContext true

        val uniquePayload = records.distinctBy {
            "${it.patientId}_${it.metricType}_${it.recordedAt}_${it.source}"
        }

        val chunks = uniquePayload.chunked(CHUNK_SIZE)
        for ((index, chunk) in chunks.withIndex()) {
            if (isStopped) return@withContext false

            val chunkMetrics = chunk.map { it.metricType }.distinct()
            val chunkStart = System.currentTimeMillis()
            var uploadError: Throwable? = null

            val response = try {
                com.zivaa.app.data.remote.RetrofitClient.apiService.insertRawVitals(chunk)
            } catch (e: Exception) {
                uploadError = e
                null
            }

            val durationMs = System.currentTimeMillis() - chunkStart
            val isSuccess = response != null && response.isSuccessful

            HealthSyncLogger.logUploadChunk(
                context = applicationContext,
                userId = patientId,
                syncType = HealthSyncLogger.SyncType.BACKGROUND,
                chunkIndex = index + 1,
                totalChunks = chunks.size,
                recordCount = chunk.size,
                metricTypes = chunkMetrics,
                durationMs = durationMs,
                isSuccess = isSuccess,
                httpStatus = response?.code(),
                errorBody = response?.errorBody()?.string() ?: uploadError?.message,
                exception = uploadError
            )

            if (!isSuccess) {
                logE("Chunk upload failed: HTTP ${response?.code()} - ${uploadError?.message}")
                return@withContext false
            }
        }
        return@withContext true
    }
}
