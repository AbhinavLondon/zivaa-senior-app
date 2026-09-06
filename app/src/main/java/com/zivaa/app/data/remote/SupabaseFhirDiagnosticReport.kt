package com.zivaa.app.data.remote

import com.google.gson.annotations.SerializedName

data class SupabaseFhirDiagnosticReport(
    val id: String,
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("summary_explanation") val summaryExplanation: String?,
    val resource: FhirResource?
)

data class FhirResource(
    val code: FhirCode?,
    val performer: List<FhirPerformer>?,
    val effectiveDateTime: String?
)

data class FhirCode(
    val text: String?
)

data class FhirPerformer(
    val display: String?
)
