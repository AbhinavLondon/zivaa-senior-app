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
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.TimeZone
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.zivaa.app.data.model.DeviceStatusPayload
import android.app.Application
import androidx.lifecycle.AndroidViewModel

import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel(
    application: Application,
    private val healthConnectManager: HealthConnectManager,
    private val prefsManager: com.zivaa.app.data.local.SyncPrefsManager,
    private val appSettingsManager: com.zivaa.app.data.local.AppSettingsManager
) : AndroidViewModel(application) {

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

    var morningBriefingText by mutableStateOf("")
    var morningBriefingHeadline by mutableStateOf("")
    var sleepInsightText by mutableStateOf<String?>("")
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

        val todayStr = java.time.LocalDate.now().toString()
        if (prefsManager.getCachedBriefingDate() == todayStr) {
            morningBriefingText = prefsManager.getMorningBriefingText() ?: ""
            morningBriefingHeadline = prefsManager.getMorningBriefingHeadline() ?: ""
            middaySummaryText = prefsManager.getMiddaySummaryText()
            eveningSummaryText = prefsManager.getEveningSummaryText()
        }

        // Hydrate clinical vitals from SharedPreferences cache if for today
        if (prefsManager.getCachedVitalsDate() == todayStr) {
            prefsManager.getCachedSleepHours()?.let { sleepHours = it }
            prefsManager.getCachedHeartRate()?.let { heartRate = it }
            prefsManager.getCachedOxygenLevel()?.let { oxygenLevel = it }
        }

        val viewedEvening = prefsManager.getListViewedEveningDate() == todayStr
        val viewedAfternoon = prefsManager.getListViewedAfternoonDate() == todayStr
        
        hasViewedEveningSummary = viewedEvening
        hasViewedAfternoonSummary = viewedAfternoon
        
        if (viewedEvening) {
            activeHeroPeriod = "evening"
        } else if (viewedAfternoon) {
            activeHeroPeriod = "afternoon"
        } else {
            activeHeroPeriod = "morning"
        }

        fetchDailyPlan()
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

    var isGeneratingPlan by mutableStateOf(false)
        private set
    private var currentPlanId: String? = null

    private val backendApiService: ZivaaApiService = ZivaaBackendClient.apiService

    var allWeeklyPlans by mutableStateOf<List<com.zivaa.app.data.remote.SupabaseDailyPlanRecord>>(emptyList())
        private set
        
    var selectedDate by mutableStateOf(java.time.LocalDate.now().toString())
        private set

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

    private fun syncTaskStatus(period: String, index: Int, completed: Boolean) {
        val planId = currentPlanId ?: return
        
        // Update locally so other components observing allWeeklyPlans update instantly
        allWeeklyPlans = allWeeklyPlans.map { plan ->
            if (plan.id == planId) {
                plan.copy(
                    schedule = com.zivaa.app.data.remote.DailyPlanSchedule(
                        morning = morningTasks,
                        afternoon = afternoonTasks,
                        evening = eveningTasks,
                        night = nightTasks
                    )
                )
            } else {
                plan
            }
        }
        
        viewModelScope.launch {
            try {
                android.util.Log.d("ZivaaBackend", "Syncing task $period index $index to $completed via Supabase")
                val updatedSchedule = com.zivaa.app.data.remote.DailyPlanSchedule(
                    morning = morningTasks,
                    afternoon = afternoonTasks,
                    evening = eveningTasks,
                    night = nightTasks
                )
                val response = com.zivaa.app.data.remote.RetrofitClient.apiService.updateDailyPlan(
                    idQuery = "eq.$planId",
                    updates = mapOf("schedule" to updatedSchedule)
                )
                if (response.isSuccessful) {
                    android.util.Log.d("ZivaaBackend", "Successfully updated task on Supabase")
                } else {
                    android.util.Log.e("ZivaaBackend", "Failed to update task: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("ZivaaBackend", "Exception updating task", e)
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

    fun fetchDailyPlan(vitalsMap: Map<String, Double>? = null) {
        viewModelScope.launch {
            try {
                val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                
                // Fetch exactly the 7 days of the current week (Sunday to Saturday)
                val today = java.time.LocalDate.now()
                val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong())
                val startOfWeekStr = startOfWeek.toString()
                
                val response = com.zivaa.app.data.remote.RetrofitClient.apiService.getDailyPlans(
                    patientIdQuery = "eq.$patientId",
                    dateQuery = "gte.$startOfWeekStr",
                    limit = 50,
                    order = "date.asc,created_at.desc"
                )
                
                if (response.isSuccessful) {
                    val records = response.body()
                    val todayStr = today.toString()
                    val hasToday = !records.isNullOrEmpty() && records.any { it.date == todayStr || it.created_at?.startsWith(todayStr) == true }

                    if (!records.isNullOrEmpty() && hasToday) {
                        allWeeklyPlans = records
                        // Select today by default
                        selectDate(todayStr)
                    } else {
                        // Missing today or empty weekly plans: auto-provision starter routine!
                        autoProvisionStarterPlans(patientId, records ?: emptyList())
                    }
                } else {
                    autoProvisionStarterPlans(patientId, emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val patientId = com.zivaa.app.data.remote.RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                autoProvisionStarterPlans(patientId, emptyList())
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
                                    task = "Say hello to Zivaa, your AI health coach",
                                    time = "Morning",
                                    category = "coach",
                                    details = "Tap to chat with Zivaa about your day and health goals.",
                                    completed = false
                                ),
                                DailyPlanTask(
                                    task = "Drink a fresh glass of water",
                                    time = "8:00 AM",
                                    category = "hydration",
                                    details = "Start your day refreshed with a full glass of water.",
                                    completed = false
                                )
                            ),
                            afternoon = listOf(
                                DailyPlanTask(
                                    task = "Log your breakfast or lunch",
                                    time = "1:00 PM",
                                    category = "nutrition",
                                    details = "Take a photo or describe your meal to track your nutrition.",
                                    completed = false
                                ),
                                DailyPlanTask(
                                    task = "10-minute gentle walk or stretch",
                                    time = "3:30 PM",
                                    category = "movement",
                                    details = "A brief stroll or gentle stretch to keep your circulation flowing.",
                                    completed = false
                                )
                            ),
                            evening = listOf(
                                DailyPlanTask(
                                    task = "Check your blood pressure or vitals",
                                    time = "7:00 PM",
                                    category = "vitals",
                                    details = "Keep track of your vitals or connect your Health Connect data.",
                                    completed = false
                                )
                            ),
                            night = emptyList()
                        )
                    } else {
                        com.zivaa.app.data.remote.DailyPlanSchedule(
                            morning = listOf(
                                DailyPlanTask(
                                    task = "Morning glass of water",
                                    time = "8:00 AM",
                                    category = "hydration",
                                    details = "Hydrate early to jumpstart your day.",
                                    completed = false
                                ),
                                DailyPlanTask(
                                    task = "Ask Zivaa for your morning briefing",
                                    time = "9:00 AM",
                                    category = "coach",
                                    details = "Check in with your AI coach on your daily focus.",
                                    completed = false
                                )
                            ),
                            afternoon = listOf(
                                DailyPlanTask(
                                    task = "Log your lunch",
                                    time = "1:00 PM",
                                    category = "nutrition",
                                    details = "Snap a photo of what you ate today.",
                                    completed = false
                                ),
                                DailyPlanTask(
                                    task = "15-minute afternoon walk",
                                    time = "4:00 PM",
                                    category = "movement",
                                    details = "A gentle stroll to stay active and energized.",
                                    completed = false
                                )
                            ),
                            evening = listOf(
                                DailyPlanTask(
                                    task = "Evening vitals check & unwind",
                                    time = "8:00 PM",
                                    category = "vitals",
                                    details = "Review your vitals and relax before bed.",
                                    completed = false
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
                                } else if (sleepHours == "7h 40m") {
                                    sleepHours = "0h 0m"
                                }
                                
                                if (latest.minHeartRate != null && latest.maxHeartRate != null && latest.maxHeartRate > 0) {
                                    heartRate = "${latest.minHeartRate.toInt()}-${latest.maxHeartRate.toInt()}"
                                } else if (latest.avgHeartRate != null && latest.avgHeartRate > 0) {
                                    heartRate = latest.avgHeartRate.toInt().toString()
                                } else if (heartRate == "72") {
                                    heartRate = "0"
                                }
                                
                                if (latest.oxygenSatAvg != null && latest.oxygenSatAvg > 0) {
                                    oxygenLevel = "${latest.oxygenSatAvg.toInt()}%"
                                } else if (oxygenLevel == "--") {
                                    oxygenLevel = "--"
                                }

                                prefsManager.saveCachedVitals(
                                    dateStr = todayStr,
                                    sleepHours = sleepHours,
                                    heartRate = heartRate,
                                    oxygenLevel = oxygenLevel
                                )
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
                                prefsManager.saveStepsGoal(fetchedGoal)
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

    fun fetchVitalsAndSync(force: Boolean = false) {
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
                // Immediately update steps state for the UI
                steps = java.text.NumberFormat.getNumberInstance().format(resolvedStepsCount)
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
                            val briefingResponse = com.zivaa.app.data.remote.RetrofitClient.apiService.getMorningBriefing("eq.$userId")
                            if (briefingResponse.isSuccessful) {
                                val records = briefingResponse.body()
                                if (!records.isNullOrEmpty()) {
                                    morningBriefingText = records[0].summary
                                    morningBriefingHeadline = records[0].headline ?: ""
                                    prefsManager.saveMorningBriefing(todayStr, morningBriefingText, morningBriefingHeadline)
                                }
                            }
                        } else {
                            morningBriefingText = ""
                            morningBriefingHeadline = ""
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
                                    prefsManager.saveStepsGoal(fetchedGoal)
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
                            if (!insights.isNullOrEmpty()) {
                                val rawText = insights[0].insight_text
                                val match = "\\[(.*?)\\]".toRegex().find(rawText)
                                sleepInsightText = match?.groupValues?.get(1)?.lowercase()?.replaceFirstChar { it.uppercase() }
                            } else {
                                sleepInsightText = null
                            }
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
                            prefsManager.saveMiddaySummary(todayStr, middaySummaryText!!)
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
                            prefsManager.saveEveningSummary(todayStr, eveningSummaryText!!)
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

                // Fetch daily plan concurrently
                fetchDailyPlan(vitalsMap)

                // Trigger the background worker to silently handle the massive Supabase sync using changes tokens
                val workData = androidx.work.Data.Builder()
                    .putBoolean("force_backfill", force)
                    .build()
                val oneTimeWork = androidx.work.OneTimeWorkRequestBuilder<com.zivaa.app.data.health.worker.HealthDataSyncWorker>()
                    .setInputData(workData)
                    .build()
                val appContext = getApplication<Application>().applicationContext
                androidx.work.WorkManager.getInstance(appContext).enqueueUniqueWork(
                    "ManualHealthDataSync",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    oneTimeWork
                )
                
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
