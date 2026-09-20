package com.zivaa.app.presentation.movement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabaseDailyVitalRecord
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

enum class MovementTimeRange(val label: String) {
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days"),
    THREE_MONTHS("3 Months")
}

enum class MovementChartType(val label: String) {
    BAR("Bar"),
    LINE("Line")
}

data class DayMobilityData(
    val date: LocalDate,
    val steps: Int,
    val cadence: Int,
    val activeMinutes: Double,
    val activeHours: Int,
    val mobilityScore: Int = 0
)

class MovementViewModel(
    private val healthConnectManager: HealthConnectManager? = null,
    private val prefsManager: com.zivaa.app.data.local.SyncPrefsManager? = null
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

    var selectedTimeRange by mutableStateOf(MovementTimeRange.SEVEN_DAYS)

    fun setTimeRange(range: MovementTimeRange) {
        selectedTimeRange = range
        applyTimeRange(range)
    }

    var selectedChartType by mutableStateOf(MovementChartType.BAR)
        private set

    fun setChartType(type: MovementChartType) {
        selectedChartType = type
    }

    private var allDailyMobilityData: List<DayMobilityData> = emptyList()

    // Longitudinal 7-day data for Movement KPIs
    var weeklySteps by mutableStateOf<List<Pair<String, Int>>>(emptyList())
    var averageSteps by mutableStateOf(0)

    var weeklyCadence by mutableStateOf<List<Pair<String, Int>>>(emptyList())
    var averageCadence by mutableStateOf(0)

    var weeklyActiveMinutes by mutableStateOf<List<Pair<String, Double>>>(emptyList())
    var averageActiveMinutes by mutableStateOf(0.0)

    var weeklyActiveHours by mutableStateOf<List<Pair<String, Int>>>(emptyList())
    var averageActiveHours by mutableStateOf(0.0)

    var weeklyMobilityScore by mutableStateOf<List<Pair<String, Int>>>(emptyList())
    var averageMobilityScore by mutableStateOf(0)

    var mobilityScoreInsightText by mutableStateOf("")
        private set

    var cadenceInsightText by mutableStateOf("")
        private set

    var activeMinutesInsightText by mutableStateOf("")
        private set

    var activeHoursInsightText by mutableStateOf("")
        private set

    // Hourly distribution (Hour string to Steps)
    var hourlySteps by mutableStateOf<List<Pair<String, Int>>>(emptyList())
    var hourlyStepsSource by mutableStateOf<String?>(null)
    var mobilitySummary by mutableStateOf<MobilitySummary?>(null)

    private val hours24Labels = listOf(
        "12A", "", "", "3A", "", "",
        "6A", "", "", "9A", "", "",
        "12P", "", "", "3P", "", "",
        "6P", "", "", "9P", "", ""
    )

    private fun formatSourceLabel(rawSource: String?): String {
        if (rawSource.isNullOrBlank()) return "Phone Pedometer"
        return when {
            rawSource.contains("shealth", ignoreCase = true) -> "Samsung Health"
            rawSource.contains("fitbit", ignoreCase = true) -> "Fitbit"
            rawSource.contains("garmin", ignoreCase = true) -> "Garmin"
            rawSource.contains("withings", ignoreCase = true) -> "Withings"
            rawSource.contains("fitness", ignoreCase = true) -> "Google Fit"
            rawSource.contains("watch", ignoreCase = true) -> "Smartwatch"
            rawSource.contains("phone", ignoreCase = true) -> "Phone Pedometer"
            else -> rawSource.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }

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
                    // 1. Total Steps Today from on-device Health Connect with source priority
                    val todayStepsRecords = healthConnectManager.fetchTodayStepsRecords()
                    val stepsToday = if (todayStepsRecords.isNotEmpty()) {
                        val priorities = prefsManager?.getSourcePriorities() ?: emptyList()
                        com.zivaa.app.data.health.SourcePriorityManager.resolveSteps(todayStepsRecords, priorities).toInt()
                    } else {
                        healthConnectManager.aggregateSteps(todayStart, now).toInt()
                    }
                    android.util.Log.d("MovementDiag", "--- TOTAL RECORDS: ${todayStepsRecords.size} ---")
                    todayStepsRecords.forEach { rec ->
                        android.util.Log.d("MovementDiag", "Record: pkg=${rec.metadata.dataOrigin.packageName}, dev=${rec.metadata.device?.type}, start=${rec.startTime}, end=${rec.endTime}, count=${rec.count}")
                    }
                    totalStepsToday = java.text.NumberFormat.getNumberInstance().format(stepsToday)

                    // 2. Weekly steps & mobility KPIs are harmonized below once mobilitySummary is ready

                    // 3. Hourly Steps Today (24 Hours: Midnight to Midnight)
                    val userId = RetrofitClient.authManager?.getUserId()
                    var loadedFromSupabase = false
                    if (userId != null) {
                        try {
                            val hourlyResponse = RetrofitClient.apiService.getHourlyVitals("eq.$userId", limit = 48)
                            if (hourlyResponse.isSuccessful && !hourlyResponse.body().isNullOrEmpty()) {
                                val allRecords = hourlyResponse.body()!!
                                data class LocalizedHourlyRecord(val localHour: Int, val steps: Int, val source: String?)
                                val localizedRecords = allRecords.mapNotNull { record ->
                                    try {
                                        val cleanedTs = record.hourStart.replace(" ", "T").replace("+00", "+00:00")
                                        val utcTime = OffsetDateTime.parse(cleanedTs)
                                        val localTime = utcTime.atZoneSameInstant(zone)
                                        if (localTime.toLocalDate() == today) {
                                            LocalizedHourlyRecord(localTime.hour, record.totalSteps ?: 0, record.source)
                                        } else null
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                if (localizedRecords.isNotEmpty()) {
                                    val distribution = mutableListOf<Pair<String, Int>>()
                                    hours24Labels.forEachIndexed { hour, label ->
                                        val stepsForHour = localizedRecords.filter { it.localHour == hour }.sumOf { it.steps }
                                        distribution.add(label to stepsForHour)
                                    }
                                    hourlySteps = distribution
                                    val winningSource = localizedRecords
                                        .filter { it.steps > 0 && !it.source.isNullOrBlank() }
                                        .groupBy { it.source }
                                        .maxByOrNull { entry -> entry.value.sumOf { it.steps } }
                                        ?.key
                                    hourlyStepsSource = formatSourceLabel(winningSource)
                                    loadedFromSupabase = true
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MovementVM", "Error loading hourly from Supabase: ${e.message}")
                        }
                    }

                    if (!loadedFromSupabase) {
                        // Local Fallback: Exclude multi-hour/daily summary records (e.g. 24h summary) to avoid 321 flat line
                        val startOfDay = today.atStartOfDay(zone)
                        val granularRecords = todayStepsRecords.filter { rec ->
                            java.time.Duration.between(rec.startTime, rec.endTime).seconds <= 3600
                        }

                        val distribution = mutableListOf<Pair<String, Int>>()
                        if (granularRecords.isNotEmpty()) {
                            val sourcePkg = granularRecords.firstOrNull()?.metadata?.dataOrigin?.packageName
                            val deviceType = granularRecords.firstOrNull()?.metadata?.device?.type
                            val sourceTag = if (deviceType == 1) "${sourcePkg}_watch" else "${sourcePkg}_phone"
                            hourlyStepsSource = formatSourceLabel(sourceTag)

                            for (hour in 0..23) {
                                val hourStart = startOfDay.plusHours(hour.toLong()).toInstant()
                                val hourEnd = startOfDay.plusHours((hour + 1).toLong()).toInstant()
                                val stepsInHour = granularRecords.filter { rec ->
                                    rec.startTime.isBefore(hourEnd) && rec.endTime.isAfter(hourStart)
                                }.sumOf { it.count.toInt() }

                                distribution.add(hours24Labels[hour] to stepsInHour)
                            }
                        } else {
                            for (hour in 0..23) {
                                distribution.add(hours24Labels[hour] to 0)
                            }
                            hourlyStepsSource = null
                        }
                        hourlySteps = distribution
                    }

                    // Instantly stop loading spinner so graphs appear immediately
                    isLoading = false

                    // Fetch goal steps and movement profile from Supabase in background
                    var userMovementLevel: String? = null
                    if (userId != null) {
                        try {
                            val planSetupResponse = RetrofitClient.apiService.getPlanSetup(
                                patientIdQuery = "eq.$userId",
                                stepsGoalQuery = "not.is.null"
                            )
                            if (planSetupResponse.isSuccessful && planSetupResponse.body()?.isNotEmpty() == true) {
                                val setup = planSetupResponse.body()!!.first()
                                goalSteps = setup.stepsGoal
                                userMovementLevel = setup.movementLevel
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

                    val targetActiveHours = if (userMovementLevel?.contains("gentle", ignoreCase = true) == true) 6 else 8

                    // Compute comprehensive 4-KPI Mobility Health Summary
                    val winningSourcePkg = todayStepsRecords.groupBy { it.metadata.dataOrigin.packageName }
                        .maxByOrNull { entry -> entry.value.sumOf { it.count } }?.key
                    mobilitySummary = MobilityCalculator.calculateMobilitySummary(
                        todayStepsRecords = todayStepsRecords,
                        hourlySteps = hourlySteps,
                        resolvedTotalSteps = stepsToday,
                        goalSteps = goalSteps,
                        winningSourcePackage = winningSourcePkg,
                        zone = zone,
                        targetActiveHours = targetActiveHours
                    )

                    val historicalRecords = if (userId != null) {
                        try {
                            val vitalsResp = RetrofitClient.apiService.getDailyVitals("eq.$userId", limit = 100)
                            if (vitalsResp.isSuccessful) vitalsResp.body() ?: emptyList() else emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else emptyList()

                    populateWeeklyMobilityKPIs(
                        historicalRecords = historicalRecords,
                        todaySteps = stepsToday,
                        todayCadence = mobilitySummary?.cadenceSpm ?: 0,
                        todayActiveMinutes = (mobilitySummary?.activeMinutes ?: 0).toDouble(),
                        todayActiveHours = mobilitySummary?.activeHoursCount ?: 0,
                        healthConnectManager = healthConnectManager,
                        today = today,
                        zone = zone
                    )

                    return@launch
                } catch (e: Exception) {
                    android.util.Log.e("MovementVM", "Error reading local Health Connect data", e)
                }
            }

            // Fallback to remote database if healthConnectManager is null or errored
            try {
                val userId = RetrofitClient.authManager?.getUserId()
                if (userId != null) {
                    val dbResponse = RetrofitClient.apiService.getDailyVitals("eq.$userId", limit = 100)
                    if (dbResponse.isSuccessful) {
                        val records = dbResponse.body() ?: emptyList()
                        if (records.isNotEmpty()) {
                            val todayDateStr = today.toString()
                            val todayRecord = records.find { it.date.take(10) == todayDateStr }
                            val steps = todayRecord?.totalSteps ?: 0
                            totalStepsToday = java.text.NumberFormat.getNumberInstance().format(steps)

                            var userMovementLevel: String? = null
                            try {
                                val planSetupResponse = RetrofitClient.apiService.getPlanSetup(
                                    patientIdQuery = "eq.$userId",
                                    stepsGoalQuery = "not.is.null"
                                )
                                if (planSetupResponse.isSuccessful && planSetupResponse.body()?.isNotEmpty() == true) {
                                    val setup = planSetupResponse.body()!!.first()
                                    goalSteps = setup.stepsGoal
                                    userMovementLevel = setup.movementLevel
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MovementVM", "Error fetching plan setup: ${e.message}")
                            }

                            val targetActiveHours = if (userMovementLevel?.contains("gentle", ignoreCase = true) == true) 6 else 8

                            val currentGoal = goalSteps
                            if (currentGoal != null) {
                                isGoalMet = steps >= currentGoal
                                overGoalText = if (isGoalMet) "You walked past your goal today, $firstName." else "You are on your way to your goal today, $firstName."
                            } else {
                                isGoalMet = false
                                overGoalText = "Set a goal to start tracking your daily progress, $firstName."
                            }

                            val hourlyResponse = RetrofitClient.apiService.getHourlyVitals("eq.$userId", limit = 48)
                            if (hourlyResponse.isSuccessful) {
                                val allRecords = hourlyResponse.body() ?: emptyList()
                                data class LocalizedRecord(val localHour: Int, val steps: Int, val source: String?)
                                val localizedRecords = allRecords.mapNotNull { record ->
                                    try {
                                        val cleanedTs = record.hourStart.replace(" ", "T").replace("+00", "+00:00")
                                        val utcTime = OffsetDateTime.parse(cleanedTs)
                                        val localTime = utcTime.atZoneSameInstant(zone)
                                        if (localTime.toLocalDate() == today) {
                                            LocalizedRecord(localTime.hour, record.totalSteps ?: 0, record.source)
                                        } else null
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                val distribution = mutableListOf<Pair<String, Int>>()
                                hours24Labels.forEachIndexed { hour, label ->
                                    val stepsForHour = localizedRecords.filter { it.localHour == hour }.sumOf { it.steps }
                                    distribution.add(label to stepsForHour)
                                }
                                hourlySteps = distribution
                                val winningSource = localizedRecords
                                    .filter { it.steps > 0 && !it.source.isNullOrBlank() }
                                    .groupBy { it.source }
                                    .maxByOrNull { entry -> entry.value.sumOf { it.steps } }
                                    ?.key
                                hourlyStepsSource = formatSourceLabel(winningSource)
                            } else {
                                hourlySteps = hours24Labels.map { it to 0 }
                            }
                            val totalStepsInt = totalStepsToday.replace(",", "").toIntOrNull() ?: 0
                            mobilitySummary = MobilityCalculator.fromFallback(
                                totalSteps = totalStepsInt,
                                formattedSteps = totalStepsToday,
                                goalSteps = goalSteps,
                                distanceMeters = todayRecord?.distanceMeters,
                                activeCalories = todayRecord?.activeCalories,
                                avgCadenceSpm = todayRecord?.avgCadenceSpm,
                                activeMovementMinutes = todayRecord?.activeMovementMinutes,
                                activeHoursCount = todayRecord?.activeHoursCount,
                                sourceLabel = hourlyStepsSource ?: "Phone Pedometer",
                                zone = zone,
                                targetActiveHours = targetActiveHours
                            )

                            populateWeeklyMobilityKPIs(
                                historicalRecords = records,
                                todaySteps = steps,
                                todayCadence = todayRecord?.avgCadenceSpm?.toInt() ?: 0,
                                todayActiveMinutes = todayRecord?.activeMovementMinutes ?: 0.0,
                                todayActiveHours = todayRecord?.activeHoursCount ?: 0,
                                healthConnectManager = null,
                                today = today,
                                zone = zone
                            )

                            launchInsights(userId, steps, goalSteps ?: 10000, averageSteps, zone.id, firstName)
                        } else {
                            totalStepsToday = "0"
                            isGoalMet = false
                            weeklySteps = List(7) { "-" to -1 }
                            weeklyCadence = List(7) { "-" to -1 }
                            weeklyActiveMinutes = List(7) { "-" to -1.0 }
                            weeklyActiveHours = List(7) { "-" to -1 }
                            weeklyMobilityScore = List(7) { "-" to -1 }
                            averageMobilityScore = 0
                            hourlySteps = hours24Labels.map { it to 0 }
                        }
                    } else {
                        weeklySteps = List(7) { "-" to -1 }
                        weeklyCadence = List(7) { "-" to -1 }
                        weeklyActiveMinutes = List(7) { "-" to -1.0 }
                        weeklyActiveHours = List(7) { "-" to -1 }
                        weeklyMobilityScore = List(7) { "-" to -1 }
                        averageMobilityScore = 0
                        hourlySteps = hours24Labels.map { it to 0 }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                totalStepsToday = "0"
                weeklySteps = List(7) { "-" to -1 }
                weeklyCadence = List(7) { "-" to -1 }
                weeklyActiveMinutes = List(7) { "-" to -1.0 }
                weeklyActiveHours = List(7) { "-" to -1 }
                weeklyMobilityScore = List(7) { "-" to -1 }
                averageMobilityScore = 0
                hourlySteps = hours24Labels.map { it to 0 }
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun populateWeeklyMobilityKPIs(
        historicalRecords: List<SupabaseDailyVitalRecord>,
        todaySteps: Int,
        todayCadence: Int,
        todayActiveMinutes: Double,
        todayActiveHours: Int,
        healthConnectManager: HealthConnectManager?,
        today: LocalDate,
        zone: ZoneId
    ) {
        val list = mutableListOf<DayMobilityData>()

        for (i in 89 downTo 0) {
            val targetDate = today.minusDays(i.toLong())
            val targetStr = targetDate.toString()
            val dbRecord = historicalRecords.find { 
                val dateStr = (it.date as? String) ?: ""
                if (dateStr.length >= 10) dateStr.take(10) == targetStr else false
            }

            if (i == 0) {
                val todayScore = mobilitySummary?.mobilityScore?.overallScore
                    ?: MobilityCalculator.calculateMobilityScore(
                        totalSteps = todaySteps,
                        goalSteps = goalSteps,
                        cadenceSpm = todayCadence,
                        activeMinutes = todayActiveMinutes.roundToInt(),
                        activeHoursCount = todayActiveHours,
                        isCompletedDay = false
                    ).overallScore

                list.add(
                    DayMobilityData(
                        date = targetDate,
                        steps = todaySteps,
                        cadence = todayCadence,
                        activeMinutes = todayActiveMinutes,
                        activeHours = todayActiveHours,
                        mobilityScore = todayScore
                    )
                )
            } else {
                val dayStart = targetDate.atStartOfDay(zone).toInstant()
                val dayEnd = targetDate.plusDays(1).atStartOfDay(zone).toInstant()
                val daySteps = if (healthConnectManager != null && i < 30) {
                    try {
                        val hcSteps = healthConnectManager.aggregateSteps(dayStart, dayEnd).toInt()
                        if (hcSteps > 0) hcSteps else (dbRecord?.totalSteps ?: -1)
                    } catch (e: Exception) {
                        dbRecord?.totalSteps ?: -1
                    }
                } else {
                    dbRecord?.totalSteps ?: -1
                }
                val dayCadence = dbRecord?.avgCadenceSpm?.toInt() ?: -1
                val dayActiveMins = dbRecord?.activeMovementMinutes ?: -1.0
                val dayActiveHours = dbRecord?.activeHoursCount ?: -1

                val dayScore = if (daySteps >= 0 || dayCadence >= 0 || dayActiveMins >= 0.0 || dayActiveHours >= 0) {
                    MobilityCalculator.calculateMobilityScore(
                        totalSteps = maxOf(0, daySteps),
                        goalSteps = goalSteps,
                        cadenceSpm = maxOf(0, dayCadence),
                        activeMinutes = maxOf(0.0, dayActiveMins).roundToInt(),
                        activeHoursCount = maxOf(0, dayActiveHours),
                        elapsedDaytimeHours = 12,
                        isCompletedDay = true
                    ).overallScore
                } else {
                    -1
                }

                list.add(
                    DayMobilityData(
                        date = targetDate,
                        steps = daySteps,
                        cadence = dayCadence,
                        activeMinutes = dayActiveMins,
                        activeHours = dayActiveHours,
                        mobilityScore = dayScore
                    )
                )
            }
        }

        allDailyMobilityData = list
        applyTimeRange(selectedTimeRange)
    }

    private fun applyTimeRange(range: MovementTimeRange) {
        if (allDailyMobilityData.isEmpty()) return

        when (range) {
            MovementTimeRange.SEVEN_DAYS -> {
                val days = allDailyMobilityData.takeLast(7)
                weeklySteps = days.map { it.date.dayOfWeek.name.take(1) to it.steps }
                weeklyCadence = days.map { it.date.dayOfWeek.name.take(1) to it.cadence }
                weeklyActiveMinutes = days.map { it.date.dayOfWeek.name.take(1) to it.activeMinutes }
                weeklyActiveHours = days.map { it.date.dayOfWeek.name.take(1) to it.activeHours }
                weeklyMobilityScore = days.map { it.date.dayOfWeek.name.take(1) to it.mobilityScore }

                averageSteps = days.sumOf { it.steps } / 7

                val activeCadenceDays = days.filter { it.cadence > 0 }
                averageCadence = if (activeCadenceDays.isNotEmpty()) (activeCadenceDays.sumOf { it.cadence } / activeCadenceDays.size) else 0

                averageActiveMinutes = days.sumOf { it.activeMinutes } / 7.0
                averageActiveHours = days.sumOf { it.activeHours } / 7.0

                val validScoreDays = days.filter { it.mobilityScore > 0 }
                averageMobilityScore = if (validScoreDays.isNotEmpty()) (validScoreDays.map { it.mobilityScore.toDouble() }.average()).toInt() else (weeklyMobilityScore.lastOrNull()?.second ?: 0)
            }

            MovementTimeRange.THIRTY_DAYS -> {
                val days = allDailyMobilityData.takeLast(30)
                val startMonth = days.first().date.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US)
                val startDay = days.first().date.dayOfMonth
                val startLabel = "$startDay $startMonth"

                weeklySteps = days.mapIndexed { index, d ->
                    val label = when (index) {
                        0 -> startLabel
                        days.size - 1 -> "Today"
                        else -> ""
                    }
                    label to d.steps
                }
                weeklyCadence = days.mapIndexed { index, d ->
                    val label = when (index) {
                        0 -> startLabel
                        days.size - 1 -> "Today"
                        else -> ""
                    }
                    label to d.cadence
                }
                weeklyActiveMinutes = days.mapIndexed { index, d ->
                    val label = when (index) {
                        0 -> startLabel
                        days.size - 1 -> "Today"
                        else -> ""
                    }
                    label to d.activeMinutes
                }
                weeklyActiveHours = days.mapIndexed { index, d ->
                    val label = when (index) {
                        0 -> startLabel
                        days.size - 1 -> "Today"
                        else -> ""
                    }
                    label to d.activeHours
                }
                weeklyMobilityScore = days.mapIndexed { index, d ->
                    val label = when (index) {
                        0 -> startLabel
                        days.size - 1 -> "Today"
                        else -> ""
                    }
                    label to d.mobilityScore
                }

                // Strictly ignore null/zero values for averages
                val validSteps = days.filter { it.steps > 0 }
                averageSteps = if (validSteps.isNotEmpty()) (validSteps.map { it.steps.toDouble() }.average()).toInt() else 0

                val validCadence = days.filter { it.cadence > 0 }
                averageCadence = if (validCadence.isNotEmpty()) (validCadence.map { it.cadence.toDouble() }.average()).toInt() else 0

                val validMins = days.filter { it.activeMinutes > 0.0 }
                averageActiveMinutes = if (validMins.isNotEmpty()) validMins.map { it.activeMinutes }.average() else 0.0

                val validHours = days.filter { it.activeHours > 0 }
                averageActiveHours = if (validHours.isNotEmpty()) validHours.map { it.activeHours.toDouble() }.average() else 0.0

                val validScores = days.filter { it.mobilityScore > 0 }
                averageMobilityScore = if (validScores.isNotEmpty()) (validScores.map { it.mobilityScore.toDouble() }.average()).toInt() else (weeklyMobilityScore.lastOrNull()?.second ?: 0)
            }

            MovementTimeRange.THREE_MONTHS -> {
                // 12 weekly bars across 84 days
                val days = allDailyMobilityData.takeLast(84)
                val weeks = days.chunked(7)

                val stepsList = mutableListOf<Pair<String, Int>>()
                val cadenceList = mutableListOf<Pair<String, Int>>()
                val activeMinsList = mutableListOf<Pair<String, Double>>()
                val activeHoursList = mutableListOf<Pair<String, Int>>()
                val mobilityScoreList = mutableListOf<Pair<String, Int>>()

                weeks.forEachIndexed { weekIndex, weekDays ->
                    val label = when (weekIndex) {
                        0 -> {
                            val sm = weekDays.first().date.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US)
                            "${weekDays.first().date.dayOfMonth} $sm"
                        }
                        weeks.size - 1 -> "This wk"
                        else -> ""
                    }

                    val activeWeekSteps = weekDays.filter { it.steps > 0 }
                    val weekSteps = if (activeWeekSteps.isNotEmpty()) activeWeekSteps.map { it.steps.toDouble() }.average().toInt() else 0

                    val activeWeekCadence = weekDays.filter { it.cadence > 0 }
                    val weekCadence = if (activeWeekCadence.isNotEmpty()) activeWeekCadence.map { it.cadence.toDouble() }.average().toInt() else 0

                    val activeWeekMins = weekDays.filter { it.activeMinutes > 0.0 }
                    val weekMins = if (activeWeekMins.isNotEmpty()) activeWeekMins.map { it.activeMinutes }.average() else 0.0

                    val activeWeekHours = weekDays.filter { it.activeHours > 0 }
                    val weekHours = if (activeWeekHours.isNotEmpty()) activeWeekHours.map { it.activeHours.toDouble() }.average().toInt() else 0

                    val activeWeekScores = weekDays.filter { it.mobilityScore > 0 }
                    val weekScore = if (activeWeekScores.isNotEmpty()) activeWeekScores.map { it.mobilityScore.toDouble() }.average().toInt() else 0

                    stepsList.add(label to weekSteps)
                    cadenceList.add(label to weekCadence)
                    activeMinsList.add(label to weekMins)
                    activeHoursList.add(label to weekHours)
                    mobilityScoreList.add(label to weekScore)
                }

                weeklySteps = stepsList
                weeklyCadence = cadenceList
                weeklyActiveMinutes = activeMinsList
                weeklyActiveHours = activeHoursList
                weeklyMobilityScore = mobilityScoreList

                // Overall 3-month averages strictly ignoring null/zero values across all days
                val validSteps = allDailyMobilityData.filter { it.steps > 0 }
                averageSteps = if (validSteps.isNotEmpty()) (validSteps.map { it.steps.toDouble() }.average()).toInt() else 0

                val validCadence = allDailyMobilityData.filter { it.cadence > 0 }
                averageCadence = if (validCadence.isNotEmpty()) (validCadence.map { it.cadence.toDouble() }.average()).toInt() else 0

                val validMins = allDailyMobilityData.filter { it.activeMinutes > 0.0 }
                averageActiveMinutes = if (validMins.isNotEmpty()) validMins.map { it.activeMinutes }.average() else 0.0

                val validHours = allDailyMobilityData.filter { it.activeHours > 0 }
                averageActiveHours = if (validHours.isNotEmpty()) validHours.map { it.activeHours.toDouble() }.average() else 0.0

                val validScores = allDailyMobilityData.filter { it.mobilityScore > 0 }
                averageMobilityScore = if (validScores.isNotEmpty()) (validScores.map { it.mobilityScore.toDouble() }.average()).toInt() else (weeklyMobilityScore.lastOrNull()?.second ?: 0)
            }
        }

        updateInsightsForTimeRange(range)
    }

    private fun updateInsightsForTimeRange(range: MovementTimeRange) {
        val periodLabel = when (range) {
            MovementTimeRange.SEVEN_DAYS -> "week"
            MovementTimeRange.THIRTY_DAYS -> "last 30 days"
            MovementTimeRange.THREE_MONTHS -> "last 3 months"
        }

        mobilityScoreInsightText = if (averageMobilityScore >= 85) {
            "Averaging $averageMobilityScore/100 across the $periodLabel — Optimal Mobility tier with outstanding volume, pace, and daily regularity."
        } else if (averageMobilityScore >= 70) {
            "Averaging $averageMobilityScore/100 across the $periodLabel — Steady & Active tier maintaining a solid daily functional baseline."
        } else if (averageMobilityScore >= 50) {
            "Averaging $averageMobilityScore/100 across the $periodLabel — Building Rhythm tier with good activity, and opportunity to boost pace or hourly breaks."
        } else if (averageMobilityScore > 0) {
            "Averaging $averageMobilityScore/100 across the $periodLabel — Gentle Mobility tier. Short walks and frequent movement will raise your score."
        } else {
            "Composite mobility vitality (0–100) combining step volume, walking pace, moving time, and hourly breaks."
        }

        if (insightText.isEmpty() || insightText.contains("Averaging")) {
            insightText = "Averaging ${java.text.NumberFormat.getNumberInstance().format(averageSteps)} steps a day across the $periodLabel."
        }

        cadenceInsightText = if (averageCadence > 0) {
            val tier = when {
                averageCadence >= 100 -> "Brisk & Confident pace (100+ spm), supporting high cardiovascular vitality"
                averageCadence >= 80 -> "Steady & Stable pace (80-100 spm), ideal for balance and endurance"
                else -> "Gentle Mobility pace (<80 spm), excellent for joint flexibility and continuous circulation"
            }
            "Averaging $averageCadence spm across walking bouts — $tier over the $periodLabel."
        } else {
            "Gentle pacing recorded across the $periodLabel. Sustained walking bouts will populate your average cadence."
        }

        val roundedMins = kotlin.math.round(averageActiveMinutes).toInt()
        activeMinutesInsightText = if (roundedMins >= 30) {
            "Averaging $roundedMins mins/day of active upright movement, surpassing the recommended 30-minute daily activity target across the $periodLabel."
        } else if (roundedMins > 0) {
            "Averaging $roundedMins mins/day on your feet over the $periodLabel. Aiming for 30 minutes a day supports long-term mobility and bone density."
        } else {
            "Upright moving time counts every minute you spend walking on your feet each day."
        }

        val formattedHours = String.format(java.util.Locale.US, "%.1f", averageActiveHours)
        activeHoursInsightText = if (averageActiveHours >= 6.0) {
            "Active across an average of $formattedHours daytime hours each day — excellent movement regularity with few prolonged sitting stretches over the $periodLabel."
        } else if (averageActiveHours > 0) {
            "Active across an average of $formattedHours daytime hours each day over the $periodLabel. Walking at least 150 steps each hour between 8 AM and 8 PM helps break up sedentary time."
        } else {
            "Tracks hours between 8 AM and 8 PM with at least 150 steps to help you stay regularly active throughout the day."
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

                    mobilitySummary = mobilitySummary?.copy(
                        goalSteps = newGoal,
                        goalProgress = if (newGoal != null && newGoal > 0) {
                            (mobilitySummary?.totalSteps?.toFloat() ?: steps.toFloat()) / newGoal.toFloat()
                        } else null
                    )

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
    private val healthConnectManager: HealthConnectManager,
    private val prefsManager: com.zivaa.app.data.local.SyncPrefsManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovementViewModel(healthConnectManager, prefsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
