package com.zivaa.app.presentation.mood

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.mood.components.FaceIcon
import com.zivaa.app.presentation.mood.theme.SahayakTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase

// --- Data Models & Constants ---

val MOOD_LABELS = listOf("Wonderful", "Good", "Okay", "Low", "Very low")
val MOOD_DESCRIPTIONS = listOf(
    "Bright and full of life",
    "Light and at ease",
    "Steady, somewhere in between",
    "A little down today",
    "Heavy, hard to lift"
)

val EMOTION_SETS = listOf(
    // 0: Wonderful
    listOf(
        "Joyful", "Grateful", "Peaceful", "Loved", "Excited", "Light", "Playful", "Thankful",
        "Blessed", "Energetic", "Optimistic", "Vibrant", "Proud", "Inspired", "Full of life", "Refreshed"
    ),
    // 1: Good
    listOf(
        "Content", "Hopeful", "Relaxed", "Cheerful", "Calm", "At ease", "Warm", "Settled",
        "Comfortable", "Pleased", "Satisfied", "Safe", "Appreciated", "Reassured", "Gentle", "Balanced"
    ),
    // 2: Okay
    listOf(
        "Fine", "Quiet", "Bored", "A little tired", "Indifferent", "Steady", "Flat", "Calm",
        "Neutral", "Distracted", "Unmotivated", "Reflective", "Slow", "Routine", "Just getting by"
    ),
    // 3: Low
    listOf(
        "Sad", "Worried", "Tired", "Frustrated", "Restless", "Missing someone", "Lonely", "Heavy",
        "Disappointed", "Uneasy", "Downcast", "Stressed", "Irritable", "Nostalgic", "Unsettled", "Low energy"
    ),
    // 4: Very low
    listOf(
        "Anxious", "Lonely", "Overwhelmed", "Hurt", "Hopeless", "Empty", "Afraid", "Drained",
        "Exhausted", "Helpless", "In pain", "Isolated", "Heartbroken", "Panicky", "Grieving", "Struggling"
    )
)

val CAUSE_POS = listOf(
    "Family time", "A good rest & sleep", "Feeling healthy & strong", "A friend visited", "Time in the garden",
    "Call from children", "Prayer & spiritual time", "A good meal", "Morning walk", "Good health checkup",
    "Grandchildren visit", "Enjoyed a hobby / music", "Accomplished a task", "Sunny pleasant weather", "Peaceful home"
)
val CAUSE_NEU = listOf(
    "How I slept", "The weather", "A quiet day at home", "My health", "Family matters",
    "Daily routine", "Waiting for news", "Just one of those days", "A bit of both", "Household chores",
    "Resting today", "Doctor visit coming up", "Nothing in particular"
)
val CAUSE_LOW = listOf(
    "Missing family", "Aches & joint pain", "Poor sleep / insomnia", "Feeling alone", "Worry about health",
    "Medication side effects", "Fatigue & low energy", "Disagreement or friction", "Gloomy weather", "Financial worries",
    "Too quiet at home", "Recent loss or grief", "Digestive issues", "Feeling dependent", "Nothing in particular"
)

val CAUSE_SETS = listOf(CAUSE_POS, CAUSE_POS, CAUSE_NEU, CAUSE_LOW, CAUSE_LOW)

@Composable
fun getMoodColor(moodIndex: Int?): Color {
    if (moodIndex == null) return SahayakTheme.colors.sage
    return when (moodIndex) {
        0 -> SahayakTheme.colors.sage
        1 -> SahayakTheme.colors.leaf
        2 -> SahayakTheme.colors.amber
        3 -> SahayakTheme.colors.clay
        4 -> SahayakTheme.colors.rose
        else -> SahayakTheme.colors.sage
    }
}

@Composable
fun getMoodTint(moodIndex: Int?): Color {
    if (moodIndex == null) return SahayakTheme.colors.muted
    val baseColor = getMoodColor(moodIndex)
    return baseColor.copy(alpha = 0.16f) // approximate color-mix(in srgb, color 16%, bg)
}

