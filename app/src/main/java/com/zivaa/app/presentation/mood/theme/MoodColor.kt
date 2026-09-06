package com.zivaa.app.presentation.mood.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MoodColors(
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

    val accent: Color,
    val accentIcon: Color,
    val surface: Color,
    val surfaceCard: Color,
    val surfaceHero: Color,
    val textStrong: Color,
    val textBody: Color,
    val textMeta: Color,
    val textOnAccent: Color,
    val borderHairline: Color,
    val borderStrong: Color,

    val toneOk: Color,
    val toneWatch: Color,
    val toneAct: Color,
    val toneWarm: Color
)

val WarmMoodColors = MoodColors(
    bg = Color(0xFFF6F3EE),
    bgElev = Color(0xFFFBF9F5),
    ink = Color(0xFF1D211E),
    inkSoft = Color(0xFF5A5A52),
    inkMute = Color(0xFF6B6A5F),
    line = Color(0x141D211E), // rgba(29, 33, 30, 0.08)
    lineStrong = Color(0x241D211E), // rgba(29, 33, 30, 0.14)
    sage = Color(0xFF234B3F),
    sageInk = Color(0xFFF6F3EE),
    clay = Color(0xFFB6643D),
    amber = Color(0xFFC98A3A),
    rose = Color(0xFFA4493D),
    leaf = Color(0xFF5D7A4E),
    muted = Color(0xFFD4CBB8),

    accent = Color(0xFF234B3F),
    accentIcon = Color(0xFF234B3F),
    surface = Color(0xFFF6F3EE),
    surfaceCard = Color(0xFFFBF9F5),
    surfaceHero = Color(0xFF234B3F),
    textStrong = Color(0xFF1D211E),
    textBody = Color(0xFF5A5A52),
    textMeta = Color(0xFF6B6A5F),
    textOnAccent = Color(0xFFF6F3EE),
    borderHairline = Color(0x141D211E),
    borderStrong = Color(0x241D211E),

    toneOk = Color(0xFF5D7A4E),
    toneWatch = Color(0xFFC98A3A),
    toneAct = Color(0xFFA4493D),
    toneWarm = Color(0xFFB6643D)
)

val DarkMoodColors = MoodColors(
    bg = Color(0xFF14171A),
    bgElev = Color(0xFF1C2024),
    ink = Color(0xFFEDE9DF),
    inkSoft = Color(0xFFA8A59C),
    inkMute = Color(0xFFA3A196),
    line = Color(0x14EDE9DF), // rgba(237, 233, 223, 0.08)
    lineStrong = Color(0x24EDE9DF), // rgba(237, 233, 223, 0.14)
    sage = Color(0xFF2E6354),
    sageInk = Color(0xFFEDE9DF),
    clay = Color(0xFFD68A5A),
    amber = Color(0xFFD6A35A),
    rose = Color(0xFFCF6F60),
    leaf = Color(0xFF8AA676),
    muted = Color(0xFF3A3D3E),

    accent = Color(0xFF2E6354),
    accentIcon = Color(0xFF8FBFA9),
    surface = Color(0xFF14171A),
    surfaceCard = Color(0xFF1C2024),
    surfaceHero = Color(0xFF2E6354),
    textStrong = Color(0xFFEDE9DF),
    textBody = Color(0xFFA8A59C),
    textMeta = Color(0xFFA3A196),
    textOnAccent = Color(0xFFEDE9DF),
    borderHairline = Color(0x14EDE9DF),
    borderStrong = Color(0x24EDE9DF),

    toneOk = Color(0xFF8AA676),
    toneWatch = Color(0xFFD6A35A),
    toneAct = Color(0xFFCF6F60),
    toneWarm = Color(0xFFD68A5A)
)

val LocalMoodColors = staticCompositionLocalOf { WarmMoodColors }
