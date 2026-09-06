package com.zivaa.app.data.remote

data class SupabaseExerciseRecord(
    val id: String,
    val exercise_name: String,
    val benefits: String?,
    val duration_seconds: Int?,
    val equipment_needed: String?,
    val difficulty: String?,
    val type: String?,
    val body_part: String?,
    val step_by_step_instructions: String?,
    val tips: String?,
    val modifications_easier: String?,
    val progression_harder: String?,
    val reps_duration: String?,
    val precautions_contraindications: String?
)
