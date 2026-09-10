package com.zivaa.app.presentation.movement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MovementViewModel(
    private val healthConnectManager: HealthConnectManager? = null
) : ViewModel() {
    var totalStepsToday by mutableStateOf("0")
    var goalSteps by mutableStateOf<Int?>(null)
    var isGoalMet by mutableStateOf(false)
    var overGoalText by mutableStateOf("")
    var insightText by mutableStateOf("")
        private set
        
    var hourlyInsightText by mutableStateOf("")
        private set
        
    var heroInsightText by mutableStateOf("")
        private set

    var isInsightLoading by mutableStateOf(false)
        private set
        
    var isHourlyInsightLoading by mutableStateOf(false)
        private set
        
    var isHeroInsightLoading by mutableStateOf(false)
        private set

    // Last 7 days of data (e.g., pairs of "Day" to "Steps")
    var weeklySteps by mutableStateOf<List<Pair<String, Int>>>(emptyList())
    var averageSteps by mutableStateOf(0)

    // Hourly distribution (Hour string to Steps)
    var hourlySteps by mutableStateOf<List<Pair<String, Int>>>(emptyList())

    // Loading and error states
    var isLoading by mutableStateOf(true)

    init {
        fetchMovementData()
    }

    fun fetchMovementData() {
        viewModelScope.launch {
            isLoading = true
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val todayStart = today.atStartOfDay(zone).toInstant()
            val now = Instant.now()
            
            val userProfile = RetrofitClient.authManager?.getPatientProfile()
            val rawName = userProfile?.get("full_name")?.trim()
            val firstName = if (!rawName.isNullOrBlank()) {
                rawName.split(" ").firstOrNull { it.isNotBlank() } ?: "there"
            } else {
                "there"
            }

            if (healthConnectManager != null) {
                try {
                    // 1. Total Steps Today from on-device Health Connect
                    val stepsToday = healthConnectManager.aggregateSteps(todayStart, now).toInt()
                    totalStepsToday = java.text.NumberFormat.getNumberInstance().format(stepsToday)

                    // 2. Weekly Steps (Past 7 Days)
                    val weekList = mutableListOf<Pair<String, Int>>()
                    var totalWeekSteps = 0
                    for (i in 6 downTo 0) {
                        val targetDate = today.minusDays(i.toLong())
                        val dayStart = targetDate.atStartOfDay(zone).toInstant()
                        val dayEnd = targetDate.plusDays(1).atStartOfDay(zone).toInstant()
                        val daySteps = healthConnectManager.aggregateSteps(dayStart, dayEnd).toInt()
                        val dayLetter = targetDate.dayOfWeek.name.take(1)
                        weekList.add(dayLetter to daySteps)
                        totalWeekSteps += daySteps
                    }
                    weeklySteps = weekList
                    averageSteps = totalWeekSteps / 7

                    // 3. Hourly Steps Today (6 AM to 9 PM)
                    val hours = listOf("6A", "", "", "9A", "", "", "12P", "", "", "3P", "", "", "6P", "", "", "9P")
                    val startOfDay = today.atStartOfDay(zone)
                    val distribution = mutableListOf<Pair<String, Int>>()
                    hours.forEachIndexed { index, label ->
                        val hour = index + 6 // 6 AM is 6:00
                        val hourStart = startOfDay.plusHours(hour.toLong()).toInstant()
                        val hourEnd = startOfDay.plusHours((hour + 1).toLong()).toInstant()
                        val stepsForHour = if (hourStart.isBefore(now)) {
                            val queryEnd = if (hourEnd.isAfter(now)) now else hourEnd
                            healthConnectManager.aggregateSteps(hourStart, queryEnd).toInt()
                        } else {
                            0
                        }
                        distribution.add(label to stepsForHour)
                    }
                    hourlySteps = distribution

                    // Instantly stop loading spinner so graphs appear immediately
                    isLoading = false

                    // Fetch goal steps from Supabase in background
                    val userId = RetrofitClient.authManager?.getUserId()
                    if (userId != null) {
                        try {
                            val planSetupResponse = RetrofitClient.apiService.getPlanSetup(
                                patientIdQuery = "eq.$userId",
                                stepsGoalQuery = "not.is.null"
                            )
                            if (planSetupResponse.isSuccessful && planSetupResponse.body()?.isNotEmpty() == true) {
                                goalSteps = planSetupResponse.body()!!.first().stepsGoal
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MovementVM", "Error fetching plan setup: ${e.message}")
                        }

                        val currentGoal = goalSteps
                        if (currentGoal != null) {
                            isGoalMet = stepsToday >= currentGoal
                            overGoalText = if (isGoalMet) "You walked past your goal today, $firstName." else "You are on your way to your goal today, $firstName."
                        } else {
                            isGoalMet = false
                            overGoalText = "Set a goal to start tracking your daily progress, $firstName."
                        }

                        // Background AI insights
                        launchInsights(userId, stepsToday, goalSteps ?: 10000, averageSteps, zone.id, firstName)
                    } else {
                        val currentGoal = goalSteps
                        if (currentGoal != null) {
                            isGoalMet = stepsToday >= currentGoal
                            overGoalText = if (isGoalMet) "You walked past your goal today, $firstName." else "You are on your way to your goal today, $firstName."
                        } else {
                            isGoalMet = false
                            overGoalText = "Set a goal to start tracking your daily progress, $firstName."
                        }
                    }
                    return@launch
                } catch (e: Exception) {
                    android.util.Log.e("MovementVM", "Error reading local Health Connect data", e)
                }
            }

            // Fallback to remote database if healthConnectManager is null or errored
            try {
                val userId = RetrofitClient.authManager?.getUserId()
                if (userId != null) {
                    val dbResponse = RetrofitClient.apiService.getDailyVitals("eq.$userId")
                    if (dbResponse.isSuccessful) {
                        val records = dbResponse.body() ?: emptyList()
                        if (records.isNotEmpty()) {
                            val todayDateStr = today.toString()
                            val todayRecord = records.find { it.date.take(10) == todayDateStr }
                            val steps = todayRecord?.totalSteps ?: 0
                            totalStepsToday = java.text.NumberFormat.getNumberInstance().format(steps)

                            try {
                                val planSetupResponse = RetrofitClient.apiService.getPlanSetup(
                                    patientIdQuery = "eq.$userId",
                                    stepsGoalQuery = "not.is.null"
                                )
                                if (planSetupResponse.isSuccessful && planSetupResponse.body()?.isNotEmpty() == true) {
                                    goalSteps = planSetupResponse.body()!!.first().stepsGoal
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MovementVM", "Error fetching plan setup: ${e.message}")
                            }

                            val currentGoal = goalSteps
                            if (currentGoal != null) {
                                isGoalMet = steps >= currentGoal
                                overGoalText = if (isGoalMet) "You walked past your goal today, $firstName." else "You are on your way to your goal today, $firstName."
                            } else {
                                isGoalMet = false
                                overGoalText = "Set a goal to start tracking your daily progress, $firstName."
                            }

                            val weekList = mutableListOf<Pair<String, Int>>()
                            var totalWeekSteps = 0
                            for (i in 6 downTo 0) {
                                val targetDate = today.minusDays(i.toLong())
                                val dayLetter = targetDate.dayOfWeek.name.take(1)
                                val targetStr = targetDate.toString()
                                val recordForDate = records.find { record ->
                                    val safeDate = (record.date as? String) ?: ""
                                    if (safeDate.length >= 10) safeDate.take(10) == targetStr else false
                                }
                                val dailySteps = recordForDate?.totalSteps ?: 0
                                weekList.add(dayLetter to dailySteps)
                                totalWeekSteps += dailySteps
                            }
                            weeklySteps = weekList
                            averageSteps = totalWeekSteps / 7

                            val hourlyResponse = RetrofitClient.apiService.getHourlyVitals("eq.$userId")
                            val hours = listOf("6A", "", "", "9A", "", "", "12P", "", "", "3P", "", "", "6P", "", "", "9P")
                            if (hourlyResponse.isSuccessful) {
                                val allRecords = hourlyResponse.body() ?: emptyList()
                                data class LocalizedRecord(val localHour: Int, val steps: Int)
                                val localizedRecords = allRecords.mapNotNull { record ->
                                    try {
                                        val cleanedTs = record.hourStart.replace(" ", "T").replace("+00", "+00:00")
                                        val utcTime = OffsetDateTime.parse(cleanedTs)
                                        val localTime = utcTime.atZoneSameInstant(zone)
                                        if (localTime.toLocalDate() == today) {
                                            LocalizedRecord(localTime.hour, record.totalSteps ?: 0)
                                        } else null
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                val distribution = mutableListOf<Pair<String, Int>>()
                                hours.forEachIndexed { index, label ->
                                    val hour = index + 6
                                    val stepsForHour = localizedRecords.filter { it.localHour == hour }.sumOf { it.steps }
                                    distribution.add(label to stepsForHour)
                                }
                                hourlySteps = distribution
                            } else {
                                hourlySteps = hours.map { it to 0 }
                            }
                            launchInsights(userId, steps, goalSteps ?: 10000, averageSteps, zone.id, firstName)
                        } else {
                            totalStepsToday = "0"
                            isGoalMet = false
                            weeklySteps = List(7) { "-" to 0 }
                            val hours = listOf("6A", "", "", "9A", "", "", "12P", "", "", "3P", "", "", "6P", "", "", "9P")
                            hourlySteps = hours.map { it to 0 }
                        }
                    } else {
                        weeklySteps = List(7) { "-" to 0 }
                        val hours = listOf("6A", "", "", "9A", "", "", "12P", "", "", "3P", "", "", "6P", "", "", "9P")
                        hourlySteps = hours.map { it to 0 }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                totalStepsToday = "0"
                weeklySteps = List(7) { "-" to 0 }
                hourlySteps = listOf("6A" to 0, "9A" to 0, "12P" to 0, "3P" to 0, "6P" to 0, "9P" to 0)
            } finally {
                isLoading = false
            }
        }
    }

    private fun launchInsights(userId: String, steps: Int, goal: Int, avg: Int, timeZoneId: String, firstName: String) {
        isInsightLoading = true
        viewModelScope.launch {
            try {
                val dailyContext = "Today's Steps: $steps, Daily Goal: $goal, 7-Day Average: $avg"
                val insightResponse = RetrofitClient.apiService.generateInsight(
                    com.zivaa.app.data.remote.InsightRequest(
                        patient_id = userId,
                        timezone = timeZoneId,
                        context = dailyContext
                    )
                )
                if (insightResponse.isSuccessful && insightResponse.body() != null) {
                    insightText = insightResponse.body()?.insight ?: ""
                } else {
                    insightText = "Averaging ${java.text.NumberFormat.getNumberInstance().format(avg)} a day — Keep walking."
                }
            } catch (e: Exception) {
                insightText = "Averaging ${java.text.NumberFormat.getNumberInstance().format(avg)} a day — Keep walking."
            } finally {
                isInsightLoading = false
            }
        }

        isHourlyInsightLoading = true
        viewModelScope.launch {
            try {
                val hourlyInsightResponse = RetrofitClient.apiService.generateInsight(
                    com.zivaa.app.data.remote.InsightRequest(patient_id = userId, type = "hourly", timezone = timeZoneId)
                )
                if (hourlyInsightResponse.isSuccessful && hourlyInsightResponse.body() != null) {
                    hourlyInsightText = hourlyInsightResponse.body()?.insight ?: ""
                } else {
                    hourlyInsightText = "Steady steps throughout the day."
                }
            } catch (e: Exception) {
                hourlyInsightText = "Steady steps throughout the day."
            } finally {
                isHourlyInsightLoading = false
            }
        }

        isHeroInsightLoading = true
        viewModelScope.launch {
            try {
                val heroContext = "Today's Steps: $steps, Daily Goal: $goal"
                val heroInsightResponse = RetrofitClient.apiService.generateInsight(
                    com.zivaa.app.data.remote.InsightRequest(
                        patient_id = userId,
                        type = "hero",
                        timezone = timeZoneId,
                        context = heroContext
                    )
                )
                if (heroInsightResponse.isSuccessful && heroInsightResponse.body() != null) {
                    heroInsightText = heroInsightResponse.body()?.insight ?: "Keep moving, you're doing great!"
                } else {
                    heroInsightText = "You are on your way to your goal today, $firstName."
                }
            } catch (e: Exception) {
                heroInsightText = "You are on your way to your goal today, $firstName."
            } finally {
                isHeroInsightLoading = false
            }
        }
    }

    fun saveStepsGoal(newGoal: Int?) {
        viewModelScope.launch {
            try {
                val userId = RetrofitClient.authManager?.getUserId() ?: return@launch
                val userProfile = RetrofitClient.authManager?.getPatientProfile()
                val rawName = userProfile?.get("full_name")?.trim()
                val firstName = if (!rawName.isNullOrBlank()) {
                    rawName.split(" ").firstOrNull { it.isNotBlank() } ?: "there"
                } else {
                    "there"
                }
                
                // Fetch the latest setup to preserve other fields
                val response = RetrofitClient.apiService.getPlanSetup(patientIdQuery = "eq.$userId")
                val latest = if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    response.body()!!.first()
                } else {
                    com.zivaa.app.data.remote.SupabasePatientPlanSetup(
                        patientId = userId,
                        primaryFocus = null,
                        wakeTime = null,
                        movementLevel = null,
                        stepsGoal = null,
                        dietType = null,
                        heightInches = null,
                        weightKg = null,
                        goalWeightKg = null,
                        healthConditions = emptyList(),
                        eveningActivities = emptyList(),
                        reminders = null
                    )
                }

                // Create new record with updated goal
                val updatedSetup = latest.copy(stepsGoal = newGoal)
                
                // Insert new setup row
                val insertResponse = RetrofitClient.apiService.insertPlanSetup(updatedSetup)
                if (insertResponse.isSuccessful) {
                    goalSteps = newGoal
                    val steps = totalStepsToday.replace(",", "").toIntOrNull() ?: 0
                    if (newGoal != null) {
                        isGoalMet = steps >= newGoal
                        overGoalText = if (isGoalMet) "You walked past your goal today, $firstName." else "You are on your way to your goal today, $firstName."
                    } else {
                        isGoalMet = false
                        overGoalText = "Set a goal to start tracking your daily progress, $firstName."
                    }

                    // Invalidate cached hero insight so fresh AI message is generated immediately
                    try {
                        val todayStr = java.time.LocalDate.now().toString()
                        RetrofitClient.apiService.deleteUserInsights(
                            patientIdQuery = "eq.$userId",
                            insightTypeQuery = "eq.hero",
                            insightDateQuery = "eq.$todayStr"
                        )
                        val targetGoalInt = newGoal ?: 10000
                        val heroContext = "Today's Steps: $steps, Daily Goal: $targetGoalInt"
                        val heroResp = RetrofitClient.apiService.generateInsight(
                            com.zivaa.app.data.remote.InsightRequest(
                                patient_id = userId,
                                type = "hero",
                                timezone = java.time.ZoneId.systemDefault().id,
                                context = heroContext
                            )
                        )
                        if (heroResp.isSuccessful && !heroResp.body()?.insight.isNullOrBlank()) {
                            heroInsightText = heroResp.body()!!.insight
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MovementVM", "Failed to refresh insight after goal change", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MovementVM", "Failed to save steps goal", e)
            }
        }
    }
}

class MovementViewModelFactory(
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovementViewModel(healthConnectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
