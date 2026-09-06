package com.zivaa.app.ui.labs.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.SupabaseApiService
import com.zivaa.app.data.remote.ZivaaApiService
import com.zivaa.app.data.remote.SupabaseFhirObservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BiomarkerUiModel(
    val headline: String,
    val badgeText: String,
    val badgeTone: String, // "good", "watch", "bad"
    val value: String,
    val unit: String,
    val hasReferenceRange: Boolean,
    val rangeText: String,
    val progress: Float?,
    val rangeStartProgress: Float?,
    val rangeEndProgress: Float?,
    val insight: String
)

data class LabSummaryState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val goodCount: Int = 0,
    val notSoGoodCount: Int = 0,
    val badCount: Int = 0,
    val biomarkers: List<BiomarkerUiModel> = emptyList(),
    val categorySummary: String? = null,
    val isSummaryLoading: Boolean = true
)

class LabSummaryViewModel(
    private val reportId: String,
    private val categoryName: String,
    private val supabaseApiService: SupabaseApiService,
    private val zivaaApiService: ZivaaApiService
) : ViewModel() {

    private val _state = MutableStateFlow(LabSummaryState())
    val state: StateFlow<LabSummaryState> = _state

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val obsResponse = supabaseApiService.getObservationsByReport("eq.$reportId")
                if (!obsResponse.isSuccessful) {
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to load data")
                    return@launch
                }
                
                val allObservations = obsResponse.body() ?: emptyList()
                
                // Filter by category
                val categoryObs = allObservations.filter { obs ->
                    val catDisplay = obs.resource?.category?.firstOrNull()?.coding?.firstOrNull()?.display ?: "Other"
                    catDisplay == categoryName
                }

                var good = 0
                var notSoGood = 0
                var bad = 0
                
                val uiModels = categoryObs.mapNotNull { obs ->
                    val resource = obs.resource ?: return@mapNotNull null
                    
                    val headline = resource.code?.text ?: "Unknown"
                    val interpretation = resource.interpretation?.firstOrNull()?.text ?: ""
                    
                    // Determine tone
                    val badgeTone = if (interpretation.isBlank() || interpretation.equals("normal", ignoreCase = true) || interpretation.lowercase().contains("within range")) {
                        good++
                        "good"
                    } else if (interpretation.lowercase().contains("high") || interpretation.lowercase().contains("low") || interpretation.lowercase().contains("out of range")) {
                        notSoGood++
                        "watch"
                    } else {
                        bad++
                        "bad"
                    }

                    val badgeText = interpretation.ifBlank { if (badgeTone == "good") "Good" else "Needs Attention" }
                    
                    val rawValue = resource.valueQuantity?.value?.takeIf { it != -99999.0 }
                    val valueObj = resource.valueQuantity
                    val value = rawValue?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } 
                        ?: obs.valueString?.takeIf { it.isNotBlank() } 
                        ?: "--"
                    val unit = valueObj?.unit ?: ""
                    
                    val refRange = resource.referenceRange?.firstOrNull()
                    val lowVal = refRange?.low?.value
                    val highVal = refRange?.high?.value
                    
                    val hasReferenceRange = lowVal != null || highVal != null
                    
                    var progress: Float? = null
                    var rangeStartProgress: Float? = null
                    var rangeEndProgress: Float? = null
                    var rangeText = ""
                    
                    if (hasReferenceRange) {
                        val currentVal = rawValue
                        if (currentVal != null) {
                            val safeLowVal = lowVal ?: (highVal!! * 0.5) // Fallback if no low
                            val safeHighVal = highVal ?: (lowVal!! * 1.5) // Fallback if no high
                            
                            // Visual track bounds: give it breathing room on both sides
                            val trackMin = maxOf(0.0, safeLowVal - (safeHighVal - safeLowVal))
                            val trackMax = safeHighVal + (safeHighVal - safeLowVal)
                            
                            val trackSpan = trackMax - trackMin
                            if (trackSpan > 0) {
                                rangeStartProgress = ((safeLowVal - trackMin) / trackSpan).toFloat().coerceIn(0f, 1f)
                                rangeEndProgress = ((safeHighVal - trackMin) / trackSpan).toFloat().coerceIn(0f, 1f)
                                progress = ((currentVal - trackMin) / trackSpan).toFloat().coerceIn(0f, 1f)
                            } else {
                                rangeStartProgress = 0f
                                rangeEndProgress = 1f
                                progress = 0.5f
                            }
                        }
                        
                        val lowText = lowVal?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: ""
                        val highText = highVal?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: ""
                        
                        rangeText = if (lowText.isNotEmpty() && highText.isNotEmpty()) {
                            "$lowText-$highText ${unit.uppercase()}"
                        } else if (lowText.isNotEmpty()) {
                            "> $lowText ${unit.uppercase()}"
                        } else if (highText.isNotEmpty()) {
                            "< $highText ${unit.uppercase()}"
                        } else {
                            ""
                        }
                    }

                    val insightText = resource.presentedForm?.firstOrNull()?.title ?: ""

                    BiomarkerUiModel(
                        headline = headline,
                        badgeText = badgeText,
                        badgeTone = badgeTone,
                        value = value,
                        unit = unit,
                        hasReferenceRange = hasReferenceRange,
                        rangeText = rangeText,
                        progress = progress,
                        rangeStartProgress = rangeStartProgress,
                        rangeEndProgress = rangeEndProgress,
                        insight = insightText
                    )
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    goodCount = good,
                    notSoGoodCount = notSoGood,
                    badCount = bad,
                    biomarkers = uiModels
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    private fun fetchCategorySummary() {
        viewModelScope.launch {
            try {
                val response = zivaaApiService.getCategorySummary(reportId, categoryName)
                if (response.isSuccessful) {
                    val summary = response.body()?.summary
                    _state.value = _state.value.copy(categorySummary = summary, isSummaryLoading = false)
                } else {
                    _state.value = _state.value.copy(isSummaryLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isSummaryLoading = false)
            }
        }
    }
    
    init {
        fetchData()
        fetchCategorySummary()
    }
}

class LabSummaryViewModelFactory(
    private val reportId: String,
    private val categoryName: String,
    private val supabaseApiService: SupabaseApiService,
    private val zivaaApiService: ZivaaApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabSummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LabSummaryViewModel(reportId, categoryName, supabaseApiService, zivaaApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
