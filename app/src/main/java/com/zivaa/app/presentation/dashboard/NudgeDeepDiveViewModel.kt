package com.zivaa.app.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.RetrofitClient
import com.zivaa.app.data.remote.SupabaseNudgeAlert
import com.zivaa.app.data.remote.SupabaseDailyVitalRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class VitalGraphData(
    val title: String,
    val unit: String,
    val points: List<Pair<String, Double?>>, // Pair of (Date String, Value)
    val colorTone: String, // "rose" or "amber"
    val concernIndex: Int? = null
)

class NudgeDeepDiveViewModel(
    val alert: SupabaseNudgeAlert?
) : ViewModel() {

    private val _graphs = MutableStateFlow<List<VitalGraphData>>(emptyList())
    val graphs: StateFlow<List<VitalGraphData>> = _graphs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun acknowledgeAlert() {
        val currentAlert = alert ?: return
        val authManager = RetrofitClient.authManager
        val userId = authManager?.getUserId() ?: "unknown"
        val timestamp = java.time.Instant.now().toString()

        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "acknowledged" to true,
                    "acknowledged_by" to userId,
                    "acknowledged_at" to timestamp
                )
                val response = RetrofitClient.apiService.updateNudgeAlert("eq.${currentAlert.id}", updates)
                if (!response.isSuccessful) {
                    Log.e("NudgeDeepDive", "API Error: ${response.code()} ${response.errorBody()?.string()}")
                } else {
                    Log.d("NudgeDeepDive", "Successfully acknowledged nudge: ${response.body()}")
                }
            } catch (e: Exception) {
                Log.e("NudgeDeepDive", "Error acknowledging alert", e)
            }
        }
    }

    init {
        fetchHistoricalData()
    }

    private fun fetchHistoricalData() {
        if (alert == null || alert.evidence == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                // Fetch daily vitals dynamically for the last 7 days
                val response = RetrofitClient.apiService.getDailyVitalsDynamic(
                    patientIdQuery = "eq.${alert.patient_id}",
                    limit = 7
                )

                val generatedGraphs = mutableListOf<VitalGraphData>()
                // Filter out non-vital keys
                val keys = alert.evidence.keys.filter { 
                    it.lowercase() !in listOf("message", "rule", "id", "history") 
                }
                
                val isHighRisk = alert.risk_level.equals("HIGH", ignoreCase = true)
                val themeColor = if (isHighRisk) "rose" else "amber"
                
                if (response.isSuccessful && response.body() != null) {
                    val dailyVitals = response.body()!!.sortedBy { it["date"].toString() }
                    Log.d("NudgeDeepDive", "Fetched ${dailyVitals.size} dynamic daily vitals records")

                    val maxDateStr = dailyVitals.mapNotNull { it["date"]?.toString()?.substringBefore("T") }.maxOrNull()
                    val endDate = if (maxDateStr != null) {
                        try {
                            LocalDate.parse(maxDateStr)
                        } catch (e: Exception) {
                            LocalDate.now()
                        }
                    } else {
                        LocalDate.now()
                    }
                    val dateRange = (6 downTo 0).map { endDate.minusDays(it.toLong()) }

                    for (key in keys) {
                        val points = dateRange.map { date ->
                            val dateStr = date.toString()
                            val record = dailyVitals.find { it["date"]?.toString()?.startsWith(dateStr) == true }
                            
                            val dbKey = if (record != null) {
                                when {
                                    record.containsKey(key) -> key
                                    record.containsKey(key + "_avg") -> key + "_avg"
                                    record.containsKey("avg_" + key) -> "avg_" + key
                                    else -> null
                                }
                            } else null
                            
                            val value = if (dbKey != null) record!![dbKey] else null
                            val formattedDate = date.format(DateTimeFormatter.ofPattern("MMM d"))
                            val doubleValue = value?.toString()?.toDoubleOrNull()
                            
                            Pair(formattedDate, doubleValue)
                        }
                        
                        // Check if there is ANY data
                        if (points.any { it.second != null }) {
                            // Find the data point that triggered the alert (by matching the evidence value)
                            val evidenceValue = alert.evidence[key]?.toString()?.toDoubleOrNull()
                            var concernIdx: Int? = null
                            if (evidenceValue != null) {
                                concernIdx = points.indexOfLast { it.second != null && Math.abs(it.second!! - evidenceValue) < 0.02 }
                                if (concernIdx == -1) concernIdx = null
                            }

                            // Convert the key to a human-readable title, similar to the pills
                            val formattedTitle = when (key.lowercase()) {
                                "sleep_stage_1_hours" -> "Awake / Light Sleep"
                                "sleep_stage_2_hours" -> "Light Sleep"
                                "sleep_stage_3_hours", "sleep_stage_4_hours" -> "Deep Sleep"
                                "sleep_stage_5_hours" -> "REM Sleep"
                                "sleep_hours" -> "Total Sleep"
                                "oxygen_sat", "blood_oxygen", "spo2", "oxygen_sat_avg" -> "Oxygen Sat"
                                "hr_resting", "resting_heart_rate", "avg_heart_rate" -> "Resting HR"
                                "heart_rate" -> "Heart Rate"
                                else -> key.replace("_", " ").split(" ").joinToString(" ") { 
                                    it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString() } 
                                }
                            }
                            
                            // Determine units
                            val unit = when {
                                key.contains("hours") || key.contains("sleep") -> "hrs"
                                key.contains("oxygen") || key.contains("spo2") || key.contains("sat") -> "%"
                                key.contains("heart_rate") || key.contains("hr") -> "bpm"
                                key.contains("respirat") -> "rpm"
                                key.contains("step") -> "steps"
                                key.contains("calor") -> "kcal"
                                key.contains("weight") -> "kg"
                                else -> ""
                            }
                            
                            generatedGraphs.add(VitalGraphData(formattedTitle, unit, points, themeColor, concernIdx))
                        }
                    }
                }
                
                // If keys didn't match anything, provide a default fallback
                if (generatedGraphs.isEmpty()) {
                    generatedGraphs.add(VitalGraphData("Blood Oxygen", "%", listOf(
                        Pair("Jul 11", 95.0), Pair("Jul 12", 95.0), Pair("Jul 13", 96.0),
                        Pair("Jul 14", 95.0), Pair("Jul 15", 96.0), Pair("Jul 16", 95.5),
                        Pair("Jul 17", 82.0)
                    ), themeColor))
                }

                _graphs.value = generatedGraphs
                _isLoading.value = false

            } catch (e: Exception) {
                Log.e("NudgeDeepDive", "Error fetching data", e)
                
                val isHighRisk = alert.risk_level.equals("HIGH", ignoreCase = true)
                val themeColor = if (isHighRisk) "rose" else "amber"
                
                // Set fallback graphs if API fails
                _graphs.value = listOf(
                    VitalGraphData("Blood Oxygen", "%", listOf(
                        Pair("Jul 11", 95.0), Pair("Jul 12", 95.0), Pair("Jul 13", 96.0),
                        Pair("Jul 14", 95.0), Pair("Jul 15", 96.0), Pair("Jul 16", 95.5),
                        Pair("Jul 17", 82.0)
                    ), themeColor),
                    VitalGraphData("Heart Rate", "bpm", listOf(
                        Pair("Jul 11", 72.0), Pair("Jul 12", 69.9), Pair("Jul 13", 75.1),
                        Pair("Jul 14", 70.1), Pair("Jul 15", 101.3), Pair("Jul 16", 91.6),
                        Pair("Jul 17", 68.5)
                    ), themeColor)
                )
                _isLoading.value = false
            }
        }
    }
}

class NudgeDeepDiveViewModelFactory(
    private val alert: SupabaseNudgeAlert?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NudgeDeepDiveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NudgeDeepDiveViewModel(alert) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
