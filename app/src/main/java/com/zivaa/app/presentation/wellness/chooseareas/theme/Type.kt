package com.zivaa.app.presentation.wellness.chooseareas.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zivaa.app.R

val ManropeFontFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

data class ChooseAreasTypography(
    val display: TextStyle,
    val title: TextStyle,
    val card: TextStyle,
    val lead: TextStyle,
    val bodyLg: TextStyle,
    val body: TextStyle,
    val sm: TextStyle,
    val eyebrow: TextStyle,
    val meta: TextStyle
)

val DefaultChooseAreasTypography = ChooseAreasTypography(
    display = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp,
        lineHeight = 40.sp, // ~1.05
        letterSpacing = (-0.01).sp
    ),
    title = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 31.sp, // ~1.1
        letterSpacing = (-0.01).sp
    ),
    card = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 25.sp, // ~1.15
        letterSpacing = 0.sp
    ),
    lead = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 29.sp, // ~1.45
        letterSpacing = 0.sp
    ),
    bodyLg = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp, // ~1.45
        letterSpacing = 0.sp
    ),
    body = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp, // ~1.45
        letterSpacing = 0.sp
    ),
    sm = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp, // ~1.45
        letterSpacing = 0.sp
    ),
    eyebrow = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp, // ~1.45
        letterSpacing = 0.08.sp
    ),
    meta = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 15.sp, // ~1.45
        letterSpacing = 0.06.sp
    )
)

val LocalChooseAreasTypography = androidx.compose.runtime.staticCompositionLocalOf {
    DefaultChooseAreasTypography
}
