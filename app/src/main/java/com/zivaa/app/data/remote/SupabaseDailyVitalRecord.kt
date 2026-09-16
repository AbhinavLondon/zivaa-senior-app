package com.zivaa.app.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SupabaseDailyVitalRecord(
    @SerializedName("patient_id")
    val patientId: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("total_steps")
    val totalSteps: Int?,
    
    @SerializedName("distance_meters")
    val distanceMeters: Double?,
    
    @SerializedName("active_calories")
    val activeCalories: Double?,
    
    @SerializedName("avg_heart_rate")
    val avgHeartRate: Double?,
    
    @SerializedName("sleep_hours")
    val sleepHours: Double?,
    
    @SerializedName("exercise_minutes")
    val exerciseMinutes: Double?,
    
    @SerializedName("min_heart_rate")
    val minHeartRate: Double?,
    
    @SerializedName("max_heart_rate")
    val maxHeartRate: Double?,

    @SerializedName("resting_heart_rate_calculated")
    val restingHeartRateCalculated: Double?,

    @SerializedName("heart_rate_recovery_calculated")
    val heartRateRecoveryCalculated: List<HeartRateRecoveryItem>?,

    @SerializedName("oxygen_sat_avg")
    val oxygenSatAvg: Double?,
    
    @SerializedName("sleep_stage_1_hours")
    val sleepStage1Hours: Double?,
    
    @SerializedName("sleep_stage_5_hours")
    val sleepStage5Hours: Double?,

    @SerializedName("sleep_onset_time")
    val sleepOnsetTime: String? = null,

    @SerializedName("final_wakeup_time")
    val finalWakeupTime: String? = null,

    @SerializedName("sleep_consistency_pct")
    val sleepConsistencyPct: Double? = null,

    @SerializedName("sleep_stage_5_pct")
    val sleepStage5Pct: Double? = null,

    @SerializedName("sleep_stage_6_hours")
    val sleepStage6Hours: Double? = null,

    @SerializedName("sleep_stage_6_pct")
    val sleepStage6Pct: Double? = null,

    @SerializedName("sleep_efficiency_pct")
    val sleepEfficiencyPct: Double? = null,

    @SerializedName("skin_temperature_delta")
    val skinTemperatureDelta: Double? = null,

    @SerializedName("respiratory_rate_avg")
    val respiratoryRateAvg: Double? = null,

    @SerializedName("bedtime_variance_mins")
    val bedtimeVarianceMins: Double? = null,

    @SerializedName("avg_cadence_spm")
    val avgCadenceSpm: Double? = null,

    @SerializedName("active_movement_minutes")
    val activeMovementMinutes: Double? = null,

    @SerializedName("active_hours_count")
    val activeHoursCount: Int? = null
)

@Keep
data class HeartRateRecoveryItem(
    @SerializedName("bpm") val bpm: Int
)
