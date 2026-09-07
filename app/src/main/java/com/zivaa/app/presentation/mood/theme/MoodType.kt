package com.zivaa.app.presentation.mood.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.Manrope

@Immutable
data class MoodTypography(
    val display: TextStyle,
    val title: TextStyle,
    val card: TextStyle,
    val lead: TextStyle,
    val bodyLg: TextStyle,
    val body: TextStyle,
    val bodySm: TextStyle,
    val eyebrow: TextStyle,
    val meta: TextStyle
)

val defaultMoodTypography = MoodTypography(
    display = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp,
        lineHeight = (38 * 1.05).sp,
        letterSpacing = (-0.01).em
    ),
    title = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = (28 * 1.1).sp,
        letterSpacing = (-0.005).em
    ),
    card = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = (22 * 1.15).sp,
        letterSpacing = 0.em
    ),
    lead = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = (20 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    bodyLg = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = (17 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    body = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    bodySm = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = (13 * 1.45).sp,
        letterSpacing = (-0.005).em
    ),
    eyebrow = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = (11 * 1.45).sp,
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

val LocalMoodTypography = staticCompositionLocalOf { defaultMoodTypography }
