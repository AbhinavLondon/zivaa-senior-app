package com.zivaa.app.data.remote

data class SupabaseExerciseRecord(
    val id: String,
    val exercise_name: String,
    val benefits: String? = null,
    val duration_seconds: Int? = null,
    val equipment_needed: String? = null,
    val difficulty: String? = null,
    val type: String? = null,
    val body_part: String? = null,
    val step_by_step_instructions: String? = null,
    val tips: String? = null,
    val modifications_easier: String? = null,
    val progression_harder: String? = null,
    val reps_duration: String? = null,
    val precautions_contraindications: String? = null,
    val starting_position: String? = null,
    val voiceover_script: String? = null,
    val video_link: String? = null,
    val image_thumbnail_file: String? = null
)
