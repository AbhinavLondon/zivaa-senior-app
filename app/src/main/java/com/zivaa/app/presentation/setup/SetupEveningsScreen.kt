package com.zivaa.app.presentation.setup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.SelectionCard
@Composable
fun SetupEveningsScreen(
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
        ZivaaTopBar(stepNo = 7, totalSteps = 10, onBack = onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionCard(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                tone = PlanSetupTones.Ink,
                icon = Icons.Default.NightsStay,
                eyebrow = "Evenings · pick a couple",
                question = "How should the evening wind down?",
                hint = "One or two is plenty. The night follows from here.",
                options = listOf("Meditation", "Prayer", "Music", "Reading", "A call with family"),
                selectedOptions = state.evening,
                onOptionToggled = { viewModel.toggleEvening(it) }
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
                enabled = true // Optional
            )
        }
    }
}
