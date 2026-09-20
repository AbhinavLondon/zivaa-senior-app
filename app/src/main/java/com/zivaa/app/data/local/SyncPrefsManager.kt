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
        val permKey = getScopedKey(KEY_TOKEN_PERMISSIONS, patientId)
        val progressKey = getScopedKey(KEY_HISTORICAL_LOOKBACK_PROGRESS, patientId)
        prefs.edit().remove(key).remove(KEY_CHANGES_TOKEN).remove(permKey).remove(progressKey).apply()
    }

    fun getTokenPermissions(patientId: String? = null): Set<String> {
        val key = getScopedKey(KEY_TOKEN_PERMISSIONS, patientId)
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    fun saveTokenPermissions(permissions: Set<String>, patientId: String? = null) {
        val key = getScopedKey(KEY_TOKEN_PERMISSIONS, patientId)
        prefs.edit().putStringSet(key, permissions).apply()
    }

    fun getLastSyncedPatientId(): String? = prefs.getString("last_synced_patient_id", null)
    fun setLastSyncedPatientId(patientId: String) {
        prefs.edit().putString("last_synced_patient_id", patientId).apply()
    }

    fun getLastSuccessfulSyncTimestamp(patientId: String? = null): Long {
        val key = getScopedKey(KEY_LAST_SUCCESSFUL_SYNC_TIMESTAMP, patientId)
        return prefs.getLong(key, 0L)
    }

    fun setLastSuccessfulSyncTimestamp(timestampMs: Long, patientId: String? = null) {
        val key = getScopedKey(KEY_LAST_SUCCESSFUL_SYNC_TIMESTAMP, patientId)
        prefs.edit().putLong(key, timestampMs).apply()
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

    fun isTodayTourCompleted(patientId: String? = null): Boolean {
        val scopedKey = getScopedKey(KEY_TODAY_TOUR_COMPLETED, patientId)
        return prefs.getBoolean(scopedKey, false)
    }

    fun setTodayTourCompleted(completed: Boolean, patientId: String? = null) {
        val scopedKey = getScopedKey(KEY_TODAY_TOUR_COMPLETED, patientId)
        prefs.edit().putBoolean(scopedKey, completed).apply()
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

    fun getLastSleepSyncDate(patientId: String? = null): String? = prefs.getString(getScopedKey(KEY_LAST_SLEEP_SYNC, patientId), null)
    fun setLastSleepSyncDate(dateStr: String, patientId: String? = null) {
        prefs.edit().putString(getScopedKey(KEY_LAST_SLEEP_SYNC, patientId), dateStr).apply()
    }

    fun getLastStepsSyncDate(patientId: String? = null): String? = prefs.getString(getScopedKey(KEY_LAST_STEPS_SYNC, patientId), null)
    fun setLastStepsSyncDate(dateStr: String, patientId: String? = null) {
        prefs.edit().putString(getScopedKey(KEY_LAST_STEPS_SYNC, patientId), dateStr).apply()
    }

    fun getLastHeartRateSyncTime(patientId: String? = null): Long = prefs.getLong(getScopedKey(KEY_LAST_HR_SYNC, patientId), 0L)
    fun setLastHeartRateSyncTime(timeMs: Long, patientId: String? = null) {
        prefs.edit().putLong(getScopedKey(KEY_LAST_HR_SYNC, patientId), timeMs).apply()
    }

    fun getLastFallbackAttemptDate(patientId: String? = null): String? = prefs.getString(getScopedKey(KEY_LAST_FALLBACK_ATTEMPT, patientId), null)
    fun setLastFallbackAttemptDate(dateStr: String, patientId: String? = null) {
        prefs.edit().putString(getScopedKey(KEY_LAST_FALLBACK_ATTEMPT, patientId), dateStr).apply()
    }

    fun getFallbackAttemptsCount(dateStr: String, patientId: String? = null): Int {
        val lastDate = prefs.getString(getScopedKey(KEY_LAST_FALLBACK_ATTEMPT_DATE, patientId), null)
        if (lastDate != dateStr) {
            return 0
        }
        return prefs.getInt(getScopedKey(KEY_FALLBACK_ATTEMPTS_COUNT, patientId), 0)
    }

    fun recordFallbackAttempt(dateStr: String, patientId: String? = null) {
        val currentCount = getFallbackAttemptsCount(dateStr, patientId)
        prefs.edit()
            .putString(getScopedKey(KEY_LAST_FALLBACK_ATTEMPT_DATE, patientId), dateStr)
            .putInt(getScopedKey(KEY_FALLBACK_ATTEMPTS_COUNT, patientId), currentCount + 1)
            .putLong(getScopedKey(KEY_LAST_FALLBACK_TIMESTAMP, patientId), System.currentTimeMillis())
            .apply()
    }

    fun getLastFallbackTimestamp(patientId: String? = null): Long = prefs.getLong(getScopedKey(KEY_LAST_FALLBACK_TIMESTAMP, patientId), 0L)

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

    fun isBaselineBackfillComplete(patientId: String? = null): Boolean {
        val key = getScopedKey(KEY_BASELINE_BACKFILL_COMPLETE, patientId)
        return prefs.getBoolean(key, false)
    }

    fun setBaselineBackfillComplete(complete: Boolean, patientId: String? = null) {
        val key = getScopedKey(KEY_BASELINE_BACKFILL_COMPLETE, patientId)
        prefs.edit().putBoolean(key, complete).apply()
    }

    fun getHistoricalLookbackProgress(patientId: String? = null): Long {
        val key = getScopedKey(KEY_HISTORICAL_LOOKBACK_PROGRESS, patientId)
        return prefs.getLong(key, 0L)
    }

    fun setHistoricalLookbackProgress(timestampMs: Long, patientId: String? = null) {
        val key = getScopedKey(KEY_HISTORICAL_LOOKBACK_PROGRESS, patientId)
        prefs.edit().putLong(key, timestampMs).apply()
    }

    fun clearHistoricalLookbackProgress(patientId: String? = null) {
        val key = getScopedKey(KEY_HISTORICAL_LOOKBACK_PROGRESS, patientId)
        prefs.edit().remove(key).apply()
    }

    fun hasPromptedHistoryPermission(): Boolean {
        return prefs.getBoolean(KEY_PROMPTED_HISTORY_PERMISSION, false)
    }

    fun setPromptedHistoryPermission(prompted: Boolean) {
        prefs.edit().putBoolean(KEY_PROMPTED_HISTORY_PERMISSION, prompted).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "health_sync_prefs"
        private const val KEY_PROMPTED_HISTORY_PERMISSION = "prompted_history_permission"
        private const val KEY_CHANGES_TOKEN = "changes_token"
        private const val KEY_TOKEN_PERMISSIONS = "token_permissions"
        private const val KEY_BASELINE_BACKFILL_COMPLETE = "baseline_backfill_complete"
        private const val KEY_PHONE_SENSOR_ENABLED = "phone_sensor_enabled"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_TODAY_TOUR_COMPLETED = "today_tour_completed"
        private const val KEY_VIEWED_AFTERNOON = "viewed_afternoon_date"
        private const val KEY_VIEWED_EVENING = "viewed_evening_date"
        private const val KEY_CACHED_BRIEFING_DATE = "cached_briefing_date"
        private const val KEY_MORNING_BRIEFING = "morning_briefing_text"
        private const val KEY_MORNING_HEADLINE = "morning_briefing_headline"
        private const val KEY_MIDDAY_SUMMARY = "midday_summary_text"
        private const val KEY_EVENING_SUMMARY = "evening_summary_text"
        private const val KEY_LAST_SLEEP_SYNC = "last_sleep_sync"
        private const val KEY_LAST_STEPS_SYNC = "last_steps_sync"
        private const val KEY_LAST_HR_SYNC = "last_hr_sync"
        private const val KEY_LAST_FALLBACK_ATTEMPT = "last_fallback_attempt"
        private const val KEY_LAST_FALLBACK_ATTEMPT_DATE = "last_fallback_attempt_date"
        private const val KEY_FALLBACK_ATTEMPTS_COUNT = "fallback_attempts_count"
        private const val KEY_LAST_FALLBACK_TIMESTAMP = "last_fallback_timestamp"
        private const val KEY_STEPS_GOAL = "steps_goal"
        private const val KEY_METRIC_PRIORITIES = "metric_priorities"
        private const val KEY_CACHED_VITALS_DATE = "cached_vitals_date"
        private const val KEY_CACHED_SLEEP_HOURS = "cached_sleep_hours"
        private const val KEY_CACHED_HEART_RATE = "cached_heart_rate"
        private const val KEY_CACHED_OXYGEN_LEVEL = "cached_oxygen_level"
        private const val KEY_LAST_SUCCESSFUL_SYNC_TIMESTAMP = "last_successful_sync_timestamp"
        private const val KEY_HISTORICAL_LOOKBACK_PROGRESS = "historical_lookback_progress"
    }
}
