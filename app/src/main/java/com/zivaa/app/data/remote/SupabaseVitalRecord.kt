package com.zivaa.app.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SupabaseVitalRecord(
    @SerializedName("patient_id")
    val patientId: String,
    
    @SerializedName("metric_type")
    val metricType: String,
    
    @SerializedName("recorded_at")
    val recordedAt: String,
    
    @SerializedName("values")
    val values: Map<String, Any>,
    
    @SerializedName("source")
    val source: String
)
