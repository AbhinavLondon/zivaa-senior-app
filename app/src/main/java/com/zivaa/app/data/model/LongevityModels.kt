package com.zivaa.app.data.model

data class PatientPreference(
    val id: String = "",
    val domain: String = "",
    val constraint_text: String = ""
)

data class SymptomLog(
    val id: String = "",
    val severity: String = "Moderate",
    val status: String = "Active",
    val note: String? = null,
    val created_at: String = ""
)

data class PatientSymptom(
    val id: String = "",
    val name: String = "",
    val status: String = "Active",
    val severity: String = "Moderate",
    val follow_up_cadence_days: Int = 7,
    val logs: List<SymptomLog>? = null
)

data class CarePlanAction(
    val id: String = "",
    val symptom_id: String? = null,
    val description: String = "",
    val status: String = "Suggested" // Suggested, Agreed, Abandoned
)

data class PatientMedication(
    val id: String = "",
    val name: String = "",
    val dose: String? = null,
    val frequency: String? = null,
    val status: String = "Active" // Active, Discontinued
)

data class LongevityPlanResponse(
    val preferences: List<PatientPreference>? = null,
    val symptoms: List<PatientSymptom>? = null,
    val actions: List<CarePlanAction>? = null,
    val medications: List<PatientMedication>? = null
)

data class UpdateSymptomPayload(
    val status: String,
    val progression_note: String? = null
)

data class UpdateActionPayload(
    val status: String
)

data class LongevityProtocol(
    val id: String? = null,
    val title: String? = null,
    val category: String? = null,
    val reasoning: String? = null,
    val description: String? = null,
    val baseline_target: String? = null,
    val why_it_matters: String? = null,
    val linked_asset_id: String? = null,
    val is_modified_today: Boolean = false,
    val is_new_from_coach: Boolean = false,
    val modification_note: String? = null,
    var isCompleted: Boolean = false
) {
    val displayTitle: String 
        get() = (title ?: baseline_target ?: "Daily Habit").ifBlank { "Daily Habit" }

    val displayDescription: String 
        get() = (description ?: why_it_matters ?: "").ifBlank { "Daily habit for your health journey." }

    val displayReasoning: String 
        get() = (reasoning ?: why_it_matters ?: "").ifBlank { "Recommended for your longevity plan." }

    val displayCategory: String 
        get() = (category ?: "General").ifBlank { "General" }

    val safeId: String 
        get() = id ?: "${displayTitle}_${displayCategory}".replace(" ", "_")
}

data class LongevityProtocolListResponse(
    val date: String? = null,
    val effective_date: String? = null,
    val protocols: List<LongevityProtocol>? = null
)
