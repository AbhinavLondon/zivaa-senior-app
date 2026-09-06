package com.zivaa.app.data.local

import android.content.Context
import android.content.SharedPreferences

class SyncPrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveChangesToken(token: String, patientId: String? = null) {
        val key = if (!patientId.isNullOrBlank()) "${KEY_CHANGES_TOKEN}_$patientId" else KEY_CHANGES_TOKEN
        prefs.edit().putString(key, token).apply()
    }

    fun getChangesToken(patientId: String? = null): String? {
        val key = if (!patientId.isNullOrBlank()) "${KEY_CHANGES_TOKEN}_$patientId" else KEY_CHANGES_TOKEN
        return prefs.getString(key, null)
    }

    fun clearChangesToken(patientId: String? = null) {
        val editor = prefs.edit().remove(KEY_CHANGES_TOKEN)
        if (!patientId.isNullOrBlank()) {
            editor.remove("${KEY_CHANGES_TOKEN}_$patientId")
        }
        editor.apply()
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

    fun isSetupComplete(): Boolean {
        return prefs.getBoolean(KEY_SETUP_COMPLETE, false)
    }

    fun setSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, complete).apply()
    }

    fun getListViewedAfternoonDate(): String? {
        return prefs.getString(KEY_VIEWED_AFTERNOON, null)
    }

    fun setViewedAfternoonDate(dateStr: String) {
        prefs.edit().putString(KEY_VIEWED_AFTERNOON, dateStr).apply()
    }

    fun getListViewedEveningDate(): String? {
        return prefs.getString(KEY_VIEWED_EVENING, null)
    }

    fun setViewedEveningDate(dateStr: String) {
        prefs.edit().putString(KEY_VIEWED_EVENING, dateStr).apply()
    }

    fun getCachedBriefingDate(): String? = prefs.getString(KEY_CACHED_BRIEFING_DATE, null)

    fun getMorningBriefingText(): String? = prefs.getString(KEY_MORNING_BRIEFING, null)
    fun getMorningBriefingHeadline(): String? = prefs.getString(KEY_MORNING_HEADLINE, null)
    fun getMiddaySummaryText(): String? = prefs.getString(KEY_MIDDAY_SUMMARY, null)
    fun getEveningSummaryText(): String? = prefs.getString(KEY_EVENING_SUMMARY, null)

    fun saveMorningBriefing(dateStr: String, text: String, headline: String) {
        prefs.edit()
            .putString(KEY_CACHED_BRIEFING_DATE, dateStr)
            .putString(KEY_MORNING_BRIEFING, text)
            .putString(KEY_MORNING_HEADLINE, headline)
            .apply()
    }

    fun saveMiddaySummary(dateStr: String, text: String) {
        prefs.edit()
            .putString(KEY_CACHED_BRIEFING_DATE, dateStr)
            .putString(KEY_MIDDAY_SUMMARY, text)
            .apply()
    }

    fun saveEveningSummary(dateStr: String, text: String) {
        prefs.edit()
            .putString(KEY_CACHED_BRIEFING_DATE, dateStr)
            .putString(KEY_EVENING_SUMMARY, text)
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
    }
}
