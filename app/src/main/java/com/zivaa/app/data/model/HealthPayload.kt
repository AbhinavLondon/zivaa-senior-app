package com.zivaa.app.data.model

import androidx.health.connect.client.records.*
import java.time.Instant

data class DeviceStatusPayload(
    val battery_level: Int,
    val is_charging: Boolean,
    val is_connected: Boolean = true
)

data class HealthPayload(
    val client_time: String,
    val timezone: String,
    val records: List<MetricRecord>,
    val device_status: DeviceStatusPayload? = null,
    val patient_id: String? = null
)

data class MetricRecord(
    val type: String,
    val timestamp: String,
    val values: Map<String, Double>
) {
    companion object {
        fun extractRecordTimestamp(record: Record): String {
            return when (record) {
                is StepsRecord -> record.startTime.toString()
                is ActiveCaloriesBurnedRecord -> record.startTime.toString()
                is TotalCaloriesBurnedRecord -> record.startTime.toString()
                is DistanceRecord -> record.startTime.toString()
                is ElevationGainedRecord -> record.startTime.toString()
                is FloorsClimbedRecord -> record.startTime.toString()
                is SpeedRecord -> record.startTime.toString()
                is ExerciseSessionRecord -> record.startTime.toString()
                is HeartRateRecord -> record.startTime.toString()
                is RestingHeartRateRecord -> record.time.toString()
                is HeartRateVariabilityRmssdRecord -> record.time.toString()
                is BloodPressureRecord -> record.time.toString()
                is BloodGlucoseRecord -> record.time.toString()
                is OxygenSaturationRecord -> record.time.toString()
                is BodyTemperatureRecord -> record.time.toString()
                is BasalBodyTemperatureRecord -> record.time.toString()
                is RespiratoryRateRecord -> record.time.toString()
                is Vo2MaxRecord -> record.time.toString()
                is WeightRecord -> record.time.toString()
                is HeightRecord -> record.time.toString()
                is BodyFatRecord -> record.time.toString()
                is BoneMassRecord -> record.time.toString()
                is LeanBodyMassRecord -> record.time.toString()
                is BodyWaterMassRecord -> record.time.toString()
                is BasalMetabolicRateRecord -> record.time.toString()
                is HydrationRecord -> record.startTime.toString()
                is NutritionRecord -> record.startTime.toString()
                is SleepSessionRecord -> record.startTime.toString()
                is MenstruationFlowRecord -> record.time.toString()
                is MenstruationPeriodRecord -> record.startTime.toString()
                is OvulationTestRecord -> record.time.toString()
                is CervicalMucusRecord -> record.time.toString()
                is SexualActivityRecord -> record.time.toString()
                else -> Instant.now().toString()
            }
        }

        fun fromHealthConnectRecord(record: Record): MetricRecord? {
            val timestamp = extractRecordTimestamp(record)

            return when (record) {
                // Activity
                is StepsRecord -> MetricRecord("steps", timestamp, mapOf("count" to record.count.toDouble()))
                is ActiveCaloriesBurnedRecord -> MetricRecord("active_calories", timestamp, mapOf("kcal" to record.energy.inKilocalories))
                is TotalCaloriesBurnedRecord -> MetricRecord("total_calories", timestamp, mapOf("kcal" to record.energy.inKilocalories))
                is DistanceRecord -> MetricRecord("distance", timestamp, mapOf("meters" to record.distance.inMeters))
                is ElevationGainedRecord -> MetricRecord("elevation_gained", timestamp, mapOf("meters" to record.elevation.inMeters))
                is FloorsClimbedRecord -> MetricRecord("floors_climbed", timestamp, mapOf("count" to record.floors))
                is SpeedRecord -> MetricRecord("speed", timestamp, mapOf("mps" to (record.samples.map { it.speed.inMetersPerSecond }.average().takeIf { !it.isNaN() } ?: 0.0)))
                is ExerciseSessionRecord -> MetricRecord("exercise_session", timestamp, mapOf("duration_min" to java.time.Duration.between(record.startTime, record.endTime).toMinutes().toDouble()))

                // Vitals
                is HeartRateRecord -> MetricRecord("heart_rate", timestamp, mapOf("bpm" to (record.samples.map { it.beatsPerMinute }.average().takeIf { !it.isNaN() } ?: 72.0)))
                is RestingHeartRateRecord -> MetricRecord("resting_heart_rate", timestamp, mapOf("bpm" to record.beatsPerMinute.toDouble()))
                is HeartRateVariabilityRmssdRecord -> MetricRecord("hrv", timestamp, mapOf("rmssd" to record.heartRateVariabilityMillis))
                is BloodPressureRecord -> MetricRecord("blood_pressure", timestamp, mapOf("systolic" to record.systolic.inMillimetersOfMercury, "diastolic" to record.diastolic.inMillimetersOfMercury))
                is BloodGlucoseRecord -> MetricRecord("blood_glucose", timestamp, mapOf("glucose_mg_dl" to record.level.inMilligramsPerDeciliter))
                is OxygenSaturationRecord -> MetricRecord("oxygen_sat", timestamp, mapOf("percentage" to record.percentage.value))
                is BodyTemperatureRecord -> MetricRecord("body_temp", timestamp, mapOf("celsius" to record.temperature.inCelsius))
                is BasalBodyTemperatureRecord -> MetricRecord("basal_temp", timestamp, mapOf("celsius" to record.temperature.inCelsius))
                is RespiratoryRateRecord -> MetricRecord("respiratory_rate", timestamp, mapOf("rpm" to record.rate))
                is Vo2MaxRecord -> MetricRecord("vo2_max", timestamp, mapOf("vo2" to record.vo2MillilitersPerMinuteKilogram))

                // Body Measurements
                is WeightRecord -> MetricRecord("weight", timestamp, mapOf("kg" to record.weight.inKilograms))
                is HeightRecord -> MetricRecord("height", timestamp, mapOf("meters" to record.height.inMeters))
                is BodyFatRecord -> MetricRecord("body_fat", timestamp, mapOf("percentage" to record.percentage.value))
                is BoneMassRecord -> MetricRecord("bone_mass", timestamp, mapOf("kg" to record.mass.inKilograms))
                is LeanBodyMassRecord -> MetricRecord("lean_body_mass", timestamp, mapOf("kg" to record.mass.inKilograms))
                is BodyWaterMassRecord -> MetricRecord("body_water_mass", timestamp, mapOf("kg" to record.mass.inKilograms))
                is BasalMetabolicRateRecord -> MetricRecord("basal_metabolic_rate", timestamp, mapOf("kcal_day" to record.basalMetabolicRate.inKilocaloriesPerDay))

                // Nutrition & Hydration
                is HydrationRecord -> MetricRecord("hydration", timestamp, mapOf("liters" to record.volume.inLiters))
                is NutritionRecord -> MetricRecord("nutrition", timestamp, mapOf(
                    "calories" to (record.energy?.inKilocalories ?: 0.0),
                    "protein" to (record.protein?.inGrams ?: 0.0),
                    "carbs" to (record.totalCarbohydrate?.inGrams ?: 0.0)
                ))

                // Sleep
                is SleepSessionRecord -> MetricRecord("sleep", timestamp, mapOf("duration_min" to java.time.Duration.between(record.startTime, record.endTime).toMinutes().toDouble()))

                // Women's Health
                is MenstruationFlowRecord -> MetricRecord("menstruation_flow", timestamp, mapOf("flow" to record.flow.toDouble()))
                is MenstruationPeriodRecord -> MetricRecord("menstruation_period", timestamp, mapOf("active" to 1.0))
                is OvulationTestRecord -> MetricRecord("ovulation_test", timestamp, mapOf("result" to record.result.toDouble()))
                is CervicalMucusRecord -> MetricRecord("cervical_mucus", timestamp, mapOf("sensation" to record.sensation.toDouble()))
                is SexualActivityRecord -> MetricRecord("sexual_activity", timestamp, mapOf("protection" to record.protectionUsed.toDouble()))

                else -> null
            }
        }
    }
}
