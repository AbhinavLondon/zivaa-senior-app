package com.zivaa.app.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ProfileState(
    val isLoading: Boolean = true,
    val patientId: String = "",
    val fullName: String = "User",
    val locationCity: String = "",
    val age: String = "",
    val withZivaa: String = "",
    val profilePicUrl: String? = null,
    val conditions: List<ConditionRecord> = emptyList(),
    val caregivers: List<CaregiverRecord> = emptyList(),
    val wearables: List<String> = emptyList(),
    val batteryLevel: Int? = null,
    val caregiverNudgePreference: String = "HIGH",
    val morningReportsCount: Int = 0,
    val homeVisitsCount: Int = 0,
    val doctorCallsCount: Int = 0,
    val sosResolvedCount: Int = 0,
    val documentsCount: Int = 0,
    val error: String? = null
)

class ProfileViewModel(
    private val authManager: AuthManager,
    private val appSettingsManager: com.zivaa.app.data.local.AppSettingsManager,
    private val syncPrefsManager: com.zivaa.app.data.local.SyncPrefsManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    val darkThemeEnabled: StateFlow<Boolean> = appSettingsManager.darkThemeFlow
    val showLongevityPlanEnabled: StateFlow<Boolean> = appSettingsManager.showLongevityPlanFlow

    init {
        loadProfile()
    }

    fun setDarkTheme(enabled: Boolean) {
        appSettingsManager.setDarkTheme(enabled)
    }
    
    fun setShowLongevityPlan(enabled: Boolean) {
        appSettingsManager.setShowLongevityPlan(enabled)
    }

    fun loadProfile() {
        val userId = authManager.getUserId()
        if (userId == null) {
            _state.value = ProfileState(error = "User not logged in", isLoading = false)
            return
        }

        // If the user changed or state was empty, clear out previous profile data immediately
        if (_state.value.patientId != userId) {
            _state.value = ProfileState(patientId = userId, isLoading = true)
        }

        // 1. Load from Cache first for immediate display
        val cached = authManager.getPatientProfile()
        val fullName = cached["full_name"] ?: "User"
        val locationCity = cached["location_city"] ?: ""
        val dob = cached["dob"]
        val createdAt = cached["created_at"]
        val profilePicUrl = cached["profile_pic_url"]
        val wearablesStr = cached["wearables"] ?: ""
        val wearablesList = if (wearablesStr.isNotBlank()) wearablesStr.split(",").map { it.trim() } else emptyList()

        _state.value = _state.value.copy(
            patientId = userId,
            fullName = fullName,
            locationCity = locationCity,
            age = calculateAge(dob),
            withZivaa = calculateWithZivaa(createdAt),
            profilePicUrl = profilePicUrl,
            wearables = wearablesList,
            isLoading = true
        )

        // 2. Fetch fresh from network
        viewModelScope.launch {
            try {
                // Fetch Patient
                val patientRes = RetrofitClient.apiService.getPatient("eq.$userId")
                if (patientRes.isSuccessful && patientRes.body()?.isNotEmpty() == true) {
                    val patient = patientRes.body()!![0]
                    
                    val freshName = patient.fullName
                    val freshLocation = patient.locationCity ?: ""
                    val freshDob = patient.dateOfBirth
                    val freshCreatedAt = patient.createdAt
                    val freshPic = patient.profilePicUrl
                    val freshWearables = patient.wearables ?: ""

                    // Update Cache
                    authManager.savePatientProfile(freshName, freshLocation, freshCreatedAt, freshDob, freshPic)
                    // (We don't need to manually save wearables string to authManager unless we modify authManager signature, which we can skip for brevity)

                    val freshWearablesList = if (freshWearables.isNotBlank()) freshWearables.split(",").map { it.trim() } else emptyList()
                    val freshNudgePref = patient.caregiverNudgePreference ?: "HIGH"

                    _state.value = _state.value.copy(
                        fullName = freshName,
                        locationCity = freshLocation,
                        age = calculateAge(freshDob),
                        withZivaa = calculateWithZivaa(freshCreatedAt),
                        profilePicUrl = freshPic,
                        wearables = freshWearablesList,
                        caregiverNudgePreference = freshNudgePref
                    )
                }

                // Fetch Conditions
                val conditionsRes = RetrofitClient.apiService.getConditions("eq.$userId")
                if (conditionsRes.isSuccessful) {
                    _state.value = _state.value.copy(conditions = conditionsRes.body() ?: emptyList())
                }

                // Fetch Caregivers
                val caregiversRes = RetrofitClient.apiService.getCaregivers("eq.$userId")
                if (caregiversRes.isSuccessful) {
                    _state.value = _state.value.copy(caregivers = caregiversRes.body() ?: emptyList())
                }

                // Fetch Battery level from device_status in vitals_raw
                try {
                    val vitalsRes = RetrofitClient.apiService.getRawVitals(patientIdQuery = "eq.$userId", metricTypeQuery = "eq.device_status")
                    if (vitalsRes.isSuccessful && vitalsRes.body()?.isNotEmpty() == true) {
                        val latestStatus = vitalsRes.body()!!.first()
                        val batteryLvl = (latestStatus.values["battery_level"] as? Number)?.toInt()
                        if (batteryLvl != null) {
                            _state.value = _state.value.copy(batteryLevel = batteryLvl)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore battery fetch error
                }

                // Fetch Morning Briefings Count (distinct morning briefings generated for this user)
                try {
                    val briefingsRes = RetrofitClient.apiService.getMorningBriefings("eq.$userId")
                    if (briefingsRes.isSuccessful) {
                        val briefings = briefingsRes.body() ?: emptyList()
                        val distinctCount = briefings.mapNotNull {
                            it.date?.take(10) ?: it.created_at?.take(10) ?: it.id
                        }.distinct().size
                        _state.value = _state.value.copy(morningReportsCount = distinctCount)
                    }
                } catch (e: Exception) {
                    // Ignore error, keep default 0
                }

                // Fetch Diagnostic Reports / Documents Count
                try {
                    val docsRes = RetrofitClient.apiService.getDiagnosticReports("eq.$userId", select = "id")
                    if (docsRes.isSuccessful) {
                        val count = docsRes.body()?.size ?: 0
                        _state.value = _state.value.copy(documentsCount = count)
                    }
                } catch (e: Exception) {
                    // Ignore error, keep default 0
                }

                _state.value = _state.value.copy(isLoading = false, error = null)

            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    private fun calculateAge(dobStr: String?): String {
        if (dobStr.isNullOrEmpty()) return ""
        try {
            val dob = LocalDate.parse(dobStr.substringBefore("T"))
            val age = ChronoUnit.YEARS.between(dob, LocalDate.now())
            return "$age years"
        } catch (e: Exception) {
            return ""
        }
    }

    private fun calculateWithZivaa(createdAtStr: String?): String {
        if (createdAtStr.isNullOrEmpty()) return ""
        try {
            val createdDate = LocalDate.parse(createdAtStr.substringBefore("T"))
            val now = LocalDate.now()
            val days = ChronoUnit.DAYS.between(createdDate, now)
            
            return when {
                days < 30 -> "$days days"
                days < 365 -> "${days / 30} months"
                else -> {
                    val years = days / 365
                    if (years == 1L) "1 year" else "$years years"
                }
            }
        } catch (e: Exception) {
            return ""
        }
    }

    fun uploadProfilePicture(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val userId = _state.value.patientId
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                
                val reqBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val fileName = "$userId-profile.jpg"
                
                val res = RetrofitClient.apiService.uploadProfilePicture(fileName, reqBody)
                if (res.isSuccessful) {
                    // Update Patient table with new URL, adding a timestamp to bust Coil cache
                    val picUrl = "https://ecwueqoxfjcepktubqrs.supabase.co/storage/v1/object/public/profile_pictures/$fileName?t=${System.currentTimeMillis()}"
                    val updateRes = RetrofitClient.apiService.updatePatient("eq.$userId", mapOf("profile_pic_url" to picUrl))
                    if (updateRes.isSuccessful) {
                        _state.value = _state.value.copy(profilePicUrl = picUrl, error = null)
                        
                        // Update cache
                        val cached = authManager.getPatientProfile()
                        authManager.savePatientProfile(
                            cached["full_name"] ?: "User",
                            cached["location_city"],
                            cached["created_at"],
                            cached["dob"],
                            picUrl
                        )
                    } else {
                        _state.value = _state.value.copy(error = "Failed to update profile: ${updateRes.errorBody()?.string()}")
                    }
                } else {
                    _state.value = _state.value.copy(error = "Upload failed: ${res.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Upload failed: ${e.message}")
            }
        }
    }

    fun addConditions(conditionNames: List<String>) {
        if (conditionNames.isEmpty()) return
        viewModelScope.launch {
            try {
                val newConditions = conditionNames.map { name ->
                    ConditionRecord(patientId = _state.value.patientId, conditionName = name)
                }
                val res = RetrofitClient.apiService.addConditions(newConditions)
                if (res.isSuccessful) {
                    // Refresh
                    val freshRes = RetrofitClient.apiService.getConditions("eq.${_state.value.patientId}")
                    if (freshRes.isSuccessful) {
                        _state.value = _state.value.copy(conditions = freshRes.body() ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun deleteCondition(condition: ConditionRecord) {
        viewModelScope.launch {
            try {
                if (condition.id == null) return@launch
                val res = RetrofitClient.apiService.deleteCondition("eq.${condition.id}", "eq.${_state.value.patientId}")
                if (res.isSuccessful) {
                    val updatedList = _state.value.conditions.filter { it.id != condition.id }
                    _state.value = _state.value.copy(conditions = updatedList)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun addCaregiver(name: String, relation: String, phone: String, city: String, role: String = "primary") {
        viewModelScope.launch {
            try {
                val newCaregiver = CaregiverRecord(
                    patientId = _state.value.patientId,
                    name = name,
                    relation = relation,
                    phone = phone,
                    city = city.ifBlank { null },
                    role = role
                )
                val res = RetrofitClient.apiService.addCaregivers(listOf(newCaregiver))
                if (res.isSuccessful) {
                    val freshRes = RetrofitClient.apiService.getCaregivers("eq.${_state.value.patientId}")
                    if (freshRes.isSuccessful) {
                        _state.value = _state.value.copy(caregivers = freshRes.body() ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun toggleCaregiverAlerts(caregiver: CaregiverRecord) {
        viewModelScope.launch {
            try {
                if (caregiver.id == null) return@launch
                val newAlerts = !caregiver.receivesAlerts
                
                val updates = mapOf<String, Any>("receives_alerts" to newAlerts)
                val res = RetrofitClient.apiService.updateCaregiver("eq.${caregiver.id}", "eq.${_state.value.patientId}", updates)
                if (res.isSuccessful) {
                    val currentList = _state.value.caregivers.toMutableList()
                    val idx = currentList.indexOfFirst { it.id == caregiver.id }
                    if (idx != -1) {
                        currentList[idx] = caregiver.copy(receivesAlerts = newAlerts)
                        _state.value = _state.value.copy(caregivers = currentList)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val res = RetrofitClient.apiService.deleteUserAccount()
                if (res.isSuccessful) {
                    signOut()
                } else {
                    _state.value = _state.value.copy(
                        error = "Failed to delete account: ${res.errorBody()?.string()}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Delete account failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun signOut() {
        _state.value = ProfileState()
        authManager.clearSession()
        syncPrefsManager.setSetupComplete(false)
    }

    fun updateNudgePreference(pref: String) {
        val userId = authManager.getUserId() ?: return
        _state.value = _state.value.copy(caregiverNudgePreference = pref)
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.updatePatient("eq.$userId", mapOf("caregiver_nudge_preference" to pref))
            } catch (e: Exception) {
                // Ignore error for now
            }
        }
    }
}

class ProfileViewModelFactory(
    private val authManager: AuthManager,
    private val appSettingsManager: com.zivaa.app.data.local.AppSettingsManager,
    private val syncPrefsManager: com.zivaa.app.data.local.SyncPrefsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authManager, appSettingsManager, syncPrefsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
