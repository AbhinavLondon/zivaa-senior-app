package com.zivaa.app.data.health.logging

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * HealthSyncLogger
 *
 * Dedicated per-user logger purely focused on data sync between Health Connect and the Zivaa App.
 *
 * Key features:
 * 1. Per-user file isolation: Each user has their own log file (e.g. sync_user_usr_123.log) in private app storage.
 * 2. Real-time Logcat stream: Emits structured, machine-parseable log lines under TAG "HealthSyncLogger"
 *    so laptop scripts (e.g. Sync-UserLogs.ps1) can route events dynamically to individual laptop log files.
 * 3. File Rolling & Retention: Caps file size at 2MB per user with a .1 rollover backup to protect storage.
 * 4. Rich telemetry: Captures timestamps (ISO-8601), user ID, foreground/background context, metric types,
 *    record counts, upload latencies, deletion purges, and full exception traces.
 */
object HealthSyncLogger {

    const val TAG = "HealthSyncLogger"
    private const val LOGS_DIR_NAME = "health_sync_logs"
    private const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024L // 2 MB per user file

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val logcatDateFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss.SSS")
    private val sessionLogBuffers = java.util.concurrent.ConcurrentHashMap<String, MutableList<String>>()

    enum class SyncType {
        BACKGROUND,
        FOREGROUND,
        UNKNOWN
    }

    enum class Stage {
        SYNC_START,
        PERMISSION_AUDIT,
        CHANGES_QUERY,
        METRIC_QUERY,
        DEDUPLICATION_MAPPING,
        BATCH_UPLOAD,
        DELETION_DETECTED,
        DELETION_PURGE,
        FALLBACK_TRIGGER,
        ORCHESTRATION_TRIGGER,
        SYNC_SUCCESS,
        SYNC_FAILED,
        SYNC_RETRY
    }

    enum class Status {
        SUCCESS,
        FAILURE,
        WARNING,
        IN_PROGRESS,
        RETRY_QUEUED,
        SKIPPED
    }

    /**
     * Primary logging method. Dispatches asynchronously to background I/O to avoid blocking sync workers.
     */
    fun log(
        context: Context,
        userId: String?,
        syncType: SyncType,
        stage: Stage,
        metricType: String? = null,
        recordCount: Int? = null,
        status: Status? = null,
        durationMs: Long? = null,
        trigger: String? = null,
        details: String? = null,
        exception: Throwable? = null
    ) {
        val safeUserId = if (userId.isNullOrBlank()) "unauthenticated" else userId
        val timestamp = ZonedDateTime.now().format(isoFormatter)

        // Build single-line header for Logcat & file
        val sb = StringBuilder()
        sb.append("[$timestamp]")
        sb.append(" [${syncType.name}]")
        sb.append(" [USER: $safeUserId]")
        sb.append(" [STAGE: ${stage.name}]")
        
        if (!trigger.isNullOrBlank()) {
            sb.append(" [TRIGGER: $trigger]")
        }
        if (!metricType.isNullOrBlank()) {
            sb.append(" [METRIC: $metricType]")
        }
        if (recordCount != null) {
            sb.append(" [RECORDS: $recordCount]")
        }
        if (status != null) {
            sb.append(" [STATUS: ${status.name}]")
        }
        if (durationMs != null) {
            sb.append(" [LATENCY: ${durationMs}ms]")
        }
        if (!details.isNullOrBlank()) {
            sb.append(" $details")
        }

        val logLine = sb.toString()

        // 1. Emit to Logcat immediately with structured prefix for laptop ADB streaming
        val logcatMsg = "[HEALTH_SYNC] $logLine"
        if (exception != null || status == Status.FAILURE) {
            Log.e(TAG, logcatMsg, exception)
        } else if (status == Status.WARNING) {
            Log.w(TAG, logcatMsg)
        } else {
            Log.i(TAG, logcatMsg)
        }

        // 2. Persist to on-device per-user log file asynchronously
        scope.launch {
            writeToFile(context.applicationContext, safeUserId, logLine, exception)
        }
    }