// --- Main Screen ---

@Composable
fun MoodCheckInScreen(
    viewModel: MoodViewModel,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    SahayakTheme(darkTheme = isDarkTheme) {
        var step by remember { mutableIntStateOf(0) }
        var selectedMood by remember { mutableStateOf<Int?>(null) }
        var selectedEmotions by remember { mutableStateOf(setOf<String>()) }
        var selectedCauses by remember { mutableStateOf(setOf<String>()) }
        var isSaving by remember { mutableStateOf(false) }

        val moodColor = getMoodColor(selectedMood)
        val moodTint = getMoodTint(selectedMood)

        val handleBack = {
            when (step) {
                0 -> onNavigateBack()
                1 -> {
                    step = 0
                    selectedMood = null
                }
                else -> step -= 1
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SahayakTheme.colors.bg)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SahayakTheme.colors.bgElev)
                        .border(0.5.dp, SahayakTheme.colors.line, CircleShape)
                        .clickable(onClick = handleBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SahayakTheme.colors.ink,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Mood Check-In",
                    style = SahayakTheme.typography.eyebrow,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0..2) {
                        val active = i == step
                        val done = i < step
                        val dotColor = if (active || done) {
                            if (step > 0) moodColor else SahayakTheme.colors.ink
                        } else SahayakTheme.colors.lineStrong
                        
                        Box(
                            modifier = Modifier
                                .height(7.dp)
                                .width(if (active) 22.dp else 7.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }

            // Content Area
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "StepAnimation"
                ) { currentStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (currentStep) {
                            0 -> StepMood(
                                userName = viewModel.getUserName(),
                                onMoodSelected = { 
                                    selectedMood = it
                                    selectedEmotions = emptySet()
                                    selectedCauses = emptySet()
                                    step = 1
                                }
                            )
                            1 -> StepEmotions(
                                moodIndex = selectedMood ?: 0,
                                moodColor = moodColor,
                                moodTint = moodTint,
                                selectedEmotions = selectedEmotions,
                                onEmotionToggle = { emotion ->
                                    selectedEmotions = if (selectedEmotions.contains(emotion)) {
                                        selectedEmotions - emotion
                                    } else {
                                        selectedEmotions + emotion
                                    }
                                }
                            )
                            2 -> StepCauses(
                                moodIndex = selectedMood ?: 0,
                                moodColor = moodColor,
                                moodTint = moodTint,
                                selectedEmotions = selectedEmotions,
                                selectedCauses = selectedCauses,
                                onCauseToggle = { cause ->
                                    selectedCauses = if (selectedCauses.contains(cause)) {
                                        selectedCauses - cause
                                    } else {
                                        selectedCauses + cause
                                    }
                                }
                            )
                            3 -> StepSummary(
                                userName = viewModel.getUserName(),
                                moodIndex = selectedMood ?: 0,
                                moodColor = moodColor,
                                selectedEmotions = selectedEmotions,
                                selectedCauses = selectedCauses
                            )
                        }
                        Spacer(modifier = Modifier.height(130.dp))
                    }
                }
            }

            // Footer Actions
            val showContinue = step == 1 || step == 2
            val canContinue = when (step) {
                1 -> selectedEmotions.isNotEmpty()
                2 -> selectedCauses.isNotEmpty()
                else -> true
            }

            if (showContinue) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp)
                ) {
                    Button(
                        onClick = { if (canContinue) step++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = canContinue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = moodColor,
                            disabledContainerColor = moodColor.copy(alpha = 0.4f),
                            contentColor = Color.White,
                            disabledContentColor = Color.White
                        ),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Continue",
                            fontFamily = SahayakTheme.typography.body.fontFamily,
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                }
            } else if (step == 3) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                val moodLabel = selectedMood?.let { MOOD_LABELS[it] } ?: ""
                                viewModel.saveCheckIn(
                                    moodLabel = moodLabel,
                                    emotions = selectedEmotions,
                                    causes = selectedCauses
                                ) {
                                    isSaving = false
                                    onFinish()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SahayakTheme.colors.sage,
                            contentColor = SahayakTheme.colors.sageInk
                        ),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Done for today",
                            fontFamily = SahayakTheme.typography.body.fontFamily,
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                    
                    OutlinedButton(
                        onClick = {
                            step = 0
                            selectedMood = null
                            selectedEmotions = emptySet()
                            selectedCauses = emptySet()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SahayakTheme.colors.inkSoft
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SahayakTheme.colors.lineStrong),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Start over",
                            fontFamily = SahayakTheme.typography.body.fontFamily,
                            fontSize = 14.5.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

// --- Step 0: Mood ---

@Composable
fun StepMood(userName: String, onMoodSelected: (Int) -> Unit) {
    Column {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 8.dp)) {
            Text(
                text = "Tue 16 Jun · Step 1 Of 3",
                style = SahayakTheme.typography.eyebrow,
                color = Color(0xFF111111)
            )
            Text(
                text = "How are you feeling today, $userName?",
                style = SahayakTheme.typography.display.copy(fontSize = 31.sp, lineHeight = 33.sp),
                color = SahayakTheme.colors.ink,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Take your time — there's no wrong answer. Just tap the one closest to your heart right now.",
                style = SahayakTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = SahayakTheme.colors.inkSoft,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            for (i in 0..4) {
                MoodSelectionCard(
                    moodIndex = i,
                    label = MOOD_LABELS[i],
                    description = MOOD_DESCRIPTIONS[i],
                    onClick = { onMoodSelected(i) }
                )
            }
        }
    }
}

