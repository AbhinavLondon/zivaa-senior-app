package com.zivaa.app.presentation.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.local.AppSettingsManager
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
    val preferredLanguage: String = "English",
    val smokingStatus: String = "Non-smoker",
    val alcoholStatus: String = "Never / Teetotaler",
    val isOtpSent: Boolean = false,
    val isEmailVerified: Boolean = false,
    val selectedConditions: Set<String> = emptySet(),
    val selectedWearable: String = "Google Health Connect", // Default or None
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

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val appSettingsManager = AppSettingsManager(application)
    private val _state = MutableStateFlow(SetupState(preferredLanguage = appSettingsManager.preferredLanguageFlow.value))
    val state: StateFlow<SetupState> = _state.asStateFlow()

    fun reset() {
        _state.value = SetupState()
    }

    fun startFreshEnrollment() {
        RetrofitClient.authManager?.clearSession()
        _state.value = SetupState()
    }

    init {
        // Check if session is already active
        checkExistingSession()

        // Listen for Deep Link auth successes
        viewModelScope.launch {
            AuthManager.authEvents.collect { success ->
                if (success) {
                    handleAuthSuccess()
                } else {
                    reset()
                }
            }
        }
    }

    fun checkExistingSession() {
        val auth = RetrofitClient.authManager
        if (auth != null && auth.hasValidSession()) {
            viewModelScope.launch {
                handleAuthSuccess()
            }
        }
    }

    private suspend fun handleAuthSuccess() {
        val auth = RetrofitClient.authManager
        if (auth == null || !auth.hasValidSession()) {
            return
        }
        val userId = auth.getUserId() ?: return
        try {
            // Verify that the token actually belongs to an existing user in Supabase Auth
            val authUserResponse = RetrofitClient.apiService.getCurrentUser()
            if (!authUserResponse.isSuccessful) {
                auth.clearSession()
                _state.value = _state.value.copy(isEmailVerified = false, isSubmitting = false)
                return
            }

            // Extract metadata from Supabase auth user
            val authBody = authUserResponse.body()
            val userMetadata = authBody?.get("user_metadata") as? Map<*, *>
            val metaName = (userMetadata?.get("full_name") ?: userMetadata?.get("name")) as? String
            val metaEmail = (authBody?.get("email") ?: userMetadata?.get("email")) as? String

            val response = RetrofitClient.apiService.getPatient("eq.$userId")
            val patient = response.body()?.firstOrNull()
            
            // A patient who actually completed onboarding will have dateOfBirth non-null.
            // The DB trigger creates a skeleton row with dateOfBirth = NULL.
            val hasCompletedOnboarding = patient != null && !patient.dateOfBirth.isNullOrBlank()
            
            val cachedName = auth.getPatientProfile()["full_name"]
            val resolvedName = when {
                _state.value.name.isNotBlank() -> _state.value.name
                !metaName.isNullOrBlank() -> metaName
                !patient?.fullName.isNullOrBlank() && patient.fullName != "New User" -> patient.fullName
                !cachedName.isNullOrBlank() -> cachedName
                else -> ""
            }

            val cachedEmail = auth.getUserEmail()
            val resolvedEmail = when {
                _state.value.email.isNotBlank() -> _state.value.email
                !metaEmail.isNullOrBlank() -> metaEmail
                !cachedEmail.isNullOrBlank() -> cachedEmail
                !patient?.phone.isNullOrBlank() && patient.phone.contains("@") -> patient.phone
                else -> ""
            }

            if (hasCompletedOnboarding && patient != null) {
                // Cache profile locally in AuthManager
                auth.savePatientProfile(
                    fullName = patient.fullName,
                    locationCity = patient.locationCity,
                    createdAt = patient.createdAt,
                    dob = patient.dateOfBirth,
                    profilePicUrl = patient.profilePicUrl
                )
                // Existing user who previously completed onboarding, bypass to dashboard
                _state.value = _state.value.copy(
                    isSetupComplete = true,
                    isSubmitting = false,
                    name = resolvedName,
                    email = resolvedEmail
                )
            } else {
                // Brand-new user or trigger-created skeleton row: continue onboarding with pre-filled name & email!
                _state.value = _state.value.copy(
                    isEmailVerified = true,
                    isSubmitting = false,
                    name = resolvedName,
                    email = resolvedEmail
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(isEmailVerified = true, isSubmitting = false)
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
        if (condition == "None") {
            if (current.contains("None")) {
                current.remove("None")
            } else {
                current.clear()
                current.add("None")
            }
        } else {
            current.remove("None")
            if (current.contains(condition)) {
                current.remove(condition)
            } else {
                current.add(condition)
            }
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

    fun updateLanguage(lang: String) {
        appSettingsManager.setPreferredLanguage(lang)
        _state.value = _state.value.copy(preferredLanguage = lang)
    }

    fun updateSmokingStatus(status: String) {
        _state.value = _state.value.copy(smokingStatus = status)
    }

    fun updateAlcoholStatus(status: String) {
        _state.value = _state.value.copy(alcoholStatus = status)
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

                val normalizedGender = when (_state.value.gender.trim().lowercase()) {
                    "male" -> "male"
                    "female" -> "female"
                    "other" -> "other"
                    else -> null
                }
                val patientRecord = PatientRecord(
                    id = userId,
                    fullName = _state.value.name.ifBlank { "User" },
                    dateOfBirth = _state.value.dob.ifBlank { null },
                    gender = normalizedGender,
                    phone = _state.value.email,
                    wearables = _state.value.selectedWearable,
                    preferredLanguage = _state.value.preferredLanguage
                )
                val patientResponse = RetrofitClient.apiService.createPatient(patientRecord)
                
                if (patientResponse.isSuccessful && patientResponse.body()?.isNotEmpty() == true) {
                    val patientId = patientResponse.body()!![0].id ?: userId
                    
                    // Cache profile locally in AuthManager
                    RetrofitClient.authManager?.savePatientProfile(
                        fullName = _state.value.name,
                        locationCity = null,
                        createdAt = null,
                        dob = _state.value.dob.ifBlank { null },
                        profilePicUrl = null
                    )

                    // 2. Add Conditions
                    if (_state.value.selectedConditions.isNotEmpty()) {
                        val conditions = _state.value.selectedConditions.map { 
                            ConditionRecord(patientId = patientId, conditionName = it) 
                        }
                        try {
                            RetrofitClient.apiService.addConditions(conditions)
                        } catch (e: Exception) {
                            android.util.Log.e("SetupViewModel", "Error adding conditions", e)
                        }
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
                        try {
                            RetrofitClient.apiService.addCaregivers(caregivers)
                        } catch (e: Exception) {
                            android.util.Log.e("SetupViewModel", "Error adding caregivers", e)
                        }
                    }

                    // 4. Save Plan Setup
                    try {
                        val planSetup = SupabasePatientPlanSetup(
                            patientId = patientId,
                            primaryFocus = _state.value.primaryFocus,
                            wakeTime = _state.value.wakeTime,
                            movementLevel = _state.value.movementLevel,
                            stepsGoal = _state.value.stepsGoal,
                            dietType = _state.value.dietType,
                            heightInches = _state.value.heightInches,
                            weightKg = _state.value.weightKg,
                            goalWeightKg = _state.value.goalWeightKg,
                            healthConditions = _state.value.selectedConditions.toList(),
                            eveningActivities = _state.value.evening.toList(),
                            reminders = _state.value.reminders,
                            smokingStatus = _state.value.smokingStatus,
                            alcoholStatus = _state.value.alcoholStatus
                        )
                        RetrofitClient.apiService.insertPlanSetup(planSetup)
                    } catch (e: Exception) {
                        android.util.Log.e("SetupViewModel", "Error saving plan setup", e)
                    }

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
