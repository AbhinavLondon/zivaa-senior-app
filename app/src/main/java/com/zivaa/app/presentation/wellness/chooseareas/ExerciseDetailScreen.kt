package com.zivaa.app.presentation.wellness.chooseareas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.data.remote.SupabaseExerciseRecord
import com.zivaa.app.presentation.wellness.chooseareas.theme.ChooseAreasTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase

@Composable
fun ExerciseDetailScreen(
    exercise: SupabaseExerciseRecord,
    isDarkTheme: Boolean = false,
    onNavigateBack: () -> Unit
) {
    ChooseAreasTheme(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChooseAreasTheme.colors.bg)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ChooseAreasTheme.colors.bgElev)
                        .border(1.dp, ChooseAreasTheme.colors.line, CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = ChooseAreasTheme.colors.ink,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 130.dp)
            ) {
                item {
                    // Video Placeholder (16:9 ratio)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(ChooseAreasTheme.colors.sage.copy(alpha = 0.1f))
                            .border(1.dp, ChooseAreasTheme.colors.lineStrong, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ChooseAreasTheme.colors.sage),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play Video",
                                tint = ChooseAreasTheme.colors.sageInk,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    // Title & Reps
                    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                        Text(
                            text = exercise.exercise_name,
                            style = ChooseAreasTheme.typography.display.copy(fontSize = 28.sp, lineHeight = 34.sp),
                            color = ChooseAreasTheme.colors.ink
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (!exercise.reps_duration.isNullOrEmpty()) {
                            MetaChip(
                                icon = Icons.Rounded.Schedule,
                                text = exercise.reps_duration
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Instructions Section
                if (!exercise.step_by_step_instructions.isNullOrEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Rounded.Info,
                            title = "Instructions",
                            content = exercise.step_by_step_instructions
                        )
                    }
                }

                // Tips Section
                if (!exercise.tips.isNullOrEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Rounded.Lightbulb,
                            title = "Tips",
                            content = exercise.tips
                        )
                    }
                }

                // Easier Section
                if (!exercise.modifications_easier.isNullOrEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Rounded.ArrowDownward,
                            title = "Make it easier",
                            content = exercise.modifications_easier
                        )
                    }
                }

                // Harder Section
                if (!exercise.progression_harder.isNullOrEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Rounded.ArrowUpward,
                            title = "Make it harder",
                            content = exercise.progression_harder
                        )
                    }
                }

                // Precautions Section
                if (!exercise.precautions_contraindications.isNullOrEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Rounded.Warning,
                            title = "Precautions",
                            content = exercise.precautions_contraindications,
                            isWarning = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    icon: ImageVector,
    title: String,
    content: String,
    isWarning: Boolean = false
) {
    val color = if (isWarning) ChooseAreasTheme.colors.rose else ChooseAreasTheme.colors.sage

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .background(ChooseAreasTheme.colors.bgElev, RoundedCornerShape(20.dp))
            .border(1.dp, ChooseAreasTheme.colors.line, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title.toEyebrowTitleCase(),
                style = ChooseAreasTheme.typography.eyebrow.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = content,
            style = ChooseAreasTheme.typography.body.copy(lineHeight = 24.sp),
            color = ChooseAreasTheme.colors.inkSoft
        )
    }
}
