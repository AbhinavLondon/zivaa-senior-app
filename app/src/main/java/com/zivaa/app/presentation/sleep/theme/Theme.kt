package com.zivaa.app.presentation.sleep.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val SleepColorScheme = lightColorScheme(
    primary = SleepSage,
    onPrimary = SleepSageInk,
    background = SleepBg,
    surface = SleepBgElev,
    onBackground = SleepInk,
    onSurface = SleepInk
)



@Composable
fun SleepTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalZivaaSpacing provides ZivaaSpacing()
    ) {
        MaterialTheme(
            colorScheme = SleepColorScheme,
            typography = SleepTypography,
            content = content
        )
    }
}

// Extension to easily access spacing
val MaterialTheme.spacing: ZivaaSpacing
    @Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = LocalZivaaSpacing.current
