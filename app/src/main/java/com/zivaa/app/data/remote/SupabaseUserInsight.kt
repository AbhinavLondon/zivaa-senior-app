package com.zivaa.app.data.remote

data class SupabaseUserInsight(
    val id: String,
    val patient_id: String,
    val insight_date: String,
    val insight_type: String,
    val insight_text: String,
    val created_at: String
)
