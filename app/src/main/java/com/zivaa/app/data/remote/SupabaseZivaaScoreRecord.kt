package com.zivaa.app.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SupabaseZivaaScoreRecord(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("patient_id")
    val patientId: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("mobility_score")
    val mobilityScore: Int?,

    @SerializedName("mobility_breakdown")
    val mobilityBreakdown: Map<String, Int>? = null,

    @SerializedName("rest_score")
    val restScore: Int? = null,

    @SerializedName("rest_breakdown")
    val restBreakdown: Map<String, Any>? = null,

    @SerializedName("heart_score")
    val heartScore: Int? = null,

    @SerializedName("heart_breakdown")
    val heartBreakdown: Map<String, Any>? = null
)
