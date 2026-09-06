package com.zivaa.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ZivaaSpacing(
    // Base 4px scale
    val space1: Dp = 4.dp,
    val space2: Dp = 8.dp,
    val space3: Dp = 12.dp,
    val space4: Dp = 16.dp,
    val space5: Dp = 20.dp,
    val space6: Dp = 24.dp,
    val space7: Dp = 32.dp,
    val space8: Dp = 40.dp,
    val space9: Dp = 48.dp,

    // Surface paddings
    val padScreen: Dp = 22.dp,
    val padCard: Dp = 20.dp,

    // Radii
    val radiusCard: Dp = 22.dp,
    val radiusSoft: Dp = 14.dp,
    val radiusClinical: Dp = 12.dp,
    val radiusPill: Dp = 999.dp,
    val radiusBubble: Dp = 12.dp
)

val LocalZivaaSpacing = androidx.compose.runtime.staticCompositionLocalOf { ZivaaSpacing() }
