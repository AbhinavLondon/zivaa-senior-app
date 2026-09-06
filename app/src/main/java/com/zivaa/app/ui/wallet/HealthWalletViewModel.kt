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

class HealthWalletViewModel(
    private val repository: DiagnosticReportRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val patientId = authManager.getUserId() ?: "default_patient"

    private val _groupedDocuments = MutableStateFlow<Map<String, List<DocumentItem>>>(emptyMap())
    val groupedDocuments: StateFlow<Map<String, List<DocumentItem>>> = _groupedDocuments

    val uploadState = UploadManager.uploadState

    init {
        viewModelScope.launch {
            repository.getReports(patientId).collect { entities ->
                val docs = entities.map { entity ->
                    DocumentItem(
                        id = entity.id,
                        title = entity.title,
                        subtitle = entity.performer,
                        dateStr = formatDate(entity.effectiveDate),
                        docType = "PDF",
                        iconLetters = "Lt",
                        iconColor = Color(0xFF386641) // IconGreen
                    )
                }
                
                val grouped = docs.groupBy { getGroupName(it.dateStr) }
                _groupedDocuments.value = grouped
            }
        }
        sync()
        
        viewModelScope.launch {
            UploadManager.uploadState.collect { state ->
                if (state is GlobalUploadState.Success) {
                    sync() // Refresh documents
                    UploadManager.reset() // Optionally reset after showing success, or let the screen do it.
                }
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            repository.syncReports(patientId)
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
        if (parts.size >= 2) {
            return parts[1].uppercase(Locale.US)
        }
        return "OLDER"
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
