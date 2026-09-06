package com.zivaa.app.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FamilyMember(
    val name: String,
    val phone: String,
    val relation: String,
    val isActive: Boolean = true
)

data class SetupState(
    val name: String = "",
    val dob: String = "",
    val gender: String = "Male",
    val email: String = "",
    val isOtpSent: Boolean = false,
    val isEmailVerified: Boolean = false,
    val selectedConditions: Set<String> = emptySet(),
    val selectedWearable: String = "Google Fit", // Default or None
    val familyMembers: List<FamilyMember> = emptyList(),
    val isAddingFamilyMember: Boolean = false,
    
    // Plan setup states
    val primaryFocus: String? = null,
    val wakeTime: String? = null,
    val movementLevel: String? = null,
    val dietType: String? = null,
    val heightInches: Int? = null,
    val weightKg: Int? = null,
    val goalWeightKg: Int? = null,
    val stepsGoal: Int = 3000,
    val evening: Set<String> = emptySet(),
    val reminders: String? = null,

    val isSubmitting: Boolean = false,
    val isSetupComplete: Boolean = false,
    val error: String? = null
)

class SetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    fun reset() {
        _state.value = SetupState()
    }

    init {
        // Listen for Deep Link auth successes
        viewModelScope.launch {
            RetrofitClient.authManager?.authEvents?.collect { success ->
                if (success) {
                    val userId = RetrofitClient.authManager?.getUserId()
                    if (userId != null) {
                        try {
                            val response = RetrofitClient.apiService.getPatient("eq.$userId")
                            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                // Existing user, bypass onboarding
                                _state.value = _state.value.copy(isSetupComplete = true, isSubmitting = false)
                            } else {
                                // New user authenticated, proceed to NameDobScreen
                                _state.value = _state.value.copy(isEmailVerified = true, isSubmitting = false)
                            }
                        } catch (e: Exception) {
                            // On failure, fall back to email verified step to let them continue setup
                            _state.value = _state.value.copy(isEmailVerified = true, isSubmitting = false)
                        }
                    } else {
                        _state.value = _state.value.copy(isEmailVerified = true, isSubmitting = false)
                    }
                } else {
                    reset()
                }
            }
        }
    }

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updateAboutYou(name: String, dob: String, gender: String) {
        _state.value = _state.value.copy(name = name, dob = dob, gender = gender)
    }

    fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            try {
                val url = "https://ecwueqoxfjcepktubqrs.supabase.co/auth/v1/authorize?provider=google&redirect_to=zivaa://auth"
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                context.startActivity(intent)
                // We do not set isSubmitting = false here because the user is leaving the app.
                // The loading state will remain until they return and authEvents emits true.
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Could not open browser: ${e.message}", isSubmitting = false)
            }
        }
    }

    fun toggleCondition(condition: String) {
        val current = _state.value.selectedConditions.toMutableSet()
        if (current.contains(condition)) {
            current.remove(condition)
        } else {
            current.add(condition)
        }
        _state.value = _state.value.copy(selectedConditions = current)
    }

    fun selectWearable(wearable: String) {
        _state.value = _state.value.copy(selectedWearable = wearable)
    }

    fun addFamilyMember(member: FamilyMember) {
        val current = _state.value.familyMembers.toMutableList()
        current.add(member)
        _state.value = _state.value.copy(familyMembers = current, isAddingFamilyMember = false)
    }

    fun toggleFamilyMemberActive(member: FamilyMember) {
        val current = _state.value.familyMembers.toMutableList()
        val index = current.indexOf(member)
        if (index != -1) {
            current[index] = member.copy(isActive = !member.isActive)
            _state.value = _state.value.copy(familyMembers = current)
        }
    }

    fun setIsAddingFamilyMember(isAdding: Boolean) {
        _state.value = _state.value.copy(isAddingFamilyMember = isAdding)
    }

    // Plan setup mutators
    fun updatePrimaryFocus(focus: String?) {
        _state.value = _state.value.copy(primaryFocus = focus)
    }
    
    fun updateWakeTime(time: String?) {
        _state.value = _state.value.copy(wakeTime = time)
    }
    
    fun updateMovementLevel(level: String?) {
        _state.value = _state.value.copy(movementLevel = level)
    }
    
    fun updateDietType(diet: String?) {
        _state.value = _state.value.copy(dietType = diet)
    }
    
    fun updateHeight(inches: Int?) {
        _state.value = _state.value.copy(heightInches = inches)
    }
    
    fun updateWeight(kg: Int?) {
        _state.value = _state.value.copy(weightKg = kg)
    }

    fun updateGoalWeight(kg: Int?) {
        _state.value = _state.value.copy(goalWeightKg = kg)
    }

    fun updateStepsGoal(steps: Int) {
        _state.value = _state.value.copy(stepsGoal = steps)
    }
    
    fun toggleEvening(opt: String) {
        val current = _state.value.evening.toMutableSet()
        if (current.contains(opt)) current.remove(opt) else current.add(opt)
        _state.value = _state.value.copy(evening = current)
    }
    
    fun updateReminders(reminders: String?) {
        _state.value = _state.value.copy(reminders = reminders)
    }

    fun finishSetup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            try {
                val bypassMode = _state.value.email == "test@zivaa.app"
                
                // 1. Create Patient using authenticated user's ID
                val userId = RetrofitClient.authManager?.getUserId() ?: if (bypassMode) java.util.UUID.randomUUID().toString() else null
                
                if (userId == null) {
                    _state.value = _state.value.copy(error = "User not authenticated", isSubmitting = false)
                    return@launch
                }

                if (bypassMode) {
                    // Simulate success to avoid Supabase RLS rejection since we don't have a real JWT
                    kotlinx.coroutines.delay(1000)
                    _state.value = _state.value.copy(isSetupComplete = true, isSubmitting = false)
                    return@launch
                }

                val patientRecord = PatientRecord(
                    id = userId,
                    fullName = _state.value.name,
                    dateOfBirth = _state.value.dob.ifBlank { null },
                    phone = _state.value.email,
                    wearables = _state.value.selectedWearable
                )
                val patientResponse = RetrofitClient.apiService.createPatient(patientRecord)
                
                if (patientResponse.isSuccessful && patientResponse.body()?.isNotEmpty() == true) {
                    val patientId = patientResponse.body()!![0].id ?: return@launch
                    
                    // 2. Add Conditions
                    if (_state.value.selectedConditions.isNotEmpty()) {
                        val conditions = _state.value.selectedConditions.map { 
                            ConditionRecord(patientId = patientId, conditionName = it) 
                        }
                        RetrofitClient.apiService.addConditions(conditions)
                    }

                    // 3. Add Caregivers
                    if (_state.value.familyMembers.isNotEmpty()) {
                        val caregivers = _state.value.familyMembers.map {
                            CaregiverRecord(
                                patientId = patientId,
                                name = it.name,
                                phone = it.phone,
                                relation = it.relation
                            )
                        }
                        RetrofitClient.apiService.addCaregivers(caregivers)
                    }

                    // 4. Removed Doctor step as per requirements

                    _state.value = _state.value.copy(isSetupComplete = true, isSubmitting = false)
                } else {
                    _state.value = _state.value.copy(
                        error = "Failed to create profile: ${patientResponse.errorBody()?.string()}",
                        isSubmitting = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isSubmitting = false)
            }
        }
    }
}
