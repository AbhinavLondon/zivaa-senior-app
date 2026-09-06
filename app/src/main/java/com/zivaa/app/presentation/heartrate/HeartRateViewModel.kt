package com.zivaa.app.presentation.heartrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.zivaa.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class DayData(
    val dayName: String,
    val minHr: Float,
    val maxHr: Float,
    val restingHr: Float,
    val sleepHours: Float,
    val isToday: Boolean = false
)

data class ZoneDistribution(
    val rangeText: String,
    val percentageText: String,
    val fraction: Float
)

class HeartRateViewModel : ViewModel() {
    
    private val _chartEntryModelProducer = MutableStateFlow<ChartEntryModelProducer?>(null)
    val chartEntryModelProducer: StateFlow<ChartEntryModelProducer?> = _chartEntryModelProducer.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasData = MutableStateFlow(true)
    val hasData: StateFlow<Boolean> = _hasData.asStateFlow()

    private val _weeklyData = MutableStateFlow<List<DayData>>(emptyList())
    val weeklyData: StateFlow<List<DayData>> = _weeklyData.asStateFlow()

    private val _monthAvg = MutableStateFlow<Float>(72f)
    val monthAvg: StateFlow<Float> = _monthAvg.asStateFlow()

    private val _weeklyInsight = MutableStateFlow<String?>(null)
    val weeklyInsight: StateFlow<String?> = _weeklyInsight.asStateFlow()

    private val _isLoadingInsight = MutableStateFlow(false)
    val isLoadingInsight: StateFlow<Boolean> = _isLoadingInsight.asStateFlow()

    private val _heartRateZones = MutableStateFlow<List<ZoneDistribution>?>(null)
    val heartRateZones: StateFlow<List<ZoneDistribution>?> = _heartRateZones.asStateFlow()

