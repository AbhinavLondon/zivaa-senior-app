package com.zivaa.app.presentation.mood.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

object SahayakTheme {
    val colors: MoodColors
        @Composable
        get() = LocalMoodColors.current
    val typography: MoodTypography
        @Composable
        get() = LocalMoodTypography.current
}

@Composable
fun SahayakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkMoodColors else WarmMoodColors
    val typography = defaultMoodTypography

    CompositionLocalProvider(
        LocalMoodColors provides colors,
        LocalMoodTypography provides typography,
        content = content
    )
}
