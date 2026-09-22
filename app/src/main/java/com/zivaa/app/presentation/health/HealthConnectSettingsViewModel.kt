package com.zivaa.app.presentation.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.worker.HealthDataSyncWorker
import com.zivaa.app.data.health.worker.HealthHistoricalBackfillWorker
import com.zivaa.app.data.remote.AuthManager
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabaseSyncLogRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class SyncTypeFilter {
    ALL,
    FOREGROUND,
    BACKGROUND
}

data class SyncLogUiItem(
    val id: String,
    val syncType: String,
    val status: String,
    val localTimeString: String,
    val rawTimestamp: String,
    val recordsSynced: Int,
    val durationString: String?,
    val metricsSummary: String?,
    val errorMessage: String?
)

class HealthConnectSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val healthConnectManager = HealthConnectManager(application)
    private val syncPrefsManager = SyncPrefsManager(application)
    private val authManager = AuthManager(application)
    private val workManager = WorkManager.getInstance(application)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<String?>("Never")
    val lastSyncTimestamp: StateFlow<String?> = _lastSyncTimestamp.asStateFlow()

    private val _grantedPermissionsCount = MutableStateFlow(0)
    val grantedPermissionsCount: StateFlow<Int> = _grantedPermissionsCount.asStateFlow()

    private val _totalPermissionsCount = MutableStateFlow(healthConnectManager.permissions.size)
    val totalPermissionsCount: StateFlow<Int> = _totalPermissionsCount.asStateFlow()

    private val _isBackgroundReadGranted = MutableStateFlow(false)
    val isBackgroundReadGranted: StateFlow<Boolean> = _isBackgroundReadGranted.asStateFlow()

    private val _isHistoryReadGranted = MutableStateFlow(false)
    val isHistoryReadGranted: StateFlow<Boolean> = _isHistoryReadGranted.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<SyncLogUiItem>>(emptyList())
    val syncLogs: StateFlow<List<SyncLogUiItem>> = _syncLogs.asStateFlow()

    private val _isLoadingLogs = MutableStateFlow(false)
    val isLoadingLogs: StateFlow<Boolean> = _isLoadingLogs.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SyncTypeFilter.ALL)
    val selectedFilter: StateFlow<SyncTypeFilter> = _selectedFilter.asStateFlow()

    private val _reSyncStatus = MutableStateFlow<String?>(null)
    val reSyncStatus: StateFlow<String?> = _reSyncStatus.asStateFlow()

    init {
        refreshState()
        loadSyncLogs()
    }

    fun refreshState() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val patientId = authManager.getUserId()
                val lastTimestampMs = syncPrefsManager.getLastSuccessfulSyncTimestamp(patientId)
                if (lastTimestampMs > 0L) {
                    val instant = Instant.ofEpochMilli(lastTimestampMs)
                    _lastSyncTimestamp.value = formatToLocalTime(instant.toString())
                }

                val granted = healthConnectManager.getGrantedPermissions()
                _grantedPermissionsCount.value = granted.intersect(healthConnectManager.permissions).size
                _isBackgroundReadGranted.value = healthConnectManager.hasBackgroundReadPermission()
                _isHistoryReadGranted.value = healthConnectManager.hasHistoryReadPermission()
            } catch (e: Exception) {
                android.util.Log.e("HealthConnectVM", "Error refreshing Health Connect state: ${e.message}", e)
            }
        }
    }

    fun triggerSyncNow() {
        val patientId = authManager.getUserId()
        if (patientId.isNullOrBlank()) {
            _syncStatusMessage.value = "No active user logged in"
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatusMessage.value = "Syncing latest data..."

            val oneTimeWork = OneTimeWorkRequestBuilder<HealthDataSyncWorker>()
                .build()

            workManager.enqueueUniqueWork(
                "HealthDataSyncWorker_ManualNow",
                ExistingWorkPolicy.REPLACE,
                oneTimeWork
            )

            // Observe the work status
            workManager.getWorkInfoByIdFlow(oneTimeWork.id).collect { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            _isSyncing.value = false
                            _syncStatusMessage.value = "Sync completed successfully"
                            refreshState()
                            loadSyncLogs()
                        }
                        WorkInfo.State.FAILED -> {
                            _isSyncing.value = false
                            _syncStatusMessage.value = "Sync encountered an issue"
                            loadSyncLogs()
                        }
                        WorkInfo.State.CANCELLED -> {
                            _isSyncing.value = false
                            _syncStatusMessage.value = "Sync was cancelled"
                        }
                        else -> {
                            _isSyncing.value = true
                        }
                    }
                }
            }
        }
    }

    fun triggerDeepReSync() {
        val patientId = authManager.getUserId() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _reSyncStatus.value = "Resetting backfill cursor..."
                syncPrefsManager.setBaselineBackfillComplete(false, patientId)
                syncPrefsManager.clearHistoricalLookbackProgress(patientId)

                HealthHistoricalBackfillWorker.enqueue(getApplication(), patientId)
                _reSyncStatus.value = "90-day deep backfill queued in background."
                loadSyncLogs()
            } catch (e: Exception) {
                _reSyncStatus.value = "Failed to start re-sync: ${e.message}"
            }
        }
    }

    fun setFilter(filter: SyncTypeFilter) {
        _selectedFilter.value = filter
    }

    fun loadSyncLogs() {
        val patientId = authManager.getUserId() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingLogs.value = true
            try {
                RetrofitClient.initialize(getApplication())
                val res = RetrofitClient.apiService.getSyncLogs(
                    patientIdQuery = "eq.$patientId",
                    order = "timestamp.desc",
                    limit = 50
                )

                if (res.isSuccessful && res.body() != null) {
                    val rawList = res.body()!!
                    val uiItems = rawList.map { parseSyncLogToUi(it) }
                    _syncLogs.value = uiItems
                }
            } catch (e: Exception) {
                android.util.Log.w("HealthConnectVM", "Failed to fetch cloud sync logs: ${e.message}")
            } finally {
                _isLoadingLogs.value = false
            }
        }
    }

    private fun parseSyncLogToUi(log: SupabaseSyncLogRecord): SyncLogUiItem {
        val localTime = formatToLocalTime(log.timestamp)
        var durationStr: String? = null
        var metricsSummary: String? = null

        try {
            val meta = log.metric_types
            if (meta is Map<*, *>) {
                val durMs = (meta["duration_ms"] as? Number)?.toLong()
                if (durMs != null && durMs > 0L) {
                    durationStr = if (durMs >= 60000L) {
                        val mins = durMs / 60000L
                        val secs = (durMs % 60000L) / 1000L
                        "${mins}m ${secs}s"
                    } else {
                        val secs = String.format(java.util.Locale.US, "%.1fs", durMs / 1000.0)
                        secs
                    }
                }

                val metricsMap = meta["metrics"] as? Map<*, *>
                if (metricsMap != null && metricsMap.isNotEmpty()) {
                    val parts = mutableListOf<String>()
                    metricsMap.forEach { (k, v) ->
                        val prettyName = when (k.toString()) {
                            "HeartRateRecord" -> "Heart Rate"
                            "SleepSessionRecord" -> "Sleep"
                            "StepsRecord" -> "Steps"
                            "RestingHeartRateRecord" -> "Resting HR"
                            "TotalCaloriesBurnedRecord" -> "Calories"
                            "DistanceRecord" -> "Distance"
                            "RespiratoryRateRecord" -> "Resp. Rate"
                            "SkinTemperatureRecord" -> "Skin Temp"
                            "HeartRateVariabilityRmssdRecord" -> "HRV"
                            else -> k.toString().replace("Record", "")
                        }
                        val count = (v as? Number)?.toInt() ?: 0
                        if (count > 0) {
                            parts.add("$count $prettyName")
                        }
                    }
                    if (parts.isNotEmpty()) {
                        metricsSummary = parts.joinToString(" · ")
                    }
                }

                if (metricsSummary == null && meta["details"] != null) {
                    metricsSummary = meta["details"].toString()
                }
            } else if (meta is List<*>) {
                val prettyList = meta.mapNotNull { it?.toString()?.replace("Record", "") }
                if (prettyList.isNotEmpty()) {
                    metricsSummary = prettyList.joinToString(" · ")
                }
            }
        } catch (e: Exception) {
            // Graceful fallback
        }

        return SyncLogUiItem(
            id = log.id ?: (log.timestamp ?: java.util.UUID.randomUUID().toString()),
            syncType = log.sync_type.uppercase(),
            status = log.status.uppercase(),
            localTimeString = localTime,
            rawTimestamp = log.timestamp ?: "",
            recordsSynced = log.records_synced,
            durationString = durationStr,
            metricsSummary = metricsSummary,
            errorMessage = log.error_message
        )
    }

    private fun formatToLocalTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Unknown time"
        return try {
            val instant = Instant.parse(isoString)
            val zone = ZoneId.systemDefault()
            val localDateTime = instant.atZone(zone)
            val now = ZonedDateTime.now(zone)

            val timeStr = localDateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
            when {
                localDateTime.toLocalDate() == now.toLocalDate() -> "Today, $timeStr"
                localDateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> "Yesterday, $timeStr"
                localDateTime.year == now.year -> localDateTime.format(DateTimeFormatter.ofPattern("d MMM, h:mm a"))
                else -> localDateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a"))
            }
        } catch (e: Exception) {
            isoString
        }
    }
}
