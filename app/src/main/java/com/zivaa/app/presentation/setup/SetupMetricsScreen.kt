package com.zivaa.app.presentation.setup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.plan.HeightCard
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.WeightCard
@Composable
fun SetupMetricsScreen(
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
        ZivaaTopBar(stepNo = 5, totalSteps = 10, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            HeightCard(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                tone = PlanSetupTones.Sage,
                icon = Icons.Default.Accessibility,
                eyebrow = "About you",
                heightInches = state.heightInches,
                onHeightChanged = { viewModel.updateHeight(it) }
            )
            WeightCard(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                tone = PlanSetupTones.Clay,
                icon = Icons.Default.MonitorWeight,
                eyebrow = "About you",
                weightKg = state.weightKg,
                onWeightChanged = { viewModel.updateWeight(it) },
                goalWeightKg = state.goalWeightKg,
                onGoalWeightChanged = { viewModel.updateGoalWeight(it) }
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
                enabled = state.heightInches != null && state.weightKg != null
            )
        }
    }
}
