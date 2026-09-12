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

    @SerializedName("bedtime_variance_mins")
    val bedtimeVarianceMins: Double? = null
)

@Keep
data class HeartRateRecoveryItem(
    @SerializedName("bpm") val bpm: Int
)
