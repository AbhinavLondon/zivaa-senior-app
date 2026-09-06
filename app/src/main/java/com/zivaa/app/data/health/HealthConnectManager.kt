package com.zivaa.app.data.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.Duration
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import com.zivaa.app.data.remote.SupabaseDailyVitalRecord

data class AggregatedHealthData(
    val steps: Long = 0L,
    val distanceMeters: Double = 0.0,
    val activeCalories: Double = 0.0,
    val totalCalories: Double = 0.0,
    val heartRateAvg: Long = 0L,
    val sleepDurationMinutes: Long = 0L,
    val activeTimeMinutes: Long = 0L
)

class HealthConnectManager(private val context: Context) {

    val healthConnectClient by lazy {
        if (isSdkAvailable()) HealthConnectClient.getOrCreate(context) else null
    }

    fun isSdkAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    fun checkHealthConnectSupportAndRedirect(): HealthConnectSupport {
        val status = HealthConnectClient.getSdkStatus(context)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectSupport.AVAILABLE
            else -> {
                // SDK not available — try to open Play Store for install
                try {
                    val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                        setPackage("com.android.vending")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(playStoreIntent)
                } catch (e: Exception) {
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                }
                HealthConnectSupport.INSTALL_REQUIRED
            }
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ElevationGainedRecord::class),
        HealthPermission.getReadPermission(StepsCadenceRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SkinTemperatureRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(Vo2MaxRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    fun isBackgroundReadAvailable(): Boolean {
        return true
    }

    fun isHistoryReadAvailable(): Boolean {
        return true
    }

    suspend fun aggregateSteps(start: Instant, end: Instant): Long {
        val client = healthConnectClient ?: return 0L
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    suspend fun aggregateAllMetrics(start: Instant, end: Instant): AggregatedHealthData {
        val client = healthConnectClient ?: return AggregatedHealthData()
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        HeartRateRecord.BPM_AVG,
                        SleepSessionRecord.SLEEP_DURATION_TOTAL,
                        ExerciseSessionRecord.EXERCISE_DURATION_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            
            AggregatedHealthData(
                steps = response[StepsRecord.COUNT_TOTAL] ?: 0L,
                distanceMeters = response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0,
                activeCalories = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
                totalCalories = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
                heartRateAvg = response[HeartRateRecord.BPM_AVG] ?: 0L,
                sleepDurationMinutes = response[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.toMinutes() ?: 0L,
                activeTimeMinutes = response[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes() ?: 0L
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AggregatedHealthData()
        }
    }



    suspend fun fetchAllAvailableMetrics(days: Long = 90): List<Record> {
        val client = healthConnectClient ?: return emptyList()
        val endTime = Instant.now()
        val startTime = endTime.minus(days, ChronoUnit.DAYS)
        val timeFilter = TimeRangeFilter.between(startTime, endTime)

        val records = mutableListOf<Record>()

        suspend fun <T : Record> safeRead(recordClass: kotlin.reflect.KClass<T>) {
            try {
                var pageToken: String? = null
                do {
                    val request = ReadRecordsRequest(
                        recordType = recordClass,
                        timeRangeFilter = timeFilter,
                        pageToken = pageToken,
                        ascendingOrder = false
                    )
                    val result = client.readRecords(request)
                    records.addAll(result.records)
                    pageToken = result.pageToken
                } while (pageToken != null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Activity
        safeRead(StepsRecord::class)
        safeRead(ActiveCaloriesBurnedRecord::class)
        safeRead(TotalCaloriesBurnedRecord::class)
        safeRead(DistanceRecord::class)
        safeRead(ElevationGainedRecord::class)
        safeRead(FloorsClimbedRecord::class)
        safeRead(SpeedRecord::class)
        safeRead(ExerciseSessionRecord::class)

        // Vitals
        safeRead(WheelchairPushesRecord::class)
        safeRead(StepsCadenceRecord::class)
        safeRead(RestingHeartRateRecord::class)
        safeRead(HeartRateVariabilityRmssdRecord::class)
        safeRead(BloodPressureRecord::class)
        safeRead(BloodGlucoseRecord::class)
        safeRead(OxygenSaturationRecord::class)
        safeRead(BodyTemperatureRecord::class)
        safeRead(BasalBodyTemperatureRecord::class)
        safeRead(RespiratoryRateRecord::class)
        safeRead(Vo2MaxRecord::class)
        safeRead(SkinTemperatureRecord::class)

        // Vitals Requested
        safeRead(HeartRateRecord::class)

        // Body Measurements
        safeRead(WeightRecord::class)
        safeRead(HeightRecord::class)
        safeRead(BodyFatRecord::class)
        safeRead(BoneMassRecord::class)
        safeRead(LeanBodyMassRecord::class)
        safeRead(BodyWaterMassRecord::class)
        safeRead(BasalMetabolicRateRecord::class)

        // Nutrition & Hydration
        safeRead(NutritionRecord::class)
        safeRead(HydrationRecord::class)

        // Sleep
        safeRead(SleepSessionRecord::class)

        // Women's Health
        safeRead(MenstruationFlowRecord::class)
        safeRead(MenstruationPeriodRecord::class)
        safeRead(OvulationTestRecord::class)
        safeRead(CervicalMucusRecord::class)
        safeRead(SexualActivityRecord::class)

        return records
    }

    suspend fun getChangesToken(): String? {
        val client = healthConnectClient ?: return null
        
        // Only request tokens for permissions we actually have granted
        val granted = client.permissionController.getGrantedPermissions()
        val allTypes = setOf(
            BloodPressureRecord::class,
            HeartRateRecord::class,
            RestingHeartRateRecord::class,
            BodyTemperatureRecord::class,
            BloodGlucoseRecord::class,
            OxygenSaturationRecord::class,
            RespiratoryRateRecord::class,
            SpeedRecord::class,
            DistanceRecord::class,
            TotalCaloriesBurnedRecord::class,
            ElevationGainedRecord::class,
            StepsCadenceRecord::class,
            SleepSessionRecord::class,
            ExerciseSessionRecord::class,
            HeartRateVariabilityRmssdRecord::class,
            SkinTemperatureRecord::class,
            StepsRecord::class,
            Vo2MaxRecord::class
        )
        val requestedTypes = allTypes.filter { HealthPermission.getReadPermission(it) in granted }.toSet()
        if (requestedTypes.isEmpty()) return null

        val request = androidx.health.connect.client.request.ChangesTokenRequest(recordTypes = requestedTypes)
        return try {
            client.getChangesToken(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getChanges(token: String): androidx.health.connect.client.response.ChangesResponse? {
        val client = healthConnectClient ?: return null
        return try {
            client.getChanges(token)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun mapRecordToSupabase(record: androidx.health.connect.client.records.Record, patientId: String): List<com.zivaa.app.data.remote.SupabaseVitalRecord> {
        val metricType = record::class.simpleName ?: "UnknownRecord"
        var recordedAt = Instant.now().toString()
        val values = mutableMapOf<String, Any>()

        // Extract source from metadata
        val meta = record.metadata
        var source = meta.dataOrigin.packageName

        val deviceType = meta.device?.type
        if (deviceType == 1) { // TYPE_WATCH
            source += "_watch"
        } else if (deviceType == 2) { // TYPE_PHONE
            source += "_phone"
        }
        
        val results = mutableListOf<com.zivaa.app.data.remote.SupabaseVitalRecord>()

        when (record) {
            is androidx.health.connect.client.records.StepsRecord -> {
                recordedAt = record.endTime.toString()
                values["count"] = record.count
            }
            is androidx.health.connect.client.records.DistanceRecord -> {
                recordedAt = record.endTime.toString()
                values["distanceMeters"] = record.distance.inMeters
            }
            is androidx.health.connect.client.records.ActiveCaloriesBurnedRecord -> {
                recordedAt = record.endTime.toString()
                values["energyKcal"] = record.energy.inKilocalories
            }
            is androidx.health.connect.client.records.TotalCaloriesBurnedRecord -> {
                recordedAt = record.endTime.toString()
                values["energyKcal"] = record.energy.inKilocalories
            }
            is androidx.health.connect.client.records.HeartRateRecord -> {
                recordedAt = record.endTime.toString()
                values["zone_offset"] = record.endZoneOffset?.toString() ?: ""
                values["samples"] = record.samples.map { 
                    mapOf("time" to it.time.toString(), "bpm" to it.beatsPerMinute)
                }
            }
            is androidx.health.connect.client.records.SleepSessionRecord -> {
                recordedAt = record.endTime.toString()
                values["title"] = record.title ?: ""
                values["notes"] = record.notes ?: ""
                values["start_time"] = record.startTime.toString()
                values["end_time"] = record.endTime.toString()
                values["duration_minutes"] = java.time.Duration.between(record.startTime, record.endTime).toMinutes()
                values["stages"] = record.stages.map {
                    mapOf(
                        "start_time" to it.startTime.toString(),
                        "end_time" to it.endTime.toString(),
                        "stage" to it.stage
                    )
                }
            }
            is androidx.health.connect.client.records.BloodPressureRecord -> {
                recordedAt = record.time.toString()
                values["systolic"] = record.systolic.inMillimetersOfMercury
                values["diastolic"] = record.diastolic.inMillimetersOfMercury
            }
            is androidx.health.connect.client.records.RestingHeartRateRecord -> {
                recordedAt = record.time.toString()
                values["bpm"] = record.beatsPerMinute
            }
            is androidx.health.connect.client.records.BodyTemperatureRecord -> {
                recordedAt = record.time.toString()
                values["temperatureCelsius"] = record.temperature.inCelsius
            }
            is androidx.health.connect.client.records.BloodGlucoseRecord -> {
                recordedAt = record.time.toString()
                values["levelMmolPerL"] = record.level.inMillimolesPerLiter
            }
            is androidx.health.connect.client.records.OxygenSaturationRecord -> {
                recordedAt = record.time.toString()
                values["percentage"] = record.percentage.value
            }
            is androidx.health.connect.client.records.RespiratoryRateRecord -> {
                recordedAt = record.time.toString()
                values["rate"] = record.rate
            }
            is androidx.health.connect.client.records.SpeedRecord -> {
                recordedAt = record.endTime.toString()
                values["samples"] = record.samples.map {
                    mapOf("time" to it.time.toString(), "speedMetersPerSec" to it.speed.inMetersPerSecond)
                }
            }
            is androidx.health.connect.client.records.ElevationGainedRecord -> {
                recordedAt = record.endTime.toString()
                values["elevationMeters"] = record.elevation.inMeters
            }
            is androidx.health.connect.client.records.StepsCadenceRecord -> {
                recordedAt = record.endTime.toString()
                values["samples"] = record.samples.map {
                    mapOf("time" to it.time.toString(), "rate" to it.rate)
                }
            }
            is androidx.health.connect.client.records.ExerciseSessionRecord -> {
                recordedAt = record.endTime.toString()
                values["exerciseType"] = record.exerciseType
                values["title"] = record.title ?: ""
                values["start_time"] = record.startTime.toString()
                values["end_time"] = record.endTime.toString()
                values["duration_minutes"] = java.time.Duration.between(record.startTime, record.endTime).toMinutes()
                
                // Calculate Heart Rate Recovery
                val hrrData = calculateHeartRateRecovery(record.startTime, record.endTime)
                if (hrrData != null) {
                    val hrrValues = mutableMapOf<String, Any>(
                        "heart_rate_recovery_bpm" to hrrData["hrr"]!!,
                        "peak_bpm" to hrrData["peak"]!!,
                        "recovery_bpm" to hrrData["recovery"]!!
                    )
                    results.add(
                        com.zivaa.app.data.remote.SupabaseVitalRecord(
                            patientId = patientId,
                            metricType = "HeartRateRecoveryRecord",
                            recordedAt = record.endTime.plusSeconds(120).toString(),
                            values = hrrValues,
                            source = "zivaa_derived"
                        )
                    )
                }
            }
            is androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord -> {
                recordedAt = record.time.toString()
                values["rmssdMillis"] = record.heartRateVariabilityMillis
            }
            is androidx.health.connect.client.records.SkinTemperatureRecord -> {
                recordedAt = record.endTime.toString()
                values["baselineCelsius"] = record.baseline?.inCelsius ?: 0.0
                values["deltas"] = record.deltas.map {
                    mapOf("time" to it.time.toString(), "deltaCelsius" to it.delta.inCelsius)
                }
            }
            is androidx.health.connect.client.records.Vo2MaxRecord -> {
                recordedAt = record.time.toString()
                values["vo2MlPerMinKg"] = record.vo2MillilitersPerMinuteKilogram
            }
            else -> {
                values["raw_string"] = record.toString()
            }
        }
        
        results.add(
            com.zivaa.app.data.remote.SupabaseVitalRecord(
                patientId = patientId,
                metricType = metricType,
                recordedAt = recordedAt,
                values = values,
                source = source
            )
        )
        return results
    }

    private suspend fun calculateHeartRateRecovery(
        sessionStart: Instant,
        sessionEnd: Instant
    ): Map<String, Int>? {
        val client = healthConnectClient ?: return null
        
        // 1. Find Peak HR in the last 5 minutes of exercise
        val peakStart = if (sessionEnd.minusSeconds(300).isBefore(sessionStart)) sessionStart else sessionEnd.minusSeconds(300)
        
        val peakRequest = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(peakStart, sessionEnd)
        )
        val peakResult = try { client.readRecords(peakRequest) } catch(e: Exception) { null }
        var peakBpm = 0L
        peakResult?.records?.forEach { record ->
            record.samples.forEach { sample ->
                if (sample.beatsPerMinute > peakBpm) {
                    peakBpm = sample.beatsPerMinute
                }
            }
        }
        
        // 2. Find Recovery HR around 2 minutes after exercise
        val recoveryEnd = sessionEnd.plusSeconds(180) // search up to 3 minutes after
        val recoveryRequest = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(sessionEnd, recoveryEnd)
        )
        val recoveryResult = try { client.readRecords(recoveryRequest) } catch(e: Exception) { null }
        
        // We want the HR closest to 2 minutes after (target = sessionEnd + 120s)
        var recoveryBpm: Long? = null
        var closestTimeDiff = Long.MAX_VALUE
        val targetTime = sessionEnd.toEpochMilli() + 120000L
        
        recoveryResult?.records?.forEach { record ->
            record.samples.forEach { sample ->
                val sampleTime = sample.time.toEpochMilli()
                val diff = Math.abs(sampleTime - targetTime)
                if (diff < closestTimeDiff) {
                    closestTimeDiff = diff
                    recoveryBpm = sample.beatsPerMinute
                }
            }
        }
        
        if (peakBpm > 0 && recoveryBpm != null) {
            val hrr = (peakBpm - recoveryBpm!!).toInt()
            return mapOf(
                "hrr" to hrr,
                "peak" to peakBpm.toInt(),
                "recovery" to recoveryBpm!!.toInt()
            )
        }
        return null
    }
}

enum class HealthConnectSupport {
    AVAILABLE,
    INSTALL_REQUIRED,
    UNSUPPORTED
}
