package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.ZivaaTheme

import com.zivaa.app.ui.theme.toEyebrowTitleCase

data class SettingToggle(
    val title: String,
    val subtitle: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

@Composable
fun SettingsCard(
    header: String,
    toggles: List<SettingToggle>,
    modifier: Modifier = Modifier,
    bottomContent: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = header.toEyebrowTitleCase(),
            style = ProfileTheme.typography.sectionHeader
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProfileTheme.colors.cardBackground, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            toggles.forEachIndexed { index, toggle ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            text = toggle.title,
                            style = ProfileTheme.typography.cardTitle
                        )
                        Text(
                            text = toggle.subtitle,
                            style = ProfileTheme.typography.cardSubtitle,
                            color = ProfileTheme.colors.textSecondary
                        )
                    }
                    
                    Switch(
                        checked = toggle.checked,
                        onCheckedChange = toggle.onCheckedChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ProfileTheme.colors.accentGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = ProfileTheme.colors.divider.copy(alpha = 0.2f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
                
                if (index < toggles.lastIndex) {
                    HorizontalDivider(
                        color = ProfileTheme.colors.divider,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            
            if (bottomContent != null) {
                if (toggles.isNotEmpty()) {
                    HorizontalDivider(
                        color = ProfileTheme.colors.divider,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                bottomContent()
            }
        }
    }
}
