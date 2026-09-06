package com.zivaa.app.presentation.wellness.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.zivaa.app.ui.theme.Manrope

val WellnessHeadline = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = (36 * 1.1).sp,
    letterSpacing = (-0.01).em
)

val WellnessHeadlineItalic = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Italic,
    fontSize = 36.sp,
    lineHeight = (36 * 1.1).sp,
    letterSpacing = (-0.01).em
)

val WellnessTitle = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = (24 * 1.2).sp
)

val WellnessBody = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = (16 * 1.45).sp
)

val WellnessLabel = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = (11 * 1.5).sp,
    letterSpacing = 0.05.em
)
