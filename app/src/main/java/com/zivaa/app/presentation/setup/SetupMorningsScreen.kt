package com.zivaa.app.presentation.setup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.SelectionCard
@Composable
fun SetupMorningsScreen(
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
        ZivaaTopBar(stepNo = 2, totalSteps = 10, onBack = onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionCard(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                tone = PlanSetupTones.Amber,
                icon = Icons.Default.WbSunny,
                eyebrow = "Mornings",
                question = "When does your day begin?",
                hint = "The plan starts where you do — no 6 am yoga for an 8 am riser.",
                options = listOf("Before 6", "6 - 7", "7 - 8", "After 8"),
                selectedOptions = state.wakeTime?.let { setOf(it) } ?: emptySet(),
                onOptionToggled = { viewModel.updateWakeTime(if (state.wakeTime == it) null else it) }
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
                enabled = state.wakeTime != null
            )
        }
    }
}
