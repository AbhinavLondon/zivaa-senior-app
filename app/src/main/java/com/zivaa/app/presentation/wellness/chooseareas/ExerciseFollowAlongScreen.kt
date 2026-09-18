package com.zivaa.app.presentation.wellness.chooseareas

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.data.remote.SupabaseExerciseRecord
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import androidx.compose.foundation.BorderStroke

@Composable
fun ExerciseFollowAlongScreen(
    viewModel: ExerciseFollowAlongViewModel,
    onNavigateBack: () -> Unit,
    onRoutineCompleted: () -> Unit
) {
    val mode by viewModel.mode.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val secondsRemaining by viewModel.secondsRemaining.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    // Handle system back navigation
    BackHandler {
        when (mode) {
            FollowAlongMode.ACTIVE_PLAYER -> viewModel.backToOverview()
            FollowAlongMode.COMPLETED -> {
                onRoutineCompleted()
                onNavigateBack()
            }
            FollowAlongMode.OVERVIEW -> onNavigateBack()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.bg
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colors.sage,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Preparing your gentle routine...",
                            style = typography.bodyMedium,
                            color = colors.textMeta
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = mode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "FollowAlongTransition"
                ) { targetMode ->
                    when (targetMode) {
                        FollowAlongMode.OVERVIEW -> {
                            RoutineOverviewContent(
                                routineTitle = viewModel.routineTitle,
                                exercises = exercises,
                                onNavigateBack = onNavigateBack,
                                onStartRoutine = { viewModel.startFollowAlong() }
                            )
                        }
                        FollowAlongMode.ACTIVE_PLAYER -> {
                            val currentExercise = exercises.getOrNull(currentIndex)
                            if (currentExercise != null) {
                                ActivePlayerContent(
                                    currentExercise = currentExercise,
                                    currentIndex = currentIndex,
                                    totalExercises = exercises.size,
                                    secondsRemaining = secondsRemaining,
                                    isPlaying = isPlaying,
                                    onTogglePlay = { viewModel.togglePlayPause() },
                                    onPrevious = { viewModel.skipToPrevious() },
                                    onNext = { viewModel.skipToNext() },
                                    onClose = { viewModel.backToOverview() }
                                )
                            }
                        }
                        FollowAlongMode.COMPLETED -> {
                            RoutineCompletedContent(
                                routineTitle = viewModel.routineTitle,
                                exerciseCount = exercises.size,
                                onDone = {
                                    onRoutineCompleted()
                                    onNavigateBack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineOverviewContent(
    routineTitle: String,
    exercises: List<SupabaseExerciseRecord>,
    onNavigateBack: () -> Unit,
    onStartRoutine: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val totalSeconds = exercises.sumOf { it.duration_seconds ?: 45 }
    val totalMins = (totalSeconds / 60).coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colors.bgElev)
                    .border(1.dp, colors.borderHairline, CircleShape)
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textStrong,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Movement · Follow-Along".toEyebrowTitleCase(),
                    style = typography.eyebrow.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    color = colors.eyebrow
                )
                Text(
                    text = routineTitle,
                    style = typography.titleLarge.copy(
                        fontFamily = InstrumentSerif,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = colors.textStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Scrollable Overview
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp)
        ) {
            item {
                // Meta Summary Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.bgElev)
                        .border(1.dp, colors.borderHairline, RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge 1: Duration
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.amber.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = colors.amber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "~$totalMins mins",
                                style = typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.textStrong
                            )
                        }

                        // Badge 2: Movements
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.sage.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = null,
                                tint = colors.sage,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${exercises.size} moves",
                                style = typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.textStrong
                            )
                        }

                        // Badge 3: Pace
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.leaf.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = colors.leaf,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Gentle Pace",
                                style = typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.textStrong
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "A seated and standing gentle sequence curated for your joint mobility, stability, and steady longevity.",
                        style = typography.bodySmall.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = colors.textBody
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Movements Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movements In This Routine".toEyebrowTitleCase(),
                        style = typography.eyebrow.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.em
                        ),
                        color = colors.eyebrow
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.bgElev)
                            .border(1.dp, colors.borderHairline, RoundedCornerShape(999.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${exercises.size} total",
                            style = typography.meta.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textMeta
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(exercises) { index, exercise ->
                ZivaaRoutineExerciseCard(index = index + 1, exercise = exercise)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Docked Bottom CTA Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.bg,
            border = BorderStroke(1.dp, colors.borderHairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 26.dp)
            ) {
                Button(
                    onClick = onStartRoutine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.sage,
                        contentColor = colors.sageInk
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = colors.sageInk
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Follow-Along Routine",
                            style = typography.bodyLarge.copy(
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.sageInk
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.sageInk
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZivaaRoutineExerciseCard(
    index: Int,
    exercise: SupabaseExerciseRecord
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    var isExpanded by remember { mutableStateOf(false) }

    val palette = listOf(colors.sage, colors.amber, colors.clay, colors.leaf)
    val stepAccent = palette[(index - 1) % palette.size]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(22.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step index indicator
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(stepAccent.copy(alpha = 0.14f))
                    .border(1.dp, stepAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = stepAccent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exercise_name,
                    style = typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp
                    ),
                    color = colors.textStrong
                )

                val details = listOfNotNull(
                    exercise.body_part,
                    exercise.equipment_needed ?: "No equipment"
                ).joinToString(" · ")

                Text(
                    text = details,
                    style = typography.bodySmall.copy(
                        fontSize = 12.5.sp
                    ),
                    color = colors.textMeta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Duration badge
            val dur = exercise.duration_seconds ?: 45
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.bg)
                    .border(1.dp, colors.borderHairline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${dur}s",
                    style = typography.meta.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textBody
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = colors.textMeta,
                modifier = Modifier.size(18.dp)
            )
        }

        // Expanded preview details
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                HorizontalDivider(color = colors.borderHairline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                val instructions = exercise.step_by_step_instructions
                    ?: exercise.benefits
                    ?: "Perform gently and comfortably at your own pace."

                Text(
                    text = instructions,
                    style = typography.bodySmall.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    ),
                    color = colors.textBody
                )

                if (!exercise.tips.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.sage.copy(alpha = 0.08f))
                            .border(1.dp, colors.sage.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "💡 Coach Zivaa Tip: ${exercise.tips}",
                            style = typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            ),
                            color = colors.sage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivePlayerContent(
    currentExercise: SupabaseExerciseRecord,
    currentIndex: Int,
    totalExercises: Int,
    secondsRemaining: Int,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val totalDuration = (currentExercise.duration_seconds ?: 45).coerceAtLeast(1)
    val progress = ((totalDuration - secondsRemaining).toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "TimerProgress")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colors.bgElev)
                    .border(1.dp, colors.borderHairline, CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Player",
                    tint = colors.textStrong,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.sage.copy(alpha = 0.12f))
                    .border(1.dp, colors.sage.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "MOVEMENT ${currentIndex + 1} OF $totalExercises",
                    style = typography.meta.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.06.em
                    ),
                    color = colors.sage
                )
            }

            Spacer(modifier = Modifier.size(38.dp))
        }

        // Circular Timer Visual & Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(136.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 7.dp.toPx()
                    // Background track (high-contrast across themes)
                    drawCircle(
                        color = colors.lineStrong,
                        style = Stroke(width = strokeW)
                    )
                    // Progress sweep
                    drawArc(
                        color = colors.sage,
                        startAngle = -90f,
                        sweepAngle = 360f * (1f - animatedProgress),
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${secondsRemaining}s",
                        style = typography.titleLarge.copy(
                            fontFamily = InstrumentSerif,
                            fontSize = 42.sp,
                            lineHeight = 44.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.02).em
                        ),
                        color = colors.sage
                    )
                    Text(
                        text = if (isPlaying) "Remaining" else "Paused",
                        style = typography.meta.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.05.em
                        ),
                        color = colors.textMeta
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = currentExercise.exercise_name,
                style = typography.titleLarge.copy(
                    fontFamily = InstrumentSerif,
                    fontSize = 25.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                ),
                color = colors.textStrong,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            val metaRow = listOfNotNull(
                currentExercise.body_part,
                currentExercise.starting_position ?: currentExercise.type,
                currentExercise.equipment_needed ?: "No equipment needed"
            ).joinToString(" · ")

            Text(
                text = metaRow,
                style = typography.bodySmall.copy(
                    fontSize = 12.5.sp
                ),
                color = colors.textMeta,
                textAlign = TextAlign.Center
            )
        }

        // Instructions Card (Scrollable)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.bgElev)
                        .border(1.dp, colors.borderHairline, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "How To Perform".toEyebrowTitleCase(),
                        style = typography.eyebrow.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.em
                        ),
                        color = colors.eyebrow
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val instructions = currentExercise.step_by_step_instructions
                        ?: currentExercise.benefits
                        ?: "Breathe steadily and move gently within your comfortable range of motion."

                    Text(
                        text = instructions,
                        style = typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp
                        ),
                        color = colors.textStrong
                    )

                    if (!currentExercise.tips.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = colors.borderHairline, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.sage.copy(alpha = 0.08f))
                                .border(1.dp, colors.sage.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = "💡 Coach Zivaa Tip: ${currentExercise.tips}",
                                style = typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                ),
                                color = colors.sage
                            )
                        }
                    }
                }
            }
        }

        // Tactile Playback Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.bg,
            border = BorderStroke(1.dp, colors.borderHairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 26.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                IconButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(colors.bgElev)
                        .border(1.dp, colors.borderHairline, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = if (currentIndex > 0) colors.textStrong else colors.textMeta.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play / Pause Hero Button
                Surface(
                    onClick = onTogglePlay,
                    shape = CircleShape,
                    color = colors.sage,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(66.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = colors.sageInk,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Next / Skip Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(colors.bgElev)
                        .border(1.dp, colors.borderHairline, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = colors.textStrong,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineCompletedContent(
    routineTitle: String,
    exerciseCount: Int,
    onDone: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(colors.sage.copy(alpha = 0.12f))
                .border(2.dp, colors.sage, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = colors.sage,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Wonderful Work!",
            style = typography.displayMedium.copy(
                fontFamily = InstrumentSerif,
                fontSize = 32.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Normal
            ),
            color = colors.textStrong
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You completed all $exerciseCount movements in '$routineTitle'. Your daily activity has been recorded in your longevity log.",
            style = typography.bodyMedium.copy(
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            ),
            color = colors.textBody,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.bgElev)
                .border(1.dp, colors.borderHairline, RoundedCornerShape(22.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPLETED".toEyebrowTitleCase(),
                        style = typography.eyebrow.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.em
                        ),
                        color = colors.eyebrow
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$exerciseCount Movements",
                        style = typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = colors.textStrong
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.sage.copy(alpha = 0.12f))
                        .border(1.dp, colors.sage.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "On Track",
                        style = typography.meta.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.sage
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.sage,
                contentColor = colors.sageInk
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 1.dp
            )
        ) {
            Text(
                text = "Back to Today",
                style = typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.sageInk
            )
        }
    }
}

