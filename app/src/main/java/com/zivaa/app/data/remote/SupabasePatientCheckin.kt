package com.zivaa.app.data.remote

data class SupabasePatientCheckin(
    val patient_id: String,
    val date: String,
    val checkin_type: String, // SELF_INITIATED or PROMPTED
    val mood_label: String,
    val emotions: List<String>,
    val causes: List<String>,
    val user_comments: String? = null
)
