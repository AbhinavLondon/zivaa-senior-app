package com.zivaa.app.data.model

data class PatientPreference(
    val id: String,
    val domain: String,
    val constraint_text: String
)

data class SymptomLog(
    val id: String,
    val severity: String,
    val status: String,
    val note: String?,
    val created_at: String
)

data class PatientSymptom(
    val id: String,
    val name: String,
    val status: String,
    val severity: String,
    val follow_up_cadence_days: Int,
    val logs: List<SymptomLog>? = null
)

data class CarePlanAction(
    val id: String,
    val symptom_id: String?,
    val description: String,
    val status: String // Suggested, Agreed, Abandoned
)

data class PatientMedication(
    val id: String,
    val name: String,
    val dose: String?,
    val frequency: String?,
    val status: String // Active, Discontinued
)

data class LongevityPlanResponse(
    val preferences: List<PatientPreference>,
    val symptoms: List<PatientSymptom>,
    val actions: List<CarePlanAction>,
    val medications: List<PatientMedication>
)

data class UpdateSymptomPayload(
    val status: String,
    val progression_note: String? = null
)

data class UpdateActionPayload(
    val status: String
)

data class LongevityProtocol(
    val id: String,
    val title: String,
    val category: String,
    val reasoning: String,
    val description: String,
    val linked_asset_id: String? = null,
    val is_modified_today: Boolean = false,
    val is_new_from_coach: Boolean = false,
    val modification_note: String? = null,
    var isCompleted: Boolean = false
)

data class LongevityProtocolListResponse(
    val date: String,
    val protocols: List<LongevityProtocol>
)

