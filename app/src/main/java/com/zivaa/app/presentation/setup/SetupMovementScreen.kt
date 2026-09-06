package com.zivaa.app.presentation.setup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.components.StepsGoalSection
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.SelectionCard
@Composable
fun SetupMovementScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.zivaa.app.ui.theme.LocalZivaaColors.current.bg)
    ) {
        ZivaaTopBar(stepNo = 3, totalSteps = 10, onBack = onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionCard(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                tone = PlanSetupTones.Leaf,
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                eyebrow = "Movement",
                question = "How much movement feels right?",
                hint = "Be honest, not ambitious. We build up slowly.",
                options = listOf("Gentle", "Steady", "Active"),
                selectedOptions = state.movementLevel?.let { setOf(it) } ?: emptySet(),
                onOptionToggled = { viewModel.updateMovementLevel(if (state.movementLevel == it) null else it) },
                bottomContent = {
                    if (state.movementLevel != null) {
                        StepsGoalSection(
                            stepsGoal = state.stepsGoal,
                            onStepsGoalChanged = { viewModel.updateStepsGoal(it ?: 3000) }
                        )
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            ZivaaButton(
                text = "Continue",
                onClick = onNext,
                enabled = state.movementLevel != null
            )
        }
    }
}
