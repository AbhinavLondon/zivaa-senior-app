package com.zivaa.app.presentation.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zivaa.app.presentation.setup.wearable.WearableSetupCoordinator
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun SetupHealthConnectScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZivaaTheme.colors.bg)
    ) {
        WearableSetupCoordinator(
            state = state,
            viewModel = viewModel,
            onNext = onNext,
            onBack = onBack
        )
    }
}
