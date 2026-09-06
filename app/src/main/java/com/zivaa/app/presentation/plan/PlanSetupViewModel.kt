package com.zivaa.app.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabasePatientPlanSetup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlanSetupState(
    val primaryFocus: String? = null,
    val wakeTime: String? = null,
    val movementLevel: String? = null,
    val stepsGoal: Int? = null,
    val dietType: String? = null,
    val heightInches: Int? = null,
    val weightKg: Int? = null,
    val goalWeightKg: Int? = null,
    val conditions: Set<String> = emptySet(),
    val evening: Set<String> = emptySet(),
    val reminders: String? = null,
    val targetCalories: Int? = null,
    val proteinPct: Float = 0.3f,
    val carbsPct: Float = 0.4f,
    val fatPct: Float = 0.3f,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val hasBuiltPlanOnce: Boolean = false
)

class PlanSetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(PlanSetupState())
    val state: StateFlow<PlanSetupState> = _state.asStateFlow()

    init {
        fetchPlanSetup()
    }

    private fun fetchPlanSetup() {
        viewModelScope.launch {
            try {
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                val response = RetrofitClient.apiService.getPlanSetup(patientIdQuery = "eq.$patientId")
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val latest = response.body()!!.first()
                    _state.value = _state.value.copy(
                        primaryFocus = latest.primaryFocus,
                        wakeTime = latest.wakeTime,
                        movementLevel = latest.movementLevel,
                        stepsGoal = latest.stepsGoal,
                        dietType = latest.dietType,
                        heightInches = latest.heightInches,
                        weightKg = latest.weightKg,
                        goalWeightKg = latest.goalWeightKg,
                        conditions = latest.healthConditions?.toSet() ?: emptySet(),
                        evening = latest.eveningActivities?.toSet() ?: emptySet(),
                        reminders = latest.reminders,
                        targetCalories = latest.targetCaloriesUser ?: latest.targetCaloriesSystem,
                        // Convert system or user generated grams to percentages if user overrides are missing
                        proteinPct = calculatePct(latest.proteinGUser, 4, latest.targetCaloriesUser) ?: calculatePct(latest.proteinGSystem, 4, latest.targetCaloriesSystem) ?: 0.3f,
                        carbsPct = calculatePct(latest.carbsGUser, 4, latest.targetCaloriesUser) ?: calculatePct(latest.carbsGSystem, 4, latest.targetCaloriesSystem) ?: 0.4f,
                        fatPct = calculatePct(latest.fatGUser, 9, latest.targetCaloriesUser) ?: calculatePct(latest.fatGSystem, 9, latest.targetCaloriesSystem) ?: 0.3f
                    )
                }
            } catch (e: Exception) {
                // Ignore failure and fallback to empty state
            }
        }
    }

    fun updatePrimaryFocus(focus: String?) {
        _state.value = _state.value.copy(primaryFocus = focus, isSaved = false)
    }

    fun updateWakeTime(time: String?) {
        _state.value = _state.value.copy(wakeTime = time, isSaved = false)
    }

    fun updateMovementLevel(level: String?) {
        val newStepsGoal = if (level != null && _state.value.stepsGoal == null) 10000 else _state.value.stepsGoal
        _state.value = _state.value.copy(movementLevel = level, stepsGoal = newStepsGoal, isSaved = false)
    }

    fun updateStepsGoal(goal: Int?) {
        _state.value = _state.value.copy(stepsGoal = goal, isSaved = false)
    }

    fun updateDietType(diet: String?) {
        _state.value = _state.value.copy(dietType = diet, isSaved = false)
    }

    fun updateHeight(inches: Int?) {
        _state.value = _state.value.copy(heightInches = inches, isSaved = false)
    }

    fun updateWeight(kg: Int?) {
        _state.value = _state.value.copy(weightKg = kg, isSaved = false)
    }

    fun updateGoalWeight(kg: Int?) {
        _state.value = _state.value.copy(goalWeightKg = kg, isSaved = false)
    }

    fun toggleCondition(condition: String) {
        val current = _state.value.conditions.toMutableSet()
        if (current.contains(condition)) {
            current.remove(condition)
        } else {
            current.add(condition)
        }
        _state.value = _state.value.copy(conditions = current, isSaved = false)
    }

    fun toggleEvening(activity: String) {
        val current = _state.value.evening.toMutableSet()
        if (current.contains(activity)) {
            current.remove(activity)
        } else {
            current.add(activity)
        }
        _state.value = _state.value.copy(evening = current, isSaved = false)
    }

    fun updateReminders(reminders: String?) {
        _state.value = _state.value.copy(reminders = reminders, isSaved = false)
    }

    fun savePlanAndProceed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            
            try {
                // Retrieve the actual patientId from AuthManager
                val patientId = RetrofitClient.authManager?.getUserId() ?: "0c445588-b36c-478f-9be3-2addfc77dc1c"
                
                val p = _state.value.proteinPct
                val c = _state.value.carbsPct
                val f = _state.value.fatPct
                
                val dietPref = when {
                    kotlin.math.abs(p - 0.3f) < 0.01f && kotlin.math.abs(c - 0.4f) < 0.01f && kotlin.math.abs(f - 0.3f) < 0.01f -> "Balanced"
                    kotlin.math.abs(p - 0.4f) < 0.01f && kotlin.math.abs(c - 0.3f) < 0.01f && kotlin.math.abs(f - 0.3f) < 0.01f -> "More protein"
                    kotlin.math.abs(p - 0.3f) < 0.01f && kotlin.math.abs(c - 0.2f) < 0.01f && kotlin.math.abs(f - 0.5f) < 0.01f -> "Lighter carbs"
                    kotlin.math.abs(p - 0.25f) < 0.01f && kotlin.math.abs(c - 0.05f) < 0.01f && kotlin.math.abs(f - 0.7f) < 0.01f -> "Keto"
                    else -> "Custom"
                }

                val cals = _state.value.targetCalories
                val payload = SupabasePatientPlanSetup(
                    patientId = patientId,
                    primaryFocus = _state.value.primaryFocus,
                    wakeTime = _state.value.wakeTime,
                    movementLevel = _state.value.movementLevel,
                    stepsGoal = _state.value.stepsGoal ?: if (_state.value.movementLevel != null) 10000 else null,
                    dietType = _state.value.dietType,
                    heightInches = _state.value.heightInches,
                    weightKg = _state.value.weightKg,
                    goalWeightKg = _state.value.goalWeightKg,
                    healthConditions = _state.value.conditions.toList(),
                    eveningActivities = _state.value.evening.toList(),
                    reminders = _state.value.reminders,
                    targetCaloriesUser = cals,
                    proteinGUser = cals?.let { (it * p / 4).toInt() },
                    carbsGUser = cals?.let { (it * c / 4).toInt() },
                    fatGUser = cals?.let { (it * f / 9).toInt() },
                    dietPreference = dietPref
                )
                
                val insertResponse = RetrofitClient.apiService.insertPlanSetup(payload)
                if (insertResponse.isSuccessful) {
                    _state.value = _state.value.copy(isSaving = false, isSaved = true)
                } else {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = "Failed to save: ${insertResponse.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = "An error occurred: ${e.message}"
                )
            }
        }
    }

    private fun calculatePct(grams: Int?, factor: Int, totalCalories: Int?): Float? {
        if (grams == null || totalCalories == null || totalCalories == 0) return null
        return (grams * factor).toFloat() / totalCalories
    }

    fun setTargetCalories(calories: Int?) {
        _state.value = _state.value.copy(targetCalories = calories)
    }

    fun setMacroSplit(protein: Float, carbs: Float, fat: Float) {
        _state.value = _state.value.copy(
            proteinPct = protein,
            carbsPct = carbs,
            fatPct = fat
        )
    }
}
