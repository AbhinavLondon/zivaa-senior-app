package com.zivaa.app.data.remote

import com.google.gson.annotations.SerializedName

data class SupabaseFhirObservation(
    val id: String,
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("report_id") val reportId: String?,
    @SerializedName("value_numeric") val valueNumeric: Double?,
    @SerializedName("value_string") val valueString: String?,
    val resource: ObservationResource?
)

data class ObservationResource(
    val code: FhirCode?,
    val status: String?,
    val category: List<ObservationCategory>?,
    val valueQuantity: ObservationValueQuantity?,
    val interpretation: List<ObservationInterpretation>?,
    val referenceRange: List<ObservationReferenceRange>?,
    val presentedForm: List<ObservationPresentedForm>?
)

data class ObservationCategory(
    val text: String?,
    val coding: List<ObservationCoding>?
)

data class ObservationCoding(
    val display: String?,
    val code: String?,
    val system: String?
)

data class ObservationValueQuantity(
    val value: Double?,
    val unit: String?,
    val system: String?
)

data class ObservationInterpretation(
    val text: String?,
    val coding: List<ObservationCoding>?
)

data class ObservationReferenceRange(
    val low: ObservationValueQuantity?,
    val high: ObservationValueQuantity?
)

data class ObservationPresentedForm(
    val title: String?
)
