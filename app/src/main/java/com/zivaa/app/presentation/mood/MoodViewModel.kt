package com.zivaa.app.presentation.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.remote.AuthManager
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabasePatientCheckin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MoodScreenState(
    val isLoading: Boolean = false,
    val checkins: List<SupabasePatientCheckin> = emptyList()
)

class MoodViewModel(
    private val authManager: AuthManager,
    private val prefsManager: SyncPrefsManager
) : ViewModel() {

    private val _state = MutableStateFlow(MoodScreenState())
    val state: StateFlow<MoodScreenState> = _state.asStateFlow()

    init {
        loadCachedCheckins()
    }

    private fun loadCachedCheckins() {
        try {
            val patientId = authManager.getUserId()
            val cachedJson = prefsManager.getCachedPatientCheckins(patientId)
            if (!cachedJson.isNullOrBlank()) {
                val listType = object : TypeToken<List<SupabasePatientCheckin>>() {}.type
                val list: List<SupabasePatientCheckin>? = Gson().fromJson(cachedJson, listType)
                if (!list.isNullOrEmpty()) {
                    _state.value = MoodScreenState(checkins = list, isLoading = false)
                    android.util.Log.i("MoodViewModel", "Loaded ${list.size} checkins from local cache (0ms hydration)")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MoodViewModel", "Error loading cached checkins", e)
        }
    }

    fun fetchCheckins() {
        viewModelScope.launch {
            val patientId = authManager.getUserId()
            if (patientId != null) {
                try {
                    // Only show spinner if we don't already have checkins in state
                    if (_state.value.checkins.isEmpty()) {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    val response = RetrofitClient.apiService.getPatientCheckins(patientIdQuery = "eq.$patientId")
                    if (response.isSuccessful && response.body() != null) {
                        val checkins = response.body()!!
                        _state.value = _state.value.copy(
                            checkins = checkins,
                            isLoading = false
                        )
                        prefsManager.saveCachedPatientCheckins(Gson().toJson(checkins), patientId)
                    } else {
                        android.util.Log.e("MoodViewModel", "Failed to fetch checkins: ${response.code()}")
                        _state.value = _state.value.copy(isLoading = false)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("MoodViewModel", "Network error fetching checkins (offline). Retaining local cache.", e)
                    _state.value = _state.value.copy(isLoading = false)
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun getUserName(): String {
        val fullName = authManager.getPatientProfile()["full_name"] ?: ""
        return fullName.split(" ").firstOrNull()?.takeIf { it.isNotBlank() } ?: "there"
    }

    fun saveCheckIn(moodLabel: String, emotions: Set<String>, causes: Set<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val patientId = authManager.getUserId()
            if (patientId != null) {
                val checkin = SupabasePatientCheckin(
                    patient_id = patientId,
                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    checkin_type = "SELF_INITIATED",
                    mood_label = moodLabel,
                    emotions = emotions.toList(),
                    causes = causes.toList(),
                    user_comments = null
                )
                
                // 1. Optimistic UI update immediately
                val updatedCheckins = listOf(checkin) + _state.value.checkins.filterNot { it.date == checkin.date }
                _state.value = _state.value.copy(checkins = updatedCheckins)
                
                // 2. Persist to local disk cache immediately
                val gson = Gson()
                val checkinJson = gson.toJson(checkin)
                prefsManager.saveCachedPatientCheckins(gson.toJson(updatedCheckins), patientId)
                
                // 3. Queue into offline outbox
                prefsManager.addPendingPatientCheckin(checkinJson, patientId)
                
                try {
                    val response = RetrofitClient.apiService.insertPatientCheckin(checkin)
                    if (response.isSuccessful) {
                        android.util.Log.i("MoodViewModel", "Successfully synced checkin to Supabase")
                        prefsManager.removePendingPatientCheckin(checkinJson, patientId)
                    } else {
                        android.util.Log.e("MoodViewModel", "Failed to insert checkin: ${response.code()}, kept in pending outbox")
                        com.zivaa.app.data.health.worker.DailyPlanSyncWorker.enqueue(prefsManager.context, patientId)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("MoodViewModel", "Network error inserting checkin (offline), kept in pending outbox", e)
                    com.zivaa.app.data.health.worker.DailyPlanSyncWorker.enqueue(prefsManager.context, patientId)
                }
            }
            onComplete()
        }
    }
}
