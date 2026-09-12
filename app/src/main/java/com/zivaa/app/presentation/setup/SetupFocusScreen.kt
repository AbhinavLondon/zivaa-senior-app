package com.zivaa.app.presentation.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.plan.PrimaryFocusContent

@Composable
fun SetupFocusScreen(
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
        ZivaaTopBar(stepNo = 2, totalSteps = 5, onBack = onBack)
        // The content takes the rest of the space
        Box(modifier = Modifier.weight(1f)) {
            PrimaryFocusContent(
                selectedId = state.primaryFocus,
                onOptionSelected = { viewModel.updatePrimaryFocus(it) },
                header = {
                    Column {
                        ZivaaHeader(
                            label = "Step 2 of 5 · Focus",
                            title = "What's your primary focus?",
                            subtitle = "One answer shapes your whole plan. You can change it any time."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                footer = {}
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
                enabled = state.primaryFocus != null
            )
        }
    }
}
