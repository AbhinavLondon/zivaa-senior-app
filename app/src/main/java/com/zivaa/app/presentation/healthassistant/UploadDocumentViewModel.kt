package com.zivaa.app.presentation.healthassistant

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.AuthManager
import com.zivaa.app.data.remote.ZivaaBackendClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import com.zivaa.app.data.remote.UploadManager
import com.zivaa.app.data.remote.ProgressRequestBody
sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Success(val reportId: String?) : UploadState()
    data class Error(val message: String) : UploadState()
}

class UploadDocumentViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun uploadDocument(context: Context, uri: Uri, effectiveDate: String? = null, onUploadStarted: () -> Unit) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            
            try {
                val patientId = authManager.getUserId() ?: "default_patient"

                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    _uploadState.value = UploadState.Error("Failed to read file.")
                    return@launch
                }

                // Extract real file name
                var displayName = "upload_${System.currentTimeMillis()}"
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = it.getString(nameIndex)
                        }
                    }
                }
                if (!displayName.contains(".")) {
                    val extension = if (mimeType.contains("pdf")) "pdf" else "jpg"
                    displayName = "$displayName.$extension"
                }

                val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                
                val progressRequestBody = ProgressRequestBody(requestFile) { progress ->
                    UploadManager.setUploading(displayName, progress)
                }

                val filePart = MultipartBody.Part.createFormData("file", displayName, progressRequestBody)

                UploadManager.startUpload(patientId, effectiveDate, filePart, displayName)
                
                // Immediately notify UI to navigate away
                onUploadStarted()

            } catch (e: Exception) {
                e.printStackTrace()
                _uploadState.value = UploadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uploadState.value = UploadState.Idle
    }
}

class UploadDocumentViewModelFactory(
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadDocumentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadDocumentViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
