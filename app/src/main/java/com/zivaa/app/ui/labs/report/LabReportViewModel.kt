package com.zivaa.app.ui.labs.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.SupabaseApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoryGroup(
    val title: String,
    val description: String,
    val statusText: String,
    val isOut: Boolean
)

data class LabReportState(
    val isLoading: Boolean = true,
    val reportTitle: String = "Lab Report",
    val heroTitle: String = "THE SHORT VERSION",
    val heroText: String = "",
    val inRangeCount: Int = 0,
    val outOfRangeCount: Int = 0,
    val totalCount: Int = 0,
    val categories: List<CategoryGroup> = emptyList(),
    val error: String? = null
)

class LabReportViewModel(
    private val reportId: String,
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _state = MutableStateFlow(LabReportState())
    val state: StateFlow<LabReportState> = _state

    init {
        fetchReportData()
    }

    private fun fetchReportData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Fetch report
                val reportResponse = apiService.getDiagnosticReportById("eq.$reportId")
                if (!reportResponse.isSuccessful || reportResponse.body().isNullOrEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to load report")
                    return@launch
                }
                val report = reportResponse.body()!!.first()
                val summaryText = report.summaryExplanation ?: "Your report has been generated. Ask your doctor for more details."
                val reportTitle = report.resource?.code?.text ?: "Lab Report"

                // Fetch observations
                val obsResponse = apiService.getObservationsByReport("eq.$reportId")
                if (!obsResponse.isSuccessful) {
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to load observations")
                    return@launch
                }
                val observations = obsResponse.body() ?: emptyList()

                var inRange = 0
                var outOfRange = 0
                val categoryMap = mutableMapOf<String, MutableList<Boolean>>() // isOut

                observations.forEach { obs ->
                    val resource = obs.resource ?: return@forEach
                    
                    // Determine if in range
                    val interpretationText = resource.interpretation?.firstOrNull()?.text?.lowercase() ?: ""
                    val isOut = if (interpretationText.isBlank() || interpretationText.contains("within range") || interpretationText.contains("normal")) {
                        inRange++
                        false
                    } else {
                        outOfRange++
                        true
                    }

                    // Group by consumer category
                    val consumerCoding = resource.category?.firstOrNull()?.coding?.find { it.system == "https://zivaa.com/consumer-category" }
                    val categoryDisplay = consumerCoding?.display ?: "Other"
                    if (!categoryMap.containsKey(categoryDisplay)) {
                        categoryMap[categoryDisplay] = mutableListOf()
                    }
                    categoryMap[categoryDisplay]!!.add(isOut)
                }

                val total = inRange + outOfRange

                // Build category groups
                val categories = categoryMap.map { (title, statusList) ->
                    val totalInCategory = statusList.size
                    val outInCategory = statusList.count { it }
                    val statusText = if (outInCategory > 0) {
                        "$outInCategory of $totalInCategory OUT OF RANGE"
                    } else {
                        "All $totalInCategory IN RANGE"
                    }
                    CategoryGroup(
                        title = title,
                        description = "Tap to view details", // Placeholder
                        statusText = statusText,
                        isOut = outInCategory > 0
                    )
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    reportTitle = reportTitle,
                    heroText = summaryText,
                    inRangeCount = inRange,
                    outOfRangeCount = outOfRange,
                    totalCount = total,
                    categories = categories
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}

class LabReportViewModelFactory(
    private val reportId: String,
    private val apiService: SupabaseApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LabReportViewModel(reportId, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
