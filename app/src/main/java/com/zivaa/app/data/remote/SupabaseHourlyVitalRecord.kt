package com.zivaa.app.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SupabaseHourlyVitalRecord(
    @SerializedName("patient_id")
    val patientId: String,
    
    @SerializedName("hour_start")
    val hourStart: String,
    
    @SerializedName("total_steps")
    val totalSteps: Int?,
    
    @SerializedName("source")
    val source: String? = null
)
