package com.zivaa.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Neutrals - Warm Paper
val ZivaaBg = Color(0xFFF6F3EE)
val ZivaaBgElev = Color(0xFFFBF9F5)
val ZivaaInk = Color(0xFF111111)
val ZivaaInkSoft = Color(0xFF5A5A52)
val ZivaaInkMute = Color(0xFF6B6A5F)
val ZivaaLine = Color(0x141D211E) // 0.08 alpha
val ZivaaLineStrong = Color(0x241D211E) // 0.14 alpha

// Brand & Accents
val ZivaaSage = Color(0xFF234B3F)
val ZivaaSageInk = Color(0xFFF6F3EE)
val ZivaaClay = Color(0xFFB6643D)
val ZivaaAmber = Color(0xFFC98A3A)
val ZivaaRose = Color(0xFFA4493D)
val ZivaaLeaf = Color(0xFF5D7A4E)
val ZivaaMuted = Color(0xFFD4CBB8)

val ZivaaEveningTint = Color(0xFFE9E5DD)

@Immutable
data class ZivaaColors(
    val bg: Color = ZivaaBg,
    val bgElev: Color = ZivaaBgElev,
    val ink: Color = ZivaaInk,
    val inkSoft: Color = ZivaaInkSoft,
    val inkMute: Color = ZivaaInkMute,
    val line: Color = ZivaaLine,
    val lineStrong: Color = ZivaaLineStrong,
    val sage: Color = ZivaaSage,
    val sageInk: Color = ZivaaSageInk,
    val clay: Color = ZivaaClay,
    val amber: Color = ZivaaAmber,
    val rose: Color = ZivaaRose,
    val leaf: Color = ZivaaLeaf,
    val muted: Color = ZivaaMuted,
    val eveningTint: Color = ZivaaEveningTint,
    
    // Semantic Roles
    val accent: Color = ZivaaSage,
    val accentIcon: Color = ZivaaSage,
    val surface: Color = ZivaaBg,
    val surfaceCard: Color = ZivaaBgElev,
    val surfaceHero: Color = ZivaaSage,
    val textStrong: Color = ZivaaInk,
    val textBody: Color = ZivaaInkSoft,
    val textMeta: Color = ZivaaInkMute,
    val textOnAccent: Color = ZivaaSageInk,
    val borderHairline: Color = ZivaaLine,
    val borderStrong: Color = ZivaaLineStrong,
    val eyebrow: Color = Color(0xFF111111),
    
    // Tones
    val toneOk: Color = ZivaaLeaf,
    val toneWatch: Color = ZivaaAmber,
    val toneAct: Color = ZivaaRose,
    val toneWarm: Color = ZivaaClay,
    val isDark: Boolean = false
)

fun darkZivaaColors(): ZivaaColors {
    val darkBg = Color(0xFF14171A)
    val darkBgElev = Color(0xFF1C2024)
    val darkInk = Color(0xFFEDE9DF)
    val darkInkSoft = Color(0xFFA8A59C)
    val darkInkMute = Color(0xFF8E8D84)
    val darkLine = Color(0x14EDE9DF)
    val darkLineStrong = Color(0x24EDE9DF)
    val darkSage = Color(0xFF7CB89E)
    val darkSageInk = Color(0xFF14171A)
    val darkClay = Color(0xFFD68A5A)
    val darkAmber = Color(0xFFD6A35A)
    val darkRose = Color(0xFFCF6F60)
    val darkLeaf = Color(0xFF8AA676)
    val darkMuted = Color(0xFF3A3D3E)
    val darkEveningTint = Color(0xFF2A1215)

    return ZivaaColors(
        bg = darkBg,
        bgElev = darkBgElev,
        ink = darkInk,
        inkSoft = darkInkSoft,
        inkMute = darkInkMute,
        line = darkLine,
        lineStrong = darkLineStrong,
        sage = darkSage,
        sageInk = darkSageInk,
        clay = darkClay,
        amber = darkAmber,
        rose = darkRose,
        leaf = darkLeaf,
        muted = darkMuted,
        eveningTint = darkEveningTint,
        
        accent = darkSage,
        accentIcon = darkSage,
        surface = darkBg,
        surfaceCard = darkBgElev,
        surfaceHero = darkSage,
        textStrong = darkInk,
        textBody = darkInkSoft,
        textMeta = darkInkMute,
        textOnAccent = darkSageInk,
        borderHairline = darkLine,
        borderStrong = darkLineStrong,
        eyebrow = darkInk,
        
        toneOk = darkLeaf,
        toneWatch = darkAmber,
        toneAct = darkRose,
        toneWarm = darkClay,
        isDark = true
    )
}

val LocalZivaaColors = androidx.compose.runtime.staticCompositionLocalOf { ZivaaColors() }

val CareCircleAvatarPalette = listOf(
    Color(0xFF234B3F), // 0: Zivaa Sage Green
    Color(0xFFB6643D), // 1: Zivaa Terracotta Clay
    Color(0xFF385A6C), // 2: Deep Slate Blue
    Color(0xFFC98A3A), // 3: Zivaa Warm Amber
    Color(0xFF5E435E), // 4: Deep Mulberry Plum
    Color(0xFF5D7A4E), // 5: Zivaa Olive Leaf
    Color(0xFFA4493D), // 6: Zivaa Rose
    Color(0xFF2E6562)  // 7: Deep Forest Teal
)

fun getCareCircleAvatarColor(name: String, index: Int? = null): Color {
    if (index != null && index >= 0) {
        return CareCircleAvatarPalette[index % CareCircleAvatarPalette.size]
    }
    if (name.isBlank()) return CareCircleAvatarPalette[0]
    val hash = name.trim().lowercase().fold(0) { acc, char -> acc * 31 + char.code }
    val paletteIndex = (hash and 0x7FFFFFFF) % CareCircleAvatarPalette.size
    return CareCircleAvatarPalette[paletteIndex]
}

