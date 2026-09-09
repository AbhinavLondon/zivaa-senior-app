package com.zivaa.app.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.components.StepsGoalSection
import com.zivaa.app.ui.theme.Manrope

@Composable
fun PlanSetupScreen(
    viewModel: PlanSetupViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    
    val totalQuestions = 8
    val answeredCount = listOf(
        uiState.wakeTime != null,
        uiState.movementLevel != null,
        uiState.dietType != null,
        uiState.heightInches != null,
        uiState.weightKg != null,
        uiState.conditions.isNotEmpty(),
        uiState.evening.isNotEmpty(),
        uiState.reminders != null
    ).count { it }
    
    val isReady = answeredCount == totalQuestions
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PlanSetupTheme.Bg)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Status bar area padding handled by windowInsetsPadding
        
        // Top Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PlanSetupTheme.BgElev)
                    .border(0.5.dp, PlanSetupTheme.Line, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PlanSetupTheme.Ink,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Text(
                text = "DAILY PLAN - SETUP",
                fontFamily = Manrope,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = PlanSetupTheme.InkMute,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Sticky Progress Bar Overlay (Fades in when scrolling past Hero card)
        val showStickyProgress by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
        AnimatedVisibility(
            visible = showStickyProgress,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlanSetupTheme.SurfaceHero)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalQuestions) {
                        val isAnswered = i < answeredCount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(if (isAnswered) PlanSetupTheme.Leaf.copy(alpha = 0.85f) else PlanSetupTheme.SageInk.copy(alpha = 0.16f))
                                .then(
                                    if (isAnswered) Modifier.border(0.5.dp, PlanSetupTheme.Sage, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }
        
        // Scrollable content
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 130.dp)
        ) {
            item {
                PlanSetupHeroCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                    totalQuestions = totalQuestions,
                    answeredQuestions = answeredCount
                )
            }
            
            // --- PART 1: DAILY RHYTHM ---
            item {
                PlanSetupSectionHeader(
                    sectionNumber = 1,
                    title = "Daily Rhythm",
                    subtitle = "Your morning start time and movement baseline.",
                    modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
                )
            }
            
            item {
                SelectionCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Amber,
                    icon = Icons.Default.WbSunny,
                    eyebrow = "Mornings",
                    question = "When does your day begin?",
                    hint = "The plan starts where you do \u2014 no 6 am yoga for an 8 am riser.",
                    options = listOf("Before 6", "6 - 7", "7 - 8", "After 8"),
                    selectedOptions = uiState.wakeTime?.let { setOf(it) } ?: emptySet(),
                    onOptionToggled = { viewModel.updateWakeTime(if (uiState.wakeTime == it) null else it) },
                    stepNumber = 1,
                    totalSteps = totalQuestions
                )
            }
            
            item {
                SelectionCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Leaf,
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    eyebrow = "Movement",
                    question = "How much movement feels right?",
                    hint = "Be honest, not ambitious. We build up slowly.",
                    options = listOf("Gentle", "Steady", "Active"),
                    selectedOptions = uiState.movementLevel?.let { setOf(it) } ?: emptySet(),
                    onOptionToggled = { viewModel.updateMovementLevel(if (uiState.movementLevel == it) null else it) },
                    stepNumber = 2,
                    totalSteps = totalQuestions,
                    bottomContent = {
                        if (uiState.movementLevel != null) {
                            StepsGoalSection(
                                stepsGoal = uiState.stepsGoal,
                                onStepsGoalChanged = { viewModel.updateStepsGoal(it) }
                            )
                        }
                    }
                )
            }
            
            // --- PART 2: BODY & NUTRITION ---
            item {
                PlanSetupSectionHeader(
                    sectionNumber = 2,
                    title = "Body & Nutrition",
                    subtitle = "Dietary preferences, physical profile, and daily calorie targets.",
                    modifier = Modifier.padding(top = 22.dp, bottom = 6.dp)
                )
            }
            
            item {
                SelectionCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Sage,
                    icon = Icons.Default.Restaurant,
                    eyebrow = "Meals",
                    question = "What's on the plate?",
                    hint = "Meals are suggested to fit the kitchen you already run.",
                    options = listOf("No specific diet", "Vegetarian", "Non-Vegetarian", "Vegan", "Pescatarian", "Low Carb", "Dairy-Free"),
                    selectedOptions = uiState.dietType?.let { setOf(it) } ?: emptySet(),
                    onOptionToggled = { viewModel.updateDietType(if (uiState.dietType == it) null else it) },
                    stepNumber = 3,
                    totalSteps = totalQuestions
                )
            }
            
            item {
                HeightCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Sage,
                    icon = Icons.Default.Accessibility,
                    eyebrow = "About you",
                    heightInches = uiState.heightInches,
                    onHeightChanged = { viewModel.updateHeight(it) },
                    stepNumber = 4,
                    totalSteps = totalQuestions
                )
            }
            
            item {
                WeightCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Clay,
                    icon = Icons.Default.MonitorWeight,
                    eyebrow = "About you",
                    weightKg = uiState.weightKg,
                    onWeightChanged = { viewModel.updateWeight(it) },
                    goalWeightKg = uiState.goalWeightKg,
                    onGoalWeightChanged = { viewModel.updateGoalWeight(it) },
                    stepNumber = 5,
                    totalSteps = totalQuestions
                )
            }
            
            item {
                MacroCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    targetCalories = uiState.targetCalories,
                    proteinPct = uiState.proteinPct,
                    carbsPct = uiState.carbsPct,
                    fatPct = uiState.fatPct,
                    onCaloriesChanged = { viewModel.setTargetCalories(it) },
                    onClearCalories = { viewModel.setTargetCalories(null) },
                    onSplitChanged = { option ->
                        viewModel.setMacroSplit(option.protein, option.carbs, option.fat)
                    },
                    onProteinChanged = { newPct ->
                        val diff = newPct - uiState.proteinPct
                        viewModel.setMacroSplit(newPct, uiState.carbsPct - diff, uiState.fatPct)
                    },
                    onCarbsChanged = { newPct ->
                        val diff = newPct - uiState.carbsPct
                        viewModel.setMacroSplit(uiState.proteinPct, newPct, uiState.fatPct - diff)
                    },
                    onFatChanged = { newPct ->
                        val diff = newPct - uiState.fatPct
                        viewModel.setMacroSplit(uiState.proteinPct - diff, uiState.carbsPct, newPct)
                    },
                    badgeLabel = "CUSTOM TARGETS · OPTIONAL"
                )
            }
            
            // --- PART 3: HEALTH & ROUTINES ---
            item {
                PlanSetupSectionHeader(
                    sectionNumber = 3,
                    title = "Health & Routines",
                    subtitle = "Health conditions to plan around, evening wind-down, and reminders.",
                    modifier = Modifier.padding(top = 22.dp, bottom = 6.dp)
                )
            }
            
            item {
                SelectionCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Clay,
                    icon = Icons.Default.Favorite,
                    eyebrow = "Health \u00b7 pick all that apply",
                    question = "Anything to plan around?",
                    hint = "Timings, meals and movement adjust quietly around these.",
                    options = listOf(
                        "Diabetes",
                        "Blood pressure",
                        "Heart condition",
                        "High cholesterol",
                        "Knee or joint pain",
                        "Back pain",
                        "Thyroid",
                        "Acid reflux / GERD",
                        "Asthma / Breathing",
                        "Light sleep",
                        "Low appetite",
                        "Fatigue",
                        "None"
                    ),
                    selectedOptions = uiState.conditions,
                    onOptionToggled = { opt ->
                        viewModel.toggleCondition(opt)
                    },
                    stepNumber = 6,
                    totalSteps = totalQuestions
                )
            }
            
            item {
                SelectionCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Ink,
                    icon = Icons.Default.NightsStay,
                    eyebrow = "Evenings \u00b7 pick a couple",
                    question = "How should the evening wind down?",
                    hint = "One or two is plenty. The night follows from here.",
                    options = listOf("Meditation", "Prayer", "Music", "Reading", "A call with family"),
                    selectedOptions = uiState.evening,
                    onOptionToggled = { opt ->
                        viewModel.toggleEvening(opt)
                    },
                    stepNumber = 7,
                    totalSteps = totalQuestions
                )
            }
            
            item {
                SelectionCard(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    tone = PlanSetupTones.Amber,
                    icon = Icons.Default.Notifications,
                    eyebrow = "Reminders",
                    question = "And how should we nudge you?",
                    hint = "You can always change your mind later.",
                    options = listOf("Soft nudges", "Clear reminders"),
                    selectedOptions = uiState.reminders?.let { setOf(it) } ?: emptySet(),
                    onOptionToggled = { viewModel.updateReminders(if (uiState.reminders == it) null else it) },
                    stepNumber = 8,
                    totalSteps = totalQuestions
                )
            }
            
            // CTA Card / Button
            item {
                Box(modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)) {
                    if (uiState.isSaved) {
                        // Built Hero CTA
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(PlanSetupTheme.SurfaceHero)
                                .padding(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(x = (-40).dp, y = (-50).dp)
                                    .size(190.dp)
                                    .align(Alignment.TopStart)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(PlanSetupTheme.SageInk.copy(alpha = 0.12f), Color.Transparent),
                                            radius = 190f
                                        )
                                    )
                            )
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("Your plan is ")
                                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                            append("taking shape.")
                                        }
                                    },
                                    fontFamily = Manrope,
                                    fontSize = 26.sp,
                                    lineHeight = 29.sp,
                                    letterSpacing = (-0.1).sp,
                                    color = PlanSetupTheme.SageInk,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Ready by tomorrow morning \u2014 and it learns as you go.",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = PlanSetupTheme.SageInk.copy(alpha = 0.82f),
                                    fontWeight = FontWeight.Normal
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier
                                        .height(46.dp)
                                        .clip(CircleShape)
                                        .background(PlanSetupTheme.SageInk.copy(alpha = 0.14f))
                                        .border(0.5.dp, PlanSetupTheme.SageInk.copy(alpha = 0.30f), CircleShape)
                                        .clickable { onNavigateNext() }
                                        .padding(horizontal = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "See today's five",
                                        fontFamily = Manrope,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PlanSetupTheme.SageInk
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = PlanSetupTheme.SageInk,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    } else if (!uiState.hasBuiltPlanOnce) {
                        // Build Button
                        val buildLabel = if (isReady) {
                            if (uiState.isSaving) "Saving..." else "Build my daily plan"
                        } else {
                            "Answer all eight to build the plan"
                        }
                        val bgColor = if (isReady && !uiState.isSaving) PlanSetupTheme.Sage else PlanSetupTheme.LineStrong
                        val fgColor = if (isReady && !uiState.isSaving) PlanSetupTheme.SageInk else PlanSetupTheme.InkMute
                        
                        Button(
                            onClick = {
                                if (isReady && !uiState.isSaving) {
                                    viewModel.savePlanAndProceed()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = if (isReady && !uiState.isSaving) 12.dp else 0.dp,
                                    shape = CircleShape,
                                    ambientColor = if (isReady && !uiState.isSaving) PlanSetupTheme.Sage else Color.Transparent,
                                    spotColor = if (isReady && !uiState.isSaving) PlanSetupTheme.Sage else Color.Transparent
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = bgColor,
                                contentColor = fgColor,
                                disabledContainerColor = bgColor,
                                disabledContentColor = fgColor
                            ),
                            enabled = isReady && !uiState.isSaving,
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = buildLabel,
                                fontFamily = Manrope,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = fgColor
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = fgColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Closing text
            item {
                Text(
                    text = "Nothing here is final \u2014 change any answer, any day, and the plan quietly follows.",
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp),
                    fontFamily = Manrope,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = PlanSetupTheme.InkMute,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        // Sticky Update Plan Button
        if (uiState.hasBuiltPlanOnce && !uiState.isSaved) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PlanSetupTheme.Bg)
                    .padding(horizontal = 22.dp, vertical = 16.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp)
            ) {
                Button(
                    onClick = {
                        if (isReady && !uiState.isSaving) {
                            viewModel.savePlanAndProceed()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = PlanSetupTheme.Ink),
                    colors = ButtonDefaults.buttonColors(containerColor = PlanSetupTheme.Ink),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = if (uiState.isSaving) "Saving..." else "Update my daily plan",
                        fontFamily = Manrope,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PlanSetupTheme.Bg
                    )
                }
            }
        }
    }
}

@Composable
fun PlanSetupSectionHeader(
    sectionNumber: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PlanSetupTheme.Sage.copy(alpha = 0.22f))
                    .border(0.5.dp, PlanSetupTheme.Sage.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 3.5.dp)
            ) {
                Text(
                    text = "PART $sectionNumber",
                    fontFamily = Manrope,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = PlanSetupTheme.SageInk
                )
            }
            Text(
                text = title.uppercase(),
                fontFamily = Manrope,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = PlanSetupTheme.Ink
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(PlanSetupTheme.LineStrong)
            )
        }
        
        Spacer(modifier = Modifier.height(5.dp))
        
        Text(
            text = subtitle,
            fontFamily = Manrope,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            color = PlanSetupTheme.InkMute,
            fontWeight = FontWeight.Normal
        )
    }
}

