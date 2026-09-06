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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@Composable
fun LabPanelDetailsScreen(
    panelId: String,
    onBack: () -> Unit,
    onNavigateToScheduling: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val scrollState = rememberScrollState()

    val panel = LabPanels.getPanelById(panelId) ?: LabPanels.panels.first()

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
                    text = panel.category,
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
                        text = panel.title,
                        style = typography.displayMedium.copy(fontSize = 38.sp),
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = panel.description,
                        style = typography.bodyMedium,
                        color = colors.inkSoft
                    )
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PanelStatCard(panel.testsCount, "TESTS", modifier = Modifier.weight(1f))
                    PanelStatCard(panel.hoursToReport, "REPORTS IN", modifier = Modifier.weight(1f))
                    PanelStatCard(panel.price, "ALL IN", modifier = Modifier.weight(1f))
                }

                // Fasting Warning
                panel.fastingInfo?.let { fastingInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEBE3D5)) // A specific warm highlight color
                            .border(0.5.dp, Color(0xFFD3C8B3), RoundedCornerShape(16.dp))
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = colors.clay,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = fastingInfo,
                            style = typography.bodyMedium,
                            color = colors.ink
                        )
                    }
                }

                // What's Included
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "WHAT'S INCLUDED",
                        style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                        color = colors.inkMute,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    panel.includedTests.forEachIndexed { index, test ->
                        IncludedTestItem(test.title, test.subtitle)
                        if (index < panel.includedTests.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 190.dp)) // padding for bottom CTA
            }
        }

        // Bottom CTA Sticky
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { onNavigateToScheduling() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .zivaaShadow(cornerRadius = 999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceHero),
                shape = CircleShape
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Choose a pickup time",
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    Box(
        modifier = modifier
            .zivaaShadow(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bg)
            .border(0.5.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = typography.cardTitle.copy(fontSize = 24.sp),
                color = colors.ink
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = typography.meta.copy(fontSize = 8.5.sp, letterSpacing = 0.08.em),
                color = colors.inkSoft
            )
        }
    }
}

@Composable
private fun IncludedTestItem(title: String, subtitle: String) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zivaaShadow(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bg)
            .border(0.5.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.sage.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.sage,
                modifier = Modifier.size(14.dp)
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
    }
}
