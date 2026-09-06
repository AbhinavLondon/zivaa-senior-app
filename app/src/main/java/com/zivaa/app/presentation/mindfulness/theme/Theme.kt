package com.zivaa.app.presentation.mindfulness.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val MindfulnessColorScheme = darkColorScheme(
    background = MindfulnessBackgroundDark,
    surface = MindfulnessSurfaceDark,
    surfaceVariant = MindfulnessSurfaceActive,
    onBackground = MindfulnessTextPrimary,
    onSurface = MindfulnessTextPrimary,
    onSurfaceVariant = MindfulnessTextSecondary,
    primary = MindfulnessAccent,
    outline = MindfulnessDivider
)

// We want to force dark theme tokens as per design unless specified,
// but let's provide a consistent color scheme for both if we strictly follow dark mode.
@Composable
fun MindfulnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // The design is inherently dark. We'll use the same color scheme for now, 
    // but the background can optionally change to warm.
    val colors = if (darkTheme) {
        MindfulnessColorScheme
    } else {
        MindfulnessColorScheme.copy(
            background = MindfulnessBackgroundWarm // Slightly warm/lighter for light theme preference
        )
    }

    CompositionLocalProvider(
        LocalMindfulnessTypography provides defaultMindfulnessTypography
    ) {
        MaterialTheme(
            colorScheme = colors,
            content = content
        )
    }
}

// Helper to access custom typography easily
object MindfulnessTheme {
    val typography: MindfulnessTypography
        @Composable
        get() = LocalMindfulnessTypography.current
}
