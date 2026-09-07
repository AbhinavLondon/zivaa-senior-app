package com.zivaa.app.presentation.profile.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.zivaa.app.R

val ManropeProfile = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

data class ProfileTypography(
    val heroName: TextStyle,
    val sectionHeader: TextStyle,
    val cardTitle: TextStyle,
    val cardSubtitle: TextStyle,
    val statNumber: TextStyle,
    val statLabel: TextStyle,
    val meta: TextStyle
)

val defaultProfileTypography = ProfileTypography(
    heroName = TextStyle(
        fontFamily = ManropeProfile,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        color = ProfileTextPrimary
    ),
    sectionHeader = TextStyle(
        fontFamily = ManropeProfile,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.04.em,
        color = androidx.compose.ui.graphics.Color.Unspecified
    ),
    cardTitle = TextStyle(
        fontFamily = ManropeProfile,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        color = ProfileTextPrimary
    ),
    cardSubtitle = TextStyle(
        fontFamily = ManropeProfile,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        color = ProfileTextSecondary
    ),
    statNumber = TextStyle(
        fontFamily = ManropeProfile,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        color = ProfileAccentGreen
    ),
    statLabel = TextStyle(
        fontFamily = ManropeProfile,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = ProfileTextSecondary
    ),
    meta = TextStyle(
        fontFamily = ManropeProfile,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = ProfileTextSecondary
    )
)
