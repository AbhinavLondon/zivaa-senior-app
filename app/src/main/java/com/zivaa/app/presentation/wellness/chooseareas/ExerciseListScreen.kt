package com.zivaa.app.presentation.wellness.chooseareas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.data.remote.SupabaseExerciseRecord
import com.zivaa.app.presentation.wellness.chooseareas.theme.ChooseAreasTheme

@Composable
fun ExerciseListScreen(
    viewModel: ExerciseListViewModel,
    bodyPartName: String,
    isDarkTheme: Boolean = false,
    onNavigateBack: () -> Unit,
    onExerciseClick: (SupabaseExerciseRecord) -> Unit,
) {
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val availableTypes by viewModel.availableTypes.collectAsState()
    val availableDifficulties by viewModel.availableDifficulties.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()

    ChooseAreasTheme(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChooseAreasTheme.colors.bg)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ChooseAreasTheme.colors.sage)
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Unknown error", color = ChooseAreasTheme.colors.rose)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 130.dp)
                ) {
                    item {
                        ExerciseHeader(onBackClick = onNavigateBack, bodyPartName = bodyPartName)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        ExerciseFilters(
                            types = availableTypes,
                            selectedType = selectedType,
                            onTypeSelect = { viewModel.selectType(it) },
                            difficulties = availableDifficulties,
                            selectedDifficulty = selectedDifficulty,
                            onDifficultySelect = { viewModel.selectDifficulty(it) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = "THE MOVES · IN ORDER",
                            style = ChooseAreasTheme.typography.eyebrow,
                            color = ChooseAreasTheme.colors.inkMute,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (exercises.isEmpty()) {
                        item {
                            Text(
                                text = "No exercises found for the selected filters.",
                                style = ChooseAreasTheme.typography.body,
                                color = ChooseAreasTheme.colors.inkSoft,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
                            )
                        }
                    } else {
                        items(exercises) { move ->
                            ExerciseCard(
                                move = move,
                                onExerciseClick = { onExerciseClick(move) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseFilters(
    types: List<String>,
    selectedType: String?,
    onTypeSelect: (String) -> Unit,
    difficulties: List<String>,
    selectedDifficulty: String?,
    onDifficultySelect: (String) -> Unit
) {
    if (types.isEmpty() && difficulties.isEmpty()) return

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types) { type ->
            FilterChipItem(
                text = type,
                isSelected = selectedType == type,
                onClick = { onTypeSelect(type) }
            )
        }
        item {
            if (types.isNotEmpty() && difficulties.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(24.dp)
                        .width(1.dp)
                        .background(ChooseAreasTheme.colors.lineStrong)
                )
            }
        }
        items(difficulties) { diff ->
            FilterChipItem(
                text = diff,
                isSelected = selectedDifficulty == diff,
                onClick = { onDifficultySelect(diff) }
            )
        }
    }
}

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) ChooseAreasTheme.colors.sage else ChooseAreasTheme.colors.bgElev
    val textColor = if (isSelected) ChooseAreasTheme.colors.sageInk else ChooseAreasTheme.colors.inkSoft
    val borderColor = if (isSelected) ChooseAreasTheme.colors.sage else ChooseAreasTheme.colors.lineStrong

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ChooseAreasTheme.typography.meta.copy(fontSize = 13.sp),
            color = textColor
        )
    }
}

@Composable
fun ExerciseHeader(onBackClick: () -> Unit, bodyPartName: String) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        // Breadcrumbs
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ChooseAreasTheme.colors.bgElev)
                    .border(1.dp, ChooseAreasTheme.colors.line, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = ChooseAreasTheme.colors.ink,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = "BODY · ${bodyPartName.uppercase()}",
                style = ChooseAreasTheme.typography.eyebrow,
                color = ChooseAreasTheme.colors.inkMute
            )
        }

        // Title and Subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Loosen the ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ChooseAreasTheme.colors.sage)) {
                        append(bodyPartName.lowercase())
                    }
                    append(".")
                },
                style = ChooseAreasTheme.typography.display,
                color = ChooseAreasTheme.colors.ink
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Gentle moves to ease tension. About five minutes together — or do just one.",
                style = ChooseAreasTheme.typography.body,
                color = ChooseAreasTheme.colors.inkSoft
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFF2), RoundedCornerShape(24.dp)) // Hardcoded light grayish blue
                    .border(1.dp, ChooseAreasTheme.colors.line, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF385265), RoundedCornerShape(16.dp)), // Hardcoded slate blue
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Chair, // Requires chair icon or fallback
                            contentDescription = "Chair",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("All you need is ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("a firm chair")
                            }
                            append(". Sit near the front edge, feet flat on the floor.")
                        },
                        style = ChooseAreasTheme.typography.body.copy(fontSize = 15.sp),
                        color = Color(0xFF2E3B46) // Hardcoded dark slate text
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    move: SupabaseExerciseRecord,
    onExerciseClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = ChooseAreasTheme.colors.bgElev,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .border(1.dp, ChooseAreasTheme.colors.line, RoundedCornerShape(24.dp)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Image Placeholder
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ChooseAreasTheme.colors.sage.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = ChooseAreasTheme.colors.sage.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = move.exercise_name,
                        style = ChooseAreasTheme.typography.display.copy(fontSize = 20.sp, lineHeight = 24.sp),
                        color = ChooseAreasTheme.colors.sage
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (!move.benefits.isNullOrEmpty()) {
                        Text(
                            text = move.benefits,
                            style = ChooseAreasTheme.typography.sm.copy(lineHeight = 20.sp),
                            color = ChooseAreasTheme.colors.inkSoft
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = ChooseAreasTheme.colors.line)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Meta Info using chips
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mins = (move.duration_seconds ?: 0) / 60
                val durationText = if (mins > 0) "$mins min" else "${move.duration_seconds ?: 0} s"
                
                MetaChip(icon = Icons.Rounded.Schedule, text = durationText)
                MetaChip(icon = Icons.Rounded.FitnessCenter, text = move.equipment_needed ?: "None")
                MetaChip(icon = Icons.Rounded.StarBorder, text = move.difficulty ?: "Easy")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Button
            Button(
                onClick = { onExerciseClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF385265),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(999.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show me", style = ChooseAreasTheme.typography.bodyLg)
            }
        }
    }
}

@Composable
fun MetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(ChooseAreasTheme.colors.sage.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ChooseAreasTheme.colors.sage,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = ChooseAreasTheme.typography.meta.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = ChooseAreasTheme.colors.sage
        )
    }
}
