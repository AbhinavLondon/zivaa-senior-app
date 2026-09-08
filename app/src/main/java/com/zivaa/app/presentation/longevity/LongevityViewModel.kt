package com.zivaa.app.presentation.longevity

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.model.LongevityProtocol
import com.zivaa.app.data.model.PatientSymptom
import com.zivaa.app.data.model.UpdateSymptomPayload
import com.zivaa.app.data.remote.ZivaaBackendClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LongevityViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Main state
    var protocols by mutableStateOf<List<LongevityProtocol>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Grouped for UI
    var groupedProtocols by mutableStateOf<Map<String, List<LongevityProtocol>>>(emptyMap())
    
    // Progress
    var totalTasks by mutableStateOf(0)
    var completedTasks by mutableStateOf(0)
    
    // Symptoms
    var activeSymptoms by mutableStateOf<List<PatientSymptom>>(emptyList())
    var resolvedSymptoms by mutableStateOf<List<PatientSymptom>>(emptyList())

    fun fetchTodayProtocols(patientId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch Plan
                val planRes = ZivaaBackendClient.apiService.getLongevityPlan(patientId)
                if (planRes.isSuccessful && planRes.body() != null) {
                    val symptoms = planRes.body()?.symptoms ?: emptyList()
                    activeSymptoms = symptoms.filter { it.status == "Active" || it.status == "Resolving" || it.status == "Worse" || it.status == "Chronic" }
                    resolvedSymptoms = symptoms.filter { it.status == "Resolved" }
                }

                // Fetch Daily Checklist
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH))
                val response = ZivaaBackendClient.apiService.getLongevityProtocols(
                    patientId = patientId,
                    date = today
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val rawProtocols = response.body()?.protocols ?: emptyList()
                    protocols = rawProtocols
                    recalculateDerivedState()
                } else {
                    errorMessage = "Failed to load protocols"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An unexpected error occurred"
                Log.e("LongevityVM", "Error fetching data", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun recalculateDerivedState() {
        totalTasks = protocols.size
        completedTasks = protocols.count { it.isCompleted }
        groupedProtocols = protocols.groupBy { it.displayCategory }
    }

    fun toggleProtocolCompletion(patientId: String, protocolId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                // Optimistic UI update
                protocols = protocols.map { 
                    if (it.safeId == protocolId) it.copy(isCompleted = !currentStatus) else it 
                }
                recalculateDerivedState()
                
                // Sync with backend
                ZivaaBackendClient.apiService.completeLongevityProtocol(
                    patientId = patientId,
                    protocolId = protocolId
                )
            } catch (e: Exception) {
                Log.e("LongevityVM", "Error toggling protocol", e)
                // Revert on error
                protocols = protocols.map { 
                    if (it.safeId == protocolId) it.copy(isCompleted = currentStatus) else it 
                }
                recalculateDerivedState()
            }
        }
    }
    
    fun updateSymptom(patientId: String, symptomId: String, status: String, note: String?) {
        viewModelScope.launch {
            try {
                val response = ZivaaBackendClient.apiService.updateSymptom(
                    symptomId, 
                    UpdateSymptomPayload(status = status, progression_note = note)
                )
                if (response.isSuccessful) {
                    fetchTodayProtocols(patientId)
                } else {
                    Log.e("LongevityVM", "Error updating symptom: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LongevityVM", "Exception updating symptom", e)
            }
        }
    }
}

class LongevityViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LongevityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LongevityViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


