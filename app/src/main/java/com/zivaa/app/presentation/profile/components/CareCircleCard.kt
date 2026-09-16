package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.getCareCircleAvatarColor

data class CareContact(
    val id: String? = null,
    val initial: String,
    val name: String,
    val relation: String,
    val isPrimary: Boolean = false,
    val receivesAlerts: Boolean = true,
    val hasPattern: Boolean = false
)

@Composable
fun CareCircleCard(
    contacts: List<CareContact>,
    onAddClick: () -> Unit = {},
    onToggleAlerts: ((CareContact) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Care Circle",
            style = ProfileTheme.typography.sectionHeader,
            modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProfileTheme.colors.cardBackground, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            contacts.forEachIndexed { index, contact ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar
                    val avatarBgColor = if (contact.isPrimary) {
                        ProfileTheme.colors.accentGreen
                    } else {
                        getCareCircleAvatarColor(contact.name, index)
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(avatarBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contact.hasPattern) {
                            val lineColor = Color.White.copy(alpha = 0.2f)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val step = 8.dp.toPx()
                                var x = -size.height
                                while (x < size.width) {
                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x + size.height, size.height),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    x += step
                                }
                            }
                        }
                        Text(
                            text = contact.initial,
                            fontFamily = com.zivaa.app.presentation.profile.theme.ManropeProfile,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            style = ProfileTheme.typography.cardTitle
                        )
                        Text(
                            text = contact.relation,
                            style = ProfileTheme.typography.cardSubtitle,
                            color = ProfileTheme.colors.textSecondary
                        )
                    }

                    if (onToggleAlerts != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = contact.receivesAlerts,
                            onCheckedChange = { onToggleAlerts(contact) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ZivaaTheme.colors.bgElev,
                                checkedTrackColor = ZivaaTheme.colors.sage,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = ZivaaTheme.colors.bgElev,
                                uncheckedTrackColor = ZivaaTheme.colors.lineStrong,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
                
                HorizontalDivider(
                    color = ProfileTheme.colors.divider,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // "Add someone" row
            val strokeColor = ProfileTheme.colors.textTertiary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddClick() }
            ) {
                // Dashed Circle with +
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(
                                color = strokeColor,
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add someone",
                        tint = ProfileTheme.colors.accentGreen
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add someone",
                        style = ProfileTheme.typography.cardTitle.copy(color = ProfileTheme.colors.accentGreen)
                    )
                    Text(
                        text = "Family, a neighbour, or a doctor",
                        style = ProfileTheme.typography.cardSubtitle,
                        color = ProfileTheme.colors.textSecondary
                    )
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ProfileTheme.colors.textSecondary
                )
            }
        }
    }
}
