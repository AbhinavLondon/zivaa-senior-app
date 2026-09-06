package com.zivaa.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.HealthConnectSupport
import com.zivaa.app.data.health.CloudSyncFallback
import com.zivaa.app.data.model.HealthPayload
import com.zivaa.app.data.model.MetricRecord
import com.zivaa.app.data.remote.ZivaaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.TimeZone
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.zivaa.app.data.model.DeviceStatusPayload

class CloudSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // Ideally injected via Hilt, instantiated here for standalone modular blueprint
    private val healthConnectManager = HealthConnectManager(appContext)
    
    // Retrofit service initializer
    private var apiService: ZivaaApiService? = null
    
    // Cloud fallback instance (fitbit/garmin oauth bridge)
    private var fallbackSync: CloudSyncFallback? = null

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val support = healthConnectManager.checkHealthConnectSupportAndRedirect()

        when (support) {
            HealthConnectSupport.AVAILABLE -> {
                val rawRecords = healthConnectManager.fetchAllAvailableMetrics()
                if (rawRecords.isEmpty()) {
                    return@withContext Result.success()
                }

                // Map all raw records to API compatible models
                val apiRecords = rawRecords.mapNotNull { record ->
                    MetricRecord.fromHealthConnectRecord(record)
                }

                val batteryStatus: Intent? = applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val batteryPct = batteryStatus?.let { intent ->
                    val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) (level * 100 / scale) else 100
                } ?: 100

                val isCharging = batteryStatus?.let { intent ->
                    val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                } ?: false

                val payload = HealthPayload(
                    client_time = Instant.now().toString(),
                    timezone = TimeZone.getDefault().id,
                    records = apiRecords,
                    device_status = DeviceStatusPayload(
                        battery_level = batteryPct,
                        is_charging = isCharging,
                        is_connected = true
                    ),
                    patient_id = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
                )

                try {
                    val service = apiService
                    if (service == null) {
                        // Fallback if network client not instantiated
                        return@withContext Result.failure()
                    }
                    val response = service.uploadHealthData(payload)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        return@withContext Result.success()
                    } else {
                        return@withContext Result.retry()
                    }
                } catch (e: Exception) {
                    return@withContext Result.retry()
                }
            }
            HealthConnectSupport.UNSUPPORTED -> {
                // local API unavailable -> fallback to cloud synchronization
                val fallback = fallbackSync
                if (fallback != null) {
                    val fallbackSuccess = fallback.triggerCloudToCloudSync()
                    return@withContext if (fallbackSuccess) Result.success() else Result.retry()
                }
                return@withContext Result.failure()
            }
            HealthConnectSupport.INSTALL_REQUIRED -> {
                // Graceful failure (waiting for installation from Play Store link redirect)
                return@withContext Result.failure()
            }
        }
    }
}
