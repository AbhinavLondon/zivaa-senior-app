package com.zivaa.app.presentation.setup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.SelectionCard
@Composable
fun SetupDietScreen(
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
        ZivaaTopBar(stepNo = 4, totalSteps = 10, onBack = onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionCard(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                tone = PlanSetupTones.Sage,
                icon = Icons.Default.Restaurant,
                eyebrow = "Meals",
                question = "What's on the plate?",
                hint = "Meals are suggested to fit the kitchen you already run.",
                options = listOf("No specific diet", "Vegetarian", "Non-Vegetarian", "Vegan", "Pescatarian", "Low Carb", "Dairy-Free"),
                selectedOptions = state.dietType?.let { setOf(it) } ?: emptySet(),
                onOptionToggled = { viewModel.updateDietType(if (state.dietType == it) null else it) }
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
                enabled = state.dietType != null
            )
        }
    }
}
