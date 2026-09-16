package com.zivaa.app.presentation.rest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabaseZivaaScoreRecord
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.zivaa.app.data.remote.SupabaseDailyVitalRecord

enum class RestTimeRange(val label: String) {
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days"),
    THREE_MONTHS("3 Months")
}

enum class RestChartType(val label: String) {
    BAR("Bar"),
    LINE("Line")
}

class RestViewModel : ViewModel() {
    var restScore by mutableStateOf<Int?>(null)
    var restBreakdown by mutableStateOf<Map<String, Any?>?>(null)
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedTimeRange by mutableStateOf(RestTimeRange.SEVEN_DAYS)
        private set
    var selectedChartType by mutableStateOf(RestChartType.BAR)
        private set

    private var allVitals = emptyList<SupabaseDailyVitalRecord>()
    private var allScores = emptyList<SupabaseZivaaScoreRecord>()

    var chartDates by mutableStateOf<List<String>>(emptyList())
    var chartDetailedDates by mutableStateOf<List<String>>(emptyList())
    var chartRestScores by mutableStateOf<List<Float>>(emptyList())
    var chartTotalSleep by mutableStateOf<List<Float>>(emptyList())
    var chartSleepDeep by mutableStateOf<List<Float>>(emptyList())
    var chartSleepRem by mutableStateOf<List<Float>>(emptyList())
    var chartSleepLight by mutableStateOf<List<Float>>(emptyList())
    var chartRestingHr by mutableStateOf<List<Float>>(emptyList())
    var chartSkinTemp by mutableStateOf<List<Float>>(emptyList())
    var chartRespRate by mutableStateOf<List<Float>>(emptyList())

    var startDateLabel by mutableStateOf("")
    var midDateLabel by mutableStateOf("")
    var hasSkinTempData by mutableStateOf(false)
    var hasRespRateData by mutableStateOf(false)

    var averageRestScore by mutableStateOf(0f)
    var averageTotalSleep by mutableStateOf(0f)
    var averageRestingHr by mutableStateOf(0f)

    fun setTimeRange(range: RestTimeRange) {
        selectedTimeRange = range
        applyTimeRange(range)
    }

    fun setChartType(type: RestChartType) {
        selectedChartType = type
    }

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: return@launch
                
                // Fetch up to 100 days of history
                val scoreResponse = RetrofitClient.apiService.getZivaaScores(
                    patientIdQuery = "eq.${patientId}",
                    limit = 100
                )
                
                val vitalsResponse = RetrofitClient.apiService.getDailyVitals(
                    patientIdQuery = "eq.${patientId}",
                    limit = 100
                )

