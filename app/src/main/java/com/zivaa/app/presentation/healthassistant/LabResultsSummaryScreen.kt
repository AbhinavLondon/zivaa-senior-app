package com.zivaa.app.presentation.healthassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.IBMPlexMono
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun LabResultsSummaryScreen(
    onNavigateBack: () -> Unit,
    onAskAboutResults: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val bgColors = ZivaaTheme.colors
    val typography = ZivaaTheme.typography
    val isDark = isSystemInDarkTheme()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, bgColors.ink.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColors.bgElev)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Back",
                    tint = bgColors.textStrong
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "FULL-BODY CHECK - 18 JUN - VS 12 MAR",
                style = typography.eyebrow,
                fontFamily = IBMPlexMono,
                color = bgColors.textMeta
            )
        }

        // Header
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Mostly good news, Ranjit.",
                style = typography.displayMedium.copy(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) bgColors.ink else Color(0xFF1D211E)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your full results are in. Here's the plain-language picture, and how each number has moved since March.",
                style = typography.bodyLarge,
                color = bgColors.textBody
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(bgColors.muted.copy(alpha = 0.5f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColors.bgElev)
                    .clickable { /* Toggle logic */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Simple",
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = bgColors.textStrong
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { /* Toggle logic */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Clinical",
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = bgColors.textBody
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Card
        val cardBg = if (isDark) Color(0xFF1A332B) else Color(0xFF234B3F)
        val cardText = if (isDark) Color(0xFFE9E5DD) else Color(0xFFF6F3EE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(cardBg)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(cardText.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        style = typography.cardTitle.copy(
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            color = cardText
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SAHAYAK - PLAIN SUMMARY",
                    style = typography.eyebrow,
                    fontFamily = IBMPlexMono,
                    color = cardText.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "I've gone through your full check. The big picture is reassuring — your sugar control has clearly improved since March. Two things are worth a gentle look: your cholesterol and your vitamin D. Nothing here is urgent.",
                style = typography.leadParagraph.copy(fontSize = 18.sp, lineHeight = 26.sp),
                color = cardText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(bgColors.textStrong)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("NOW", style = typography.meta, fontFamily = IBMPlexMono, color = bgColors.textMeta)

            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, bgColors.textStrong, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("IN MARCH", style = typography.meta, fontFamily = IBMPlexMono, color = bgColors.textMeta)

            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(bgColors.muted)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("HEALTHY", style = typography.meta, fontFamily = IBMPlexMono, color = bgColors.textMeta)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section: Worth a gentle look
        Row(modifier = Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(bgColors.toneWatch))
            Spacer(modifier = Modifier.width(8.dp))
            Text("WORTH A GENTLE LOOK", style = typography.eyebrow, fontFamily = IBMPlexMono, color = bgColors.toneWatch)
        }
        Spacer(modifier = Modifier.height(16.dp))

        MetricCard(
            title = "LDL cholesterol",
            subtitle = "The 'bad' cholesterol",
            value = "138",
            unit = "MG/DL",
            trend = "↑ from 120",
            trendColor = bgColors.toneWatch,
            chipText = "Worth a look",
            chipBg = bgColors.toneWatch.copy(alpha = 0.15f),
            chipTextCol = bgColors.toneWatch,
            healthyText = "HEALTHY 60–100 MG/DL",
            description = "Higher than in March. A little less ghee and fried food — and the evening walks — will bring it down.",
            rangeStart = 0.1f,
            rangeEnd = 0.4f,
            prevPos = 0.45f,
            currentPos = 0.55f
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetricCard(
            title = "Vitamin D",
            subtitle = "For bones and energy",
            value = "22",
            unit = "NG/ML",
            trend = "↑ from 18",
            trendColor = bgColors.toneWatch,
            chipText = "Still low",
            chipBg = bgColors.toneWatch.copy(alpha = 0.15f),
            chipTextCol = bgColors.toneWatch,
            healthyText = "HEALTHY 30–60 NG/ML",
            description = "Climbing in the right direction, but still below ideal. A little morning sun and it will keep rising.",
            rangeStart = 0.4f,
            rangeEnd = 0.9f,
            prevPos = 0.2f,
            currentPos = 0.3f
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricCard(
            title = "Blood pressure",
            subtitle = "Clinic reading",
            value = "138/86",
            unit = "MMHG",
            trend = "↑ from 132/82",
            trendColor = bgColors.toneWatch,
            chipText = "Slightly high",
            chipBg = bgColors.toneWatch.copy(alpha = 0.15f),
            chipTextCol = bgColors.toneWatch,
            healthyText = "HEALTHY 100–130 MMHG",
            description = "A touch higher than last time. Worth a quick word with Dr. Mehta at the next review.",
            rangeStart = 0.1f,
            rangeEnd = 0.4f,
            prevPos = 0.45f,
            currentPos = 0.5f
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Section: Improving & steady
        Row(modifier = Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(bgColors.toneOk))
            Spacer(modifier = Modifier.width(8.dp))
            Text("IMPROVING & STEADY", style = typography.eyebrow, fontFamily = IBMPlexMono, color = bgColors.toneOk)
        }
        Spacer(modifier = Modifier.height(16.dp))

        MetricCard(
            title = "HbA1c",
            subtitle = "Three-month sugar average",
            value = "6.8",
            unit = "%",
            trend = "↓ from 7.2%",
            trendColor = bgColors.toneOk,
            chipText = "Improved",
            chipBg = bgColors.toneOk.copy(alpha = 0.15f),
            chipTextCol = bgColors.toneOk,
            healthyText = "HEALTHY 5–7 %",
            description = "Down from 7.2 — and now under the 7% mark. The diet changes are clearly working.",
            rangeStart = 0.1f,
            rangeEnd = 0.5f,
            prevPos = 0.6f,
            currentPos = 0.45f
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricCard(
            title = "HDL cholesterol",
            subtitle = "The 'good' cholesterol",
            value = "44",
            unit = "MG/DL",
            trend = "↑ from 41",
            trendColor = bgColors.textMeta,
            chipText = "Good",
            chipBg = bgColors.toneOk.copy(alpha = 0.15f),
            chipTextCol = bgColors.toneOk,
            healthyText = "HEALTHY 40–70 MG/DL",
            description = "A little higher than before — with this one, higher is exactly what we want.",
            rangeStart = 0.3f,
            rangeEnd = 0.9f,
            prevPos = 0.4f,
            currentPos = 0.45f
        )

        Spacer(modifier = Modifier.height(48.dp))

        // What it means section
        Text(
            text = "WHAT IT MEANS, DAY TO DAY",
            style = typography.eyebrow,
            fontFamily = IBMPlexMono,
            color = bgColors.textMeta,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(bgColors.bgElev)
                .padding(24.dp)
        ) {
            ChecklistItem("A little less ghee and fried food — it nudges the cholesterol down.")
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 12.dp).background(bgColors.line))
            ChecklistItem("Ten quiet minutes of morning sun for the vitamin D.")
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 12.dp).background(bgColors.line))
            ChecklistItem("Keep the daily walks and the lighter meals — the sugar numbers show they work.")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(bgColors.sage)
                    .clickable { onAskAboutResults() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = bgColors.sageInk,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ask about these results",
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = bgColors.sageInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, bgColors.ink.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    .background(bgColors.bgElev)
                    .clickable { onShare() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.IosShare,
                        contentDescription = null,
                        tint = bgColors.textStrong,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share with Dr. Mehta",
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = bgColors.textStrong
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "This summary helps you understand the report — it doesn't replace a doctor's advice.",
            style = typography.meta.copy(fontSize = 12.sp, lineHeight = 16.sp),
            color = bgColors.textMeta,
            modifier = Modifier.padding(horizontal = 48.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun ChecklistItem(text: String) {
    val bgColors = ZivaaTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(bgColors.sage.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = bgColors.sage.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = ZivaaTheme.typography.bodyMedium.copy(color = bgColors.textStrong)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    subtitle: String,
    value: String,
    unit: String,
    trend: String,
    trendColor: Color,
    chipText: String,
    chipBg: Color,
    chipTextCol: Color,
    healthyText: String,
    description: String,
    rangeStart: Float,
    rangeEnd: Float,
    prevPos: Float,
    currentPos: Float
) {
    val bgColors = ZivaaTheme.colors
    val typography = ZivaaTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColors.bgElev)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = title,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = bgColors.textStrong
                )
                Text(
                    text = subtitle,
                    style = typography.bodyMedium,
                    color = bgColors.textBody
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(chipBg)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = chipText,
                    style = typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = chipTextCol
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = typography.displayMedium.copy(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    color = bgColors.textStrong,
                    lineHeight = 32.sp
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = unit,
                style = typography.eyebrow,
                fontFamily = IBMPlexMono,
                color = bgColors.textMeta,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = trend,
                style = typography.eyebrow,
                fontFamily = IBMPlexMono,
                color = trendColor,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Slider Graphic
        val trackColor = bgColors.lineStrong
        val healthyColor = bgColors.muted
        val prevColor = bgColors.ink.copy(alpha = 0.5f)
        val currentColor = bgColors.ink

        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            val barHeight = 4.dp.toPx()
            val yCenter = size.height / 2f

            // Full track
            drawLine(
                color = trackColor,
                start = Offset(0f, yCenter),
                end = Offset(size.width, yCenter),
                strokeWidth = barHeight,
                cap = StrokeCap.Round
            )

            // Healthy Range
            val hStart = size.width * rangeStart
            val hEnd = size.width * rangeEnd
            drawLine(
                color = healthyColor,
                start = Offset(hStart, yCenter),
                end = Offset(hEnd, yCenter),
                strokeWidth = barHeight,
                cap = StrokeCap.Round
            )

            // Previous Marker (Outline)
            val pX = size.width * prevPos
            drawCircle(
                color = prevColor,
                radius = 6.dp.toPx(),
                center = Offset(pX, yCenter),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = bgColors.bgElev, // Clear inside
                radius = 4.5.dp.toPx(),
                center = Offset(pX, yCenter)
            )

            // Current Marker (Filled)
            val cX = size.width * currentPos
            drawCircle(
                color = currentColor,
                radius = 6.dp.toPx(),
                center = Offset(cX, yCenter)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = healthyText,
            style = typography.meta,
            fontFamily = IBMPlexMono,
            color = bgColors.textMeta
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = typography.bodyMedium,
            color = bgColors.textBody
        )
    }
}
