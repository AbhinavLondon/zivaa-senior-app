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
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.records.metadata.DataOrigin
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
        HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ElevationGainedRecord::class),
        HealthPermission.getReadPermission(FloorsClimbedRecord::class),
        HealthPermission.getReadPermission(StepsCadenceRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(WheelchairPushesRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SkinTemperatureRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(Vo2MaxRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(BoneMassRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(BodyWaterMassRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getReadPermission(MenstruationFlowRecord::class),
        HealthPermission.getReadPermission(MenstruationPeriodRecord::class),
        HealthPermission.getReadPermission(OvulationTestRecord::class),
        HealthPermission.getReadPermission(CervicalMucusRecord::class),
        HealthPermission.getReadPermission(SexualActivityRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
        HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
    )

    val essentialPermissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
    )

    suspend fun getGrantedPermissions(): Set<String> {
        val client = healthConnectClient ?: return emptySet()
        return try {
            client.permissionController.getGrantedPermissions()
        } catch (e: Exception) {
            android.util.Log.e("HealthConnectManager", "Error querying granted permissions from Health Connect: ${e.message}", e)
            emptySet()
        }
    }

    suspend fun hasEssentialPermissions(): Boolean {
        val granted = getGrantedPermissions()
        return granted.containsAll(essentialPermissions)
    }

    suspend fun hasAnyPermissions(): Boolean {
        val granted = getGrantedPermissions()
        return granted.intersect(permissions).isNotEmpty()
    }

    suspend fun hasBackgroundReadPermission(): Boolean {
        val granted = getGrantedPermissions()
        return granted.contains(HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND)
    }

    suspend fun hasHistoryReadPermission(): Boolean {
        val granted = getGrantedPermissions()
        return granted.contains(HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY)
    }

    suspend fun hasAllPermissions(): Boolean {
        val granted = getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    fun isBackgroundReadAvailable(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            client.features.getFeatureStatus(
                androidx.health.connect.client.HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
            ) == androidx.health.connect.client.HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
        } catch (e: Exception) {
            android.util.Log.w("HealthConnectManager", "Error checking background read feature status: ${e.message}", e)
            false
        }
    }

    fun isHistoryReadAvailable(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            client.features.getFeatureStatus(
                androidx.health.connect.client.HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY
            ) == androidx.health.connect.client.HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
        } catch (e: Exception) {
            android.util.Log.w("HealthConnectManager", "Error checking history read feature status: ${e.message}", e)
            true
        }
    }

    suspend fun checkBrandSyncStatus(brandPackage: String): com.zivaa.app.presentation.setup.wearable.BrandSyncReport {
        val isInstalled = com.zivaa.app.presentation.setup.wearable.WearableCompanionDetector.isPackageInstalled(context, brandPackage)
        val client = healthConnectClient ?: return com.zivaa.app.presentation.setup.wearable.BrandSyncReport(isAppInstalled = isInstalled)

        return try {
            val now = Instant.now()
            val startOfDay = java.time.ZonedDateTime.now().toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
            val timeFilter48h = TimeRangeFilter.between(now.minus(48, java.time.temporal.ChronoUnit.HOURS), now)

            fun isOriginMatch(recordPackage: String): Boolean {
                if (brandPackage.isBlank()) return true
                if (recordPackage.equals(brandPackage, ignoreCase = true)) return true
                val stem = brandPackage.substringAfterLast(".").lowercase()
                val target = if (stem.length >= 4) stem else brandPackage.lowercase()
                return recordPackage.lowercase().contains(target) || (brandPackage.lowercase().contains("fitbit") && recordPackage.lowercase().contains("fitbit"))
            }

            // 1. Read latest HeartRateRecords (last 48 hours)
            val hrRequest = androidx.health.connect.client.request.ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeFilter48h,
                ascendingOrder = false,
                pageSize = 25
            )
            val hrResult = client.readRecords(hrRequest)
            val matchingHrRecord = hrResult.records.firstOrNull { isOriginMatch(it.metadata.dataOrigin.packageName) }
            val hrRecordToUse = matchingHrRecord ?: hrResult.records.firstOrNull()
            val latestBpm = hrRecordToUse?.samples?.maxByOrNull { it.time }?.beatsPerMinute?.toInt()
                ?: hrRecordToUse?.samples?.lastOrNull()?.beatsPerMinute?.toInt()

            // 2. Read latest StepsRecords to check dataOrigin
            val stepsRequest = androidx.health.connect.client.request.ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeFilter48h,
                ascendingOrder = false,
                pageSize = 25
            )
            val stepsResult = client.readRecords(stepsRequest)
            val matchingStepsRecord = stepsResult.records.firstOrNull { isOriginMatch(it.metadata.dataOrigin.packageName) }

            // 3. Aggregate real total steps for TODAY (midnight to now)
            val todayStepsAgg = try {
                val agg = client.aggregate(
                    androidx.health.connect.client.request.AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                    )
                )
                val c = agg[StepsRecord.COUNT_TOTAL] ?: 0L
                if (c > 0L) c else null
            } catch (e: Exception) {
                null
            }

            // Fallback to 24h aggregate if today's is 0 or null
            val totalStepsToday = todayStepsAgg ?: try {
                val agg24 = client.aggregate(
                    androidx.health.connect.client.request.AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(now.minus(24, java.time.temporal.ChronoUnit.HOURS), now)
                    )
                )
                val c = agg24[StepsRecord.COUNT_TOTAL] ?: 0L
                if (c > 0L) c else null
            } catch (e: Exception) {
                null
            }

            val hasBrandRecords = (matchingHrRecord != null) || (matchingStepsRecord != null)
            val lastSyncTime = matchingHrRecord?.endTime ?: matchingStepsRecord?.endTime ?: hrResult.records.firstOrNull()?.endTime

            com.zivaa.app.presentation.setup.wearable.BrandSyncReport(
                isAppInstalled = isInstalled,
                hasHealthConnectRecords = hasBrandRecords,
                latestHeartRateBpm = latestBpm,
                totalStepsToday = totalStepsToday,
                lastSyncTime = lastSyncTime
            )
        } catch (e: Exception) {
            e.printStackTrace()
            com.zivaa.app.presentation.setup.wearable.BrandSyncReport(isAppInstalled = isInstalled)
        }
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



    suspend fun readStepsRecordsSafely(
        start: Instant,
        end: Instant,
        minWindowSeconds: Long = 30
    ): List<StepsRecord> {
        val client = healthConnectClient ?: return emptyList()
        if (!start.isBefore(end)) return emptyList()

        return try {
            val records = mutableListOf<StepsRecord>()
            var pageToken: String? = null
            do {
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                    pageToken = pageToken,
                    ascendingOrder = true
                )
                val result = client.readRecords(request)
                records.addAll(result.records)
                pageToken = result.pageToken
            } while (pageToken != null)
            records
        } catch (e: IllegalArgumentException) {
            // Android Health Connect SDK throws IllegalArgumentException when a corrupted zero-duration record exists
            val durationSeconds = java.time.Duration.between(start, end).seconds
            if (durationSeconds <= minWindowSeconds) {
                // Isolated the corrupted zero-duration record window (<= 30 seconds)!
                // Completely ignore this invalid entry so authentic records are never affected.
                android.util.Log.w(
                    "HealthConnectManager",
                    "Ignoring corrupted zero-duration StepsRecord in window $start to $end (${e.message})"
                )
                emptyList()
            } else {
                // Bisect the time window into two halves
                val mid = start.plusSeconds(durationSeconds / 2)
                val left = readStepsRecordsSafely(start, mid, minWindowSeconds)
                val right = readStepsRecordsSafely(mid, end, minWindowSeconds)
                (left + right).distinctBy {
                    it.metadata.id.ifEmpty { "${it.startTime}_${it.endTime}_${it.count}" }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HealthConnectManager", "Unexpected error reading steps between $start and $end", e)
            emptyList()
        }
    }

    suspend fun fetchAllAvailableMetrics(days: Long = 90): List<Record> {
        val endTime = Instant.now()
        val startTime = endTime.minus(days, ChronoUnit.DAYS)
        return fetchAllAvailableMetrics(startTime, endTime)
    }

    suspend fun fetchAllAvailableMetrics(startTime: Instant, endTime: Instant): List<Record> {
        val client = healthConnectClient ?: return emptyList()
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
                android.util.Log.w("HealthConnectManager", "safeRead failed for ${recordClass.simpleName}: ${e.message}")
            }
        }

        // 1. Critical Clinical Vitals (Prioritized for immediate triage and daily aggregations)
        safeRead(HeartRateRecord::class)
        safeRead(SleepSessionRecord::class)

        // Read StepsRecord safely: isolates & ignores any corrupted zero-duration records, preserving 100% authentic records
        val authenticSteps = readStepsRecordsSafely(startTime, endTime)
        records.addAll(authenticSteps)

        safeRead(OxygenSaturationRecord::class)
        safeRead(BloodPressureRecord::class)
        safeRead(RestingHeartRateRecord::class)
        safeRead(BloodGlucoseRecord::class)
        safeRead(BodyTemperatureRecord::class)
        safeRead(BasalBodyTemperatureRecord::class)
        safeRead(RespiratoryRateRecord::class)
        safeRead(HeartRateVariabilityRmssdRecord::class)
        safeRead(Vo2MaxRecord::class)
        safeRead(SkinTemperatureRecord::class)

        // 2. Activity & Movement (Note: TotalCaloriesBurnedRecord omitted to prevent 130,000 synthetic BMR minute records)
        safeRead(ActiveCaloriesBurnedRecord::class)
        safeRead(DistanceRecord::class)
        safeRead(ElevationGainedRecord::class)
        safeRead(FloorsClimbedRecord::class)
        safeRead(SpeedRecord::class)
        safeRead(ExerciseSessionRecord::class)
        safeRead(WheelchairPushesRecord::class)
        safeRead(StepsCadenceRecord::class)

        // 3. Body Measurements
        safeRead(WeightRecord::class)
        safeRead(HeightRecord::class)
        safeRead(BodyFatRecord::class)
        safeRead(BoneMassRecord::class)
        safeRead(LeanBodyMassRecord::class)
        safeRead(BodyWaterMassRecord::class)
        safeRead(BasalMetabolicRateRecord::class)

        // 4. Nutrition & Hydration
        safeRead(NutritionRecord::class)
        safeRead(HydrationRecord::class)

        // 5. Women's Health
        safeRead(MenstruationFlowRecord::class)
        safeRead(MenstruationPeriodRecord::class)
        safeRead(OvulationTestRecord::class)
        safeRead(CervicalMucusRecord::class)
        safeRead(SexualActivityRecord::class)

        return records
    }

    suspend fun fetchTodayStepsRecords(): List<StepsRecord> {
        val zone = ZoneId.systemDefault()
        val todayStart = java.time.LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val todayEnd = java.time.LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant()
        return readStepsRecordsSafely(todayStart, todayEnd)
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
            BasalBodyTemperatureRecord::class,
            BloodGlucoseRecord::class,
            OxygenSaturationRecord::class,
            RespiratoryRateRecord::class,
            SpeedRecord::class,
            DistanceRecord::class,
            ActiveCaloriesBurnedRecord::class,
            TotalCaloriesBurnedRecord::class,
            ElevationGainedRecord::class,
            FloorsClimbedRecord::class,
            StepsCadenceRecord::class,
            SleepSessionRecord::class,
            ExerciseSessionRecord::class,
            WheelchairPushesRecord::class,
            HeartRateVariabilityRmssdRecord::class,
            SkinTemperatureRecord::class,
            StepsRecord::class,
            Vo2MaxRecord::class,
            WeightRecord::class,
            HeightRecord::class,
            BodyFatRecord::class,
            BoneMassRecord::class,
            LeanBodyMassRecord::class,
            BodyWaterMassRecord::class,
            BasalMetabolicRateRecord::class,
            NutritionRecord::class,
            HydrationRecord::class,
            MenstruationFlowRecord::class,
            MenstruationPeriodRecord::class,
            OvulationTestRecord::class,
            CervicalMucusRecord::class,
            SexualActivityRecord::class
        )
        val requestedTypes = allTypes.filter { HealthPermission.getReadPermission(it) in granted }.toSet()
        if (requestedTypes.isEmpty()) return null

        val request = androidx.health.connect.client.request.ChangesTokenRequest(recordTypes = requestedTypes)
        return try {
            client.getChangesToken(request)
        } catch (e: Exception) {
            android.util.Log.e("HealthConnectManager", "Failed to get changes token for types $requestedTypes: ${e.message}", e)
            null
        }
    }

    suspend fun getChanges(token: String): androidx.health.connect.client.response.ChangesResponse? {
        val client = healthConnectClient ?: return null
        return try {
            client.getChanges(token)
        } catch (e: Exception) {
            android.util.Log.e("HealthConnectManager", "Failed to get changes for token [${token.take(12)}...]: ${e.message}", e)
            null
        }
    }

    suspend fun mapRecordToSupabase(record: androidx.health.connect.client.records.Record, patientId: String): List<com.zivaa.app.data.remote.SupabaseVitalRecord> {
        val metricType = record::class.simpleName ?: "UnknownRecord"
        var recordedAt = Instant.now().toString()
        val values = mutableMapOf<String, Any>()

        // Extract source & record IDs from metadata
        val meta = record.metadata
        if (meta.id.isNotBlank()) {
            values["health_connect_id"] = meta.id
        }
        if (!meta.clientRecordId.isNullOrBlank()) {
            values["client_record_id"] = meta.clientRecordId!!
        }

        var source = if (meta.dataOrigin.packageName.isNotBlank()) meta.dataOrigin.packageName else "com.sec.android.app.shealth"

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
                values["start_time"] = record.startTime.toString()
                values["end_time"] = record.endTime.toString()
                values["duration_seconds"] = java.time.Duration.between(record.startTime, record.endTime).seconds
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
        val peakResult = try { 
            client.readRecords(peakRequest) 
        } catch(e: Exception) { 
            android.util.Log.w("HealthConnectManager", "Failed to read peak HR records for HRR calculation: ${e.message}", e)
            null 
        }
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
        val recoveryResult = try { 
            client.readRecords(recoveryRequest) 
        } catch(e: Exception) { 
            android.util.Log.w("HealthConnectManager", "Failed to read recovery HR records for HRR calculation: ${e.message}", e)
            null 
        }
        
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