    /**
     * Records an exact Logcat-style line (e.g. `09-19 20:58:38.540 I/HealthDataSyncWorker(26072): ...`)
     * directly into the user's isolated local file and session telemetry buffer.
     */
    fun recordRawLogLine(
        context: Context,
        userId: String?,
        tag: String,
        level: String,
        message: String,
        throwable: Throwable? = null
    ) {
        val safeUserId = if (userId.isNullOrBlank()) "unauthenticated" else userId
        val now = java.time.LocalDateTime.now().format(logcatDateFormatter)
        val pid = android.os.Process.myPid()
        val line = "$now $level/$tag($pid): $message"

        val buffer = sessionLogBuffers.computeIfAbsent(safeUserId) {
            java.util.Collections.synchronizedList(mutableListOf())
        }
        buffer.add(line)

        if (throwable != null) {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            for (stackLine in sw.toString().lines().take(15)) {
                if (stackLine.isNotBlank()) {
                    val sLine = "$now $level/$tag($pid): \t$stackLine"
                    buffer.add(sLine)
                }
            }
        }

        scope.launch {
            writeRawLineToFile(context.applicationContext, safeUserId, line, throwable)
        }
    }

    fun popSessionLogs(userId: String?): List<String> {
        val safeUserId = if (userId.isNullOrBlank()) "unauthenticated" else userId
        return sessionLogBuffers.remove(safeUserId)?.toList() ?: emptyList()
    }

    // --- Convenience Helper Methods ---

    fun logSyncStart(
        context: Context,
        userId: String?,
        syncType: SyncType,
        trigger: String,
        details: String? = null
    ) {
        log(
            context = context,
            userId = userId,
            syncType = syncType,
            stage = Stage.SYNC_START,
            trigger = trigger,
            status = Status.IN_PROGRESS,
            details = details ?: "Initializing Health Connect sync session."
        )
    }

    fun logMetricQuery(
        context: Context,
        userId: String?,
        syncType: SyncType,
        metricType: String,
        recordCount: Int,
        durationMs: Long? = null,
        details: String? = null
    ) {
        log(
            context = context,
            userId = userId,
            syncType = syncType,
            stage = Stage.METRIC_QUERY,
            metricType = metricType,
            recordCount = recordCount,
            status = Status.SUCCESS,
            durationMs = durationMs,
            details = details
        )
    }

    fun logChangesPage(
        context: Context,
        userId: String?,
        syncType: SyncType,
        metricType: String,
        recordCount: Int,
        pageIndex: Int,
        hasMore: Boolean,
        details: String? = null
    ) {
        val pageInfo = "Token page $pageIndex (hasMore: $hasMore)"
        val fullDetails = if (details != null) "$pageInfo. $details" else pageInfo
        log(
            context = context,
            userId = userId,
            syncType = syncType,
            stage = Stage.CHANGES_QUERY,
            metricType = metricType,
            recordCount = recordCount,
            status = Status.SUCCESS,
            details = fullDetails
        )
    }

    fun logUploadChunk(
        context: Context,
        userId: String?,
        syncType: SyncType,
        chunkIndex: Int,
        totalChunks: Int,
        recordCount: Int,
        metricTypes: List<String>,
        durationMs: Long,
        isSuccess: Boolean,
        httpStatus: Int? = null,
        errorBody: String? = null,
        exception: Throwable? = null
    ) {
        val metricsStr = metricTypes.distinct().joinToString(",")
        val status = if (isSuccess) Status.SUCCESS else Status.FAILURE
        val details = if (isSuccess) {
            "Chunk $chunkIndex/$totalChunks uploaded to Supabase (HTTP ${httpStatus ?: 200})."
        } else {
            "Chunk $chunkIndex/$totalChunks failed (HTTP ${httpStatus ?: "ERR"})${if (!errorBody.isNullOrBlank()) " - $errorBody" else ""}."
        }

        log(
            context = context,
            userId = userId,
            syncType = syncType,
            stage = Stage.BATCH_UPLOAD,
            metricType = metricsStr,
            recordCount = recordCount,
            status = status,
            durationMs = durationMs,
            details = details,
            exception = exception
        )
    }

