package com.zivaa.app.presentation.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.components.StepsGoalSection
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.SelectionCard
import com.zivaa.app.ui.theme.LocalZivaaColors

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
            .background(LocalZivaaColors.current.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            ZivaaTopBar(stepNo = 5, totalSteps = 6, onBack = onBack)
            ZivaaHeader(
                label = "Step 5 of 6 · Daily Rhythm",
                title = "Your Daily Rhythm",
                subtitle = "Design a rhythm that fits your existing habits naturally."
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Card 1: Mornings / Wake Time
                SelectionCard(
                    tone = PlanSetupTones.Amber,
                    icon = Icons.Default.WbSunny,
                    eyebrow = "Mornings",
                    question = "When does your day begin?",
                    hint = "The plan starts where you do — no 6 am rush for an 8 am riser.",
                    options = listOf("Before 6", "6 - 7", "7 - 8", "After 8"),
                    selectedOptions = state.wakeTime?.let { setOf(it) } ?: emptySet(),
                    onOptionToggled = { viewModel.updateWakeTime(if (state.wakeTime == it) null else it) }
                )

                // Card 2: Movement & Steps Goal
                SelectionCard(
                    tone = PlanSetupTones.Leaf,
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    eyebrow = "Movement",
                    question = "How much movement feels right?",
                    hint = "Be honest, not ambitious. We build up slowly.",
                    options = listOf("Gentle", "Steady", "Active"),
                    selectedOptions = state.movementLevel?.let { setOf(it) } ?: emptySet(),
                    onOptionToggled = { viewModel.updateMovementLevel(if (state.movementLevel == it) null else it) },
                    bottomContent = {
                        StepsGoalSection(
                            stepsGoal = state.stepsGoal,
                            onStepsGoalChanged = { viewModel.updateStepsGoal(it ?: 3000) }
                        )
                    }
                )

                // Card 3: Meals & Diet
                SelectionCard(
                    tone = PlanSetupTones.Sage,
                    icon = Icons.Default.Restaurant,
                    eyebrow = "Meals",
                    question = "What's on the plate?",
                    hint = "Meals are suggested to fit the kitchen you already run.",
                    options = listOf("No specific diet", "Vegetarian", "Non-Vegetarian", "Vegan", "Pescatarian", "Low Carb", "Dairy-Free"),
                    selectedOptions = state.dietType?.let { setOf(it) } ?: emptySet(),
                    onOptionToggled = { viewModel.updateDietType(if (state.dietType == it) null else it) }
                )

                // Card 4: Evening Wind-Down
                SelectionCard(
                    tone = PlanSetupTones.Ink,
                    icon = Icons.Default.NightsStay,
                    eyebrow = "Evenings · pick a couple",
                    question = "How should the evening wind down?",
                    hint = "One or two is plenty. The night follows from here.",
                    options = listOf("Meditation", "Prayer", "Music", "Reading", "A call with family"),
                    selectedOptions = state.evening,
                    onOptionToggled = { viewModel.toggleEvening(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

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
