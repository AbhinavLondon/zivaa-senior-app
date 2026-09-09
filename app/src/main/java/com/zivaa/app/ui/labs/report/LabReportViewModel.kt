package com.zivaa.app.ui.labs.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.SupabaseApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.zivaa.app.ui.labs.summary.BiomarkerUiModel
import com.zivaa.app.ui.labs.ObservationCategoryHelper

data class CategoryGroup(
    val title: String,
    val description: String,
    val statusText: String,
    val isOut: Boolean
)

data class LabReportState(
    val isLoading: Boolean = true,
    val reportTitle: String = "Lab Report",
    val heroText: String = "",
    val inRangeCount: Int = 0,
    val outOfRangeCount: Int = 0,
    val totalCount: Int = 0,
    val categories: List<CategoryGroup> = emptyList(),
    val error: String? = null,
    val patientName: String = "",
    val performer: String = "",
    val formattedDate: String = "",
    val reportId: String = "",
    val categorizedBiomarkers: Map<String, List<BiomarkerUiModel>> = emptyMap()
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

                val performer = report.performer?.takeIf { it.isNotBlank() }
                    ?: report.resource?.performer?.firstOrNull()?.display?.takeIf { it.isNotBlank() }

                val rawDate = report.effectiveDatetime?.takeIf { it.isNotBlank() }
                    ?: report.resource?.effectiveDateTime?.takeIf { it.isNotBlank() }

                val formattedDate = formatReportDate(rawDate)

                val reportTitle = when {
                    !performer.isNullOrBlank() && !formattedDate.isNullOrBlank() -> "$performer · $formattedDate"
                    !performer.isNullOrBlank() -> performer
                    !formattedDate.isNullOrBlank() -> formattedDate
                    else -> report.resource?.code?.text?.takeIf { it.isNotBlank() } ?: "Lab Report"
                }

                var patientName = ""
                val pId = report.patientId
                if (!pId.isNullOrBlank()) {
                    try {
                        val patRes = apiService.getPatient("eq.$pId")
                        if (patRes.isSuccessful && !patRes.body().isNullOrEmpty()) {
                            patientName = patRes.body()!!.first().fullName
                        }
                    } catch (ignored: Exception) {
                    }
                }

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
                val categorizedBiomarkersMap = mutableMapOf<String, MutableList<BiomarkerUiModel>>()

                observations.forEach { obs ->
                    val resource = obs.resource ?: return@forEach
                    
                    // Determine if in range
                    val headline = resource.code?.text ?: "Unknown"
                    val interpretation = resource.interpretation?.firstOrNull()?.text ?: ""
                    val interpretationLower = interpretation.lowercase()
                    val badgeTone = if (interpretationLower.isBlank() || interpretationLower.contains("within range") || interpretationLower.contains("normal")) {
                        inRange++
                        "good"
                    } else if (interpretationLower.contains("high") || interpretationLower.contains("low") || interpretationLower.contains("out of range")) {
                        outOfRange++
                        "watch"
                    } else {
                        outOfRange++
                        "bad"
                    }
                    val isOut = badgeTone != "good"

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
                            val safeLowVal = lowVal ?: (highVal!! * 0.5)
                            val safeHighVal = highVal ?: (lowVal!! * 1.5)
                            
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

                    val uiModel = BiomarkerUiModel(
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

                    // Group by consumer category
                    val categoryDisplay = ObservationCategoryHelper.extractCategory(resource)
                    if (!categoryMap.containsKey(categoryDisplay)) {
                        categoryMap[categoryDisplay] = mutableListOf()
                    }
                    categoryMap[categoryDisplay]!!.add(isOut)

                    if (!categorizedBiomarkersMap.containsKey(categoryDisplay)) {
                        categorizedBiomarkersMap[categoryDisplay] = mutableListOf()
                    }
                    categorizedBiomarkersMap[categoryDisplay]!!.add(uiModel)
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
                    categories = categories,
                    patientName = patientName,
                    performer = performer ?: "Laboratory",
                    formattedDate = formattedDate ?: "",
                    reportId = reportId,
                    categorizedBiomarkers = categorizedBiomarkersMap
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    private fun formatReportDate(rawDate: String?): String? {
        if (rawDate.isNullOrBlank()) return null
        val trimmed = rawDate.trim()

        try {
            if (trimmed.contains("T")) {
                val zonedDateTime = try {
                    OffsetDateTime.parse(trimmed).toZonedDateTime()
                } catch (e: Exception) {
                    try {
                        Instant.parse(trimmed).atZone(ZoneId.systemDefault())
                    } catch (e2: Exception) {
                        LocalDateTime.parse(trimmed).atZone(ZoneId.systemDefault())
                    }
                }
                return zonedDateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
            } else if (trimmed.matches(Regex("""^\d{4}-\d{2}-\d{2}$"""))) {
                val localDate = LocalDate.parse(trimmed)
                return localDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
            }
        } catch (ignored: Exception) {
        }

        val candidatePatterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "d MMM yyyy",
            "dd MMM yyyy"
        )
        for (pattern in candidatePatterns) {
            try {
                val sdfIn = SimpleDateFormat(pattern, Locale.US)
                sdfIn.isLenient = false
                val parsed = sdfIn.parse(trimmed)
                if (parsed != null) {
                    val sdfOut = SimpleDateFormat("d MMM yyyy", Locale.US)
                    return sdfOut.format(parsed)
                }
            } catch (ignored: Exception) {
            }
        }

        return trimmed
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
