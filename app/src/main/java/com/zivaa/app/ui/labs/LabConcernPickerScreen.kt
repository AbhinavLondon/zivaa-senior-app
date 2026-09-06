package com.zivaa.app.ui.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@Composable
fun LabConcernPickerScreen(
    onBack: () -> Unit,
    onNavigateToPanelDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bgElev,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.bg)
                        .border(0.5.dp, colors.line, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.ink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "PICK BY CONCERN",
                    style = typography.meta.copy(fontSize = 11.sp, letterSpacing = 0.08.em),
                    color = colors.inkMute
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Title
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("What's the ")
                            withStyle(style = SpanStyle(fontFamily = typography.displayMedium.fontFamily, fontStyle = FontStyle.Italic, color = colors.sage)) {
                                append("worry")
                            }
                            append("?")
                        },
                        style = typography.displayMedium.copy(fontSize = 38.sp),
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap the one closest. We'll choose the right tests — no medical words needed.",
                        style = typography.bodyMedium,
                        color = colors.inkSoft
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of concerns
                ConcernCard(
                    icon = Icons.Outlined.WaterDrop,
                    iconBg = Color(0xFFD39A52), // Orange/Gold
                    title = "Sugar & diabetes",
                    subtitle = "HbA1c, fasting & post-meal sugar",
                    onClick = { onNavigateToPanelDetails("diabetes") }
                )

                ConcernCard(
                    icon = Icons.Outlined.MonitorHeart,
                    iconBg = Color(0xFFB56A49), // Clay/Red
                    title = "Heart & cholesterol",
                    subtitle = "Lipid profile — the good & bad kinds",
                    onClick = { onNavigateToPanelDetails("heart") }
                )

                ConcernCard(
                    icon = Icons.Outlined.WbSunny,
                    iconBg = colors.sage, // Dark green
                    title = "Full body check-up",
                    subtitle = "Everything, head to toe",
                    onClick = { onNavigateToPanelDetails("full_body") }
                )

                ConcernCard(
                    icon = Icons.Outlined.Speed,
                    iconBg = colors.leaf, // Lighter green
                    title = "Kidney, liver & thyroid",
                    subtitle = "How the organs are coping",
                    onClick = { onNavigateToPanelDetails("organs") }
                )

                ConcernCard(
                    icon = Icons.Outlined.EnergySavingsLeaf,
                    iconBg = Color(0xFFD39A52), // Orange/Gold
                    title = "Tiredness & vitamins",
                    subtitle = "Vitamin D, B12, iron",
                    onClick = { onNavigateToPanelDetails("vitamins") }
                )

                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }
}

@Composable
private fun ConcernCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zivaaShadow(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bg)
            .border(0.5.dp, colors.line, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.ink
            )
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.inkSoft,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.inkMute,
            modifier = Modifier.size(20.dp)
        )
    }
}
