package com.zivaa.app.data.remote

import com.google.gson.annotations.SerializedName

// Auth Models
data class SendOtpRequest(
    val email: String,
    @SerializedName("create_user") val createUser: Boolean = true
)

data class VerifyOtpRequest(
    val type: String = "email",
    val email: String,
    val token: String
)

data class VerifyOtpResponse(
    val user: SupabaseUser,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class SupabaseUser(
    val id: String,
    val email: String
)

// Setup Models
data class PatientRecord(
    val id: String? = null,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    val phone: String,
    val wearables: String, // Storing as JSON string or comma-separated
    @SerializedName("location_city") val locationCity: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("profile_pic_url") val profilePicUrl: String? = null,
    @SerializedName("caregiver_nudge_preference") val caregiverNudgePreference: String? = null,
    @SerializedName("preferred_language") val preferredLanguage: String? = null
)

data class ConditionRecord(
    val id: String? = null,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("condition_name") val conditionName: String,
    val status: String = "active"
)

data class DoctorRecord(
    val id: String? = null,
    @SerializedName("full_name") val fullName: String,
    val specialization: String,
    val phone: String
)

data class CaregiverRecord(
    val id: String? = null,
    @SerializedName("patient_id") val patientId: String,
    val name: String,
    val relation: String,
    val phone: String,
    val city: String? = null,
    val role: String = "primary",
    @SerializedName("receives_alerts") val receivesAlerts: Boolean = true
)
