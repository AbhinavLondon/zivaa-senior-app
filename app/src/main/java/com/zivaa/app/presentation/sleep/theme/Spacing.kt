package com.zivaa.app.presentation.sleep.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ZivaaSpacing(
    val space1: Dp = 4.dp,
    val space2: Dp = 8.dp,
    val space3: Dp = 12.dp,
    val space4: Dp = 16.dp,
    val space5: Dp = 20.dp,
    val space6: Dp = 24.dp,
    val space7: Dp = 32.dp,
    val space8: Dp = 40.dp,
    val space9: Dp = 48.dp,
    
    // Key Layout Constants
    val padScreen: Dp = 22.dp,
    val padCard: Dp = 20.dp
)

val LocalZivaaSpacing = staticCompositionLocalOf { ZivaaSpacing() }
