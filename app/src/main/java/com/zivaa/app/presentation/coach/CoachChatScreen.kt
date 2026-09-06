package com.zivaa.app.presentation.coach

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme

fun extractCleanMessageAndSuggestions(rawMessage: String): Pair<String, List<String>> {
    val delimiter = "[SUGGESTIONS]"
    val index = rawMessage.indexOf(delimiter)
    if (index == -1) return Pair(rawMessage, emptyList())
    
    val clean = rawMessage.substring(0, index).trim()
    val suggestionsPart = rawMessage.substring(index + delimiter.length)
    val suggestions = suggestionsPart.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.startsWith("[") }
        .map { it.replace(Regex("^[-*•0-9.]+\\s*"), "").trim() }
        .filter { it.isNotBlank() }
        .take(3)
        
    return Pair(clean, suggestions)
}

fun formatSessionTimestamp(isoTimestamp: String): String {
    if (isoTimestamp.isBlank()) return "Recent"
    return try {
        val instant = java.time.Instant.parse(isoTimestamp)
        val zone = java.time.ZoneId.systemDefault()
        val zonedDateTime = instant.atZone(zone)
        val now = java.time.ZonedDateTime.now(zone)
        
        val date = zonedDateTime.toLocalDate()
        val nowDate = now.toLocalDate()
        
        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
        val formattedTime = zonedDateTime.format(timeFormatter)
        
        when {
            date.isEqual(nowDate) -> "Today at $formattedTime"
            date.isEqual(nowDate.minusDays(1)) -> "Yesterday at $formattedTime"
            date.isAfter(nowDate.minusDays(7)) -> "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} at $formattedTime"
            else -> {
                val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")
                date.format(dateFormatter)
            }
        }
    } catch (e: Exception) {
        if (isoTimestamp.length >= 10) isoTimestamp.substring(0, 10) else isoTimestamp
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachChatScreen(
    viewModel: CoachChatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCare: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val acuityLevel by viewModel.acuityLevel.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val isViewingHistory by viewModel.isViewingHistory.collectAsState()
    val historySessionTitle by viewModel.historySessionTitle.collectAsState()
    val isLoadingHistory by viewModel.isLoadingHistory.collectAsState()
    
    var showHistoryView by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // Voice-to-Text Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                inputText = matches[0]
            }
        }
    }

    // Back handler for history view
    BackHandler(enabled = showHistoryView) {
        showHistoryView = false
    }

    // Always fetch latest sessions whenever the history view is opened
    LaunchedEffect(showHistoryView) {
        if (showHistoryView) {
            viewModel.loadSessions()
        }
    }

    val itemCount = if (messages.isEmpty()) 1 else messages.size + (if (isTyping) 1 else 0)
    LaunchedEffect(itemCount, showHistoryView) {
        if (!showHistoryView && itemCount > 0) {
            listState.scrollToItem(itemCount - 1)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        if (showHistoryView) {
                            Text(
                                "Chat History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ZivaaTheme.colors.textStrong
                            )
                        } else {
                            Text(
                                "Ask coach",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = ZivaaTheme.colors.textStrong
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (showHistoryView) {
                                showHistoryView = false
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ZivaaTheme.colors.textStrong
                            )
                        }
                    },
                    actions = {
                        if (showHistoryView) {
                            IconButton(onClick = { viewModel.loadSessions() }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = ZivaaTheme.colors.textStrong
                                )
                            }
                            IconButton(onClick = {
                                viewModel.startNewChat()
                                showHistoryView = false
                            }) {
                                Icon(
                                    Icons.Default.AddComment,
                                    contentDescription = "New Chat",
                                    tint = ZivaaTheme.colors.accent
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                viewModel.loadSessions()
                                showHistoryView = true
                            }) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = "Past Chats",
                                    tint = ZivaaTheme.colors.textStrong
                                )
                            }
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                        tint = ZivaaTheme.colors.textStrong
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(ZivaaTheme.colors.surfaceCard)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("New chat", color = ZivaaTheme.colors.textStrong) },
                                        onClick = {
                                            viewModel.startNewChat()
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.AddComment,
                                                contentDescription = null,
                                                tint = ZivaaTheme.colors.accent
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ZivaaTheme.colors.bg
                    )
                )

                // Acuity Alert Banner
                if (!showHistoryView && acuityLevel != null && acuityLevel != "Self-Care") {
                    Surface(
                        color = when(acuityLevel) {
                            "Emergency" -> Color(0xFFD32F2F)
                            "Urgent" -> Color(0xFFF57C00)
                            else -> Color(0xFF1976D2)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when(acuityLevel) {
                                        "Emergency" -> "Medical Emergency Detected"
                                        "Urgent" -> "Urgent Care Recommended"
                                        else -> "Consult your Primary Care Doctor"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.dismissAcuityBanner() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    when(acuityLevel) {
                                        "Emergency" -> {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:999")
                                            }
                                            context.startActivity(intent)
                                        }
                                        "Urgent" -> {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("geo:0,0?q=hospitals")
                                            }
                                            context.startActivity(intent)
                                        }
                                        else -> {
                                            onNavigateToCare()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when(acuityLevel) {
                                        "Emergency" -> "Call 999"
                                        "Urgent" -> "Find Nearby Hospitals"
                                        else -> "Go to Care Page"
                                    },
                                    color = when(acuityLevel) {
                                        "Emergency" -> Color(0xFFD32F2F)
                                        "Urgent" -> Color(0xFFF57C00)
                                        else -> Color(0xFF1976D2)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Banner when viewing an archived past conversation
                if (!showHistoryView && isViewingHistory) {
                    Surface(
                        color = ZivaaTheme.colors.accent.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, ZivaaTheme.colors.accent.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = ZivaaTheme.colors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Past Chat: ${historySessionTitle ?: "Archived Session"}",
                                    style = ZivaaTheme.typography.bodySmall,
                                    color = ZivaaTheme.colors.textStrong,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(
                                onClick = { viewModel.startNewChat() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    "New Chat",
                                    color = ZivaaTheme.colors.accent,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!showHistoryView) {
                Surface(
                    color = ZivaaTheme.colors.bg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // AI Generated Suggestions row
                        val lastMessage = messages.lastOrNull()
                        if (lastMessage?.role == "assistant" && !isTyping) {
                            val (_, suggestions) = extractCleanMessageAndSuggestions(lastMessage.message)
                            if (suggestions.isNotEmpty()) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(suggestions) { suggestion ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = ZivaaTheme.colors.surfaceCard,
                                            border = BorderStroke(1.dp, ZivaaTheme.colors.accent.copy(alpha = 0.35f)),
                                            onClick = { viewModel.sendMessage(suggestion) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = ZivaaTheme.colors.accent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = suggestion,
                                                    style = ZivaaTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                                    color = ZivaaTheme.colors.textStrong
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = ZivaaTheme.colors.surfaceCard,
                                border = BorderStroke(1.dp, ZivaaTheme.colors.borderHairline.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Add (+) button
                                IconButton(
                                    onClick = { /* Additional options / tools */ },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = ZivaaTheme.colors.textMeta,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Text input
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    textStyle = ZivaaTheme.typography.bodyLarge.copy(
                                        color = ZivaaTheme.colors.textStrong,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(ZivaaTheme.colors.accent),
                                    decorationBox = { innerTextField ->
                                        if (inputText.isEmpty()) {
                                            Text(
                                                text = "Ask coach",
                                                style = ZivaaTheme.typography.bodyLarge.copy(
                                                    color = ZivaaTheme.colors.textMeta.copy(alpha = 0.65f),
                                                    fontSize = 16.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                // Voice input Mic button
                                IconButton(
                                    onClick = {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                                        }
                                        speechRecognizerLauncher.launch(intent)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(ZivaaTheme.colors.bg)
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = ZivaaTheme.colors.textStrong,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Send button
                                IconButton(
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            viewModel.sendMessage(inputText)
                                            inputText = ""
                                        }
                                    },
                                    enabled = inputText.isNotBlank(),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (inputText.isNotBlank()) ZivaaTheme.colors.accent
                                            else ZivaaTheme.colors.bg.copy(alpha = 0.6f)
                                        )
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (inputText.isNotBlank()) Color.White else ZivaaTheme.colors.textMeta.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = ZivaaTheme.colors.bg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showHistoryView) {
                CoachHistoryView(
                    sessions = sessions,
                    isLoading = isLoadingHistory,
                    onSelectSession = { session ->
                        viewModel.openSession(session)
                        showHistoryView = false
                    },
                    onStartNewChat = {
                        viewModel.startNewChat()
                        showHistoryView = false
                    },
                    onRefresh = { viewModel.loadSessions() }
                )
            } else if (isViewingHistory && isLoadingHistory && messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = ZivaaTheme.colors.accent)
                        Text(
                            "Loading conversation...",
                            style = ZivaaTheme.typography.bodySmall,
                            color = ZivaaTheme.colors.textMeta
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (messages.isEmpty() && !isViewingHistory) {
                        item {
                            CoachFreshStartView(
                                userName = viewModel.getUserFirstName(),
                                onSelectPrompt = { prompt ->
                                    viewModel.sendMessage(prompt)
                                }
                            )
                        }
                    } else {
                        items(messages) { msg ->
                            if (msg.role == "user") {
                                UserMessageBubble(msg.message, msg.isSending)
                            } else {
                                val (cleanMsg, _) = extractCleanMessageAndSuggestions(msg.message)
                                AssistantMessageBubble(
                                    text = cleanMsg,
                                    metadata = msg.metadata,
                                    onOptionSelected = { symptomId, option ->
                                        viewModel.sendSymptomReply(symptomId, option)
                                    }
                                )
                            }
                        }
                        
                        if (isTyping) {
                            item {
                                AssistantMessageBubble("Thinking...", isTyping = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoachFreshStartView(
    userName: String,
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Hello $userName! I'm Zivaa, your AI Health & Longevity Coach",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 34.sp
            ),
            color = ZivaaTheme.colors.textStrong
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You can ask me about:",
            style = ZivaaTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = ZivaaTheme.colors.textMeta
        )

        Spacer(modifier = Modifier.height(16.dp))

        data class PromptGuide(
            val number: Int,
            val emoji: String,
            val category: String,
            val question: String
        )

        val prompts = listOf(
            PromptGuide(1, "🩺", "Symptoms & Recovery", "I have a mild headache since morning, what should I do?"),
            PromptGuide(2, "🥗", "Longevity & Nutrition", "Suggest low-sodium breakfast ideas for blood pressure?"),
            PromptGuide(3, "🏃‍♂️", "Movement & Sleep", "How can I improve my deep sleep at night?")
        )

        prompts.forEach { item ->
            val promptAnnotated = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = ZivaaTheme.colors.textStrong
                    )
                ) {
                    append("${item.number}. ${item.emoji} ${item.category}: ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Normal,
                        color = ZivaaTheme.colors.textBody
                    )
                ) {
                    append(item.question)
                }
            }

            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                onClick = { onSelectPrompt(item.question) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = promptAnnotated,
                    style = ZivaaTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CoachHistoryView(
    sessions: List<com.zivaa.app.data.remote.CoachSessionSummary>,
    isLoading: Boolean,
    onSelectSession: (com.zivaa.app.data.remote.CoachSessionSummary) -> Unit,
    onStartNewChat: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    if (isLoading && sessions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ZivaaTheme.colors.accent)
        }
    } else if (sessions.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ZivaaTheme.colors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = ZivaaTheme.colors.accent,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Past Conversations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ZivaaTheme.colors.textStrong
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your conversations with your health coach will be saved here so you can review advice, symptoms, and health answers anytime.",
                style = ZivaaTheme.typography.bodySmall,
                color = ZivaaTheme.colors.textMeta,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh")
                }
                Button(
                    onClick = onStartNewChat,
                    colors = ButtonDefaults.buttonColors(containerColor = ZivaaTheme.colors.accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Chat", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Past Conversations (${sessions.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZivaaTheme.colors.textMeta,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onStartNewChat) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = ZivaaTheme.colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "New Chat",
                            color = ZivaaTheme.colors.accent,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            items(sessions) { session ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ZivaaTheme.colors.surfaceCard,
                    border = BorderStroke(1.dp, ZivaaTheme.colors.borderHairline),
                    onClick = { onSelectSession(session) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ZivaaTheme.colors.accent.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = ZivaaTheme.colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.title.ifBlank { "Health Consultation" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ZivaaTheme.colors.textStrong,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!session.preview.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = session.preview,
                                    style = ZivaaTheme.typography.bodySmall,
                                    color = ZivaaTheme.colors.textBody,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 17.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatSessionTimestamp(session.updated_at.ifBlank { session.created_at })} • ${session.message_count} messages",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZivaaTheme.colors.textMeta
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = ZivaaTheme.colors.textMeta,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun UserMessageBubble(text: String, isSending: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        Box(
            modifier = Modifier
                .weight(0.8f, fill = false)
                .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                .background(ZivaaTheme.colors.accent.copy(alpha = if (isSending) 0.5f else 1f))
                .padding(16.dp)
        ) {
            Text(text, color = Color.White)
        }
    }
}

@Composable
fun AssistantMessageBubble(
    text: String, 
    isTyping: Boolean = false, 
    metadata: Map<String, @JvmSuppressWildcards Any>? = null,
    onOptionSelected: ((String, String) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(0.9f, fill = false)
                    .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                    .background(ZivaaTheme.colors.surface)
                    .padding(16.dp)
            ) {
                if (isTyping) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TypingIndicator()
                    }
                } else {
                    com.zivaa.app.presentation.components.MarkdownText(text = text, color = ZivaaTheme.colors.textStrong)
                }
            }
            Spacer(modifier = Modifier.weight(0.1f))
        }
        
        if (metadata != null && metadata["type"] == "symptom_checkin" && onOptionSelected != null) {
            val symptomId = metadata["symptom_id"] as? String ?: ""
            val optionsList = metadata["options"] as? List<*> ?: emptyList<Any>()
            val options = optionsList.mapNotNull { it?.toString() }
            
            if (options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    items(options) { option ->
                        Button(
                            onClick = { onOptionSelected(symptomId, option) },
                            colors = ButtonDefaults.buttonColors(containerColor = ZivaaTheme.colors.accent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(option, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        for (i in 0 until 3) {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000
                        0.3f at 0
                        1f at 200 + (i * 200)
                        0.3f at 400 + (i * 200)
                    },
                    repeatMode = RepeatMode.Restart
                ), label = "TypingIndicatorAlpha"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ZivaaTheme.colors.accent.copy(alpha = alpha))
            )
        }
    }
}
