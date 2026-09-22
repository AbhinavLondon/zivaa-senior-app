package com.zivaa.app.presentation.heartrate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabaseDailyVitalRecord
import com.zivaa.app.data.remote.SupabaseZivaaScoreRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class HeartTimeRange(val label: String) {
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days"),
    THREE_MONTHS("3 Months")
}

enum class HeartChartType(val label: String) {
    BAR("Bar"),
    LINE("Line")
}

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

data class IntradayHrBucket(
    val bucketIndex: Int,
    val timeLabel: String,
    val minBpm: Float,
    val maxBpm: Float,
    val avgBpm: Float,
    val count: Int,
    val hasData: Boolean
)

class HeartRateViewModel : ViewModel() {
    
    // Heart Health Score & Factor States
    var heartScore by mutableStateOf<Int?>(null)
    var heartBreakdown by mutableStateOf<Map<String, Any?>?>(null)
    var scoreDateLabel by mutableStateOf("")
        private set

    // Intraday 30-min range states (midnight to midnight)
    var intradayBuckets by mutableStateOf<List<IntradayHrBucket>>(emptyList())
        private set
    var intradayDateLabel by mutableStateOf("")
        private set
    var intradayMinHr by mutableStateOf(0)
        private set
    var intradayMaxHr by mutableStateOf(0)
        private set
    var intradayAvgHr by mutableStateOf(0)
        private set

    var selectedTimeRange by mutableStateOf(HeartTimeRange.SEVEN_DAYS)
        private set
    var selectedChartType by mutableStateOf(HeartChartType.BAR)
        private set

    // Sliced Series for Historical Charts
    var chartDates by mutableStateOf<List<String>>(emptyList())
    var chartDetailedDates by mutableStateOf<List<String>>(emptyList())
    var chartHeartScores by mutableStateOf<List<Float>>(emptyList())
    var chartRestingHr by mutableStateOf<List<Float>>(emptyList())
    var chartHrv by mutableStateOf<List<Float>>(emptyList())
    var chartMinHr by mutableStateOf<List<Float>>(emptyList())
    var chartMaxHr by mutableStateOf<List<Float>>(emptyList())
    var chartAvgHr by mutableStateOf<List<Float>>(emptyList())
    var chartHrSpans by mutableStateOf<List<Float>>(emptyList())
    var chartZoneResting by mutableStateOf<List<Float>>(emptyList())
    var chartZoneModerate by mutableStateOf<List<Float>>(emptyList())
    var chartZonePeak by mutableStateOf<List<Float>>(emptyList())

    // Rollup Summary Averages
    var averageHeartScore by mutableStateOf(0f)
    var averageRestingHr by mutableStateOf(0f)
    var averageHrv by mutableStateOf(0f)
    var averageDailyAvgHr by mutableStateOf(0f)
    var averageHrSpan by mutableStateOf(0f)
    var averageZoneResting by mutableStateOf(0f)
    var averageZoneModerate by mutableStateOf(0f)
    var averageZonePeak by mutableStateOf(0f)
    var hasHrvData by mutableStateOf(false)

    var startDateLabel by mutableStateOf("")
    var midDateLabel by mutableStateOf("")

    private var allVitals = emptyList<SupabaseDailyVitalRecord>()
    private var allScores = emptyList<SupabaseZivaaScoreRecord>()

    // Intraday Beat-by-Beat Chart States
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

    fun setTimeRange(range: HeartTimeRange) {
        selectedTimeRange = range
        applyTimeRange(range)
    }

    fun setChartType(type: HeartChartType) {
        selectedChartType = type
    }

    fun fetchAllData() {
        fetchTodayHeartRate()
        fetchWeeklyHeartRate()
        fetchHeartHealthHistory()
    }

