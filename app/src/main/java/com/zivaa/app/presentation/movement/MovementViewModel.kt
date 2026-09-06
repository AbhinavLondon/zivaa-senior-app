package com.zivaa.app.presentation.movement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MovementViewModel : ViewModel() {
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
            try {
                val userId = RetrofitClient.authManager?.getUserId()
                if (userId != null) {
                    val dbResponse = RetrofitClient.apiService.getDailyVitals("eq.$userId")
                    if (dbResponse.isSuccessful) {
                        val records = dbResponse.body() ?: emptyList()
                        
                        if (records.isNotEmpty()) {
                            val todayDateStr = java.time.LocalDate.now().toString()
                            val todayRecord = records.find { it.date.take(10) == todayDateStr }
                            val steps = todayRecord?.totalSteps ?: 0
                            totalStepsToday = java.text.NumberFormat.getNumberInstance().format(steps)
                            
                            // Fetch steps goal
                            try {
                                val planSetupResponse = RetrofitClient.apiService.getPlanSetup(patientIdQuery = "eq.$userId")
                                if (planSetupResponse.isSuccessful && planSetupResponse.body()?.isNotEmpty() == true) {
                                    goalSteps = planSetupResponse.body()!!.first().stepsGoal
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MovementVM", "Error fetching plan setup: ${e.message}")
                            }

                            val currentGoal = goalSteps
                            if (currentGoal != null) {
                                isGoalMet = steps >= currentGoal
                                overGoalText = if (isGoalMet) "You walked past your goal today, Ranjit." else "You are on your way to your goal today, Ranjit."
                            } else {
                                isGoalMet = false
                                overGoalText = "Set a goal to start tracking your daily progress, Ranjit."
                            }
                            
                            val weekList = mutableListOf<Pair<String, Int>>()
                            var totalWeekSteps = 0
                            
                            val today = LocalDate.now()
                            
                            for (i in 6 downTo 0) {
                                val targetDate = today.minusDays(i.toLong())
                                val dayLetter = targetDate.dayOfWeek.name.take(1)
                                // Ignore time from the timestamp string by extracting only the date part (YYYY-MM-DD)
                                val targetStr = targetDate.toString()
                                
                                // Safely extract the date string to prevent null pointer exceptions if the API returns malformed or missing dates
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
                            android.util.Log.d("MovementVM", "Total week steps: $totalWeekSteps, Average: $averageSteps")


                            val localZone = ZoneId.systemDefault()
                            val todayLocal = LocalDate.now(localZone)
                            val todayStr = todayLocal.toString()
                            val hourlyResponse = RetrofitClient.apiService.getHourlyVitals("eq.$userId")
                            
                            val hours = listOf("6A", "", "", "9A", "", "", "12P", "", "", "3P", "", "", "6P", "", "", "9P")
                            
                            if (hourlyResponse.isSuccessful) {
                                val allRecords = hourlyResponse.body() ?: emptyList()
                                
                                // Convert UTC hour_start to local timezone, then filter by local date and extract local hour
                                data class LocalizedRecord(val localHour: Int, val steps: Int)
                                val localizedRecords = allRecords.mapNotNull { record ->
                                    try {
                                        // Parse UTC timestamp (format: "2026-07-09 19:00:00+00")
                                        val cleanedTs = record.hourStart.replace(" ", "T").replace("+00", "+00:00")
                                        val utcTime = OffsetDateTime.parse(cleanedTs)
                                        val localTime = utcTime.atZoneSameInstant(localZone)
                                        if (localTime.toLocalDate() == todayLocal) {
                                            LocalizedRecord(localTime.hour, record.totalSteps ?: 0)
                                        } else null
                                    } catch (e: Exception) {
                                        android.util.Log.w("MovementVM", "Failed to parse hourStart: ${record.hourStart}", e)
                                        null
                                    }
                                }
                                android.util.Log.d("MovementVM", "Fetched ${localizedRecords.size} hourly records for $todayStr (local tz: $localZone)")
                                val distribution = mutableListOf<Pair<String, Int>>()
                                hours.forEachIndexed { index, label ->
                                    val hour = index + 6 // index 0 is 6A
                                    val stepsForHour = localizedRecords.filter { it.localHour == hour }.sumOf { it.steps }
                                    distribution.add(label to stepsForHour)
                                }
                                hourlySteps = distribution
                            } else {
                                android.util.Log.e("MovementVM", "Hourly HTTP Error: ${hourlyResponse.code()} - ${hourlyResponse.errorBody()?.string()}")
                                hourlySteps = hours.map { it to 0 }
                            }
                            
                            // Fetch dynamic insight (Daily)
                            isInsightLoading = true
                            try {
                                val insightResponse = RetrofitClient.apiService.generateInsight(com.zivaa.app.data.remote.InsightRequest(patient_id = userId, timezone = localZone.id))
                                if (insightResponse.isSuccessful && insightResponse.body() != null) {
                                    insightText = insightResponse.body()?.insight ?: ""
                                } else {
                                    android.util.Log.e("MovementVM", "Insight HTTP Error: ${insightResponse.code()} - ${insightResponse.errorBody()?.string()}")
                                    insightText = "Averaging ${java.text.NumberFormat.getNumberInstance().format(averageSteps)} a day — Keep walking."
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                insightText = "Averaging ${java.text.NumberFormat.getNumberInstance().format(averageSteps)} a day — Keep walking."
                            } finally {
                                isInsightLoading = false
                            }
                            
                            // Fetch dynamic insight (Hourly)
                            isHourlyInsightLoading = true
                            try {
                                val hourlyInsightResponse = RetrofitClient.apiService.generateInsight(com.zivaa.app.data.remote.InsightRequest(patient_id = userId, type = "hourly", timezone = localZone.id))
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

                            // Fetch dynamic insight (Hero)
                            isHeroInsightLoading = true
                            try {
                                val heroInsightResponse = RetrofitClient.apiService.generateInsight(com.zivaa.app.data.remote.InsightRequest(patient_id = userId, type = "hero", timezone = localZone.id))
                                if (heroInsightResponse.isSuccessful && heroInsightResponse.body() != null) {
                                    heroInsightText = heroInsightResponse.body()?.insight ?: "Keep moving, you're doing great!"
                                } else {
                                    heroInsightText = "You are on your way to your goal today, Ranjit."
                                }
                            } catch (e: Exception) {
                                heroInsightText = "You are on your way to your goal today, Ranjit."
                            } finally {
                                isHeroInsightLoading = false
                            }

                        } else {
                            // No records
                            totalStepsToday = "0"
                            isGoalMet = false
                            weeklySteps = List(7) { "-" to 0 }
                            val hours = listOf("6A", "", "", "9A", "", "", "12P", "", "", "3P", "", "", "6P", "", "", "9P")
                            hourlySteps = hours.map { it to 0 }
                        }
                    } else {
                        android.util.Log.e("MovementVM", "HTTP Error: ${dbResponse.code()} - ${dbResponse.errorBody()?.string()}")
                        weeklySteps = List(7) { "-" to 0 }
                    }
                } else {
                    android.util.Log.e("MovementVM", "Error: userId is null")
                    weeklySteps = List(7) { "-" to 0 }
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

    fun saveStepsGoal(newGoal: Int?) {
        viewModelScope.launch {
            try {
                val userId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                
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
                        overGoalText = if (isGoalMet) "You walked past your goal today, Ranjit." else "You are on your way to your goal today, Ranjit."
                    } else {
                        isGoalMet = false
                        overGoalText = "Set a goal to start tracking your daily progress, Ranjit."
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MovementVM", "Failed to save steps goal", e)
            }
        }
    }
}
