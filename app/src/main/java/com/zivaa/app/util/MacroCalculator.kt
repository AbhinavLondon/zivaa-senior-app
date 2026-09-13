package com.zivaa.app.util

import java.time.LocalDate

data class CalculatedMacros(
    val targetCalories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
)

object MacroCalculator {

    /**
     * Calculates Mifflin-St Jeor BMR, TDEE, goal-adjusted calories, and clinical macro splits
     * mirroring the clinical logic from backend macro_calculator.py
     */
    fun calculateDailyMacros(
        weightKg: Int?,
        heightInches: Int?,
        goalWeightKg: Int?,
        dobStr: String?,
        gender: String?,
        movementLevel: String?,
        dietType: String?,
        healthConditions: List<String>
    ): CalculatedMacros {
        // Fallback default if basic physical metrics are missing
        if (weightKg == null || weightKg <= 0 || heightInches == null || heightInches <= 0) {
            return CalculatedMacros(
                targetCalories = 2000,
                proteinG = 100,
                carbsG = 250,
                fatG = 65
            )
        }

        // 1. Age & Sex
        val age = parseAge(dobStr) ?: 70
        val isFemale = gender?.trim()?.lowercase() == "female"

        // 2. Mifflin-St Jeor BMR
        val heightCm = heightInches * 2.54
        var bmr = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        if (isFemale) {
            bmr -= 161.0
        } else {
            bmr += 5.0
        }

        // 3. Activity Multiplier
        val movement = (movementLevel ?: "").lowercase()
        val activityMultiplier = when {
            "sedentary" in movement -> 1.20
            "light" in movement -> 1.375
            "active" in movement -> 1.55
            "very active" in movement -> 1.725
            else -> 1.375 // Default to light
        }

        val tdee = bmr * activityMultiplier

        // 4. Goal Adjustment
        var targetCalories = tdee
        if (goalWeightKg != null && goalWeightKg > 0) {
            if (goalWeightKg < weightKg) {
                targetCalories -= 300.0 // Weight loss deficit
            } else if (goalWeightKg > weightKg) {
                targetCalories += 300.0 // Weight gain surplus
            }
        }

        // Prevent extreme starvation
        if (targetCalories < 1200.0) {
            targetCalories = 1200.0
        }

        // 5. Macro Splits
        val diet = (dietType ?: "").lowercase()
        val conditionsLower = healthConditions.map { it.lowercase() }

        var carbPct = 0.50
        var proteinPct = 0.20
        var fatPct = 0.30

        if ("keto" in diet) {
            carbPct = 0.05
            proteinPct = 0.25
            fatPct = 0.70
        } else if (conditionsLower.any { it.contains("diabetes") || it.contains("diabetic") } || diet.contains("diabetic")) {
            carbPct = 0.40
            proteinPct = 0.25
            fatPct = 0.35
        }

        // 6. Grams calculation (Carbs: 4 kcal/g, Protein: 4 kcal/g, Fat: 9 kcal/g)
        val carbsG = (targetCalories * carbPct) / 4.0
        val proteinG = (targetCalories * proteinPct) / 4.0
        val fatG = (targetCalories * fatPct) / 9.0

        return CalculatedMacros(
            targetCalories = targetCalories.toInt(),
            proteinG = proteinG.toInt(),
            carbsG = carbsG.toInt(),
            fatG = fatG.toInt()
        )
    }

    private fun parseAge(dobStr: String?): Int? {
        if (dobStr.isNullOrBlank()) return null
        return try {
            val date = if (dobStr.contains("-")) {
                val parts = dobStr.split("-")
                if (parts[0].length == 4) {
                    LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].take(2).toInt())
                } else {
                    LocalDate.of(parts[2].take(4).toInt(), parts[1].toInt(), parts[0].toInt())
                }
            } else {
                null
            }
            if (date != null) {
                java.time.Period.between(date, LocalDate.now()).years.coerceIn(18, 120)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
