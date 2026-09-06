package com.zivaa.app.data.sensors

import android.content.Context
import android.content.SharedPreferences

class SensorDataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sensor_data_store", Context.MODE_PRIVATE)

    fun incrementCoughCount() {
        val current = prefs.getInt("cough_count_night", 0)
        prefs.edit().putInt("cough_count_night", current + 1).apply()
    }

    fun incrementSnoringEvent() {
        val current = prefs.getInt("snoring_events_count", 0)
        prefs.edit().putInt("snoring_events_count", current + 1).apply()
    }

    fun addSitToStandTime(seconds: Double) {
        val count = prefs.getInt("sts_count", 0)
        val totalSeconds = prefs.getFloat("sts_total_seconds", 0f)
        prefs.edit()
            .putInt("sts_count", count + 1)
            .putFloat("sts_total_seconds", totalSeconds + seconds.toFloat())
            .apply()
    }

    fun getAndClearDailyData(): Map<String, Any> {
        val coughCount = prefs.getInt("cough_count_night", 0)
        val snoringCount = prefs.getInt("snoring_events_count", 0)
        val stsCount = prefs.getInt("sts_count", 0)
        val stsTotalSeconds = prefs.getFloat("sts_total_seconds", 0f)

        val averageStsSeconds = if (stsCount > 0) (stsTotalSeconds / stsCount).toDouble() else 0.0

        // Clear data after reading
        prefs.edit().clear().apply()

        val map = mutableMapOf<String, Any>()
        if (coughCount > 0) map["cough_count_night"] = coughCount
        if (snoringCount > 0) map["snoring_events_count"] = snoringCount
        if (averageStsSeconds > 0) map["sit_to_stand_seconds"] = averageStsSeconds
        
        return map
    }
}
