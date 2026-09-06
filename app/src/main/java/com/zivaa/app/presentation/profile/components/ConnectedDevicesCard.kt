package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.ZivaaTheme

data class ConnectedDevice(
    val id: String,
    val initial: String,
    val name: String,
    val statusText: String,
    val indicatorColor: Color,
    val indicatorText: String,
    val iconBackgroundColor: Color,
    val hasPattern: Boolean = false,
    val initialColor: Color = Color.White
)

@Composable
fun ConnectedDevicesCard(
    devices: List<ConnectedDevice>,
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "CONNECTED DEVICES",
            style = ProfileTheme.typography.sectionHeader,
            modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProfileTheme.colors.cardBackground, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            devices.forEach { device ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar/Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(device.iconBackgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (device.hasPattern) {
                            val lineColor = ProfileTheme.colors.textSecondary.copy(alpha = 0.15f)
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
                            text = device.initial,
                            fontFamily = com.zivaa.app.ui.theme.InstrumentSerif,
                            fontSize = 22.sp,
                            color = device.initialColor,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            style = ProfileTheme.typography.cardTitle
                        )
                        Text(
                            text = device.statusText,
                            style = ProfileTheme.typography.cardSubtitle,
                            color = ProfileTheme.colors.textSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(device.indicatorColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = device.indicatorText,
                            style = ProfileTheme.typography.meta,
                            color = ProfileTheme.colors.textSecondary
                        )
                    }
                }
                
                HorizontalDivider(
                    color = ProfileTheme.colors.divider,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // "Pair a new device" row
            val strokeColor = ProfileTheme.colors.textTertiary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddClick() }
            ) {
                // Dashed rounded rect with +
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .drawBehind {
                            drawRoundRect(
                                color = strokeColor,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
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
                        contentDescription = "Pair a new device",
                        tint = ProfileTheme.colors.accentGreen
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Pair a new device",
                    style = ProfileTheme.typography.cardTitle.copy(color = ProfileTheme.colors.accentGreen)
                )
            }
        }
    }
}
