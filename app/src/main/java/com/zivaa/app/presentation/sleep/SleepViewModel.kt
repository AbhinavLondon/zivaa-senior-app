package com.zivaa.app.presentation.sleep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SleepNightSummary(
    val date: String,
    val sleepHours: Double
)

enum class NightEventType { ASLEEP, AWAKE, DEEP, REM }

data class NightEvent(
    val time: String,
    val text: String,
    val type: NightEventType
)

class SleepViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var sleepHistory by mutableStateOf<List<SleepNightSummary>>(emptyList())
    
    // Derived values
    var latestSleepHoursText by mutableStateOf("0h 0m")
    var averageSleepHoursText by mutableStateOf("0h 0m")
    
    var latestSleepStartEndTime by mutableStateOf("")
        private set
    
    var weeklyInsightText by mutableStateOf("")
        private set
        
    var isWeeklyInsightLoading by mutableStateOf(false)
        private set
    
    var heroInsightText by mutableStateOf("")
        private set
    
    var isHeroInsightLoading by mutableStateOf(false)
        private set
        
    var nightEvents by mutableStateOf<List<NightEvent>>(emptyList())
        private set
        
    var nightSummaryText by mutableStateOf("Loading night summary...")
        private set
    
    init {
        fetchSleepData()
    }
    
    private fun fetchSleepData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = RetrofitClient.authManager?.getUserId()
                if (userId != null) {
                    val response = RetrofitClient.apiService.getDailyVitals(
                        patientIdQuery = "eq.$userId",
                        order = "date.desc",
                        limit = 7
                    )
                    
                    if (response.isSuccessful) {
                        val records = response.body() ?: emptyList()
                        val localZone = java.time.ZoneId.systemDefault()
                        val todayLocal = java.time.LocalDate.now(localZone)
                        
                        val summaries = mutableListOf<SleepNightSummary>()
                        
                        // We want exactly 7 days ending today, in chronological order
                        for (i in 6 downTo 0) {
                            val targetDate = todayLocal.minusDays(i.toLong())
                            val targetStr = targetDate.toString()
                            
                            val record = records.find { (it.date?.take(10) ?: "") == targetStr }
                            if (record != null && record.sleepHours != null && record.sleepHours > 0) {
                                summaries.add(SleepNightSummary(targetStr, record.sleepHours))
                            } else {
                                summaries.add(SleepNightSummary(targetStr, 0.0))
                            }
                        }
                        
                        sleepHistory = summaries
                        
                        // Calculate average of days that actually have sleep data
                        val validDays = sleepHistory.filter { it.sleepHours > 0 }
                        val avg = if (validDays.isNotEmpty()) validDays.map { it.sleepHours }.average() else 0.0
                        averageSleepHoursText = formatHoursMinutes(avg)
                        
                        // Latest sleep is today's record (which is the last one in the 7-day list)
                        val latest = sleepHistory.last().sleepHours
                        latestSleepHoursText = formatHoursMinutes(latest)
                        
                    } // end if response.isSuccessful
                    
                    // Fetch raw sleep session for "How the night went"
                    try {
                        val rawResponse = RetrofitClient.apiService.getRawVitals(
                            patientIdQuery = "eq.$userId",
                            metricTypeQuery = "eq.SleepSessionRecord",
                            limit = 10
                        )
                        if (rawResponse.isSuccessful) {
                            val rawRecords = rawResponse.body() ?: emptyList()
                            if (rawRecords.isNotEmpty()) {
                                val contextStr = parseRawSleepEvents(rawRecords)
                                if (contextStr != null) {
                                    val localZone = java.time.ZoneId.systemDefault()
                                    val summaryResponse = RetrofitClient.apiService.generateInsight(
                                        com.zivaa.app.data.remote.InsightRequest(patient_id = userId, type = "sleep_summary", timezone = localZone.id, context = contextStr)
                                    )
                                    if (summaryResponse.isSuccessful) {
                                        nightSummaryText = summaryResponse.body()?.insight ?: "A calm night of sleep."
                                    } else {
                                        nightSummaryText = "Unable to generate summary."
                                    }
                                }
                            } else {
                                nightSummaryText = "Waiting for tonight's sleep..."
                                nightEvents = emptyList()
                                latestSleepStartEndTime = ""
                            }
                        } else {
                            nightSummaryText = "Unable to fetch sleep details."
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        nightSummaryText = "Unable to connect."
                    }
                    
                    val localZone = java.time.ZoneId.systemDefault()
                    
                    // Fetch dynamic insight for sleep
                    isWeeklyInsightLoading = true
                    try {
                        val insightResponse = RetrofitClient.apiService.generateInsight(
                            com.zivaa.app.data.remote.InsightRequest(patient_id = userId, type = "sleep_weekly", timezone = localZone.id)
                        )
                        if (insightResponse.isSuccessful && insightResponse.body() != null) {
                            weeklyInsightText = insightResponse.body()?.insight ?: ""
                        } else {
                            android.util.Log.e("SleepVM", "Insight HTTP Error: ${insightResponse.code()} - ${insightResponse.errorBody()?.string()}")
                            weeklyInsightText = "Averaging $averageSleepHoursText this week. Try maintaining a consistent schedule."
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        weeklyInsightText = "Averaging $averageSleepHoursText this week. Try maintaining a consistent schedule."
                    } finally {
                        isWeeklyInsightLoading = false
                    }
                    
                    // Fetch hero insight for sleep (max 5 words)
                    isHeroInsightLoading = true
                    try {
                        val heroResponse = RetrofitClient.apiService.generateInsight(
                            com.zivaa.app.data.remote.InsightRequest(patient_id = userId, type = "sleep_hero", timezone = localZone.id)
                        )
                        if (heroResponse.isSuccessful && heroResponse.body() != null) {
                            heroInsightText = heroResponse.body()?.insight ?: ""
                        } else {
                            heroInsightText = "A calm, restful night"
                        }
                    } catch (e: Exception) {
                        heroInsightText = "A calm, restful night"
                    } finally {
                        isHeroInsightLoading = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun formatHoursMinutes(hoursDecimal: Double): String {
        val hours = hoursDecimal.toInt()
        val minutes = ((hoursDecimal - hours) * 60).toInt()
        return "${hours}h ${minutes}m"
    }

    private fun parseRawSleepEvents(records: List<com.zivaa.app.data.remote.SupabaseVitalRecord>): String? {
        try {
            if (records.isEmpty()) return null
            val localZone = java.time.ZoneId.systemDefault()
            
            data class RawStage(val stage: Int, val start: java.time.Instant, val end: java.time.Instant)
            val allStages = mutableListOf<RawStage>()
            
            for (record in records) {
                val stagesObj = record.values["stages"] as? List<*> ?: continue
                val parsedStages = stagesObj.mapNotNull {
                    val map = it as? Map<*, *> ?: return@mapNotNull null
                    val stage = (map["stage"] as? Double)?.toInt() ?: return@mapNotNull null
                    val startStr = map["start_time"] as? String ?: return@mapNotNull null
                    val endStr = map["end_time"] as? String ?: return@mapNotNull null
                    RawStage(
                        stage = stage,
                        start = java.time.Instant.parse(startStr),
                        end = java.time.Instant.parse(endStr)
                    )
                }
                allStages.addAll(parsedStages)
            }
            
            if (allStages.isEmpty()) return null
            
            // 1. Find absolute latest stage to anchor the window
            allStages.sortBy { it.start }
            val absoluteLatestStage = allStages.last()
            
            // 2. Noon-to-Noon logic: shift end time back 12 hours to find the "Sleep Date"
            val sleepDate = absoluteLatestStage.end.atZone(localZone).minusHours(12).toLocalDate()
            val todayLocal = java.time.LocalDate.now(localZone)
            
            // 3. Only show data if the sleep date is "today" or "yesterday"
            if (sleepDate != todayLocal && sleepDate != todayLocal.minusDays(1)) {
                nightSummaryText = "Waiting for tonight's sleep..."
                nightEvents = emptyList()
                latestSleepStartEndTime = ""
                return null
            }
            
            // 4. Define the 24-hour Noon-to-Noon window
            val windowStart = sleepDate.atTime(12, 0).atZone(localZone).toInstant()
            val windowEnd = sleepDate.plusDays(1).atTime(12, 0).atZone(localZone).toInstant()
            
            // 5. Filter stages strictly within this window
            val stages = allStages.filter { it.end > windowStart && it.start < windowEnd }
            
            if (stages.isEmpty()) return null
            
            val first = stages.first()
            val last = stages.last()
            
            val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a").withZone(localZone)
            
            latestSleepStartEndTime = "${formatter.format(first.start)} — ${formatter.format(last.end)}"
            val deepest = stages.filter { it.stage == 5 }.maxByOrNull { java.time.Duration.between(it.start, it.end).toMillis() }
            val longestAwake = stages.filter { it.stage == 1 || it.stage == 3 }.maxByOrNull { java.time.Duration.between(it.start, it.end).toMillis() }
            var awakeDuration = 0L
            if (longestAwake != null) {
                awakeDuration = java.time.Duration.between(longestAwake.start, longestAwake.end).toMinutes()
            }
            val longestRem = stages.filter { it.stage == 6 }.maxByOrNull { java.time.Duration.between(it.start, it.end).toMillis() }
            
            data class TempEvent(val instant: java.time.Instant, val text: String, val type: NightEventType)
            val tempEvents = mutableListOf<TempEvent>()
            tempEvents.add(TempEvent(first.start, "Fell asleep", NightEventType.ASLEEP))
            if (deepest != null && java.time.Duration.between(deepest.start, deepest.end).toMinutes() > 10) {
                tempEvents.add(TempEvent(deepest.start, "Deep, restorative sleep", NightEventType.DEEP))
            }
            if (longestAwake != null && awakeDuration > 3) {
                val text = if (awakeDuration > 15) "Long waking period" else "Short waking — back asleep soon"
                tempEvents.add(TempEvent(longestAwake.start, text, NightEventType.AWAKE))
            }
            if (longestRem != null && java.time.Duration.between(longestRem.start, longestRem.end).toMinutes() > 10) {
                tempEvents.add(TempEvent(longestRem.start, "Vivid dreaming (REM)", NightEventType.REM))
            }
            tempEvents.add(TempEvent(last.end, "Awake, rested", NightEventType.ASLEEP))
            
            nightEvents = tempEvents.sortedBy { it.instant }.distinctBy { it.instant.toEpochMilli() }.take(5).map { NightEvent(formatter.format(it.instant), it.text, it.type) }
                
            val startFormatted = formatter.format(first.start)
            val endFormatted = formatter.format(last.end)
            
            val deepMins = deepest?.let { java.time.Duration.between(it.start, it.end).toMinutes() } ?: 0
            val remMins = longestRem?.let { java.time.Duration.between(it.start, it.end).toMinutes() } ?: 0
            
            return "Asleep: $startFormatted, Up: $endFormatted. Awake duration: $awakeDuration mins. Deep sleep: $deepMins mins. REM: $remMins mins."
            
        } catch (e: Exception) {
            e.printStackTrace()
            nightSummaryText = "Unable to process detailed sleep stages."
            return null
        }
    }
}

class SleepViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
