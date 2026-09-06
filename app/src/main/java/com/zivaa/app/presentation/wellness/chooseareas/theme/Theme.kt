package com.zivaa.app.presentation.wellness.chooseareas.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ChooseAreasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkChooseAreasColors else LightChooseAreasColors
    val typography = DefaultChooseAreasTypography

    CompositionLocalProvider(
        LocalChooseAreasColors provides colors,
        LocalChooseAreasTypography provides typography
    ) {
        // Fallback for Material3 components that might sneak in
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme(
                background = colors.bg,
                surface = colors.bgElev,
                onBackground = colors.ink,
                onSurface = colors.ink,
                primary = colors.sage,
                onPrimary = colors.sageInk
            ) else lightColorScheme(
                background = colors.bg,
                surface = colors.bgElev,
                onBackground = colors.ink,
                onSurface = colors.ink,
                primary = colors.sage,
                onPrimary = colors.sageInk
            ),
            content = content
        )
    }
}

object ChooseAreasTheme {
    val colors: ChooseAreasColors
        @Composable
        get() = LocalChooseAreasColors.current

    val typography: ChooseAreasTypography
        @Composable
        get() = LocalChooseAreasTypography.current
}
