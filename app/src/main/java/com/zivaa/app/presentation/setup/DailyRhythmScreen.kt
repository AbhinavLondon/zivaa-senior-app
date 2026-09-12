package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import kotlin.math.roundToInt

@Composable
fun DailyRhythmScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val isFormComplete = state.wakeTime != null && state.movementLevel != null && state.dietType != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZivaaTheme.colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            ZivaaTopBar(stepNo = 5, totalSteps = 6, onBack = onBack)

            ZivaaHeader(
                label = "Step 5 of 6 · Daily Rhythm",
                title = buildAnnotatedString {
                    append("Your daily ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ZivaaTheme.colors.sage)) {
                        append("rhythm")
                    }
                },
                subtitle = "Design a rhythm that fits your existing habits naturally."
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 1. Mornings / Wake Time
            RhythmQuestionSection(
                icon = Icons.Default.WbSunny,
                iconColor = ZivaaTheme.colors.amber,
                eyebrow = "Mornings",
                question = "When does your day begin?",
                hint = "The plan starts where you do — no 6 am rush for an 8 am riser.",
                options = listOf("Before 6", "6 - 7", "7 - 8", "After 8"),
                selectedOptions = state.wakeTime?.let { setOf(it) } ?: emptySet(),
                onOptionToggled = { viewModel.updateWakeTime(if (state.wakeTime == it) null else it) }
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = ZivaaTheme.colors.line
            )
            Spacer(modifier = Modifier.height(28.dp))

            // 2. Movement Level
            RhythmQuestionSection(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                iconColor = ZivaaTheme.colors.sage,
                eyebrow = "Movement",
                question = "How much movement feels right?",
                hint = "Be honest, not ambitious. We build up slowly.",
                options = listOf("Gentle", "Steady", "Active"),
                selectedOptions = state.movementLevel?.let { setOf(it) } ?: emptySet(),
                onOptionToggled = { viewModel.updateMovementLevel(if (state.movementLevel == it) null else it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Simple Steps Goal Slider
            StepsGoalSliderSection(
                stepsGoal = state.stepsGoal,
                onStepsGoalChanged = { viewModel.updateStepsGoal(it) }
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = ZivaaTheme.colors.line
            )
            Spacer(modifier = Modifier.height(28.dp))

            // 4. Meals & Diet
            RhythmQuestionSection(
                icon = Icons.Default.Restaurant,
                iconColor = ZivaaTheme.colors.clay,
                eyebrow = "Meals & Diet",
                question = "What's on the plate?",
                hint = "Meals are suggested to fit the kitchen you already run.",
                options = listOf("No specific diet", "Vegetarian", "Non-Vegetarian", "Vegan", "Pescatarian", "Low Carb", "Dairy-Free"),
                selectedOptions = state.dietType?.let { setOf(it) } ?: emptySet(),
                onOptionToggled = { viewModel.updateDietType(if (state.dietType == it) null else it) }
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = ZivaaTheme.colors.line
            )
            Spacer(modifier = Modifier.height(28.dp))

            // 5. Evening Wind-Down
            RhythmQuestionSection(
                icon = Icons.Default.NightsStay,
                iconColor = ZivaaTheme.colors.inkSoft,
                eyebrow = "Evenings · Pick a couple (Optional)",
                question = "How should the evening wind down?",
                hint = "One or two is plenty. The night follows from here.",
                options = listOf("Meditation", "Prayer", "Music", "Reading", "A call with family"),
                selectedOptions = state.evening,
                onOptionToggled = { viewModel.toggleEvening(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Bottom Continue CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ZivaaButton(
                text = "Continue",
                onClick = onNext,
                enabled = isFormComplete
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RhythmQuestionSection(
    icon: ImageVector,
    iconColor: Color,
    eyebrow: String,
    question: String,
    hint: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionToggled: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = eyebrow.toEyebrowTitleCase(),
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.eyebrow
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = question,
            style = ZivaaTheme.typography.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
            color = ZivaaTheme.colors.ink
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = hint,
            style = ZivaaTheme.typography.bodyMedium,
            color = ZivaaTheme.colors.inkSoft
        )

        Spacer(modifier = Modifier.height(14.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                Surface(
                    onClick = { onOptionToggled(option) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.12f) else ZivaaTheme.colors.bgElev,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.lineStrong
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ZivaaTheme.colors.sage,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = option,
                            style = ZivaaTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.ink
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsGoalSliderSection(
    stepsGoal: Int,
    onStepsGoalChanged: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ZivaaTheme.colors.bgElev,
        border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "DAILY STEPS GOAL",
                style = ZivaaTheme.typography.eyebrow.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = ZivaaTheme.colors.inkMute
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "%,d".format(stepsGoal),
                    style = ZivaaTheme.typography.displayMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZivaaTheme.colors.sage
                    )
                )
                Text(
                    text = "steps / day",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = ZivaaTheme.colors.inkSoft,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Slider(
                value = stepsGoal.toFloat().coerceIn(1000f, 15000f),
                onValueChange = { newValue ->
                    val rounded = ((newValue / 500f).roundToInt() * 500).coerceIn(1000, 15000)
                    onStepsGoalChanged(rounded)
                },
                valueRange = 1000f..15000f,
                steps = 27,
                colors = SliderDefaults.colors(
                    thumbColor = ZivaaTheme.colors.sage,
                    activeTrackColor = ZivaaTheme.colors.sage,
                    inactiveTrackColor = ZivaaTheme.colors.lineStrong
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1,000 steps",
                    style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = ZivaaTheme.colors.inkMute
                )
                Text(
                    text = "15,000 steps",
                    style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = ZivaaTheme.colors.inkMute
                )
            }
        }
    }
}
