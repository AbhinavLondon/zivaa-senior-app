package com.zivaa.app.ui.wallet

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.DiagnosticReportRepository
import com.zivaa.app.data.remote.AuthManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.zivaa.app.data.remote.UploadManager
import com.zivaa.app.data.remote.GlobalUploadState

enum class DocumentCategory(val label: String) {
    ALL("All"),
    LAB_REPORT("Lab reports"),
    PRESCRIPTION("Prescriptions"),
    XRAY("X-rays")
}

data class DocumentItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val docType: String,
    val dateStr: String,
    val iconLetters: String,
    val iconColor: Color,
    val category: DocumentCategory = DocumentCategory.LAB_REPORT
)

class HealthWalletViewModel(
    private val repository: DiagnosticReportRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val patientId = authManager.getUserId() ?: "default_patient"

    private val _allDocuments = MutableStateFlow<List<DocumentItem>>(emptyList())
    val allDocuments: StateFlow<List<DocumentItem>> = _allDocuments.asStateFlow()

    val selectedCategory = MutableStateFlow(DocumentCategory.ALL)
    val searchQuery = MutableStateFlow("")

    val categoryCounts: StateFlow<Map<DocumentCategory, Int>> = _allDocuments.map { docs ->
        mapOf(
            DocumentCategory.ALL to docs.size,
            DocumentCategory.LAB_REPORT to docs.count { it.category == DocumentCategory.LAB_REPORT },
            DocumentCategory.PRESCRIPTION to docs.count { it.category == DocumentCategory.PRESCRIPTION },
            DocumentCategory.XRAY to docs.count { it.category == DocumentCategory.XRAY }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val groupedDocuments: StateFlow<Map<String, List<DocumentItem>>> = combine(
        _allDocuments,
        selectedCategory,
        searchQuery
    ) { docs, category, query ->
        val filtered = docs.filter { doc ->
            val matchesCategory = (category == DocumentCategory.ALL || doc.category == category)
            val matchesQuery = query.isBlank() ||
                doc.title.contains(query, ignoreCase = true) ||
                doc.subtitle.contains(query, ignoreCase = true) ||
                doc.dateStr.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        filtered.groupBy { getGroupName(it.dateStr) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uploadState = UploadManager.uploadState

    init {
        viewModelScope.launch {
            repository.getReports(patientId).collect { entities ->
                val docs = entities.map { entity ->
                    val category = inferCategory(entity.title, entity.performer)
                    val (iconLetters, iconColor, docType) = when (category) {
                        DocumentCategory.LAB_REPORT -> Triple("Lt", Color(0xFF386641), "Lab")
                        DocumentCategory.PRESCRIPTION -> Triple("Rx", Color(0xFF2A5C8A), "Rx")
                        DocumentCategory.XRAY -> Triple("Xr", Color(0xFF6B4C82), "Scan")
                        DocumentCategory.ALL -> Triple("Lt", Color(0xFF386641), "PDF")
                    }
                    DocumentItem(
                        id = entity.id,
                        title = entity.title,
                        subtitle = entity.performer,
                        dateStr = formatDate(entity.effectiveDate),
                        docType = docType,
                        iconLetters = iconLetters,
                        iconColor = iconColor,
                        category = category
                    )
                }
                _allDocuments.value = docs
            }
        }
        sync()
        
        viewModelScope.launch {
            UploadManager.uploadState.collect { state ->
                if (state is GlobalUploadState.Success) {
                    sync() // Refresh documents
                    UploadManager.reset()
                }
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            repository.syncReports(patientId)
        }
    }

    fun setCategory(category: DocumentCategory) {
        selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    private fun inferCategory(title: String, performer: String): DocumentCategory {
        val lower = "$title $performer".lowercase(Locale.ROOT)
        return when {
            lower.contains("x-ray") || lower.contains("xray") || lower.contains("mri") ||
            lower.contains("ct scan") || lower.contains("ultrasound") || lower.contains("usg") ||
            lower.contains("scan") || lower.contains("imaging") || lower.contains("radiology") ||
            lower.contains("ecg") || lower.contains("echo") -> DocumentCategory.XRAY

            lower.contains("prescription") || lower.contains("rx") || lower.contains("medication") ||
            lower.contains("medicine") || lower.contains("pharmacy") || lower.contains("dosage") ||
            lower.contains("discharge") -> DocumentCategory.PRESCRIPTION

            else -> DocumentCategory.LAB_REPORT
        }
    }

    private fun formatDate(isoString: String): String {
        if (isoString.isEmpty()) return ""
        return try {
            val parser = if (isoString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            }
            val date = parser.parse(isoString)
            val formatter = SimpleDateFormat("dd MMM yy", Locale.US)
            date?.let { formatter.format(it) } ?: isoString
        } catch (e: Exception) {
            isoString
        }
    }

    private fun getGroupName(formattedDate: String): String {
        val parts = formattedDate.split(" ")
        if (parts.size >= 3) {
            return "${parts[1].uppercase(Locale.US)} 20${parts[2]}"
        } else if (parts.size >= 2) {
            return parts[1].uppercase(Locale.US)
        }
        return "RECENT"
    }
}

class HealthWalletViewModelFactory(
    private val repository: DiagnosticReportRepository,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthWalletViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthWalletViewModel(repository, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