    fun logDeletions(
        context: Context,
        userId: String?,
        syncType: SyncType,
        recordCount: Int,
        durationMs: Long? = null,
        isSuccess: Boolean,
        details: String? = null,
        exception: Throwable? = null
    ) {
        log(
            context = context,
            userId = userId,
            syncType = syncType,
            stage = Stage.DELETION_PURGE,
            metricType = "DELETION_RECORDS",
            recordCount = recordCount,
            status = if (isSuccess) Status.SUCCESS else Status.FAILURE,
            durationMs = durationMs,
            details = details ?: if (isSuccess) "Purged $recordCount deleted records from Supabase." else "Failed to purge deletions.",
            exception = exception
        )
    }

    fun logSyncSuccess(
        context: Context,
        userId: String?,
        syncType: SyncType,
        totalRecords: Int,
        durationMs: Long,
        metricSummary: Map<String, Int>,
        details: String? = null
    ) {
        val summaryStr = if (metricSummary.isNotEmpty()) {
            "Summary: " + metricSummary.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        } else {
            "No new records to sync."
        }
        val fullDetails = if (!details.isNullOrBlank()) "$summaryStr. $details" else summaryStr

        log(
            context = context,
            userId = userId,
            syncType = syncType,
            stage = Stage.SYNC_SUCCESS,
            metricType = "METRICS_SUMMARY",
            recordCount = totalRecords,
            status = Status.SUCCESS,
            durationMs = durationMs,
            details = fullDetails
        )

        val rawLogs = popSessionLogs(userId)
        dispatchCloudAuditLog(
            patientId = userId,
            syncType = syncType,
            status = Status.SUCCESS,
            recordsSynced = totalRecords,
            metricSummary = metricSummary,
            durationMs = durationMs,
            stage = Stage.SYNC_SUCCESS,
            details = fullDetails,
            rawLogLines = rawLogs
        )
    }

    fun logSyncFailure(
        context: Context,
        userId: String?,
        syncType: SyncType,
        stage: Stage,
        exception: Throwable,
        details: String? = null,
        willRetry: Boolean = false
    ) {
        val status = if (willRetry) Status.RETRY_QUEUED else Status.FAILURE
        log(
            context = context,
            userId = userId,
            syncType = syncType,
            stage = stage,
            status = status,
            details = details ?: exception.message,
            exception = exception
        )

        val rawLogs = popSessionLogs(userId)
        dispatchCloudAuditLog(
            patientId = userId,
            syncType = syncType,
            status = status,
            recordsSynced = 0,
            stage = stage,
            errorMessage = "${exception.javaClass.name}: ${exception.message}",
            details = details,
            rawLogLines = rawLogs
        )
    }