                if (scoreResponse.isSuccessful && !scoreResponse.body().isNullOrEmpty()) {
                    allScores = scoreResponse.body()!!
                    val todayDateStr = java.time.LocalDate.now().toString()
                    if (vitalsResponse.isSuccessful && !vitalsResponse.body().isNullOrEmpty()) {
                        allVitals = vitalsResponse.body()!!
                    }
                    val latestScore = allScores.first()
                    val latestVitals = if (vitalsResponse.isSuccessful && !vitalsResponse.body().isNullOrEmpty()) vitalsResponse.body()!!.first() else null
                    
                    if (latestScore.date.take(10) == todayDateStr || (latestVitals != null && latestVitals.date.take(10) == todayDateStr)) {
                        restScore = if (latestScore.date.take(10) == todayDateStr) latestScore.restScore else 0

                    
                    val mergedBreakdown: MutableMap<String, Any?> = latestScore.restBreakdown?.toMutableMap<String, Any?>() ?: mutableMapOf()
                    
                    if (latestVitals != null && latestVitals.date.take(10) == todayDateStr) {
                        latestVitals.sleepHours?.let { mergedBreakdown["sleep_hours"] = it }
                        latestVitals.sleepEfficiencyPct?.let { mergedBreakdown["sleep_efficiency_pct"] = it }
                        latestVitals.sleepStage5Hours?.let { mergedBreakdown["sleep_stage_5_hours"] = it }
                        latestVitals.sleepStage6Hours?.let { mergedBreakdown["sleep_stage_6_hours"] = it }
                        latestVitals.restingHeartRateCalculated?.let { mergedBreakdown["resting_heart_rate"] = it }
                        
                        if (latestVitals.skinTemperatureDelta == null) {
                            mergedBreakdown.remove("skin_temp_delta")
                        } else {
                            mergedBreakdown["skin_temp_delta"] = latestVitals.skinTemperatureDelta
                        }
                        
                        if (latestVitals.respiratoryRateAvg == null) {
                            mergedBreakdown.remove("respiratory_rate")
                        } else {
                            mergedBreakdown["respiratory_rate"] = latestVitals.respiratoryRateAvg
                        }
                    }
                    
                    if (mergedBreakdown.containsKey("duration") && !mergedBreakdown.containsKey("duration_pts")) {
                        mergedBreakdown["duration_pts"] = mergedBreakdown["duration"]!!
                    }
                    if (mergedBreakdown.containsKey("quality") && !mergedBreakdown.containsKey("quality_pts")) {
                        mergedBreakdown["quality_pts"] = mergedBreakdown["quality"]!!
                    }
                    if (mergedBreakdown.containsKey("vitals") && !mergedBreakdown.containsKey("vitals_pts")) {
                        mergedBreakdown["vitals_pts"] = mergedBreakdown["vitals"]!!
                    }
                    if (mergedBreakdown.containsKey("penalties") && !mergedBreakdown.containsKey("penalty_pts")) {
                        mergedBreakdown["penalty_pts"] = mergedBreakdown["penalties"]!!
                    }
                    
                    restBreakdown = mergedBreakdown
                    } else {
                        restScore = 0
                        restBreakdown = null
                    }
                } else {
                    restScore = null
                    restBreakdown = null
                    allVitals = emptyList()
                    allScores = emptyList()
                }

