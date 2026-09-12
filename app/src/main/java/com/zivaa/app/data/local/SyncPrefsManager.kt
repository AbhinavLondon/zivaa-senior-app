package com.zivaa.app.data.local

import android.content.Context
import android.content.SharedPreferences

class SyncPrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getScopedKey(baseKey: String, patientId: String? = null): String {
        val pid = if (!patientId.isNullOrBlank()) {
            patientId
        } else {
            try {
                com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
            } catch (e: Exception) {
                null
            }
        }
        return if (!pid.isNullOrBlank()) "${baseKey}_$pid" else baseKey
    }

    fun saveChangesToken(token: String, patientId: String? = null) {
        val key = getScopedKey(KEY_CHANGES_TOKEN, patientId)
        prefs.edit().putString(key, token).apply()
    }

    fun getChangesToken(patientId: String? = null): String? {
        val key = getScopedKey(KEY_CHANGES_TOKEN, patientId)
        return prefs.getString(key, null)
    }

    fun clearChangesToken(patientId: String? = null) {
        val key = getScopedKey(KEY_CHANGES_TOKEN, patientId)
        prefs.edit().remove(key).remove(KEY_CHANGES_TOKEN).apply()
    }

    fun getLastSyncedPatientId(): String? = prefs.getString("last_synced_patient_id", null)
    fun setLastSyncedPatientId(patientId: String) {
        prefs.edit().putString("last_synced_patient_id", patientId).apply()
    }

    fun isPhoneSensorEnabled(): Boolean {
        return prefs.getBoolean(KEY_PHONE_SENSOR_ENABLED, true)
    }

    fun setPhoneSensorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PHONE_SENSOR_ENABLED, enabled).apply()
    }

    fun isSetupComplete(patientId: String? = null): Boolean {
        val scopedKey = getScopedKey(KEY_SETUP_COMPLETE, patientId)
        return prefs.getBoolean(scopedKey, prefs.getBoolean(KEY_SETUP_COMPLETE, false))
    }

    fun setSetupComplete(complete: Boolean, patientId: String? = null) {
        val scopedKey = getScopedKey(KEY_SETUP_COMPLETE, patientId)
        prefs.edit().putBoolean(scopedKey, complete).putBoolean(KEY_SETUP_COMPLETE, complete).apply()
    }

    fun getListViewedAfternoonDate(patientId: String? = null): String? {
        return prefs.getString(getScopedKey(KEY_VIEWED_AFTERNOON, patientId), null)
    }

    fun setViewedAfternoonDate(dateStr: String, patientId: String? = null) {
        prefs.edit().putString(getScopedKey(KEY_VIEWED_AFTERNOON, patientId), dateStr).apply()
    }

    fun getListViewedEveningDate(patientId: String? = null): String? {
        return prefs.getString(getScopedKey(KEY_VIEWED_EVENING, patientId), null)
    }

    fun setViewedEveningDate(dateStr: String, patientId: String? = null) {
        prefs.edit().putString(getScopedKey(KEY_VIEWED_EVENING, patientId), dateStr).apply()
    }

    fun getCachedBriefingDate(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_CACHED_BRIEFING_DATE, patientId), null)

    fun getMorningBriefingText(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_MORNING_BRIEFING, patientId), null)

    fun getMorningBriefingHeadline(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_MORNING_HEADLINE, patientId), null)

    fun getMiddaySummaryText(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_MIDDAY_SUMMARY, patientId), null)

    fun getEveningSummaryText(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_EVENING_SUMMARY, patientId), null)

    fun saveMorningBriefing(dateStr: String, text: String, headline: String, patientId: String? = null) {
        prefs.edit()
            .putString(getScopedKey(KEY_CACHED_BRIEFING_DATE, patientId), dateStr)
            .putString(getScopedKey(KEY_MORNING_BRIEFING, patientId), text)
            .putString(getScopedKey(KEY_MORNING_HEADLINE, patientId), headline)
            .apply()
    }

    fun saveMiddaySummary(dateStr: String, text: String, patientId: String? = null) {
        prefs.edit()
            .putString(getScopedKey(KEY_CACHED_BRIEFING_DATE, patientId), dateStr)
            .putString(getScopedKey(KEY_MIDDAY_SUMMARY, patientId), text)
            .apply()
    }

    fun saveEveningSummary(dateStr: String, text: String, patientId: String? = null) {
        prefs.edit()
            .putString(getScopedKey(KEY_CACHED_BRIEFING_DATE, patientId), dateStr)
            .putString(getScopedKey(KEY_EVENING_SUMMARY, patientId), text)
            .apply()
    }

    fun getLastSleepSyncDate(): String? = prefs.getString(KEY_LAST_SLEEP_SYNC, null)
    fun setLastSleepSyncDate(dateStr: String) {
        prefs.edit().putString(KEY_LAST_SLEEP_SYNC, dateStr).apply()
    }

    fun getLastHeartRateSyncTime(): Long = prefs.getLong(KEY_LAST_HR_SYNC, 0L)
    fun setLastHeartRateSyncTime(timeMs: Long) {
        prefs.edit().putLong(KEY_LAST_HR_SYNC, timeMs).apply()
    }

    fun getLastFallbackAttemptDate(): String? = prefs.getString(KEY_LAST_FALLBACK_ATTEMPT, null)
    fun setLastFallbackAttemptDate(dateStr: String) {
        prefs.edit().putString(KEY_LAST_FALLBACK_ATTEMPT, dateStr).apply()
    }

    fun getStepsGoal(patientId: String? = null): Int? {
        val goal = prefs.getInt(getScopedKey(KEY_STEPS_GOAL, patientId), -1)
        return if (goal > 0) goal else null
    }

    fun saveStepsGoal(goal: Int, patientId: String? = null) {
        prefs.edit().putInt(getScopedKey(KEY_STEPS_GOAL, patientId), goal).apply()
    }

    fun saveSourcePriorities(priorities: List<com.zivaa.app.data.remote.MetricSourcePriorityRecord>) {
        val array = org.json.JSONArray()
        for (p in priorities) {
            val obj = org.json.JSONObject()
            obj.put("metric_type", p.metric_type)
            obj.put("source", p.source)
            obj.put("priority_rank", p.priority_rank)
            array.put(obj)
        }
        prefs.edit().putString(KEY_METRIC_PRIORITIES, array.toString()).apply()
    }

    fun getSourcePriorities(): List<com.zivaa.app.data.remote.MetricSourcePriorityRecord> {
        val str = prefs.getString(KEY_METRIC_PRIORITIES, null) ?: return emptyList()
        val list = mutableListOf<com.zivaa.app.data.remote.MetricSourcePriorityRecord>()
        try {
            val array = org.json.JSONArray(str)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.zivaa.app.data.remote.MetricSourcePriorityRecord(
                        metric_type = obj.getString("metric_type"),
                        source = obj.getString("source"),
                        priority_rank = obj.getInt("priority_rank")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveCachedVitals(dateStr: String, sleepHours: String, heartRate: String, oxygenLevel: String, patientId: String? = null) {
        prefs.edit()
            .putString(getScopedKey(KEY_CACHED_VITALS_DATE, patientId), dateStr)
            .putString(getScopedKey(KEY_CACHED_SLEEP_HOURS, patientId), sleepHours)
            .putString(getScopedKey(KEY_CACHED_HEART_RATE, patientId), heartRate)
            .putString(getScopedKey(KEY_CACHED_OXYGEN_LEVEL, patientId), oxygenLevel)
            .apply()
    }

    fun getCachedVitalsDate(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_CACHED_VITALS_DATE, patientId), null)

    fun getCachedSleepHours(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_CACHED_SLEEP_HOURS, patientId), null)

    fun getCachedHeartRate(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_CACHED_HEART_RATE, patientId), null)

    fun getCachedOxygenLevel(patientId: String? = null): String? = 
        prefs.getString(getScopedKey(KEY_CACHED_OXYGEN_LEVEL, patientId), null)

    fun clearUserData(patientId: String? = null) {
        val pid = if (!patientId.isNullOrBlank()) {
            patientId
        } else {
            try {
                com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
            } catch (e: Exception) {
                null
            }
        }
        val editor = prefs.edit()
        if (!pid.isNullOrBlank()) {
            for (key in prefs.all.keys) {
                if (key.endsWith("_$pid")) {
                    editor.remove(key)
                }
            }
        }
        // Always purge any legacy un-scoped cache entries
        editor.remove(KEY_CACHED_BRIEFING_DATE)
        editor.remove(KEY_MORNING_BRIEFING)
        editor.remove(KEY_MORNING_HEADLINE)
        editor.remove(KEY_MIDDAY_SUMMARY)
        editor.remove(KEY_EVENING_SUMMARY)
        editor.remove(KEY_CACHED_VITALS_DATE)
        editor.remove(KEY_CACHED_SLEEP_HOURS)
        editor.remove(KEY_CACHED_HEART_RATE)
        editor.remove(KEY_CACHED_OXYGEN_LEVEL)
        editor.remove(KEY_VIEWED_AFTERNOON)
        editor.remove(KEY_VIEWED_EVENING)
        editor.apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "health_sync_prefs"
        private const val KEY_CHANGES_TOKEN = "changes_token"
        private const val KEY_PHONE_SENSOR_ENABLED = "phone_sensor_enabled"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_VIEWED_AFTERNOON = "viewed_afternoon_date"
        private const val KEY_VIEWED_EVENING = "viewed_evening_date"
        private const val KEY_CACHED_BRIEFING_DATE = "cached_briefing_date"
        private const val KEY_MORNING_BRIEFING = "morning_briefing_text"
        private const val KEY_MORNING_HEADLINE = "morning_briefing_headline"
        private const val KEY_MIDDAY_SUMMARY = "midday_summary_text"
        private const val KEY_EVENING_SUMMARY = "evening_summary_text"
        private const val KEY_LAST_SLEEP_SYNC = "last_sleep_sync"
        private const val KEY_LAST_HR_SYNC = "last_hr_sync"
        private const val KEY_LAST_FALLBACK_ATTEMPT = "last_fallback_attempt"
        private const val KEY_STEPS_GOAL = "steps_goal"
        private const val KEY_METRIC_PRIORITIES = "metric_priorities"
        private const val KEY_CACHED_VITALS_DATE = "cached_vitals_date"
        private const val KEY_CACHED_SLEEP_HOURS = "cached_sleep_hours"
        private const val KEY_CACHED_HEART_RATE = "cached_heart_rate"
        private const val KEY_CACHED_OXYGEN_LEVEL = "cached_oxygen_level"
    }
}
