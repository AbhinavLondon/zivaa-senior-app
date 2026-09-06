package com.zivaa.app.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId

data class ChatMessage(
    val id: String,
    val role: String,
    val message: String,
    val timestamp: String,
    val isSending: Boolean = false,
    val metadata: Map<String, Any>? = null
)

class CoachChatViewModel(
    private val authManager: com.zivaa.app.data.remote.AuthManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _acuityLevel = MutableStateFlow<String?>(null)
    val acuityLevel: StateFlow<String?> = _acuityLevel.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _sessions = MutableStateFlow<List<com.zivaa.app.data.remote.CoachSessionSummary>>(emptyList())
    val sessions: StateFlow<List<com.zivaa.app.data.remote.CoachSessionSummary>> = _sessions.asStateFlow()

    private val _isViewingHistory = MutableStateFlow(false)
    val isViewingHistory: StateFlow<Boolean> = _isViewingHistory.asStateFlow()

    private val _historySessionTitle = MutableStateFlow<String?>(null)
    val historySessionTitle: StateFlow<String?> = _historySessionTitle.asStateFlow()

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    fun dismissAcuityBanner() {
        _acuityLevel.value = null
    }

    fun getUserFirstName(): String {
        val fullName = authManager.getPatientProfile()["full_name"] ?: ""
        val first = fullName.trim().split(Regex("\\s+")).firstOrNull()?.trim() ?: ""
        return if (first.isNotBlank()) first.replaceFirstChar { it.uppercase() } else "Abhinav"
    }

    init {
        // Pre-load past sessions for the history screen, but start the active chat fresh
        loadSessions()
    }

    fun startNewChat() {
        _currentSessionId.value = null
        _messages.value = emptyList()
        _acuityLevel.value = null
        _isViewingHistory.value = false
        _historySessionTitle.value = null
    }

    fun loadSessions() {
        val patientId = authManager.getUserId() ?: return
        android.util.Log.d("CoachChat", "loadSessions: patientId=$patientId")
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _isLoadingHistory.value = true
                val response = com.zivaa.app.data.remote.ZivaaBackendClient.apiService.getCoachSessions(patientId)
                android.util.Log.d("CoachChat", "loadSessions response: code=${response.code()}, count=${response.body()?.size}")
                if (response.isSuccessful && response.body() != null) {
                    _sessions.value = response.body()!!
                } else {
                    android.util.Log.e("CoachChat", "loadSessions error body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("CoachChat", "loadSessions exception", e)
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }

    fun openSession(session: com.zivaa.app.data.remote.CoachSessionSummary) {
        _currentSessionId.value = session.session_id
        _historySessionTitle.value = session.title
        _isViewingHistory.value = true
        _acuityLevel.value = null
        _messages.value = emptyList()
        
        android.util.Log.d("CoachChat", "openSession: session_id=${session.session_id}, title=${session.title}")
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _isLoadingHistory.value = true
                val response = com.zivaa.app.data.remote.ZivaaBackendClient.apiService.getCoachSessionMessages(session.session_id)
                android.util.Log.d("CoachChat", "openSession response: code=${response.code()}, count=${response.body()?.size}")
                if (response.isSuccessful && response.body() != null) {
                    val logs = response.body()!!
                    val historyMessages = logs.map { log ->
                        ChatMessage(
                            id = log.id,
                            role = log.role,
                            message = log.message,
                            timestamp = log.created_at,
                            isSending = false,
                            metadata = null
                        )
                    }
                    _messages.value = historyMessages
                } else {
                    android.util.Log.e("CoachChat", "openSession error body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("CoachChat", "openSession exception", e)
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        val patientId = authManager.getUserId() ?: return
        
        val userMsg = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            role = "user",
            message = text,
            timestamp = "",
            isSending = true
        )
        _messages.value = _messages.value + userMsg
        _isTyping.value = true
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Call FastAPI backend endpoint with active session_id (or null for fresh session)
                val request = com.zivaa.app.data.remote.CoachChatRequest(
                    patient_id = patientId,
                    message = text,
                    timezone = ZoneId.systemDefault().id,
                    session_id = _currentSessionId.value
                )
                val response = com.zivaa.app.data.remote.ZivaaBackendClient.apiService.streamCoachMessage(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val aiMsgId = java.util.UUID.randomUUID().toString()
                    var currentAiMsg = ChatMessage(
                        id = aiMsgId,
                        role = "assistant",
                        message = "",
                        timestamp = java.time.Instant.now().toString()
                    )
                    
                    // Update user message to not sending and add empty AI message
                    _messages.value = _messages.value.map {
                        if (it.id == userMsg.id) it.copy(isSending = false) else it
                    } + currentAiMsg
                    
                    _isTyping.value = false // We started receiving data
                    
                    val reader = response.body()!!.byteStream().bufferedReader()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.startsWith("data: ")) {
                            val dataStr = line!!.substring(6)
                            if (dataStr.isNotBlank() && dataStr != "[DONE]") {
                                try {
                                    val jsonObj = org.json.JSONObject(dataStr)
                                    if (jsonObj.has("text")) {
                                        val textChunk = jsonObj.getString("text")
                                        if (textChunk.isNotEmpty()) {
                                            currentAiMsg = currentAiMsg.copy(message = currentAiMsg.message + textChunk)
                                            _messages.value = _messages.value.map {
                                                if (it.id == aiMsgId) currentAiMsg else it
                                            }
                                        }
                                    }
                                    if (jsonObj.has("acuity_level")) {
                                        _acuityLevel.value = jsonObj.getString("acuity_level")
                                    }
                                    if (jsonObj.has("session_id")) {
                                        val assignedSessId = jsonObj.getString("session_id")
                                        if (assignedSessId.isNotBlank()) {
                                            _currentSessionId.value = assignedSessId
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    loadSessions() // Keep sessions updated after chat exchange
                } else {
                    _messages.value = _messages.value.map {
                        if (it.id == userMsg.id) it.copy(isSending = false) else it
                    }
                    val errorMsg = ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        role = "assistant",
                        message = "Error reaching the coach (${response.code()}). Is the backend running and updated?",
                        timestamp = ""
                    )
                    _messages.value = _messages.value + errorMsg
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Error handling (remove sending state, add error message)
                _messages.value = _messages.value.map {
                    if (it.id == userMsg.id) it.copy(isSending = false) else it
                }
                val errorMsg = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    role = "assistant",
                    message = "Network error: ${e.localizedMessage}. Please ensure your backend is running.",
                    timestamp = ""
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isTyping.value = false
            }
        }
    }
    
    fun sendSymptomReply(symptomId: String, option: String) {
        val patientId = authManager.getUserId() ?: return
        
        // Formulate the user's message locally based on the option
        var displayMsg = "My symptom is $option"
        var replyStatus = option.lowercase()
        if (option == "Completely Gone") {
            displayMsg = "My symptom is completely gone."
            replyStatus = "resolved"
        }
        
        val userMsg = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            role = "user",
            message = displayMsg,
            timestamp = "",
            isSending = true
        )
        
        // Remove the metadata from the last assistant message so buttons disappear
        _messages.value = _messages.value.map { msg ->
            if (msg.role == "assistant" && msg.metadata?.get("symptom_id") == symptomId) {
                msg.copy(metadata = null)
            } else {
                msg
            }
        } + userMsg
        
        _isTyping.value = true
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val request = com.zivaa.app.data.remote.SymptomReplyRequest(
                    patient_id = patientId,
                    symptom_id = symptomId,
                    reply_status = replyStatus
                )
                
                val response = com.zivaa.app.data.remote.ZivaaBackendClient.apiService.sendSymptomReply(request)
                if (response.isSuccessful && response.body() != null) {
                    val aiReply = response.body()!!.reply
                    
                    val aiMsg = ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        role = "assistant",
                        message = aiReply,
                        timestamp = java.time.Instant.now().toString()
                    )
                    
                    _messages.value = _messages.value.map {
                        if (it.id == userMsg.id) it.copy(isSending = false) else it
                    } + aiMsg
                } else {
                    _messages.value = _messages.value.map {
                        if (it.id == userMsg.id) it.copy(isSending = false) else it
                    }
                    val errorMsg = ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        role = "assistant",
                        message = "Error reaching the coach (${response.code()}).",
                        timestamp = ""
                    )
                    _messages.value = _messages.value + errorMsg
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value = _messages.value.map {
                    if (it.id == userMsg.id) it.copy(isSending = false) else it
                }
                val errorMsg = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    role = "assistant",
                    message = "Network error: ${e.localizedMessage}",
                    timestamp = ""
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isTyping.value = false
            }
        }
    }
}

class CoachChatViewModelFactory(
    private val authManager: com.zivaa.app.data.remote.AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoachChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoachChatViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