                // Initial slice for charts
                applyTimeRange(selectedTimeRange)

            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch rest score."
            } finally {
                isLoading = false
            }
        }
    }

    private fun applyTimeRange(range: RestTimeRange) {
        if (allVitals.isEmpty()) return

        val daysCount = when (range) {
            RestTimeRange.SEVEN_DAYS -> 7
            RestTimeRange.THIRTY_DAYS -> 30
            RestTimeRange.THREE_MONTHS -> 84
        }
        
        val targetVitalsRaw = allVitals.take(daysCount).reversed()
        val targetScoresRaw = allScores.take(daysCount).reversed()

        val scoreMap = targetScoresRaw.associateBy { it.date.take(10) }

        if (range == RestTimeRange.THREE_MONTHS) {
            val chunks = targetVitalsRaw.chunked(7)
            startDateLabel = chunks.firstOrNull()?.firstOrNull()?.let { formatShortDate(it.date) } ?: ""
            midDateLabel = "6 weeks ago"

            chartDates = chunks.mapIndexed { index, chunk ->
                if (index == chunks.size - 1) "Today"
                else if (index == 0) formatShortDate(chunk.first().date)
                else ""
            }

            chartDetailedDates = chunks.map { chunk -> formatChunkDateRange(chunk) }
            
            chartRestScores = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { scoreMap[it.date.take(10)]?.restScore?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }
            chartTotalSleep = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { it.sleepHours?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }
            chartSleepDeep = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { it.sleepStage5Hours?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }
            chartSleepRem = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { it.sleepStage6Hours?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }
            chartSleepLight = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { vital ->
                    val total = vital.sleepHours?.toFloat() ?: 0f
                    val deep = vital.sleepStage5Hours?.toFloat() ?: 0f
                    val rem = vital.sleepStage6Hours?.toFloat() ?: 0f
                    (total - deep - rem).coerceAtLeast(0f)
                }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }
            chartRestingHr = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { it.restingHeartRateCalculated?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }
            chartSkinTemp = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { it.skinTemperatureDelta?.toFloat() }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }
            chartRespRate = chunks.map { chunk -> 
                val vals = chunk.mapNotNull { it.respiratoryRateAvg?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else 0f
            }

            hasSkinTempData = chunks.any { chunk -> chunk.any { it.skinTemperatureDelta != null } }
            hasRespRateData = chunks.any { chunk -> chunk.any { it.respiratoryRateAvg != null && it.respiratoryRateAvg > 0 } }
        } else {
            startDateLabel = targetVitalsRaw.firstOrNull()?.let { formatShortDate(it.date) } ?: ""
            midDateLabel = ""

            chartDates = targetVitalsRaw.map { getShortDayLabel(it.date, range) }
            chartDetailedDates = targetVitalsRaw.map { formatDetailedDate(it.date) }
            
            chartRestScores = targetVitalsRaw.map { vital -> 
                scoreMap[vital.date.take(10)]?.restScore?.toFloat() ?: 0f 
            }
            
            chartTotalSleep = targetVitalsRaw.map { it.sleepHours?.toFloat() ?: 0f }
            
            chartSleepDeep = targetVitalsRaw.map { it.sleepStage5Hours?.toFloat() ?: 0f }
            chartSleepRem = targetVitalsRaw.map { it.sleepStage6Hours?.toFloat() ?: 0f }
            chartSleepLight = targetVitalsRaw.map { vital ->
                val total = vital.sleepHours?.toFloat() ?: 0f
                val deep = vital.sleepStage5Hours?.toFloat() ?: 0f
                val rem = vital.sleepStage6Hours?.toFloat() ?: 0f
                (total - deep - rem).coerceAtLeast(0f)
            }
            
            chartRestingHr = targetVitalsRaw.map { it.restingHeartRateCalculated?.toFloat() ?: 0f }
            chartSkinTemp = targetVitalsRaw.map { it.skinTemperatureDelta?.toFloat() ?: 0f }
            chartRespRate = targetVitalsRaw.map { it.respiratoryRateAvg?.toFloat() ?: 0f }

            hasSkinTempData = targetVitalsRaw.any { it.skinTemperatureDelta != null }
            hasRespRateData = targetVitalsRaw.any { it.respiratoryRateAvg != null && it.respiratoryRateAvg > 0 }
        }

        averageRestScore = chartRestScores.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageTotalSleep = chartTotalSleep.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageRestingHr = chartRestingHr.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
    }

    private fun formatShortDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString.take(10))
            val sm = date.month.getDisplayName(TextStyle.SHORT, Locale.US)
            "${date.dayOfMonth} $sm"
        } catch (e: Exception) {
            dateString.takeLast(5)
        }
    }

    private fun formatDetailedDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString.take(10))
            val sm = date.month.getDisplayName(TextStyle.SHORT, Locale.US)
            "${date.dayOfMonth} $sm"
        } catch (e: Exception) {
            dateString.takeLast(5)
        }
    }

    private fun formatChunkDateRange(chunk: List<SupabaseDailyVitalRecord>): String {
        if (chunk.isEmpty()) return ""
        return try {
            val start = LocalDate.parse(chunk.first().date.take(10))
            val end = LocalDate.parse(chunk.last().date.take(10))
            val sm1 = start.month.getDisplayName(TextStyle.SHORT, Locale.US)
            val sm2 = end.month.getDisplayName(TextStyle.SHORT, Locale.US)
            if (sm1 == sm2) {
                "${start.dayOfMonth} - ${end.dayOfMonth} $sm1"
            } else {
                "${start.dayOfMonth} $sm1 - ${end.dayOfMonth} $sm2"
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun getShortDayLabel(dateString: String, range: RestTimeRange): String {
        return try {
            val date = LocalDate.parse(dateString.take(10))
            if (range == RestTimeRange.SEVEN_DAYS) {
                date.dayOfWeek.name.take(1) // "M", "T"
            } else {
                "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.US)}"
            }
        } catch (e: Exception) {
            dateString.takeLast(5)
        }
    }
}

class RestViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
