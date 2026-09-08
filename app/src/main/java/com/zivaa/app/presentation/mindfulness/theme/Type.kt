package com.zivaa.app.presentation.mindfulness.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.Manrope

@Immutable
data class MindfulnessTypography(
    val titleLargeSerif: TextStyle,
    val titleMediumSerif: TextStyle,
    val titleItalicSerif: TextStyle,
    val cardTitleSerif: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val eyebrow: TextStyle,
    val timerLarge: TextStyle
)

val defaultMindfulnessTypography = MindfulnessTypography(
    titleLargeSerif = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).em,
        color = MindfulnessTextPrimary
    ),
    titleMediumSerif = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.01).em,
        color = MindfulnessTextPrimary
    ),
    titleItalicSerif = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 42.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).em,
        color = MindfulnessTextPrimary
    ),
    cardTitleSerif = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.em,
        color = MindfulnessTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).em,
        color = MindfulnessTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.em,
        color = MindfulnessTextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.em,
        color = MindfulnessTextPrimary
    ),
    eyebrow = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.15.em,
        color = MindfulnessTextPrimary
    ),
    timerLarge = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
        lineHeight = 70.sp,
        letterSpacing = 0.em,
        color = MindfulnessTextPrimary
    )
)

val LocalMindfulnessTypography = staticCompositionLocalOf { defaultMindfulnessTypography }
