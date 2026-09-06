package com.zivaa.app.presentation.wellness.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class WellnessColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val cardBody: Color,
    val cardMind: Color,
    val cardFood: Color,
    val highlightBody: Color,
    val highlightMind: Color,
    val highlightFood: Color,
    val navSelected: Color,
    val navUnselected: Color,
    val statusDot: Color
)

val LocalWellnessColors = staticCompositionLocalOf {
    WellnessColors(
        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        surface = Color.Unspecified,
        onSurface = Color.Unspecified,
        cardBody = Color.Unspecified,
        cardMind = Color.Unspecified,
        cardFood = Color.Unspecified,
        highlightBody = Color.Unspecified,
        highlightMind = Color.Unspecified,
        highlightFood = Color.Unspecified,
        navSelected = Color.Unspecified,
        navUnselected = Color.Unspecified,
        statusDot = Color.Unspecified
    )
}

val LightWellnessColors = WellnessColors(
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    cardBody = LightCardBody,
    cardMind = LightCardMind,
    cardFood = LightCardFood,
    highlightBody = LightHighlightBody,
    highlightMind = LightHighlightMind,
    highlightFood = LightHighlightFood,
    navSelected = LightNavSelected,
    navUnselected = LightNavUnselected,
    statusDot = LightStatusDot
)

val DarkWellnessColors = WellnessColors(
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    cardBody = DarkCardBody,
    cardMind = DarkCardMind,
    cardFood = DarkCardFood,
    highlightBody = DarkHighlightBody,
    highlightMind = DarkHighlightMind,
    highlightFood = DarkHighlightFood,
    navSelected = DarkNavSelected,
    navUnselected = DarkNavUnselected,
    statusDot = DarkStatusDot
)

object WellnessTheme {
    val colors: WellnessColors
        @Composable
        get() = LocalWellnessColors.current
}

@Composable
fun WellbeingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkWellnessColors else LightWellnessColors

    CompositionLocalProvider(
        LocalWellnessColors provides colors,
        content = content
    )
}
