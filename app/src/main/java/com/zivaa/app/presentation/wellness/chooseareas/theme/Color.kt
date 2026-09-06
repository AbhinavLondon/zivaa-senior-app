package com.zivaa.app.presentation.wellness.chooseareas.theme

import androidx.compose.ui.graphics.Color

// Warm Neutrals
val WarmBg = Color(0xFFF6F3EE)
val WarmBgElev = Color(0xFFFBF9F5)
val WarmInk = Color(0xFF1D211E)
val WarmInkSoft = Color(0xFF5A5A52)
val WarmInkMute = Color(0xFF6B6A5F)
val WarmLine = Color(0x141D211E) // rgba(29, 33, 30, 0.08) -> 0.08 * 255 = 20 -> 0x14
val WarmLineStrong = Color(0x241D211E) // rgba(29, 33, 30, 0.14) -> 0.14 * 255 = 35 -> 0x23/0x24

// Warm Accents
val WarmSage = Color(0xFF234B3F)
val WarmSageInk = Color(0xFFF6F3EE)
val WarmClay = Color(0xFFB6643D)
val WarmAmber = Color(0xFFC98A3A)
val WarmRose = Color(0xFFA4493D)
val WarmLeaf = Color(0xFF5D7A4E)
val WarmMuted = Color(0xFFD4CBB8)

// Dark Neutrals
val DarkBg = Color(0xFF14171A)
val DarkBgElev = Color(0xFF1C2024)
val DarkInk = Color(0xFFEDE9DF)
val DarkInkSoft = Color(0xFFA8A59C)
val DarkInkMute = Color(0xFFA3A196)
val DarkLine = Color(0x14EDE9DF)
val DarkLineStrong = Color(0x24EDE9DF)

// Dark Accents
val DarkSage = Color(0xFF2E6354)
val DarkSageInk = Color(0xFFEDE9DF)
val DarkClay = Color(0xFFD68A5A)
val DarkAmber = Color(0xFFD6A35A)
val DarkRose = Color(0xFFCF6F60)
val DarkLeaf = Color(0xFF8AA676)
val DarkMuted = Color(0xFF3A3D3E)
val DarkAccentIcon = Color(0xFF8FBFA9)

data class ChooseAreasColors(
    val bg: Color,
    val bgElev: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkMute: Color,
    val line: Color,
    val lineStrong: Color,
    val sage: Color,
    val sageInk: Color,
    val clay: Color,
    val amber: Color,
    val rose: Color,
    val leaf: Color,
    val muted: Color,
    val accentIcon: Color
)

val LightChooseAreasColors = ChooseAreasColors(
    bg = WarmBg,
    bgElev = WarmBgElev,
    ink = WarmInk,
    inkSoft = WarmInkSoft,
    inkMute = WarmInkMute,
    line = WarmLine,
    lineStrong = WarmLineStrong,
    sage = WarmSage,
    sageInk = WarmSageInk,
    clay = WarmClay,
    amber = WarmAmber,
    rose = WarmRose,
    leaf = WarmLeaf,
    muted = WarmMuted,
    accentIcon = WarmSage
)

val DarkChooseAreasColors = ChooseAreasColors(
    bg = DarkBg,
    bgElev = DarkBgElev,
    ink = DarkInk,
    inkSoft = DarkInkSoft,
    inkMute = DarkInkMute,
    line = DarkLine,
    lineStrong = DarkLineStrong,
    sage = DarkSage,
    sageInk = DarkSageInk,
    clay = DarkClay,
    amber = DarkAmber,
    rose = DarkRose,
    leaf = DarkLeaf,
    muted = DarkMuted,
    accentIcon = DarkAccentIcon
)

val LocalChooseAreasColors = androidx.compose.runtime.staticCompositionLocalOf {
    LightChooseAreasColors
}
