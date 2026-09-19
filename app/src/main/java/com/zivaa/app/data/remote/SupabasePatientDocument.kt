package com.zivaa.app.data.remote

import androidx.annotation.Keep
import com.google.gson.JsonObject

@Keep
data class SupabasePatientDocument(
    val id: String,
    val patient_id: String,
    val document_type: String,
    val file_url: String?,
    val extracted_data: JsonObject?,
    val created_at: String?
)
