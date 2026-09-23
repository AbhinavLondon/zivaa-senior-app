package com.zivaa.app.presentation.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.model.HealthPayload
import com.zivaa.app.data.model.MetricRecord
import com.zivaa.app.data.remote.NudgeAlert
import com.zivaa.app.data.remote.ZivaaApiService
import com.zivaa.app.data.remote.ZivaaBackendClient
import com.zivaa.app.data.remote.DailyPlanPayload
import com.zivaa.app.data.remote.DailyPlanResponse
import com.zivaa.app.data.remote.DailyPlanTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.util.TimeZone
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.zivaa.app.data.model.DeviceStatusPayload
import android.app.Application
import androidx.lifecycle.AndroidViewModel

import kotlinx.coroutines.flow.StateFlow

enum class MorningBriefingStatus {
    AWAITING_SLEEP,
    ANALYZING_REST,
    READY
}

class DashboardViewModel(
    application: Application,
    private val healthConnectManager: HealthConnectManager,
    private val prefsManager: com.zivaa.app.data.local.SyncPrefsManager,
    private val appSettingsManager: com.zivaa.app.data.local.AppSettingsManager
) : AndroidViewModel(application) {

    var morningBriefingStatus by mutableStateOf(MorningBriefingStatus.AWAITING_SLEEP)

    val showLongevityPlanEnabled: StateFlow<Boolean> = appSettingsManager.showLongevityPlanFlow

    var heartRate by mutableStateOf("--")
    var bloodPressure by mutableStateOf("--")
    var steps by mutableStateOf("--")
    var stepsGoal by mutableStateOf<Int?>(prefsManager.getStepsGoal() ?: 10000)
    var sleepHours by mutableStateOf("--")
    var mood by mutableStateOf("--")
    var oxygenLevel by mutableStateOf("--")
    
    var todayNudgeAlerts by mutableStateOf<List<com.zivaa.app.data.remote.SupabaseNudgeAlert>>(emptyList())
    var selectedNudgeAlert by mutableStateOf<com.zivaa.app.data.remote.SupabaseNudgeAlert?>(null)
    var isSyncing by mutableStateOf(false)
    var syncStatus by mutableStateOf("")
    var patientFirstName by mutableStateOf("")
    var patientProfilePicUrl by mutableStateOf<String?>(null)

    // Daily plan states
    var dailyPlanSummary by mutableStateOf("")
    var morningTasks by mutableStateOf<List<DailyPlanTask>>(emptyList())
    var afternoonTasks by mutableStateOf<List<DailyPlanTask>>(emptyList())
    var eveningTasks by mutableStateOf<List<DailyPlanTask>>(emptyList())
    var nightTasks by mutableStateOf<List<DailyPlanTask>>(emptyList())
    var allWeeklyPlans by mutableStateOf<List<com.zivaa.app.data.remote.SupabaseDailyPlanRecord>>(emptyList())
        private set
    var selectedDate by mutableStateOf(java.time.LocalDate.now().toString())
        private set
    var isGeneratingPlan by mutableStateOf(false)
        private set
    private var currentPlanId: String? = null
    private val backendApiService: ZivaaApiService = ZivaaBackendClient.apiService

    var morningBriefingText by mutableStateOf("")
    var morningBriefingHeadline by mutableStateOf("")
    var sleepInsightText by mutableStateOf<String?>(null)
    var middaySummaryText by mutableStateOf<String?>(null)
    var eveningSummaryText by mutableStateOf<String?>(null)
    var lateNightInsightText by mutableStateOf<String?>(null)

    var hasViewedAfternoonSummary by mutableStateOf(false)
        private set
    var hasViewedEveningSummary by mutableStateOf(false)
        private set
    var activeHeroPeriod by mutableStateOf("morning")

    private var lastFetchTime = 0L
    private val DEBOUNCE_MS = 5 * 60 * 1000L // 5 minutes

    fun markAfternoonSummaryViewed() {
        val todayStr = java.time.LocalDate.now().toString()
        prefsManager.setViewedAfternoonDate(todayStr)
        hasViewedAfternoonSummary = true
    }

    fun markEveningSummaryViewed() {
        val todayStr = java.time.LocalDate.now().toString()
        prefsManager.setViewedEveningDate(todayStr)
        hasViewedEveningSummary = true
    }

    init {
        // Mock data initialization removed to prevent flashing hardcoded plans

        val userId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
        val todayStr = java.time.LocalDate.now().toString()
        if (prefsManager.getCachedBriefingDate(userId) == todayStr) {
            val cachedText = prefsManager.getMorningBriefingText(userId)
            val cachedHeadline = prefsManager.getMorningBriefingHeadline(userId)
            if (!cachedText.isNullOrBlank()) {
                morningBriefingText = cachedText
                morningBriefingHeadline = if (!cachedHeadline.isNullOrBlank()) cachedHeadline else "Your Daily Briefing"
                morningBriefingStatus = MorningBriefingStatus.READY
            } else {
                morningBriefingStatus = MorningBriefingStatus.AWAITING_SLEEP
                morningBriefingHeadline = "Good morning"
                morningBriefingText = "Wishing you a peaceful and energizing start to your day. As soon as your watch finishes analyzing last night's rest, your full briefing will appear here."
            }
            middaySummaryText = prefsManager.getMiddaySummaryText(userId)
            eveningSummaryText = prefsManager.getEveningSummaryText(userId)
        } else {
            morningBriefingStatus = MorningBriefingStatus.AWAITING_SLEEP
            morningBriefingHeadline = "Good morning"
            morningBriefingText = "Wishing you a peaceful and energizing start to your day. As soon as your watch finishes analyzing last night's rest, your full briefing will appear here."
        }

        // Hydrate clinical vitals from SharedPreferences cache if for today
        if (prefsManager.getCachedVitalsDate(userId) == todayStr) {
            prefsManager.getCachedSleepHours(userId)?.let { sleepHours = it }
            prefsManager.getCachedHeartRate(userId)?.let { heartRate = it }
            prefsManager.getCachedOxygenLevel(userId)?.let { oxygenLevel = it }
            prefsManager.getCachedSteps(userId)?.let { steps = it }
        }

        // Fast-path immediate steps fetch from Health Connect (<50ms)
        fetchTodayStepsFastPath()

        val viewedEvening = prefsManager.getListViewedEveningDate(userId) == todayStr
        val viewedAfternoon = prefsManager.getListViewedAfternoonDate(userId) == todayStr
        
        hasViewedEveningSummary = viewedEvening
        hasViewedAfternoonSummary = viewedAfternoon
        
        if (viewedEvening) {
            activeHeroPeriod = "evening"
        } else if (viewedAfternoon) {
            activeHeroPeriod = "afternoon"
        } else {
            activeHeroPeriod = "morning"
        }

        // 0ms Instant Hydration: Load cached daily plans from disk immediately
        loadCachedDailyPlans()

        fetchDailyPlan()

        // Flush any pending offline syncs in background if connected
        flushPendingDailyPlanSyncs()
    }

    val hasPlanForToday: Boolean
        get() {
            val todayStr = java.time.LocalDate.now().toString()
            val todayPlan = allWeeklyPlans.find { it.date == todayStr || it.created_at?.startsWith(todayStr) == true }
            if (todayPlan == null) return false
            val schedule = todayPlan.schedule ?: return false
            val total = (schedule.morning?.size ?: 0) + (schedule.afternoon?.size ?: 0) + (schedule.evening?.size ?: 0) + (schedule.night?.size ?: 0)
            return total > 0
        }

    val currentStreak: Int
        get() {
            var streak = 0
            val today = java.time.LocalDate.now()
            for (i in 0..30) {
                val checkDate = today.minusDays(i.toLong()).toString()
                val plan = allWeeklyPlans.find { it.date == checkDate }
                if (plan != null) {
                    val allTasks = mutableListOf<DailyPlanTask>()
                    plan.schedule?.morning?.let { allTasks.addAll(it) }
                    plan.schedule?.afternoon?.let { allTasks.addAll(it) }
                    plan.schedule?.evening?.let { allTasks.addAll(it) }
                    plan.schedule?.night?.let { allTasks.addAll(it) }
                    if (allTasks.isNotEmpty() && allTasks.all { it.completed }) {
                        streak++
                    } else if (i > 0) {
                        break
                    }
                } else if (i > 0) {
                    break
                }
            }
            return streak
        }

    val isNewUser: Boolean
        get() {
            val todayStr = java.time.LocalDate.now().toString()
            return allWeeklyPlans.none { (it.date ?: "") < todayStr }
        }

    fun selectDate(date: String) {
        selectedDate = date
        val planForDate = allWeeklyPlans.find { it.date == date } ?: allWeeklyPlans.find { it.created_at?.startsWith(date) == true }
        if (planForDate != null) {
            currentPlanId = planForDate.id
            dailyPlanSummary = planForDate.summary ?: ""
            planForDate.schedule?.let { schedule ->
                morningTasks = schedule.morning ?: emptyList()
                afternoonTasks = schedule.afternoon ?: emptyList()
                eveningTasks = schedule.evening ?: emptyList()
                nightTasks = schedule.night ?: emptyList()
            }
        } else {
            currentPlanId = null
            dailyPlanSummary = ""
            morningTasks = emptyList()
            afternoonTasks = emptyList()
            eveningTasks = emptyList()
            nightTasks = emptyList()
        }
    }

    fun loadCachedDailyPlans() {
        try {
            val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
            val cachedJson = prefsManager.getCachedWeeklyPlans(patientId)
            if (!cachedJson.isNullOrBlank()) {
                val listType = object : TypeToken<List<com.zivaa.app.data.remote.SupabaseDailyPlanRecord>>() {}.type
                val records: List<com.zivaa.app.data.remote.SupabaseDailyPlanRecord>? = Gson().fromJson(cachedJson, listType)
                if (!records.isNullOrEmpty()) {
                    allWeeklyPlans = records
                    val todayStr = java.time.LocalDate.now().toString()
                    selectDate(todayStr)
                    android.util.Log.i("DashboardVM", "Loaded ${records.size} weekly plans from local cache (0ms instant hydration)")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DashboardVM", "Error loading cached daily plans", e)
        }
    }

    fun flushPendingDailyPlanSyncs() {
        try {
            val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
            val pending = prefsManager.getPendingPlanSyncs(patientId)
            if (pending.isNotEmpty()) {
                android.util.Log.i("DashboardVM", "Found ${pending.size} pending plan syncs in outbox. Enqueueing DailyPlanSyncWorker.")
                com.zivaa.app.data.health.worker.DailyPlanSyncWorker.enqueue(getApplication(), patientId)
            }
        } catch (e: Exception) {
            android.util.Log.e("DashboardVM", "Error triggering pending plan syncs", e)
        }
    }

    private fun persistWeeklyPlansToCache() {
        try {
            val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
            val json = Gson().toJson(allWeeklyPlans)
            prefsManager.saveCachedWeeklyPlans(json, patientId)
        } catch (e: Exception) {
            android.util.Log.e("DashboardVM", "Error persisting weekly plans to cache", e)
        }
    }

    private fun mergeWithLocalPlans(remoteRecords: List<com.zivaa.app.data.remote.SupabaseDailyPlanRecord>): List<com.zivaa.app.data.remote.SupabaseDailyPlanRecord> {
        if (allWeeklyPlans.isEmpty()) return remoteRecords

        val localById = allWeeklyPlans.associateBy { it.id }
        val localByDate = allWeeklyPlans.associateBy { it.date ?: it.created_at?.take(10) }

        return remoteRecords.map { remote ->
            val local = localById[remote.id] ?: localByDate[remote.date ?: remote.created_at?.take(10)]
            if (local?.schedule == null || remote.schedule == null) {
                remote
            } else {
                val mergedSchedule = com.zivaa.app.data.remote.DailyPlanSchedule(
                    morning = mergeTasks(local.schedule.morning, remote.schedule.morning),
                    afternoon = mergeTasks(local.schedule.afternoon, remote.schedule.afternoon),
                    evening = mergeTasks(local.schedule.evening, remote.schedule.evening),
                    night = mergeTasks(local.schedule.night, remote.schedule.night)
                )
                remote.copy(schedule = mergedSchedule)
            }
        }
    }

    private fun mergeTasks(
        localTasks: List<DailyPlanTask>?,
        remoteTasks: List<DailyPlanTask>?
    ): List<DailyPlanTask>? {
        if (remoteTasks == null) return localTasks
        if (localTasks == null) return remoteTasks

        val localTaskMap = localTasks.associateBy { it.id }
        val localByName = localTasks.associateBy { it.task.trim().lowercase() }

        return remoteTasks.map { remoteTask ->
            val localMatch = localTaskMap[remoteTask.id] ?: localByName[remoteTask.task.trim().lowercase()]
            if (localMatch != null && localMatch.completed && !remoteTask.completed) {
                remoteTask.copy(completed = true)
            } else {
                remoteTask
            }
        }
    }

    private fun syncTaskStatus(period: String, index: Int, completed: Boolean) {
        val planId = currentPlanId ?: return
        val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()

        val updatedSchedule = com.zivaa.app.data.remote.DailyPlanSchedule(
            morning = morningTasks,
            afternoon = afternoonTasks,
            evening = eveningTasks,
            night = nightTasks
        )

        // 1. Update in-memory schedule and allWeeklyPlans immediately
        allWeeklyPlans = allWeeklyPlans.map { plan ->
            if (plan.id == planId) {
                plan.copy(schedule = updatedSchedule)
            } else {
                plan
            }
        }

        // 2. Persist to local disk immediately (ensures survival if app closed/killed offline)
        persistWeeklyPlansToCache()

        // 3. Queue into offline outbox
        val scheduleJson = Gson().toJson(updatedSchedule)
        prefsManager.addPendingPlanSync(planId, scheduleJson, patientId)

        // 4. Try immediate push; if offline or network fails, WorkManager handles guaranteed background retry
        viewModelScope.launch {
            try {
                android.util.Log.d("ZivaaBackend", "Syncing task $period index $index to $completed via Supabase")
                val response = com.zivaa.app.data.remote.RetrofitClient.apiService.updateDailyPlan(
                    idQuery = "eq.$planId",
                    updates = mapOf("schedule" to updatedSchedule)
                )
                if (response.isSuccessful) {
                    android.util.Log.d("ZivaaBackend", "Successfully updated task on Supabase")
                    prefsManager.removePendingPlanSync(planId, patientId)
                } else {
                    android.util.Log.e("ZivaaBackend", "Failed to update task: ${response.errorBody()?.string()}. Scheduling WorkManager.")
                    com.zivaa.app.data.health.worker.DailyPlanSyncWorker.enqueue(getApplication(), patientId)
                }
            } catch (e: Exception) {
                android.util.Log.e("ZivaaBackend", "Exception updating task (offline), scheduling WorkManager sync", e)
                com.zivaa.app.data.health.worker.DailyPlanSyncWorker.enqueue(getApplication(), patientId)
            }
        }
    }

    private fun updateAllWeeklyPlansCurrentSchedule() {
        val targetDate = selectedDate.ifEmpty { java.time.LocalDate.now().toString() }
        allWeeklyPlans = allWeeklyPlans.map { record ->
            if (record.date == targetDate || (record.date == null && record.created_at?.startsWith(targetDate) == true)) {
                record.copy(
                    schedule = com.zivaa.app.data.remote.DailyPlanSchedule(
                        morning = morningTasks,
                        afternoon = afternoonTasks,
                        evening = eveningTasks,
                        night = nightTasks
                    )
                )
            } else {
                record
            }
        }
        persistWeeklyPlansToCache()
    }

    fun toggleMorningTask(index: Int) {
        val newStatus = !morningTasks[index].completed
        morningTasks = morningTasks.mapIndexed { idx, task ->
            if (idx == index) task.copy(completed = newStatus) else task
        }
        updateAllWeeklyPlansCurrentSchedule()
        syncTaskStatus("morning", index, newStatus)
    }

    fun toggleAfternoonTask(index: Int) {
        val newStatus = !afternoonTasks[index].completed
        afternoonTasks = afternoonTasks.mapIndexed { idx, task ->
            if (idx == index) task.copy(completed = newStatus) else task
        }
        updateAllWeeklyPlansCurrentSchedule()
        syncTaskStatus("afternoon", index, newStatus)
    }

    fun toggleEveningTask(index: Int) {
        val newStatus = !eveningTasks[index].completed
        eveningTasks = eveningTasks.mapIndexed { idx, task ->
            if (idx == index) task.copy(completed = newStatus) else task
        }
        updateAllWeeklyPlansCurrentSchedule()
        syncTaskStatus("evening", index, newStatus)
    }

    fun toggleNightTask(index: Int) {
        val newStatus = !nightTasks[index].completed
        nightTasks = nightTasks.mapIndexed { idx, task ->
            if (idx == index) task.copy(completed = newStatus) else task
        }
        updateAllWeeklyPlansCurrentSchedule()
        syncTaskStatus("night", index, newStatus)
    }

    fun markTaskCompletedById(taskId: String, completed: Boolean? = null) {
        var foundPeriod: String? = null
        var foundIndex: Int = -1
        var newStatus: Boolean = true

        val updateList = { list: List<com.zivaa.app.data.remote.DailyPlanTask> ->
            val idx = list.indexOfFirst { it.id == taskId }
            if (idx >= 0) {
                newStatus = completed ?: !list[idx].completed
                foundIndex = idx
                list.mapIndexed { i, t -> if (i == idx) t.copy(completed = newStatus) else t }
            } else {
                list
            }
        }

        val newMorning = updateList(morningTasks)
        if (foundIndex >= 0) {
            morningTasks = newMorning
            foundPeriod = "morning"
        } else {
            val newAfternoon = updateList(afternoonTasks)
            if (foundIndex >= 0) {
                afternoonTasks = newAfternoon
                foundPeriod = "afternoon"
            } else {
                val newEvening = updateList(eveningTasks)
                if (foundIndex >= 0) {
                    eveningTasks = newEvening
                    foundPeriod = "evening"
                } else {
                    val newNight = updateList(nightTasks)
                    if (foundIndex >= 0) {
                        nightTasks = newNight
                        foundPeriod = "night"
                    }
                }
            }
        }

        updateAllWeeklyPlansCurrentSchedule()
        if (foundPeriod != null && foundIndex >= 0) {
            syncTaskStatus(foundPeriod!!, foundIndex, newStatus)
        }
    }

    fun dismissTask(taskId: String?, taskTitle: String, category: String?, reason: String = "not_relevant") {
        morningTasks = morningTasks.filterNot { (taskId != null && it.id == taskId) || it.task.equals(taskTitle, ignoreCase = true) }
        afternoonTasks = afternoonTasks.filterNot { (taskId != null && it.id == taskId) || it.task.equals(taskTitle, ignoreCase = true) }
        eveningTasks = eveningTasks.filterNot { (taskId != null && it.id == taskId) || it.task.equals(taskTitle, ignoreCase = true) }
        nightTasks = nightTasks.filterNot { (taskId != null && it.id == taskId) || it.task.equals(taskTitle, ignoreCase = true) }
        updateAllWeeklyPlansCurrentSchedule()

        viewModelScope.launch {
            try {
                val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                com.zivaa.app.data.remote.ZivaaBackendClient.apiService.dismissPlanAction(
                    com.zivaa.app.data.remote.DismissActionRequest(
                        patient_id = patientId,
                        action_id = taskId,
                        task_title = taskTitle,
                        category = category,
                        reason = reason
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error dismissing task", e)
            }
        }
    }

    fun pauseHabit(actionId: String) {
        morningTasks = morningTasks.filterNot { it.anchor_id == actionId || it.id == actionId }
        afternoonTasks = afternoonTasks.filterNot { it.anchor_id == actionId || it.id == actionId }
        eveningTasks = eveningTasks.filterNot { it.anchor_id == actionId || it.id == actionId }
        nightTasks = nightTasks.filterNot { it.anchor_id == actionId || it.id == actionId }
        updateAllWeeklyPlansCurrentSchedule()

        viewModelScope.launch {
            try {
                com.zivaa.app.data.remote.ZivaaBackendClient.apiService.updateCarePlanActionStatus(
                    actionId = actionId,
                    request = com.zivaa.app.data.remote.UpdateCarePlanStatusRequest(status = "Paused")
                )
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error pausing habit", e)
            }
        }
    }

    fun fetchDailyPlan(vitalsMap: Map<String, Double>? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                val today = java.time.LocalDate.now()
                val todayStr = today.toString()

                // Smart TTL (30 mins): If we already have plans for today and cache is fresh, skip network roundtrip unless forced
                val lastFetch = prefsManager.getLastPlanFetchTimestamp(patientId)
                val isFresh = (System.currentTimeMillis() - lastFetch) < 30 * 60 * 1000L
                val hasTodayInCurrent = allWeeklyPlans.any { it.date == todayStr || it.created_at?.startsWith(todayStr) == true }
                if (!forceRefresh && isFresh && hasTodayInCurrent) {
                    android.util.Log.d("DashboardVM", "Daily plan is fresh (<30m) and contains today. Skipping network fetch.")
                    return@launch
                }
                
                // Fetch exactly the 7 days of the current week (Sunday to Saturday)
                val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong())
                val startOfWeekStr = startOfWeek.toString()
                
                val response = com.zivaa.app.data.remote.RetrofitClient.apiService.getDailyPlans(
                    patientIdQuery = "eq.$patientId",
                    dateQuery = "gte.$startOfWeekStr",
                    limit = 50,
                    order = "date.asc,created_at.desc"
                )
                
                if (response.isSuccessful) {
                    val remoteRecords = response.body()
                    val hasToday = !remoteRecords.isNullOrEmpty() && remoteRecords.any { it.date == todayStr || it.created_at?.startsWith(todayStr) == true }

                    if (!remoteRecords.isNullOrEmpty() && hasToday) {
                        // Merge invariant: preserve locally completed tasks that might not have reached server yet
                        val merged = mergeWithLocalPlans(remoteRecords)
                        allWeeklyPlans = merged
                        prefsManager.setLastPlanFetchTimestamp(System.currentTimeMillis(), patientId)
                        persistWeeklyPlansToCache()
                        // Select today by default
                        selectDate(todayStr)
                    } else if (remoteRecords.isNullOrEmpty()) {
                        // Genuine new user with zero records on server: auto-provision
                        autoProvisionStarterPlans(patientId, emptyList())
                    } else {
                        // Server has some records but today is missing
                        autoProvisionStarterPlans(patientId, remoteRecords)
                    }
                } else {
                    android.util.Log.w("DashboardVM", "Failed to fetch daily plans: HTTP ${response.code()} ${response.errorBody()?.string()}. Retaining local cache.")
                }
            } catch (e: Exception) {
                android.util.Log.w("DashboardVM", "Network exception fetching daily plans (offline/timeout). Retaining local cache.", e)
            }
        }
    }

    fun autoProvisionStarterPlans(
        patientId: String,
        existingWeeklyRecords: List<com.zivaa.app.data.remote.SupabaseDailyPlanRecord> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val today = java.time.LocalDate.now()
                val existingDates = existingWeeklyRecords.mapNotNull { it.date }.toSet()

                val daysToProvision = mutableListOf<java.time.LocalDate>()
                for (i in 0..6) {
                    val d = today.plusDays(i.toLong())
                    if (!existingDates.contains(d.toString())) {
                        daysToProvision.add(d)
                    }
                }

                if (daysToProvision.isEmpty()) return@launch

                val newRecords = daysToProvision.map { date ->
                    val dateStr = date.toString()
                    val isFirstDay = (date == today)

                    val schedule = if (isFirstDay) {
                        com.zivaa.app.data.remote.DailyPlanSchedule(
                            morning = listOf(
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Say hello to Zivaa, your AI health coach",
                                    time = "Morning",
                                    category = "coach",
                                    details = "Tap to chat with Zivaa about your day and health goals.",
                                    completed = false,
                                    tier = "coach",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "coach", badge_text = "WELCOME", reason = "Get started with your AI coach"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "COACH_CHAT", cta_label = "Ask Zivaa", prefilled_prompt = "Good morning Zivaa!")
                                ),
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Drink a fresh glass of water",
                                    time = "8:00 AM",
                                    category = "hydration",
                                    details = "Start your day refreshed with a full glass of water.",
                                    completed = false,
                                    tier = "lifestyle",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "lifestyle", badge_text = "HABIT", reason = "Hydration kickstart"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "CHECKBOX_ONLY", cta_label = "Done")
                                )
                            ),
                            afternoon = listOf(
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Log your breakfast or lunch",
                                    time = "1:00 PM",
                                    category = "nutrition",
                                    details = "Take a photo or describe your meal to track your nutrition.",
                                    completed = false,
                                    tier = "lifestyle",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "lifestyle", badge_text = "DAILY", reason = "Track nutritional balance"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "LOG_MEAL", target_body_part = "nutrition", cta_label = "Snap Meal")
                                ),
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "10-minute gentle walk or stretch",
                                    time = "3:30 PM",
                                    category = "movement",
                                    details = "A brief stroll or gentle stretch to keep your circulation flowing.",
                                    completed = false,
                                    tier = "lifestyle",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "lifestyle", badge_text = "ACTIVITY", reason = "Break up prolonged sitting"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "FOLLOW_EXERCISE", routine_title = "Gentle Circulation Routine", cta_label = "Start Routine")
                                )
                            ),
                            evening = listOf(
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Check your blood pressure or vitals",
                                    time = "7:00 PM",
                                    category = "vitals",
                                    details = "Keep track of your vitals or connect your Health Connect data.",
                                    completed = false,
                                    tier = "clinical",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "clinical_rule", badge_text = "VITALS", reason = "Evening cardiovascular baseline check"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "LOG_VITALS", target_body_part = "blood_pressure", cta_label = "Log Vitals")
                                )
                            ),
                            night = emptyList()
                        )
                    } else {
                        com.zivaa.app.data.remote.DailyPlanSchedule(
                            morning = listOf(
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Morning glass of water",
                                    time = "8:00 AM",
                                    category = "hydration",
                                    details = "Hydrate early to jumpstart your day.",
                                    completed = false,
                                    tier = "lifestyle",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "lifestyle", badge_text = "HABIT", reason = "Daily hydration habit"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "CHECKBOX_ONLY", cta_label = "Done")
                                ),
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Ask Zivaa for your morning briefing",
                                    time = "9:00 AM",
                                    category = "coach",
                                    details = "Check in with your AI coach on your daily focus.",
                                    completed = false,
                                    tier = "coach",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "coach", badge_text = "CHECK-IN", reason = "Daily briefing with Zivaa"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "COACH_CHAT", cta_label = "Ask Zivaa", prefilled_prompt = "What should I focus on today?")
                                )
                            ),
                            afternoon = listOf(
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Log your lunch",
                                    time = "1:00 PM",
                                    category = "nutrition",
                                    details = "Snap a photo of what you ate today.",
                                    completed = false,
                                    tier = "lifestyle",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "lifestyle", badge_text = "MEAL", reason = "Lunch nutrition log"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "LOG_MEAL", target_body_part = "nutrition", cta_label = "Snap Meal")
                                ),
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "15-minute afternoon walk",
                                    time = "4:00 PM",
                                    category = "movement",
                                    details = "A gentle stroll to stay active and energized.",
                                    completed = false,
                                    tier = "lifestyle",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "lifestyle", badge_text = "WALK", reason = "Afternoon energy boost"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "FOLLOW_EXERCISE", routine_title = "Afternoon Stroll & Mobility", cta_label = "Start Routine")
                                )
                            ),
                            evening = listOf(
                                DailyPlanTask(
                                    id = java.util.UUID.randomUUID().toString(),
                                    task = "Evening vitals check & unwind",
                                    time = "8:00 PM",
                                    category = "vitals",
                                    details = "Review your vitals and relax before bed.",
                                    completed = false,
                                    tier = "clinical",
                                    provenance = com.zivaa.app.data.remote.TaskProvenance(source = "clinical_rule", badge_text = "VITALS", reason = "End-of-day health check"),
                                    action = com.zivaa.app.data.remote.TaskAction(action_type = "LOG_VITALS", target_body_part = "blood_pressure", cta_label = "Log Vitals")
                                )
                            ),
                            night = emptyList()
                        )
                    }

                    val summary = if (isFirstDay) {
                        "Welcome to Zivaa! Here is your starter routine to help you get familiar with logging meals, checking vitals, and chatting with your coach."
                    } else {
                        "A steady, gentle routine focused on nutrition, hydration, and movement."
                    }

                    com.zivaa.app.data.remote.SupabaseDailyPlanRecord(
                        id = java.util.UUID.randomUUID().toString(),
                        patient_id = patientId,
                        date = dateStr,
                        created_at = java.time.Instant.now().toString(),
                        summary = summary,
                        schedule = schedule
                    )
                }

                val combined = (existingWeeklyRecords + newRecords).sortedBy { it.date ?: "" }
                allWeeklyPlans = combined
                selectDate(today.toString())
                persistWeeklyPlansToCache()

                try {
                    com.zivaa.app.data.remote.RetrofitClient.apiService.insertDailyPlanRecords(newRecords)
                } catch (e: Exception) {
                    android.util.Log.e("DashboardVM", "Failed to insert starter plans into Supabase", e)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshNudges() {
        viewModelScope.launch {
            try {
                val userId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
                if (userId != null) {
                    val nudgeResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getNudgeAlerts(
                        patientIdQuery = "eq.$userId",
                        acknowledgedQuery = "is.false"
                    )
                    if (nudgeResponse.isSuccessful && !nudgeResponse.body().isNullOrEmpty()) {
                        todayNudgeAlerts = nudgeResponse.body()!!
                    } else {
                        todayNudgeAlerts = emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun generatePlanManually(vitalsMap: Map<String, Double> = emptyMap()) {
        viewModelScope.launch {
            isGeneratingPlan = true
            try {
                val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                val payload = DailyPlanPayload(
                    patient_id = patientId,
                    vitals = vitalsMap,
                    conditions = listOf("Type 2 Diabetes")
                )
                val response = backendApiService.getDailyPlan(payload)
                if (response.isSuccessful) {
                    // Fetch from Supabase again to get the proper ID
                    fetchDailyPlan()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isGeneratingPlan = false
            }
        }
    }

    fun refreshVitalsFromDB() {
        viewModelScope.launch {
            try {
                val userId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
                if (userId != null) {
                    val dbResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getDailyVitals("eq.$userId")
                    if (dbResponse.isSuccessful) {
                        val records = dbResponse.body()
                        if (!records.isNullOrEmpty()) {
                            val latest = records[0]
                            val todayStr = java.time.LocalDate.now().toString()
                            if (latest.date.startsWith(todayStr)) {
                                // Steps are handled live with source priority via Health Connect, not overwritten by DB
                                if (latest.sleepHours != null && latest.sleepHours > 0) {
                                    val hours = latest.sleepHours.toInt()
                                    val minutes = ((latest.sleepHours - hours) * 60).toInt()
                                    sleepHours = "${hours}h ${minutes}m"
                                } else {
                                    sleepHours = "--"
                                    sleepInsightText = null
                                }
                                
                                if (latest.minHeartRate != null && latest.maxHeartRate != null && latest.maxHeartRate > 0) {
                                    heartRate = "${latest.minHeartRate.toInt()}-${latest.maxHeartRate.toInt()}"
                                } else if (latest.avgHeartRate != null && latest.avgHeartRate > 0) {
                                    heartRate = latest.avgHeartRate.toInt().toString()
                                } else {
                                    heartRate = "--"
                                }
                                
                                if (latest.oxygenSatAvg != null && latest.oxygenSatAvg > 0) {
                                    oxygenLevel = "${latest.oxygenSatAvg.toInt()}%"
                                } else {
                                    oxygenLevel = "--"
                                }

                                prefsManager.saveCachedVitals(
                                    dateStr = todayStr,
                                    sleepHours = sleepHours,
                                    heartRate = heartRate,
                                    oxygenLevel = oxygenLevel,
                                    steps = steps,
                                    patientId = userId
                                )
                            } else {
                                sleepHours = "--"
                                sleepInsightText = null
                            }
                        }
                    }

                    try {
                        val planSetupResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getPlanSetup(
                            patientIdQuery = "eq.$userId",
                            stepsGoalQuery = "not.is.null"
                        )
                        if (planSetupResponse.isSuccessful && !planSetupResponse.body().isNullOrEmpty()) {
                            val fetchedGoal = planSetupResponse.body()!!.first().stepsGoal
                            if (fetchedGoal != null && fetchedGoal > 0) {
                                stepsGoal = fetchedGoal
                                prefsManager.saveStepsGoal(fetchedGoal, userId)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardVM", "Error fetching steps goal: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchTodayStepsFastPath() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!healthConnectManager.isSdkAvailable()) return@launch
                val todayStepsRecords = try {
                    healthConnectManager.fetchTodayStepsRecords()
                } catch (e: Exception) {
                    android.util.Log.e("DashboardVM", "Failed to fetch today steps records in fast path", e)
                    emptyList()
                }
                val priorities = prefsManager.getSourcePriorities()
                val resolvedStepsCount = if (todayStepsRecords.isNotEmpty()) {
                    com.zivaa.app.data.health.SourcePriorityManager.resolveSteps(todayStepsRecords, priorities)
                } else {
                    val todayStart = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                    try {
                        healthConnectManager.aggregateSteps(todayStart, java.time.Instant.now()).toLong()
                    } catch (e: Exception) {
                        0L
                    }
                }
                val formatted = java.text.NumberFormat.getNumberInstance().format(resolvedStepsCount)
                android.util.Log.d("DashboardVM", "fetchTodayStepsFastPath resolved $resolvedStepsCount steps from ${todayStepsRecords.size} records")
                withContext(Dispatchers.Main) {
                    steps = formatted
                }
                val userId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
                val todayStr = java.time.LocalDate.now().toString()
                prefsManager.saveCachedSteps(formatted, todayStr, userId)
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error in fetchTodayStepsFastPath", e)
            }
        }
    }

    fun fetchVitalsAndSync(force: Boolean = false) {
        // Fast-path: immediately resolve steps within 20-40ms on a dedicated background thread
        fetchTodayStepsFastPath()

        val currentTime = System.currentTimeMillis()
        if (!force && (currentTime - lastFetchTime) < DEBOUNCE_MS) {
            return
        }
        lastFetchTime = currentTime
        
        viewModelScope.launch {
            isSyncing = true
            syncStatus = "Reading Health Connect..."
            try {
                val rawRecords = try {
                    healthConnectManager.fetchAllAvailableMetrics(1)
                } catch (e: Exception) {
                    android.util.Log.e("DashboardVM", "Failed to fetch Health Connect metrics", e)
                    emptyList()
                }
                
                val todayLocal = java.time.LocalDate.now()
                val apiRecords = rawRecords.mapNotNull { record ->
                    MetricRecord.fromHealthConnectRecord(record)
                }.filter { record ->
                    try {
                        val instant = java.time.Instant.parse(record.timestamp)
                        val recordDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        recordDate == todayLocal
                    } catch (e: Exception) {
                        false
                    }
                }

                // Sync latest source priorities from backend
                try {
                    val prioritiesRes = com.zivaa.app.data.remote.RetrofitClient.apiService.getSourcePriorities()
                    if (prioritiesRes.isSuccessful && !prioritiesRes.body().isNullOrEmpty()) {
                        prefsManager.saveSourcePriorities(prioritiesRes.body()!!)
                    }
                } catch (e: Exception) {
                    // Fall back to built-in defaults
                }

                // 1. Live steps resolution using Health Connect + Source Priority
                val todayStepsRecords = try {
                    healthConnectManager.fetchTodayStepsRecords()
                } catch (e: Exception) {
                    android.util.Log.e("DashboardVM", "Failed to fetch today steps records", e)
                    emptyList()
                }
                val resolvedStepsCount = if (todayStepsRecords.isNotEmpty()) {
                    val priorities = prefsManager.getSourcePriorities()
                    com.zivaa.app.data.health.SourcePriorityManager.resolveSteps(todayStepsRecords, priorities)
                } else {
                    val todayStart = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                    try {
                        healthConnectManager.aggregateSteps(todayStart, java.time.Instant.now()).toLong()
                    } catch (e: Exception) {
                        0L
                    }
                }
                // Immediately update steps state for the UI and cache
                val formattedSteps = java.text.NumberFormat.getNumberInstance().format(resolvedStepsCount)
                steps = formattedSteps
                val currentUserId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
                prefsManager.saveCachedSteps(formattedSteps, todayLocal.toString(), currentUserId)
                val hrValues = apiRecords.filter { it.type == "heart_rate" }
                    .mapNotNull { it.values["bpm"] }
                val hrAvg = hrValues.average()
                val bpSys = apiRecords.filter { it.type == "blood_pressure" }
                    .mapNotNull { it.values["systolic"] }
                    .average()
                val bpDia = apiRecords.filter { it.type == "blood_pressure" }
                    .mapNotNull { it.values["diastolic"] }
                    .average()
                val sleepDuration = apiRecords.filter { it.type == "sleep" }
                    .sumOf { it.values["duration_min"] ?: 0.0 } / 60.0
                val bgAvg = apiRecords.filter { it.type == "blood_glucose" }
                    .mapNotNull { it.values["glucose_mg_dl"] }
                    .average()

                // Fetch morning briefing
                val todayStr = java.time.LocalDate.now().toString()
                val isLateNight = java.time.LocalTime.now().hour in 0..4
                
                try {
                    val userId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId()
                    if (userId != null) {
                        if (!isLateNight) {
                            val briefingResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getMorningBriefing(
                                patientIdQuery = "eq.$userId",
                                dateQuery = "eq.$todayStr"
                            )
                            if (briefingResponse.isSuccessful) {
                                val records = briefingResponse.body()
                                if (!records.isNullOrEmpty() && records[0].date == todayStr) {
                                    morningBriefingText = records[0].summary
                                    morningBriefingHeadline = records[0].headline ?: "Your Daily Briefing"
                                    morningBriefingStatus = MorningBriefingStatus.READY
                                    prefsManager.saveMorningBriefing(todayStr, morningBriefingText, morningBriefingHeadline, userId)
                                } else {
                                    // Today's briefing has not been generated yet
                                    if (sleepHours != "--" && sleepHours.isNotBlank()) {
                                        morningBriefingStatus = MorningBriefingStatus.ANALYZING_REST
                                        morningBriefingHeadline = "Preparing your daily briefing..."
                                        morningBriefingText = "Reviewing your sleep and yesterday's activity to prepare your personalized briefing..."
                                    } else {
                                        morningBriefingStatus = MorningBriefingStatus.AWAITING_SLEEP
                                        val nameGreeting = if (patientFirstName.isNotBlank()) "Good morning, $patientFirstName" else "Good morning"
                                        morningBriefingHeadline = nameGreeting
                                        morningBriefingText = "Wishing you a peaceful and energizing start to your day. As soon as your watch finishes analyzing last night's rest, your full briefing will appear here."
                                    }
                                }
                            } else {
                                if (morningBriefingStatus != MorningBriefingStatus.READY) {
                                    if (sleepHours != "--" && sleepHours.isNotBlank()) {
                                        morningBriefingStatus = MorningBriefingStatus.ANALYZING_REST
                                        morningBriefingHeadline = "Preparing your daily briefing..."
                                        morningBriefingText = "Reviewing your sleep and yesterday's activity to prepare your personalized briefing..."
                                    } else {
                                        morningBriefingStatus = MorningBriefingStatus.AWAITING_SLEEP
                                        val nameGreeting = if (patientFirstName.isNotBlank()) "Good morning, $patientFirstName" else "Good morning"
                                        morningBriefingHeadline = nameGreeting
                                        morningBriefingText = "Wishing you a peaceful and energizing start to your day. As soon as your watch finishes analyzing last night's rest, your full briefing will appear here."
                                    }
                                }
                            }
                        } else {
                            morningBriefingHeadline = "Restful Night"
                            morningBriefingText = "It's late. Sleep well and recharge for tomorrow."
                        }
                        
                        val patientResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getPatient("eq.$userId")
                        if (patientResponse.isSuccessful) {
                            val pRecords = patientResponse.body()
                            if (!pRecords.isNullOrEmpty()) {
                                val fullName = pRecords[0].fullName
                                patientFirstName = fullName.split(" ").firstOrNull() ?: fullName
                                patientProfilePicUrl = pRecords[0].profilePicUrl
                            }
                        }

                        try {
                            val planSetupResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getPlanSetup(
                                patientIdQuery = "eq.$userId",
                                stepsGoalQuery = "not.is.null"
                            )
                            if (planSetupResponse.isSuccessful && !planSetupResponse.body().isNullOrEmpty()) {
                                val fetchedGoal = planSetupResponse.body()!!.first().stepsGoal
                                if (fetchedGoal != null && fetchedGoal > 0) {
                                    stepsGoal = fetchedGoal
                                    prefsManager.saveStepsGoal(fetchedGoal, userId)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("DashboardVM", "Error fetching steps goal: ${e.message}")
                        }
                        
                        val insightResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getUserInsights(
                            patientIdQuery = "eq.$userId",
                            insightTypeQuery = "eq.sleep_hero",
                            insightDateQuery = "eq.$todayStr"
                        )
                        if (insightResponse.isSuccessful) {
                            val insights = insightResponse.body()
                            if (!insights.isNullOrEmpty() && sleepHours != "--" && sleepHours.isNotBlank()) {
                                val rawText = insights[0].insight_text
                                val match = "\\[(.*?)\\]".toRegex().find(rawText)
                                sleepInsightText = match?.groupValues?.get(1)?.lowercase()?.replaceFirstChar { it.uppercase() }
                            } else {
                                sleepInsightText = null
                            }
                        } else {
                            sleepInsightText = null
                        }

                        // Fetch Nudge Alerts that are unacknowledged
                        val nudgeResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getNudgeAlerts(
                            patientIdQuery = "eq.$userId",
                            acknowledgedQuery = "is.false"
                        )
                        if (nudgeResponse.isSuccessful && !nudgeResponse.body().isNullOrEmpty()) {
                            todayNudgeAlerts = nudgeResponse.body()!!
                        } else {
                            todayNudgeAlerts = emptyList()
                        }

                        // Fetch MIDDAY_CHECKIN
                        val middayResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getUserInsights(
                            patientIdQuery = "eq.$userId",
                            insightTypeQuery = "eq.MIDDAY_CHECKIN",
                            insightDateQuery = "eq.$todayStr"
                        )
                        if (middayResponse.isSuccessful && !middayResponse.body().isNullOrEmpty()) {
                            middaySummaryText = middayResponse.body()!![0].insight_text
                            prefsManager.saveMiddaySummary(todayStr, middaySummaryText!!, userId)
                        } else {
                            middaySummaryText = null
                        }

                        // Fetch EVENING_CHECKIN
                        val eveningResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getUserInsights(
                            patientIdQuery = "eq.$userId",
                            insightTypeQuery = "eq.EVENING_CHECKIN",
                            insightDateQuery = "eq.$todayStr"
                        )
                        if (eveningResponse.isSuccessful && !eveningResponse.body().isNullOrEmpty()) {
                            eveningSummaryText = eveningResponse.body()!![0].insight_text
                            prefsManager.saveEveningSummary(todayStr, eveningSummaryText!!, userId)
                        } else {
                            eveningSummaryText = null
                        }

                        // Fetch LATENIGHT_CHECKIN
                        val lateNightResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getUserInsights(
                            patientIdQuery = "eq.$userId",
                            insightTypeQuery = "eq.LATENIGHT_CHECKIN",
                            insightDateQuery = "eq.$todayStr"
                        )
                        if (lateNightResponse.isSuccessful && !lateNightResponse.body().isNullOrEmpty()) {
                            lateNightInsightText = lateNightResponse.body()!![0].insight_text
                        } else {
                            lateNightInsightText = null
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Mood and blood pressure derived from available records
                if (!hrAvg.isNaN()) {
                    mood = if (hrAvg > 80.0) "Elevated" else "Bright"
                }
                if (!bpSys.isNaN() && !bpDia.isNaN()) {
                    bloodPressure = "${bpSys.toInt()}/${bpDia.toInt()}"
                }

                // Build vitals map for daily plan endpoint
                val vitalsMap = mutableMapOf<String, Double>()
                vitalsMap["avg_heart_rate"] = if (hrAvg.isNaN()) 72.0 else hrAvg
                vitalsMap["bp_systolic"] = if (bpSys.isNaN()) 120.0 else bpSys
                vitalsMap["bp_diastolic"] = if (bpDia.isNaN()) 80.0 else bpDia
                
                // Parse sleep_hours from clinical state if available, otherwise fallback to sleepDuration
                val parsedSleepHours = try {
                    if (sleepHours.contains("h")) {
                        val parts = sleepHours.split("h")
                        val h = parts[0].trim().toDoubleOrNull() ?: 0.0
                        val m = parts.getOrNull(1)?.replace("m", "")?.trim()?.toDoubleOrNull() ?: 0.0
                        h + (m / 60.0)
                    } else {
                        sleepDuration
                    }
                } catch (e: Exception) {
                    sleepDuration
                }
                vitalsMap["sleep_hours"] = parsedSleepHours
                vitalsMap["total_steps"] = resolvedStepsCount.toDouble()
                if (!bgAvg.isNaN()) {
                    vitalsMap["glucose_mg_dl"] = bgAvg
                }

                // Fetch daily plan concurrently (force refresh on explicit user sync)
                fetchDailyPlan(vitalsMap, forceRefresh = true)
                flushPendingDailyPlanSyncs()

                // Trigger the background worker to silently handle the massive Supabase sync using changes tokens
                val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId() ?: prefsManager.getLastSyncedPatientId()
                val workData = androidx.work.Data.Builder()
                    .putString("patient_id", patientId)
                    .putBoolean("force_backfill", force)
                    .putString("sync_type", "Foreground")
                    .build()
                val syncConstraints = androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
                val oneTimeWork = androidx.work.OneTimeWorkRequestBuilder<com.zivaa.app.data.health.worker.HealthDataSyncWorker>()
                    .setConstraints(syncConstraints)
                    .setInputData(workData)
                    .build()
                val appContext = getApplication<Application>().applicationContext
                val workManager = androidx.work.WorkManager.getInstance(appContext)
                workManager.enqueueUniqueWork(
                    "ManualHealthDataSync",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    oneTimeWork
                )

                // When background upload completes and Supabase computes vitals_daily, pull fresh vitals into UI
                viewModelScope.launch {
                    workManager.getWorkInfoByIdFlow(oneTimeWork.id).collect { workInfo ->
                        if (workInfo != null && workInfo.state.isFinished) {
                            if (workInfo.state == androidx.work.WorkInfo.State.SUCCEEDED) {
                                refreshVitalsFromDB()
                            }
                            return@collect
                        }
                    }
                }
                
                syncStatus = "Sync complete"
            } catch (e: Exception) {
                e.printStackTrace()
                syncStatus = "Sync error: ${e.localizedMessage}"
            } finally {
                isSyncing = false
            }
        }
    }
}

class DashboardViewModelFactory(
    private val application: Application,
    private val healthConnectManager: HealthConnectManager,
    private val prefsManager: com.zivaa.app.data.local.SyncPrefsManager,
    private val appSettingsManager: com.zivaa.app.data.local.AppSettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(application, healthConnectManager, prefsManager, appSettingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
