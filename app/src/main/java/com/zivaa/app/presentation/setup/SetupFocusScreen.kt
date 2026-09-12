package com.zivaa.app.presentation.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.plan.PrimaryFocusContent
import com.zivaa.app.ui.theme.LocalZivaaColors

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
            .background(LocalZivaaColors.current.bg)
    ) {
        ZivaaTopBar(stepNo = 2, totalSteps = 5, onBack = onBack)
        
        // Vertically scrollable content with header, focus options, and details
        Box(modifier = Modifier.weight(1f)) {
            PrimaryFocusContent(
                selectedId = state.primaryFocus,
                onOptionSelected = { viewModel.updatePrimaryFocus(it) },
                header = {
                    Column {
                        ZivaaHeader(
                            label = "Step 2 of 5 · Focus",
                            title = buildAnnotatedString {
                                append("What's your ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("primary focus?")
                                }
                            },
                            subtitle = "One answer shapes your daily plan, recovery pace, and wellness baselines. You can change this anytime."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                footer = {}
            )
        }
        
        // Bottom Continue Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            ZivaaButton(
                text = "Continue",
                onClick = onNext,
                enabled = !state.primaryFocus.isNullOrBlank()
            )
        }
    }
}