    fun fetchTodayHeartRate() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                
                // Fetch patient age for Max HR calculation (220 - age)
                var userAge = 60
                try {
                    val patientResponse = RetrofitClient.apiService.getPatient("eq.$patientId")
                    if (patientResponse.isSuccessful) {
                        val patient = patientResponse.body()?.firstOrNull()
                        val dobStr = patient?.dateOfBirth
                        if (!dobStr.isNullOrEmpty()) {
                            try {
                                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                val dob = java.time.LocalDate.parse(dobStr.take(10), formatter)
                                userAge = java.time.Period.between(dob, java.time.LocalDate.now()).years
                            } catch (e: Exception) {
                                try {
                                    val dob = java.time.LocalDate.parse(dobStr.take(10))
                                    userAge = java.time.Period.between(dob, java.time.LocalDate.now()).years
                                } catch (e2: Exception) {}
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                val maxHr = 220 - userAge
                val restingThreshold = (0.5 * maxHr).toInt()
                val activeThreshold = (0.7 * maxHr).toInt()

                // Calculate start of today in UTC
                val targetDateLocal = LocalDate.now()
                val targetStartLocal = targetDateLocal.atStartOfDay(ZoneId.systemDefault())
                val targetStartUtc = targetStartLocal.withZoneSameInstant(ZoneId.of("UTC"))
                val formattedQueryDate = "gte." + DateTimeFormatter.ISO_INSTANT.format(targetStartUtc)

                val response = RetrofitClient.apiService.getTodayVitals(
                    patientIdQuery = "eq.$patientId",
                    metricTypeQuery = "eq.HeartRateRecord",
                    recordedAtQuery = formattedQueryDate
                )

                if (response.isSuccessful) {
                    val records = response.body()
                    if (records.isNullOrEmpty()) {
                        _hasData.value = false
                    } else {
                        // Process records into buckets
                        val bucketedData = mutableMapOf<Int, MutableList<Double>>() // Bucket index (0-47) to list of bpms
                        var restingCount = 0
                        var easyCount = 0
                        var activeCount = 0
                        var totalCount = 0

                        records.forEach { record ->
                            val samples = record.values["samples"] as? List<Map<String, Any>>
                            samples?.forEach { sample ->
                                val timeStr = sample["time"] as? String
                                val bpm = (sample["bpm"] as? Number)?.toDouble()
                                
                                if (timeStr != null && bpm != null) {
                                    try {
                                        val instant = java.time.Instant.parse(timeStr)
                                        val localDateTime = instant.atZone(ZoneId.systemDefault())
                                        
                                        // Only include records from the target date
                                        if (localDateTime.toLocalDate() != targetDateLocal) return@forEach

                                        val localTime = localDateTime.toLocalTime()
                                        
                                        // Count for zones
                                        totalCount++
                                        if (bpm < restingThreshold) {
                                            restingCount++
                                        } else if (bpm < activeThreshold) {
                                            easyCount++
                                        } else {
                                            activeCount++
                                        }
                                        
                                        // Calculate fractional hour (0.0 to 23.99)
                                        val fractionalHour = localTime.hour + (localTime.minute / 60.0f)
                                        
                                        // Bucket into 30-minute intervals (48 buckets a day)
                                        // Index = 0 is 00:00 - 00:30, Index = 1 is 00:30 - 01:00...
                                        val bucketIndex = (fractionalHour * 2).toInt()
                                        
                                        if (bucketedData[bucketIndex] == null) {
                                            bucketedData[bucketIndex] = mutableListOf()
                                        }
                                        bucketedData[bucketIndex]?.add(bpm)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }

                        if (bucketedData.isEmpty()) {
                            _hasData.value = false
                        } else {
                            val series1 = (0..47).map { bucketIndex ->
                                val bpms = bucketedData[bucketIndex]
                                val minBpm = bpms?.minOrNull()?.toFloat() ?: 0f
                                FloatEntry(bucketIndex.toFloat(), minBpm)
                            }
                            
                            val series2 = (0..47).map { bucketIndex ->
                                val bpms = bucketedData[bucketIndex]
                                val minBpm = bpms?.minOrNull()?.toFloat() ?: 0f
                                val maxBpm = bpms?.maxOrNull()?.toFloat() ?: 0f
                                FloatEntry(bucketIndex.toFloat(), maxBpm - minBpm)
                            }
                            
                            _chartEntryModelProducer.value = ChartEntryModelProducer(listOf(series1, series2))
                            
                            if (totalCount > 0) {
                                _heartRateZones.value = listOf(
                                    ZoneDistribution("Below $restingThreshold", "${(restingCount * 100 / totalCount)}%", restingCount.toFloat() / totalCount),
                                    ZoneDistribution("$restingThreshold - $activeThreshold", "${(easyCount * 100 / totalCount)}%", easyCount.toFloat() / totalCount),
                                    ZoneDistribution("Above $activeThreshold", "${(activeCount * 100 / totalCount)}%", activeCount.toFloat() / totalCount)
                                )
                            } else {
                                _heartRateZones.value = null
                            }
                            
                            _hasData.value = true
                        }
                    }
                } else {
                    _hasData.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _hasData.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchWeeklyHeartRate() {
        viewModelScope.launch {
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                
                val response = RetrofitClient.apiService.getDailyVitals(
                    patientIdQuery = "eq.$patientId",
                    order = "date.desc",
                    limit = 30
                )

                if (response.isSuccessful) {
                    val records = response.body() ?: emptyList()
                    
                    // 1. Calculate 30-day average
                    val validAvgs = records.mapNotNull { it.restingHeartRateCalculated }
                    val calculatedMonthAvg = if (validAvgs.isNotEmpty()) {
                        validAvgs.average().toFloat()
                    } else {
                        72f // fallback
                    }
                    _monthAvg.value = calculatedMonthAvg
                    
                    // 2. Prepare the last 7 calendar days
                    val today = LocalDate.now(ZoneId.systemDefault())
                    val dayDataList = mutableListOf<DayData>()
                    val dayFormatter = DateTimeFormatter.ofPattern("EEEEE") // "M", "T", "W"
                    
                    // Generate 7 days in chronological order (7 days ago -> yesterday)
                    for (i in 7 downTo 1) {
                        val targetDate = today.minusDays(i.toLong())
                        val targetDateStr = targetDate.toString() // yyyy-MM-dd
                        
                        // Find matching record
                        val record = records.find { it.date.startsWith(targetDateStr) }
                        
                        val minHr = record?.minHeartRate?.toFloat() ?: 0f
                        val maxHr = record?.maxHeartRate?.toFloat() ?: 0f
                        val avgHr = record?.restingHeartRateCalculated?.toFloat() ?: 0f
                        val sleepHr = record?.sleepHours?.toFloat() ?: 0f
                        
                        dayDataList.add(
                            DayData(
                                dayName = targetDate.format(dayFormatter),
                                minHr = minHr,
                                maxHr = maxHr,
                                restingHr = avgHr,
                                sleepHours = sleepHr,
                                isToday = false
                            )
                        )
                    }
                    _weeklyData.value = dayDataList
                    fetchWeeklyInsight(patientId, calculatedMonthAvg, dayDataList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchWeeklyInsight(patientId: String, monthAvg: Float, dayDataList: List<DayData>) {
        viewModelScope.launch {
            _isLoadingInsight.value = true
            try {
                // Fetch patient for age and gender
                val patientResponse = RetrofitClient.apiService.getPatient("eq.$patientId")
                var ageText = "Unknown Age"
                var genderText = "Unknown Gender"
                if (patientResponse.isSuccessful) {
                    val patient = patientResponse.body()?.firstOrNull()
                    if (patient != null) {
                        genderText = patient.gender ?: "Unknown Gender"
                        val dobStr = patient.dateOfBirth
                        if (!dobStr.isNullOrEmpty()) {
                            try {
                                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                val dob = java.time.LocalDate.parse(dobStr.take(10), formatter)
                                val age = java.time.Period.between(dob, java.time.LocalDate.now()).years
                                ageText = "$age years old"
                            } catch (e: Exception) {
                                try {
                                    val dob = java.time.LocalDate.parse(dobStr.take(10))
                                    val age = java.time.Period.between(dob, java.time.LocalDate.now()).years
                                    ageText = "$age years old"
                                } catch (e2: Exception) {
                                    e2.printStackTrace()
                                }
                            }
                        }
                    }
                }

                val contextData = StringBuilder()
                contextData.append("Patient Info: $ageText, Gender: $genderText\n")
                contextData.append("Monthly Average: $monthAvg bpm\n")
                dayDataList.forEach { day ->
                    contextData.append("${day.dayName}: min_avg_heart_rate ${day.minHr.toInt()}, max_avg_heart_rate ${day.maxHr.toInt()}, resting_heart_rate ${day.restingHr.toInt()}, sleep_hours ${String.format("%.1f", day.sleepHours)}h\n")
                }

                val request = com.zivaa.app.data.remote.InsightRequest(
                    patient_id = patientId,
                    type = "heart_rate_weekly",
                    timezone = ZoneId.systemDefault().id,
                    context = contextData.toString()
                )
                val response = RetrofitClient.apiService.generateInsight(request)
                if (response.isSuccessful) {
                    _weeklyInsight.value = response.body()?.insight
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingInsight.value = false
            }
        }
    }
}

class HeartRateViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeartRateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeartRateViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
