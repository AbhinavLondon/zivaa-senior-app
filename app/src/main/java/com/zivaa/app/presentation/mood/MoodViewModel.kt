package com.zivaa.app.presentation.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.AuthManager
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabasePatientCheckin
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MoodScreenState(
    val isLoading: Boolean = false,
    val checkins: List<SupabasePatientCheckin> = emptyList()
)

class MoodViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(MoodScreenState())
    val state: StateFlow<MoodScreenState> = _state.asStateFlow()

    fun fetchCheckins() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val patientId = authManager.getUserId()
            if (patientId != null) {
                try {
                    val response = RetrofitClient.apiService.getPatientCheckins(patientIdQuery = "eq.$patientId")
                    if (response.isSuccessful) {
                        _state.value = _state.value.copy(
                            checkins = response.body() ?: emptyList(),
                            isLoading = false
                        )
                    } else {
                        android.util.Log.e("MoodViewModel", "Failed to fetch checkins: ${response.code()}")
                        _state.value = _state.value.copy(isLoading = false)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MoodViewModel", "Network error", e)
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
                // The checkin type should capture whether the user updated it themselves or via a prompt.
                // For now, since they launched the UI themselves from the Dashboard or Mood screen, we'll use SELF_INITIATED.
                // Later, we can pass this as an argument if they clicked a notification push.
                val checkin = SupabasePatientCheckin(
                    patient_id = patientId,
                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    checkin_type = "SELF_INITIATED",
                    mood_label = moodLabel,
                    emotions = emotions.toList(),
                    causes = causes.toList(),
                    user_comments = null
                )
                
                try {
                    val response = RetrofitClient.apiService.insertPatientCheckin(checkin)
                    if (!response.isSuccessful) {
                        android.util.Log.e("MoodViewModel", "Failed to insert checkin: ${response.code()} ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MoodViewModel", "Network error", e)
                }
            }
            onComplete()
        }
    }
}