@Composable
fun MoodSelectionCard(
    moodIndex: Int,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    val color = getMoodColor(moodIndex)
    val tint = getMoodTint(moodIndex)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .border(1.dp, SahayakTheme.colors.line, RoundedCornerShape(18.dp)),
        color = SahayakTheme.colors.bgElev,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(tint),
                contentAlignment = Alignment.Center
            ) {
                FaceIcon(
                    moodIndex = moodIndex,
                    size = 30.dp,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = SahayakTheme.typography.card,
                    color = SahayakTheme.colors.ink,
                    fontStyle = FontStyle.Normal
                )
                Text(
                    text = description,
                    style = SahayakTheme.typography.bodySm.copy(fontSize = 13.5.sp),
                    color = SahayakTheme.colors.inkSoft,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SahayakTheme.colors.inkMute,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- Step 1: Emotions ---

@Composable
fun CustomInputRow(
    placeholder: String,
    accentColor: Color,
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val submit = {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            onAdd(trimmed)
            text = ""
            focusManager.clearFocus()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SahayakTheme.colors.bgElev)
            .border(1.dp, SahayakTheme.colors.lineStrong, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            textStyle = SahayakTheme.typography.body.copy(
                fontSize = 15.sp,
                color = SahayakTheme.colors.ink
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            ),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = SahayakTheme.typography.body.copy(
                            fontSize = 15.sp,
                            color = SahayakTheme.colors.inkMute
                        )
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        val canAdd = text.isNotBlank()
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canAdd) accentColor else SahayakTheme.colors.lineStrong.copy(alpha = 0.5f))
                .clickable(enabled = canAdd, onClick = submit),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add custom item",
                tint = if (canAdd) Color.White else SahayakTheme.colors.inkMute,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StepEmotions(
    moodIndex: Int,
    moodColor: Color,
    moodTint: Color,
    selectedEmotions: Set<String>,
    onEmotionToggle: (String) -> Unit
) {
    var customEmotions by remember(moodIndex) {
        mutableStateOf(selectedEmotions.filter { it !in EMOTION_SETS[moodIndex] }.toSet())
    }

    val allEmotions = remember(moodIndex, customEmotions) {
        (EMOTION_SETS[moodIndex] + customEmotions).distinct()
    }

    Column {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 8.dp)) {
            // Mood pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(moodTint)
                    .padding(start = 9.dp, end = 13.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                FaceIcon(moodIndex = moodIndex, size = 26.dp, color = moodColor)
                Text(
                    text = "Feeling ${MOOD_LABELS[moodIndex].toEyebrowTitleCase()}",
                    style = SahayakTheme.typography.eyebrow,
                    color = moodColor
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Step 2 Of 3",
                style = SahayakTheme.typography.eyebrow,
                color = Color(0xFF111111)
            )
            Text(
                text = "Which feeling fits best?",
                style = SahayakTheme.typography.display.copy(fontSize = 30.sp, lineHeight = 33.sp),
                color = SahayakTheme.colors.ink,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Pick as many as feel true — or type your own below.",
                style = SahayakTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = SahayakTheme.colors.inkSoft,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // FlowRow for chips
        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            allEmotions.forEach { emotion ->
                val isSelected = selectedEmotions.contains(emotion)
                Chip(
                    text = emotion,
                    isSelected = isSelected,
                    selectedColor = moodColor,
                    onClick = { onEmotionToggle(emotion) }
                )
            }
        }

        // Manual entry field
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Type your own feeling:",
                style = SahayakTheme.typography.eyebrow.copy(fontSize = 11.sp),
                color = SahayakTheme.colors.inkMute,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CustomInputRow(
                placeholder = "e.g. Relieved, Nostalgic, Inspired...",
                accentColor = moodColor,
                onAdd = { newEmotion ->
                    customEmotions = customEmotions + newEmotion
                    if (!selectedEmotions.contains(newEmotion)) {
                        onEmotionToggle(newEmotion)
                    }
                }
            )
        }
    }
}

// --- Step 2: Causes ---

@Composable
fun StepCauses(
    moodIndex: Int,
    moodColor: Color,
    moodTint: Color,
    selectedEmotions: Set<String>,
    selectedCauses: Set<String>,
    onCauseToggle: (String) -> Unit
) {
    var customCauses by remember(moodIndex) {
        mutableStateOf(selectedCauses.filter { it !in CAUSE_SETS[moodIndex] }.toSet())
    }

    val allCauses = remember(moodIndex, customCauses) {
        (CAUSE_SETS[moodIndex] + customCauses).distinct()
    }

    Column {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 8.dp)) {
            // Mood pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(moodTint)
                    .padding(start = 9.dp, end = 13.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                FaceIcon(moodIndex = moodIndex, size = 26.dp, color = moodColor)
                val summaryText = if (selectedEmotions.isNotEmpty()) selectedEmotions.joinToString(", ") else "Choosing A Feeling"
                Text(
                    text = summaryText.toEyebrowTitleCase(),
                    style = SahayakTheme.typography.eyebrow,
                    color = moodColor
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Step 3 Of 3",
                style = SahayakTheme.typography.eyebrow,
                color = Color(0xFF111111)
            )
            Text(
                text = "What makes you feel that way?",
                style = SahayakTheme.typography.display.copy(fontSize = 30.sp, lineHeight = 33.sp),
                color = SahayakTheme.colors.ink,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Choose from the list, or type what's behind it today.",
                style = SahayakTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = SahayakTheme.colors.inkSoft,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // FlowRow for chips
        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            allCauses.forEach { cause ->
                val isSelected = selectedCauses.contains(cause)
                Chip(
                    text = cause,
                    isSelected = isSelected,
                    selectedColor = moodColor,
                    onClick = { onCauseToggle(cause) }
                )
            }
        }

        // Manual entry field
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Type what's behind it:",
                style = SahayakTheme.typography.eyebrow.copy(fontSize = 11.sp),
                color = SahayakTheme.colors.inkMute,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CustomInputRow(
                placeholder = "e.g. Garden bloomed, Knees were aching...",
                accentColor = moodColor,
                onAdd = { newCause ->
                    customCauses = customCauses + newCause
                    if (!selectedCauses.contains(newCause)) {
                        onCauseToggle(newCause)
                    }
                }
            )
        }
    }
}

