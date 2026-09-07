package com.zivaa.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.R

val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic)
)

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.manrope_bold, FontWeight.Bold, FontStyle.Normal)
)

val IBMPlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium, FontStyle.Normal)
)

@Immutable
data class ZivaaTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val titleLarge: TextStyle,
    val cardTitle: TextStyle,
    val leadParagraph: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val eyebrow: TextStyle,
    val meta: TextStyle
)

val LocalZivaaTypography = androidx.compose.runtime.staticCompositionLocalOf {
    ZivaaTypography(
        displayLarge = TextStyle.Default,
        displayMedium = TextStyle.Default,
        titleLarge = TextStyle.Default,
        cardTitle = TextStyle.Default,
        leadParagraph = TextStyle.Default,
        bodyLarge = TextStyle.Default,
        bodyMedium = TextStyle.Default,
        bodySmall = TextStyle.Default,
        eyebrow = TextStyle.Default,
        meta = TextStyle.Default
    )
}

val defaultZivaaTypography = ZivaaTypography(
    displayLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp,
        lineHeight = (38 * 1.05).sp,
        letterSpacing = (-0.01).em
    ),
    displayMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = (32 * 1.1).sp,
        letterSpacing = (-0.01).em
    ),
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = (28 * 1.1).sp,
        letterSpacing = (-0.005).em
    ),
    cardTitle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 22.sp,
        lineHeight = (22 * 1.15).sp,
        letterSpacing = 0.em
    ),
    leadParagraph = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = (20 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = (17 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    bodyMedium = TextStyle( // Default body
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = (13 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    eyebrow = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = (14 * 1.45).sp, // Using default leading
        letterSpacing = 0.04.em,
        color = androidx.compose.ui.graphics.Color(0xFF111111)
    ),
    meta = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 10.5.sp,
        lineHeight = (10.5 * 1.45).sp,
        letterSpacing = 0.06.em
    )
)

fun String.toEyebrowTitleCase(): String {
    return this.split(" ").joinToString(" ") { word ->
        if (word.isBlank()) word
        else {
            word.split("-").joinToString("-") { part ->
                part.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
}