    /**
     * Dispatches telemetry to Supabase `sync_logs` table so remote users' sync logs
     * can be retrieved on your laptop from anywhere in the world.
     */
    fun dispatchCloudAuditLog(
        patientId: String?,
        syncType: SyncType,
        status: Status,
        recordsSynced: Int,
        metricSummary: Map<String, Int>? = null,
        durationMs: Long? = null,
        stage: Stage? = null,
        errorMessage: String? = null,
        details: String? = null,
        rawLogLines: List<String>? = null
    ) {
        scope.launch {
            try {
                val isUuid = patientId != null && Regex("^[0-9a-fA-F-]{36}$").matches(patientId)
                val metadata = mutableMapOf<String, Any>()
                if (metricSummary != null) metadata["metrics"] = metricSummary
                if (durationMs != null) metadata["duration_ms"] = durationMs
                if (stage != null) metadata["stage"] = stage.name
                if (!details.isNullOrBlank()) metadata["details"] = details
                if (!isUuid && !patientId.isNullOrBlank()) metadata["raw_user_id"] = patientId
                if (!rawLogLines.isNullOrEmpty()) metadata["raw_log_lines"] = rawLogLines

                val payload = com.zivaa.app.data.remote.SupabaseSyncLogRecord(
                    patient_id = if (isUuid) patientId else null,
                    sync_type = syncType.name,
                    status = status.name,
                    records_synced = recordsSynced,
                    metric_types = if (metadata.isNotEmpty()) metadata else null,
                    error_message = errorMessage,
                    timestamp = ZonedDateTime.now().format(isoFormatter)
                )

                val res = com.zivaa.app.data.remote.RetrofitClient.apiService.insertSyncLog(payload)
                if (res.isSuccessful) {
                    Log.d(TAG, "Cloud sync audit log dispatched to Supabase for user $patientId ($status, $recordsSynced records, ${rawLogLines?.size ?: 0} log lines)")
                } else {
                    Log.w(TAG, "Failed to dispatch cloud sync audit log: HTTP ${res.code()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Non-blocking dispatch of cloud sync audit log encountered error: ${e.message}")
            }
        }
    }

    // --- File Storage & Rolling Management ---

    fun getLogsDirectory(context: Context): File {
        val dir = File(context.filesDir, LOGS_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getLogFile(context: Context, userId: String): File {
        val safeId = sanitizeFilename(userId)
        return File(getLogsDirectory(context), "sync_user_$safeId.log")
    }

    fun getAllUserLogFiles(context: Context): List<File> {
        val dir = getLogsDirectory(context)
        return dir.listFiles { file -> file.isFile && file.name.startsWith("sync_user_") }?.toList() ?: emptyList()
    }

    private suspend fun writeRawLineToFile(
        context: Context,
        userId: String,
        line: String,
        throwable: Throwable?
    ) = mutex.withLock {
        try {
            val file = getLogFile(context, userId)
            if (file.exists() && file.length() >= MAX_FILE_SIZE_BYTES) {
                rollFile(file)
            }
            FileWriter(file, true).use { fw ->
                PrintWriter(fw).use { pw ->
                    pw.println(line)
                    if (throwable != null) {
                        throwable.printStackTrace(pw)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing raw line to user sync log file: ${e.message}", e)
        }
    }

    private suspend fun writeToFile(
        context: Context,
        userId: String,
        line: String,
        exception: Throwable?
    ) = mutex.withLock {
        try {
            val file = getLogFile(context, userId)

            // Check if file rollover is required
            if (file.exists() && file.length() >= MAX_FILE_SIZE_BYTES) {
                rollFile(file)
            }

            FileWriter(file, true).use { fw ->
                PrintWriter(fw).use { pw ->
                    pw.println(line)
                    if (exception != null) {
                        pw.println("    Exception: ${exception.javaClass.name}: ${exception.message}")
                        val sw = StringWriter()
                        exception.printStackTrace(PrintWriter(sw))
                        val frames = sw.toString().lines().take(8)
                        for (frame in frames) {
                            pw.println("        $frame")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing to user sync log file: ${e.message}", e)
        }
    }

    private fun rollFile(activeFile: File) {
        try {
            val backupFile = File(activeFile.parentFile, "${activeFile.name}.1")
            if (backupFile.exists()) {
                backupFile.delete()
            }
            activeFile.renameTo(backupFile)
        } catch (e: Exception) {
            Log.w(TAG, "Failed rolling log file ${activeFile.name}: ${e.message}")
        }
    }

    private fun sanitizeFilename(input: String): String {
        return input.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    fun readRecentLogs(context: Context, userId: String, maxLines: Int = 200): String {
        val file = getLogFile(context, userId)
        if (!file.exists()) return "No logs found for user $userId"
        return try {
            file.readLines().takeLast(maxLines).joinToString("\n")
        } catch (e: Exception) {
            "Error reading log file: ${e.message}"
        }
    }

    fun clearLogs(context: Context, userId: String) {
        try {
            val file = getLogFile(context, userId)
            if (file.exists()) file.delete()
            val backup = File(file.parentFile, "${file.name}.1")
            if (backup.exists()) backup.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing logs for user $userId: ${e.message}")
        }
    }
}
