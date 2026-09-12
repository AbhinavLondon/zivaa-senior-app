package com.zivaa.app.data.remote

import com.google.gson.annotations.SerializedName

data class SupabasePatientPlanSetup(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("primary_focus") val primaryFocus: String?,
    @SerializedName("wake_time") val wakeTime: String?,
    @SerializedName("movement_level") val movementLevel: String?,
    @SerializedName("steps_goal") val stepsGoal: Int?,
    @SerializedName("diet_type") val dietType: String?,
    @SerializedName("height_inches") val heightInches: Int?,
    @SerializedName("weight_kg") val weightKg: Int?,
    @SerializedName("goal_weight_kg") val goalWeightKg: Int?,
    @SerializedName("health_conditions") val healthConditions: List<String>,
    @SerializedName("evening_activities") val eveningActivities: List<String>,
    @SerializedName("reminders") val reminders: String?,
    @SerializedName("target_calories_user_generated") val targetCaloriesUser: Int? = null,
    @SerializedName("protein_g_user_generated") val proteinGUser: Int? = null,
    @SerializedName("carbs_g_user_generated") val carbsGUser: Int? = null,
    @SerializedName("fat_g_user_generated") val fatGUser: Int? = null,
    @SerializedName("diet_preference") val dietPreference: String? = null,
    @SerializedName("target_calories_system_generated") val targetCaloriesSystem: Int? = null,
    @SerializedName("protein_g_system_generated") val proteinGSystem: Int? = null,
    @SerializedName("carbs_g_system_generated") val carbsGSystem: Int? = null,
    @SerializedName("fat_g_system_generated") val fatGSystem: Int? = null,
    @SerializedName("smoking_status") val smokingStatus: String? = null,
    @SerializedName("alcohol_status") val alcoholStatus: String? = null
)
