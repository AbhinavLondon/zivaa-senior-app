package com.zivaa.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object ZivaaTheme {
    val colors: ZivaaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalZivaaColors.current

    val typography: ZivaaTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalZivaaTypography.current

    val spacing: ZivaaSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalZivaaSpacing.current
}

@Composable
fun ZivaaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkZivaaColors() else ZivaaColors()
    val typography = defaultZivaaTypography.copy(
        eyebrow = defaultZivaaTypography.eyebrow.copy(color = colors.eyebrow)
    )
    val spacing = ZivaaSpacing()

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = colors.bg,
            surface = colors.bgElev,
            primary = colors.sage,
            onPrimary = colors.sageInk,
            onBackground = colors.ink,
            onSurface = colors.ink,
            outline = colors.lineStrong
        )
    } else {
        lightColorScheme(
            background = colors.bg,
            surface = colors.bgElev,
            primary = colors.sage,
            onPrimary = colors.sageInk,
            onBackground = colors.ink,
            onSurface = colors.ink,
            outline = colors.lineStrong
        )
    }

    CompositionLocalProvider(
        LocalZivaaColors provides colors,
        LocalZivaaTypography provides typography,
        LocalZivaaSpacing provides spacing
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
