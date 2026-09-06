package com.zivaa.app.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

sealed class GlobalUploadState {
    object Idle : GlobalUploadState()
    data class Uploading(val fileName: String, val progress: Int, val message: String = "Uploading file...") : GlobalUploadState()
    data class Processing(val fileName: String, val progress: Int, val message: String) : GlobalUploadState()
    data class Success(val fileName: String, val reportId: String?) : GlobalUploadState()
    data class Error(val fileName: String, val message: String) : GlobalUploadState()
}

object UploadManager {
    private val _uploadState = MutableStateFlow<GlobalUploadState>(GlobalUploadState.Idle)
    val uploadState: StateFlow<GlobalUploadState> = _uploadState

    fun setUploading(fileName: String, progress: Int) {
        // Map 0-100% network upload to 0-20% of the total pipeline
        val mappedProgress = (progress * 0.20).toInt()
        _uploadState.value = GlobalUploadState.Uploading(fileName, mappedProgress, "Uploading file...")
    }
    
    fun setProcessing(fileName: String, progress: Int, message: String) {
        _uploadState.value = GlobalUploadState.Processing(fileName, progress, message)
    }

    fun setSuccess(fileName: String, reportId: String?) {
        _uploadState.value = GlobalUploadState.Success(fileName, reportId)
    }

    fun setError(fileName: String, message: String) {
        _uploadState.value = GlobalUploadState.Error(fileName, message)
    }

    fun reset() {
        _uploadState.value = GlobalUploadState.Idle
    }

    fun startUpload(
        patientId: String, 
        effectiveDate: String?, 
        filePart: okhttp3.MultipartBody.Part,
        fileName: String
    ) {
        setUploading(fileName, 0)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Get initial report count
                val initialResponse = RetrofitClient.apiService.getDiagnosticReports("eq.$patientId")
                val initialCount = if (initialResponse.isSuccessful) initialResponse.body()?.size ?: 0 else 0

                val response = ZivaaBackendClient.apiService.uploadFhirBundle(patientId, effectiveDate, filePart)
                if (response.isSuccessful && response.body() != null) {
                    
                    // Backend accepted it, now simulate processing while polling
                    simulateProcessingAndPoll(patientId, fileName, initialCount)
                    
                } else {
                    setError(fileName, "Upload failed: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setError(fileName, e.message ?: "Unknown error")
            }
        }
    }
    
    private suspend fun simulateProcessingAndPoll(patientId: String, fileName: String, initialCount: Int) {
        var progress = 20
        setProcessing(fileName, progress, "Extracting data securely...")
        
        // Simulating the AI LLM and Mapping phases
        val steps = listOf(
            Pair(40, "AI LLM Extracting biomarkers..."),
            Pair(60, "Structuring data..."),
            Pair(80, "Mapping to LOINC dictionary..."),
            Pair(90, "Finalizing report...")
        )
        
        var stepIndex = 0
        var pollAttempts = 0
        
        // Timeout after ~10 minutes (400 attempts * 1500ms)
        // LLM extraction can sometimes take several minutes depending on the PDF size.
        while (pollAttempts < 400) { 
            delay(1500)
            
            // Advance simulated progress if we have more steps. 
            // We slow down the visual progression so it takes about 1-2 minutes to reach 90%
            if (stepIndex < steps.size && pollAttempts % 10 == 0) {
                val step = steps[stepIndex]
                progress = step.first
                setProcessing(fileName, progress, step.second)
                stepIndex++
            }
            
            // Poll the backend for new reports
            try {
                val pollResponse = RetrofitClient.apiService.getDiagnosticReports("eq.$patientId")
                if (pollResponse.isSuccessful) {
                    val currentCount = pollResponse.body()?.size ?: 0
                    if (currentCount > initialCount) {
                        // Success! A new report appeared.
                        setProcessing(fileName, 100, "Frontend availability complete!")
                        delay(500)
                        setSuccess(fileName, null) // reportId not strictly needed here since we resync all
                        return
                    }
                }
            } catch (e: Exception) {
                // Ignore poll errors, just retry
            }
            
            pollAttempts++
        }
        
        setError(fileName, "Processing timed out.")
    }
}