// --- Step 3: Summary ---

@Composable
fun StepSummary(
    userName: String,
    moodIndex: Int,
    moodColor: Color,
    selectedEmotions: Set<String>,
    selectedCauses: Set<String>
) {
    val supportLine = when (moodIndex) {
        0, 1 -> "That's lovely to hear. I'll quietly let the family know you're keeping bright today."
        2 -> "Some days are simply steady — and that's perfectly alright. I'm here if it tips either way."
        else -> "Thank you for telling me. I've gently let the family know, so you needn't carry it on your own."
    }
    
    Column {
        // Hero card
        Box(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 8.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = moodColor,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(bottom = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FaceIcon(moodIndex = moodIndex, size = 30.dp, color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    Text(
                        text = "Thank you for sharing,\n$userName.",
                        style = SahayakTheme.typography.display.copy(fontSize = 30.sp, lineHeight = 33.sp),
                        color = Color.White
                    )
                    
                    Text(
                        text = supportLine,
                        style = SahayakTheme.typography.body.copy(fontSize = 14.5.sp, lineHeight = 21.sp),
                        color = Color.White.copy(alpha = 0.86f),
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }
        }

        // Summary details
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 8.dp)) {
            Text(
                text = "What You Told Me",
                style = SahayakTheme.typography.eyebrow,
                color = Color(0xFF111111)
            )
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .border(0.5.dp, SahayakTheme.colors.line, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                color = SahayakTheme.colors.bgElev,
                shadowElevation = 2.dp
            ) {
                Column {
                    SummaryRow("Mood") {
                        Text(
                            text = MOOD_LABELS[moodIndex],
                            style = SahayakTheme.typography.card.copy(fontSize = 19.sp, fontStyle = FontStyle.Normal),
                            color = SahayakTheme.colors.ink
                        )
                    }
                    
                    androidx.compose.material3.HorizontalDivider(color = SahayakTheme.colors.line, thickness = 0.5.dp)
                    
                    SummaryRow("Feeling") {
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedEmotions.forEach { emotion ->
                                SummaryChip(emotion, moodColor)
                            }
                        }
                    }
                    
                    androidx.compose.material3.HorizontalDivider(color = SahayakTheme.colors.line, thickness = 0.5.dp)
                    
                    SummaryRow("Because") {
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedCauses.forEach { cause ->
                                SummaryChip(cause, moodColor)
                            }
                        }
                    }
                }
            }
        }
        
        Text(
            text = "A quiet note of how today felt has been saved. We check in like this each evening, so the family can see how your week is going.",
            style = SahayakTheme.typography.bodySm.copy(fontSize = 12.5.sp, lineHeight = 19.sp),
            color = SahayakTheme.colors.inkMute,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 18.dp)
        )
    }
}

@Composable
fun SummaryRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label.toEyebrowTitleCase(),
            style = SahayakTheme.typography.eyebrow.copy(fontSize = 10.sp),
            color = Color(0xFF111111),
            modifier = Modifier
                .width(64.dp)
                .padding(top = 5.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
fun SummaryChip(text: String, moodColor: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(moodColor.copy(alpha = 0.13f))
            .padding(horizontal = 13.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = SahayakTheme.typography.body.copy(fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            color = moodColor
        )
    }
}

// --- Shared Components ---

@Composable
fun Chip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) selectedColor else SahayakTheme.colors.bgElev
    val contentColor = if (isSelected) Color.White else SahayakTheme.colors.ink
    val borderColor = if (isSelected) Color.Transparent else SahayakTheme.colors.lineStrong
    
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, CircleShape),
        color = bgColor,
        shape = CircleShape,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = text,
            style = SahayakTheme.typography.body.copy(
                fontSize = 16.sp,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium
            ),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp)
        )
    }
}
