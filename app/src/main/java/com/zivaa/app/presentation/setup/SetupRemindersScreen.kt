package com.zivaa.app.presentation.setup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.SelectionCard
@Composable
fun SetupRemindersScreen(
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
        ZivaaTopBar(stepNo = 8, totalSteps = 10, onBack = onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionCard(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                tone = PlanSetupTones.Amber,
                icon = Icons.Default.Notifications,
                eyebrow = "Reminders",
                question = "How often should we nudge?",
                hint = "We only interrupt when it matters.",
                options = listOf("Often", "Balanced", "Minimal"),
                selectedOptions = state.reminders?.let { setOf(it) } ?: emptySet(),
                onOptionToggled = { viewModel.updateReminders(if (state.reminders == it) null else it) }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding()
        ) {
            ZivaaButton(
                text = "Continue",
                onClick = onNext,
                enabled = state.reminders != null
            )
        }
    }
}
