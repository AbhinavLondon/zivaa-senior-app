package com.zivaa.app.presentation.profile.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

data class ProfileColors(
    val background: androidx.compose.ui.graphics.Color,
    val cardBackground: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textTertiary: androidx.compose.ui.graphics.Color,
    val accentGreen: androidx.compose.ui.graphics.Color,
    val accentBeige: androidx.compose.ui.graphics.Color,
    val divider: androidx.compose.ui.graphics.Color
)

val defaultProfileColors = ProfileColors(
    background = ProfileBackground,
    cardBackground = ProfileCardBackground,
    textPrimary = ProfileTextPrimary,
    textSecondary = ProfileTextSecondary,
    textTertiary = ProfileTextTertiary,
    accentGreen = ProfileAccentGreen,
    accentBeige = ProfileAccentBeige,
    divider = ProfileDivider
)

val LocalProfileColors = staticCompositionLocalOf { defaultProfileColors }
val LocalProfileTypography = staticCompositionLocalOf { defaultProfileTypography }

object ProfileTheme {
    val colors: ProfileColors
        @Composable get() {
            val z = com.zivaa.app.ui.theme.ZivaaTheme.colors
            return ProfileColors(
                background = z.bg,
                cardBackground = z.bgElev,
                textPrimary = z.ink,
                textSecondary = z.inkSoft,
                textTertiary = z.inkMute,
                accentGreen = z.sage,
                accentBeige = z.sageInk,
                divider = z.line
            )
        }
    val typography: ProfileTypography
        @Composable get() {
            val c = colors
            val t = LocalProfileTypography.current
            return t.copy(
                heroName = t.heroName.copy(color = c.textPrimary),
                sectionHeader = t.sectionHeader.copy(color = androidx.compose.ui.graphics.Color(0xFF111111)),
                cardTitle = t.cardTitle.copy(color = c.textPrimary),
                cardSubtitle = t.cardSubtitle.copy(color = c.textSecondary),
                statNumber = t.statNumber.copy(color = c.accentGreen),
                statLabel = t.statLabel.copy(color = c.textSecondary),
                meta = t.meta.copy(color = c.textSecondary)
            )
        }
}

@Composable
fun ProfileTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalProfileTypography provides defaultProfileTypography,
        content = content
    )
}