    fun fetchHeartHealthHistory() {
        viewModelScope.launch {
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"

                // Fetch up to 100 days of scores & vitals
                val scoreResponse = RetrofitClient.apiService.getZivaaScores(
                    patientIdQuery = "eq.$patientId",
                    limit = 100
                )
                val vitalsResponse = RetrofitClient.apiService.getDailyVitals(
                    patientIdQuery = "eq.$patientId",
                    limit = 100
                )

                if (vitalsResponse.isSuccessful && !vitalsResponse.body().isNullOrEmpty()) {
                    allVitals = vitalsResponse.body()!!
                }

                if (scoreResponse.isSuccessful && !scoreResponse.body().isNullOrEmpty()) {
                    allScores = scoreResponse.body()!!
                }

                val todayDateStr = LocalDate.now().toString()
                val yesterdayDate = LocalDate.now().minusDays(1)
                val yesterdayDateStr = yesterdayDate.toString()

                // Anchor score card consistently to the most recently completed 24-hour cycle (Yesterday 00:00 to 24:00)
                val targetScore = allScores.firstOrNull { it.heartScore != null && it.date.take(10) < todayDateStr }
                    ?: allScores.firstOrNull { it.heartScore != null }
                
                val targetScoreDateStr = targetScore?.date?.take(10) ?: yesterdayDateStr
                val targetVitals = allVitals.firstOrNull { it.date.take(10) == targetScoreDateStr }
                    ?: allVitals.firstOrNull { it.date.take(10) < todayDateStr }

                try {
                    val scoreLocalDate = LocalDate.parse(targetScoreDateStr)
                    val today = LocalDate.now()
                    scoreDateLabel = when {
                        scoreLocalDate == today.minusDays(1) -> "Yesterday, ${scoreLocalDate.format(DateTimeFormatter.ofPattern("d MMM"))}"
                        scoreLocalDate == today -> "Today, ${scoreLocalDate.format(DateTimeFormatter.ofPattern("d MMM"))}"
                        else -> scoreLocalDate.format(DateTimeFormatter.ofPattern("d MMM"))
                    }
                } catch (e: Exception) {
                    scoreDateLabel = "Yesterday"
                }

                if (targetScore != null && targetScore.heartScore != null) {
                    heartScore = targetScore.heartScore
                    val merged: MutableMap<String, Any?> = targetScore.heartBreakdown?.toMutableMap() ?: mutableMapOf()
                    
                    if (targetVitals != null && targetVitals.date.take(10) == targetScoreDateStr) {
                        targetVitals.restingHeartRateCalculated?.let { merged["resting_heart_rate"] = it }
                        targetVitals.hrvRmssdAvg?.let { merged["hrv_rmssd"] = it }
                        targetVitals.minHeartRate?.let { merged["min_heart_rate"] = it }
                        targetVitals.maxHeartRate?.let { merged["max_heart_rate"] = it }
                        targetVitals.avgHeartRate?.let { merged["avg_heart_rate"] = it }
                    }
                    heartBreakdown = merged
                } else if (targetVitals != null) {
                    val rhr = targetVitals.restingHeartRateCalculated
                    val min = targetVitals.minHeartRate
                    val max = targetVitals.maxHeartRate
                    val span = if (min != null && max != null && max >= min) max - min else null
                    val avg = targetVitals.avgHeartRate
                    val hrv = targetVitals.hrvRmssdAvg

                    val breakdownMap = mutableMapOf<String, Any?>()
                    rhr?.let { breakdownMap["resting_heart_rate"] = it }
                    min?.let { breakdownMap["min_heart_rate"] = it }
                    max?.let { breakdownMap["max_heart_rate"] = it }
                    span?.let { breakdownMap["hr_span"] = it }
                    avg?.let { breakdownMap["avg_heart_rate"] = it }
                    hrv?.let { breakdownMap["hrv_rmssd"] = it }

                    if (breakdownMap.isNotEmpty()) {
                        heartScore = rhr?.let { (80 + ((65 - it) * 0.5)).toInt().coerceIn(50, 95) }
                        heartBreakdown = breakdownMap
                    } else {
                        heartScore = null
                        heartBreakdown = null
                    }
                } else {
                    heartScore = null
                    heartBreakdown = null
                }

                applyTimeRange(selectedTimeRange)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun computeZonesForDay(
        vital: SupabaseDailyVitalRecord,
        isToday: Boolean,
        realTimeZones: List<ZoneDistribution>?
    ): Triple<Float, Float, Float> {
        if (isToday && !realTimeZones.isNullOrEmpty()) {
            val r = (realTimeZones.getOrNull(0)?.fraction ?: 0.35f) * 100f
            val m = (realTimeZones.getOrNull(1)?.fraction ?: 0.55f) * 100f
            val p = (realTimeZones.getOrNull(2)?.fraction ?: 0.10f) * 100f
            return Triple(r, m, p)
        }

        val sleepH = vital.sleepHours?.toFloat() ?: 7.2f
        val restingHr = vital.restingHeartRateCalculated?.toFloat() ?: 64f
        val rPct = ((sleepH / 24f) * 100f + if (restingHr < 65f) 5f else 0f).coerceIn(25f, 45f)

        val exMins = vital.exerciseMinutes?.toFloat() ?: 25f
        val maxHr = vital.maxHeartRate?.toFloat() ?: 108f
        val pPct = ((exMins / 1440f) * 100f * 2.5f + if (maxHr > 115f) 5f else 2f).coerceIn(4f, 18f)

        val mPct = (100f - rPct - pPct).coerceIn(40f, 70f)
        return Triple(rPct, mPct, pPct)
    }

    private fun applyTimeRange(range: HeartTimeRange) {
        if (allVitals.isEmpty()) return

        val daysCount = when (range) {
            HeartTimeRange.SEVEN_DAYS -> 7
            HeartTimeRange.THIRTY_DAYS -> 30
            HeartTimeRange.THREE_MONTHS -> 84
        }

        val targetVitalsRaw = allVitals.take(daysCount).reversed()
        val targetScoresRaw = allScores.take(daysCount).reversed()
        val scoreMap = targetScoresRaw.associateBy { it.date.take(10) }

        if (range == HeartTimeRange.THREE_MONTHS) {
            val chunks = targetVitalsRaw.chunked(7)
            startDateLabel = chunks.firstOrNull()?.firstOrNull()?.let { formatShortDate(it.date) } ?: ""
            midDateLabel = "6 weeks ago"

            chartDates = chunks.mapIndexed { index, chunk ->
                if (index == chunks.size - 1) "Today"
                else if (index == 0) formatShortDate(chunk.first().date)
                else ""
            }

            chartDetailedDates = chunks.map { chunk -> formatChunkDateRange(chunk) }

            chartHeartScores = chunks.map { chunk ->
                val vals = chunk.mapNotNull { scoreMap[it.date.take(10)]?.heartScore?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else -1f
            }

            chartRestingHr = chunks.map { chunk ->
                val vals = chunk.mapNotNull { it.restingHeartRateCalculated?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else -1f
            }

            chartHrv = chunks.map { chunk ->
                val vals = chunk.mapNotNull {
                    val h = it.hrvRmssdAvg?.toFloat() ?: (scoreMap[it.date.take(10)]?.heartBreakdown?.get("hrv_rmssd") as? Number)?.toFloat()
                    if (h != null && h > 0f) h else null
                }
                if (vals.isNotEmpty()) vals.average().toFloat() else -1f
            }

            chartMinHr = chunks.map { chunk ->
                val vals = chunk.mapNotNull { it.minHeartRate?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else -1f
            }

            chartMaxHr = chunks.map { chunk ->
                val vals = chunk.mapNotNull { it.maxHeartRate?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else -1f
            }

            chartAvgHr = chunks.map { chunk ->
                val vals = chunk.mapNotNull { it.avgHeartRate?.toFloat() }.filter { it > 0f }
                if (vals.isNotEmpty()) vals.average().toFloat() else -1f
            }

            chartHrSpans = chunks.map { chunk ->
                val mins = chunk.mapNotNull { it.minHeartRate?.toFloat() }.filter { it > 0f }
                val maxs = chunk.mapNotNull { it.maxHeartRate?.toFloat() }.filter { it > 0f }
                if (mins.isNotEmpty() && maxs.isNotEmpty()) {
                    maxOf(0f, maxs.average().toFloat() - mins.average().toFloat())
                } else -1f
            }

            val chunkZoneTriples = chunks.map { chunk ->
                val zones = chunk.mapIndexed { idx, vital ->
                    val isChunkToday = (chunk == chunks.last()) && (idx == chunk.size - 1)
                    computeZonesForDay(vital, isChunkToday, _heartRateZones.value)
                }
                Triple(
                    zones.map { it.first }.average().toFloat(),
                    zones.map { it.second }.average().toFloat(),
                    zones.map { it.third }.average().toFloat()
                )
            }
            chartZoneResting = chunkZoneTriples.map { it.first }
            chartZoneModerate = chunkZoneTriples.map { it.second }
            chartZonePeak = chunkZoneTriples.map { it.third }

            hasHrvData = chunks.any { chunk ->
                chunk.any {
                    val h = it.hrvRmssdAvg?.toFloat() ?: (scoreMap[it.date.take(10)]?.heartBreakdown?.get("hrv_rmssd") as? Number)?.toFloat()
                    h != null && h > 0f
                }
            }
        } else {
            startDateLabel = targetVitalsRaw.firstOrNull()?.let { formatShortDate(it.date) } ?: ""
            midDateLabel = ""

            chartDates = targetVitalsRaw.map { getShortDayLabel(it.date, range) }
            chartDetailedDates = targetVitalsRaw.map { formatDetailedDate(it.date) }

            chartHeartScores = targetVitalsRaw.map { vital ->
                scoreMap[vital.date.take(10)]?.heartScore?.toFloat() ?: -1f
            }

            chartRestingHr = targetVitalsRaw.map { it.restingHeartRateCalculated?.toFloat() ?: -1f }
            chartHrv = targetVitalsRaw.map { vital ->
                val h = vital.hrvRmssdAvg?.toFloat() ?: (scoreMap[vital.date.take(10)]?.heartBreakdown?.get("hrv_rmssd") as? Number)?.toFloat()
                if (h != null && h > 0f) h else -1f
            }

            chartMinHr = targetVitalsRaw.map { it.minHeartRate?.toFloat() ?: -1f }
            chartMaxHr = targetVitalsRaw.map { it.maxHeartRate?.toFloat() ?: -1f }
            chartAvgHr = targetVitalsRaw.map { it.avgHeartRate?.toFloat() ?: -1f }
            chartHrSpans = targetVitalsRaw.map { vital ->
                val min = vital.minHeartRate?.toFloat()
                val max = vital.maxHeartRate?.toFloat()
                if (min != null && max != null && max >= min) max - min else -1f
            }

            val dailyZoneTriples = targetVitalsRaw.mapIndexed { index, vital ->
                val isToday = index == targetVitalsRaw.size - 1
                computeZonesForDay(vital, isToday, _heartRateZones.value)
            }
            chartZoneResting = dailyZoneTriples.map { it.first }
            chartZoneModerate = dailyZoneTriples.map { it.second }
            chartZonePeak = dailyZoneTriples.map { it.third }

            hasHrvData = targetVitalsRaw.any { vital ->
                val h = vital.hrvRmssdAvg?.toFloat() ?: (scoreMap[vital.date.take(10)]?.heartBreakdown?.get("hrv_rmssd") as? Number)?.toFloat()
                h != null && h > 0f
            }
        }

        averageHeartScore = chartHeartScores.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageRestingHr = chartRestingHr.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageHrv = chartHrv.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageDailyAvgHr = chartAvgHr.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageHrSpan = chartHrSpans.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageZoneResting = chartZoneResting.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageZoneModerate = chartZoneModerate.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        averageZonePeak = chartZonePeak.filter { it > 0f }.let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
    }

    fun fetchTodayHeartRate() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                
                var userAge = 60
                try {
                    val patientResponse = RetrofitClient.apiService.getPatient("eq.$patientId")
                    if (patientResponse.isSuccessful) {
                        val patient = patientResponse.body()?.firstOrNull()
                        val dobStr = patient?.dateOfBirth
                        if (!dobStr.isNullOrEmpty()) {
                            try {
                                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                val dob = LocalDate.parse(dobStr.take(10), formatter)
                                userAge = java.time.Period.between(dob, LocalDate.now()).years
                            } catch (e: Exception) {
                                try {
                                    val dob = LocalDate.parse(dobStr.take(10))
                                    userAge = java.time.Period.between(dob, LocalDate.now()).years
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

                var targetDateLocal = LocalDate.now()
                val today = targetDateLocal
                val targetStartLocal = targetDateLocal.atStartOfDay(ZoneId.systemDefault())
                val targetStartUtc = targetStartLocal.withZoneSameInstant(ZoneId.of("UTC"))
                val formattedQueryDate = "gte." + DateTimeFormatter.ISO_INSTANT.format(targetStartUtc)

                val response = RetrofitClient.apiService.getTodayVitals(
                    patientIdQuery = "eq.$patientId",
                    metricTypeQuery = "eq.HeartRateRecord",
                    recordedAtQuery = formattedQueryDate
                )

                var records: List<com.zivaa.app.data.remote.SupabaseVitalRecord>? = if (response.isSuccessful) response.body() else null

                // If today has no records yet (e.g. past midnight or wearable hasn't synced yet today),
                // query recent days to fall back to the most recent day with continuous 24h data
                if (records.isNullOrEmpty()) {
                    val fallbackQueryDate = "gte." + DateTimeFormatter.ISO_INSTANT.format(targetStartUtc.minus(3, java.time.temporal.ChronoUnit.DAYS))
                    val fallbackResponse = RetrofitClient.apiService.getTodayVitals(
                        patientIdQuery = "eq.$patientId",
                        metricTypeQuery = "eq.HeartRateRecord",
                        recordedAtQuery = fallbackQueryDate,
                        order = "recorded_at.desc",
                        limit = 1000
                    )
                    if (fallbackResponse.isSuccessful && !fallbackResponse.body().isNullOrEmpty()) {
                        val allFallback = fallbackResponse.body()!!
                        val latestRec = allFallback.firstOrNull()
                        if (latestRec != null) {
                            val latestDate = java.time.Instant.parse(latestRec.recordedAt).atZone(ZoneId.systemDefault()).toLocalDate()
                            targetDateLocal = latestDate
                            records = allFallback.filter {
                                try {
                                    val d = java.time.Instant.parse(it.recordedAt).atZone(ZoneId.systemDefault()).toLocalDate()
                                    d == targetDateLocal
                                } catch (e: Exception) { false }
                            }
                        }
                    }
                }

                if (targetDateLocal == today) {
                    intradayDateLabel = "Today, ${targetDateLocal.format(DateTimeFormatter.ofPattern("d MMM"))}"
                } else if (targetDateLocal == today.minusDays(1)) {
                    intradayDateLabel = "Yesterday, ${targetDateLocal.format(DateTimeFormatter.ofPattern("d MMM"))}"
                } else {
                    intradayDateLabel = targetDateLocal.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
                }

                val bucketedData = Array(48) { mutableListOf<Double>() }
                var restingCount = 0
                var easyCount = 0
                var activeCount = 0
                var totalCount = 0

                records?.forEach { record ->
                    val samples = record.values["samples"] as? List<Map<String, Any>>
                    samples?.forEach { sample ->
                        val timeStr = sample["time"] as? String
                        val bpm = (sample["bpm"] as? Number)?.toDouble()
                        
                        if (timeStr != null && bpm != null && bpm in 30.0..240.0) {
                            try {
                                val instant = java.time.Instant.parse(timeStr)
                                val localDateTime = instant.atZone(ZoneId.systemDefault())
                                
                                if (localDateTime.toLocalDate() == targetDateLocal) {
                                    val localTime = localDateTime.toLocalTime()
                                    
                                    totalCount++
                                    if (bpm < restingThreshold) {
                                        restingCount++
                                    } else if (bpm < activeThreshold) {
                                        easyCount++
                                    } else {
                                        activeCount++
                                    }
                                    
                                    val fractionalHour = localTime.hour + (localTime.minute / 60.0) + (localTime.second / 3600.0)
                                    val bucketIndex = (fractionalHour * 2).toInt().coerceIn(0, 47)
                                    bucketedData[bucketIndex].add(bpm)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                val buckets = (0..47).map { idx ->
                    val bpms = bucketedData[idx]
                    val startMin = idx * 30
                    val endMin = (idx + 1) * 30
                    val label = formatIntervalTimeLabel(startMin, endMin)
                    if (bpms.isNotEmpty()) {
                        IntradayHrBucket(
                            bucketIndex = idx,
                            timeLabel = label,
                            minBpm = bpms.minOrNull()!!.toFloat(),
                            maxBpm = bpms.maxOrNull()!!.toFloat(),
                            avgBpm = bpms.average().toFloat(),
                            count = bpms.size,
                            hasData = true
                        )
                    } else {
                        IntradayHrBucket(
                            bucketIndex = idx,
                            timeLabel = label,
                            minBpm = 0f,
                            maxBpm = 0f,
                            avgBpm = 0f,
                            count = 0,
                            hasData = false
                        )
                    }
                }
                intradayBuckets = buckets

                val populated = buckets.filter { it.hasData }
                if (populated.isNotEmpty()) {
                    intradayMinHr = populated.minOf { it.minBpm }.toInt()
                    intradayMaxHr = populated.maxOf { it.maxBpm }.toInt()
                    val allBpmsSum = bucketedData.sumOf { it.sum() }
                    val allBpmsCount = bucketedData.sumOf { it.size }
                    intradayAvgHr = if (allBpmsCount > 0) (allBpmsSum / allBpmsCount).toInt() else 0

                    val series1 = (0..47).map { bucketIndex ->
                        val bpms = bucketedData[bucketIndex]
                        val minBpm = bpms.minOrNull()?.toFloat() ?: 0f
                        FloatEntry(bucketIndex.toFloat(), minBpm)
                    }
                    val series2 = (0..47).map { bucketIndex ->
                        val bpms = bucketedData[bucketIndex]
                        val minBpm = bpms.minOrNull()?.toFloat() ?: 0f
                        val maxBpm = bpms.maxOrNull()?.toFloat() ?: 0f
                        FloatEntry(bucketIndex.toFloat(), maxBpm - minBpm)
                    }
                    _chartEntryModelProducer.value = ChartEntryModelProducer(listOf(series1, series2))
                    _hasData.value = true
                } else {
                    intradayMinHr = 0
                    intradayMaxHr = 0
                    intradayAvgHr = 0
                    _hasData.value = false
                }

                if (totalCount > 0) {
                    _heartRateZones.value = listOf(
                        ZoneDistribution("Below $restingThreshold", "${(restingCount * 100 / totalCount)}%", restingCount.toFloat() / totalCount),
                        ZoneDistribution("$restingThreshold - $activeThreshold", "${(easyCount * 100 / totalCount)}%", easyCount.toFloat() / totalCount),
                        ZoneDistribution("Above $activeThreshold", "${(activeCount * 100 / totalCount)}%", activeCount.toFloat() / totalCount)
                    )
                } else {
                    _heartRateZones.value = null
                }
                applyTimeRange(selectedTimeRange)
            } catch (e: Exception) {
                e.printStackTrace()
                _hasData.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatIntervalTimeLabel(startMins: Int, endMins: Int): String {
        fun fmt(totalMinutes: Int): String {
            val h24 = (totalMinutes / 60) % 24
            val m = totalMinutes % 60
            val amPm = if (h24 < 12) "AM" else "PM"
            val h12 = if (h24 == 0) 12 else if (h24 > 12) h24 - 12 else h24
            return if (m == 0) "$h12 $amPm" else String.format(Locale.US, "%d:%02d %s", h12, m, amPm)
        }
        return "${fmt(startMins)} – ${fmt(endMins)}"
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
                    
                    val validAvgs = records.mapNotNull { it.restingHeartRateCalculated }
                    val calculatedMonthAvg = if (validAvgs.isNotEmpty()) {
                        validAvgs.average().toFloat()
                    } else {
                        72f
                    }
                    _monthAvg.value = calculatedMonthAvg
                    
                    val today = LocalDate.now(ZoneId.systemDefault())
                    val dayDataList = mutableListOf<DayData>()
                    val dayFormatter = DateTimeFormatter.ofPattern("EEEEE")
                    
                    for (i in 7 downTo 1) {
                        val targetDate = today.minusDays(i.toLong())
                        val targetDateStr = targetDate.toString()
                        
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
                                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                val dob = LocalDate.parse(dobStr.take(10), formatter)
                                val age = java.time.Period.between(dob, LocalDate.now()).years
                                ageText = "$age years old"
                            } catch (e: Exception) {
                                try {
                                    val dob = LocalDate.parse(dobStr.take(10))
                                    val age = java.time.Period.between(dob, LocalDate.now()).years
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

    private fun getShortDayLabel(dateString: String, range: HeartTimeRange): String {
        return try {
            val date = LocalDate.parse(dateString.take(10))
            if (range == HeartTimeRange.SEVEN_DAYS) {
                date.dayOfWeek.name.take(1)
            } else {
                "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.US)}"
            }
        } catch (e: Exception) {
            dateString.takeLast(5)
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
