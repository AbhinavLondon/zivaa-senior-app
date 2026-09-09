package com.zivaa.app.ui.labs

import com.zivaa.app.data.remote.ObservationResource
import java.util.Locale

object ObservationCategoryHelper {

    fun extractCategory(resource: ObservationResource?): String {
        if (resource == null) return "General Labs"

        val categories = resource.category ?: return "General Labs"

        // 1. Prioritize zivaa consumer category coding
        val consumerDisplay = categories.asSequence()
            .flatMap { it.coding ?: emptyList() }
            .find { it.system == "https://zivaa.com/consumer-category" }
            ?.display
            ?.takeIf { it.isNotBlank() && !it.equals("Other", ignoreCase = true) }
        if (consumerDisplay != null) return normalizeCategoryName(consumerDisplay)

        // 2. Check zivaa clinical category coding
        val clinicalDisplay = categories.asSequence()
            .flatMap { it.coding ?: emptyList() }
            .find { it.system == "https://zivaa.com/clinical-category" }
            ?.display
            ?.takeIf { it.isNotBlank() && !it.equals("Other", ignoreCase = true) }
        if (clinicalDisplay != null) return normalizeCategoryName(clinicalDisplay)

        // 3. Check any coding display (skip generic HL7 "laboratory" and "other")
        val anyCodingDisplay = categories.asSequence()
            .flatMap { it.coding ?: emptyList() }
            .mapNotNull { it.display }
            .find { it.isNotBlank() && !it.equals("Laboratory", ignoreCase = true) && !it.equals("Other", ignoreCase = true) }
        if (anyCodingDisplay != null) return normalizeCategoryName(anyCodingDisplay)

        // 4. Check category text
        val textDisplay = categories.asSequence()
            .mapNotNull { it.text }
            .find { it.isNotBlank() && !it.equals("Laboratory", ignoreCase = true) && !it.equals("Other", ignoreCase = true) }
        if (textDisplay != null) return normalizeCategoryName(textDisplay)

        return "General Labs"
    }

    fun normalizeCategoryName(raw: String): String {
        val trimmed = raw.trim()
        val upper = trimmed.uppercase(Locale.US)
        return when {
            upper == "CHEM" -> "Metabolic Health"
            upper == "CBC" || upper == "HEM/BC" -> "Complete Blood Count"
            upper == "BLDBK" -> "Blood Bank & Typing"
            upper == "CHAL" -> "Special Diagnostics"
            upper == "MICRO" -> "Microbiology"
            upper == "LIVER" -> "Liver Health"
            upper == "RENAL" -> "Kidney Health"
            upper == "THYROID" -> "Thyroid Profile"
            upper == "LIPID PROFILE" -> "Lipid Profile"
            upper == "METABOLIC" -> "Metabolic Health"
            upper == "KIDNEY" -> "Kidney"
            upper == "BLOOD SUGAR" -> "Blood Sugar"
            upper == "HEART" -> "Heart"
            upper == "BLOOD" -> "Blood"
            upper == "INFECTION" -> "Infection"
            else -> trimmed
        }
    }
}
