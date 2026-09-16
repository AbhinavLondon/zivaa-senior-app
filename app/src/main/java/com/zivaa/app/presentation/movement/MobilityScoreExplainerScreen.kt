package com.zivaa.app.presentation.movement

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun MobilityScoreExplainerScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    Scaffold(
        containerColor = ZivaaTheme.colors.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Top Navigation Bar
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ZivaaTheme.colors.surfaceCard)
                            .border(1.dp, ZivaaTheme.colors.line, CircleShape)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZivaaTheme.colors.ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "MOVEMENT GUIDE",
                            style = ZivaaTheme.typography.eyebrow.copy(
                                fontSize = 11.sp,
                                letterSpacing = 0.08.sp
                            ),
                            color = ZivaaTheme.colors.sage
                        )
                        Text(
                            text = "How Your Score Works",
                            style = ZivaaTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ZivaaTheme.colors.ink
                        )
                    }
                }
            }

            // 2. Visual Hero Card: The 4 Pieces of the Puzzle
            item {
                VisualHeroCard()
            }

            // 3. Section Title
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "THE 4 MOVEMENT HABITS",
                        style = ZivaaTheme.typography.eyebrow.copy(fontSize = 11.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                    Text(
                        text = "What Builds Your 100 Points",
                        style = ZivaaTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ZivaaTheme.colors.ink
                    )
                }
            }

            // 4. Habit 1: Step Volume (40 pts)
            item {
                GraphicalPillarCard(
                    pillarNumber = "1",
                    title = "Step Volume",
                    weightBadge = "40 pts",
                    accentColor = Color(0xFF4EAE7B),
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    targetText = "7,000 steps daily",
                    oneLiner = "Covers your daily distance to keep your heart strong and build stamina.",
                    takeaway = "Every single step counts toward your score throughout the day.",
                    tiers = listOf(
                        PillarTier("1,750", "10 pts", 0.25f),
                        PillarTier("3,500", "20 pts", 0.50f),
                        PillarTier("5,250", "30 pts", 0.75f),
                        PillarTier("7,000+", "40 pts", 1.0f)
                    ),
                    detailedRows = listOf(
                        "100% of Step Goal (7,000+ steps)" to "40 pts",
                        "75% of Goal (5,250 steps)" to "30 pts",
                        "50% of Goal (3,500 steps)" to "20 pts",
                        "25% of Goal (1,750 steps)" to "10 pts"
                    )
                )
            }

            // 5. Habit 2: Walking Pace (20 pts)
            item {
                GraphicalPillarCard(
                    pillarNumber = "2",
                    title = "Pace Quality",
                    weightBadge = "20 pts",
                    accentColor = Color(0xFF658A73),
                    icon = Icons.Default.Bolt,
                    targetText = "85+ steps per minute",
                    oneLiner = "Walking with purpose trains balance reflexes and prevents falls.",
                    takeaway = "Even 2 to 3 minutes of brisker walking locks in your full 20 points.",
                    tiers = listOf(
                        PillarTier("Gentle (<50)", "5 pts", 0.25f),
                        PillarTier("Moderate (50-69)", "10 pts", 0.50f),
                        PillarTier("Steady (70-84)", "15 pts", 0.75f),
                        PillarTier("Brisk (85+ spm)", "20 pts", 1.0f)
                    ),
                    detailedRows = listOf(
                        "85+ steps/min (Brisk & Confident)" to "20 pts",
                        "70 – 84 steps/min (Steady & Stable)" to "15 pts",
                        "50 – 69 steps/min (Moderate Mobility)" to "10 pts",
                        "30 – 49 steps/min (Gentle Stroll)" to "5 pts",
                        "Under 30 steps/min (Resting / Light)" to "2 pts"
                    )
                )
            }

            // 6. Habit 3: Active Moving Time (20 pts)
            item {
                GraphicalPillarCard(
                    pillarNumber = "3",
                    title = "Active Time",
                    weightBadge = "20 pts",
                    accentColor = Color(0xFFD89B48),
                    icon = Icons.Default.HourglassBottom,
                    targetText = "30+ minutes on feet",
                    oneLiner = "Weight-bearing time lubricates joints and maintains bone density.",
                    takeaway = "Combines structured walks with everyday chores and strolls.",
                    tiers = listOf(
                        PillarTier("1-9 mins", "5 pts", 0.25f),
                        PillarTier("10-19 mins", "10 pts", 0.50f),
                        PillarTier("20-29 mins", "15 pts", 0.75f),
                        PillarTier("30+ mins", "20 pts", 1.0f)
                    ),
                    detailedRows = listOf(
                        "30+ minutes (Clinical Daily Target)" to "20 pts",
                        "20 – 29 minutes" to "15 pts",
                        "10 – 19 minutes" to "10 pts",
                        "1 – 9 minutes" to "5 pts",
                        "0 minutes" to "0 pts"
                    )
                )
            }

            // 7. Habit 4: Regularity (20 pts)
            item {
                GraphicalPillarCard(
                    pillarNumber = "4",
                    title = "Regularity",
                    weightBadge = "20 pts",
                    accentColor = Color(0xFF709285),
                    icon = Icons.Default.Schedule,
                    targetText = "8 active hours (8a–8p)",
                    oneLiner = "Standing up once an hour prevents muscle stiffness and boosts circulation.",
                    takeaway = "Banked hours are locked in — your score never regresses in the afternoon.",
                    tiers = listOf(
                        PillarTier("2 hrs", "5 pts", 0.25f),
                        PillarTier("4 hrs", "10 pts", 0.50f),
                        PillarTier("6 hrs", "15 pts", 0.75f),
                        PillarTier("8+ hrs", "20 pts", 1.0f)
                    ),
                    detailedRows = listOf(
                        "8+ active hours (or 6+ for Gentle tier)" to "20 pts",
                        "6 – 7 active hours (or 5 for Gentle tier)" to "15 pts",
                        "4 – 5 active hours (or 3–4 for Gentle tier)" to "10 pts",
                        "2 – 3 active hours (or 2 for Gentle tier)" to "5 pts",
                        "Under 2 active hours" to "2 pts"
                    ),
                    footnote = "Requires 150 steps/hr on smartwatch, or 100 steps/hr on phone."
                )
            }

            // 8. Visual Mobility Tiers Card
            item {
                VisualMobilityTiersCard()
            }

            // 9. Quick Action Tips
            item {
                VisualActionTipsCard()
            }

            // 10. Done Button
            item {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZivaaTheme.colors.sage,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Back to Movement",
                        style = ZivaaTheme.typography.cardTitle.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.navigationBarsPadding().height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 1. Visual Hero Card: 4 Parts of the Score
// ---------------------------------------------------------------------------
@Composable
private fun VisualHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(1.dp, ZivaaTheme.colors.line, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AT A GLANCE",
                    style = ZivaaTheme.typography.eyebrow.copy(fontSize = 11.sp),
                    color = ZivaaTheme.colors.sage
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ZivaaTheme.colors.sage.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Total 100 Points",
                        style = ZivaaTheme.typography.meta.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ZivaaTheme.colors.sage
                    )
                }
            }

            Text(
                text = "Steps measure distance. Mobility Score measures vitality.",
                style = ZivaaTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                ),
                color = ZivaaTheme.colors.ink
            )

            // Visual Segmented Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(40f)
                            .fillMaxHeight()
                            .background(Color(0xFF4EAE7B))
                    )
                    Box(
                        modifier = Modifier
                            .weight(20f)
                            .fillMaxHeight()
                            .background(Color(0xFF658A73))
                    )
                    Box(
                        modifier = Modifier
                            .weight(20f)
                            .fillMaxHeight()
                            .background(Color(0xFFD89B48))
                    )
                    Box(
                        modifier = Modifier
                            .weight(20f)
                            .fillMaxHeight()
                            .background(Color(0xFF709285))
                    )
                }

                // Legend row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = Color(0xFF4EAE7B), label = "Steps", points = "40 pts")
                    LegendItem(color = Color(0xFF658A73), label = "Pace", points = "20 pts")
                    LegendItem(color = Color(0xFFD89B48), label = "Time", points = "20 pts")
                    LegendItem(color = Color(0xFF709285), label = "Regularity", points = "20 pts")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, points: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                style = ZivaaTheme.typography.meta.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = ZivaaTheme.colors.ink
            )
            Text(
                text = points,
                style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp),
                color = ZivaaTheme.colors.inkMute
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 2. Graphical Pillar Card (Visual Meter & Friendly Explanation)
// ---------------------------------------------------------------------------
private data class PillarTier(
    val label: String,
    val points: String,
    val progressFraction: Float
)

@Composable
private fun GraphicalPillarCard(
    pillarNumber: String,
    title: String,
    weightBadge: String,
    accentColor: Color,
    icon: ImageVector,
    targetText: String,
    oneLiner: String,
    takeaway: String,
    tiers: List<PillarTier>,
    detailedRows: List<Pair<String, String>>,
    footnote: String? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isDark = ZivaaTheme.colors.isDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(1.dp, ZivaaTheme.colors.line, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "HABIT $pillarNumber",
                            style = ZivaaTheme.typography.eyebrow.copy(fontSize = 10.sp),
                            color = ZivaaTheme.colors.inkMute
                        )
                        Text(
                            text = title,
                            style = ZivaaTheme.typography.titleLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ZivaaTheme.colors.ink
                        )
                    }
                }

                // Weight Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor.copy(alpha = 0.18f))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = weightBadge,
                        style = ZivaaTheme.typography.meta.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor
                    )
                }
            }

            // Target Highlight Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.08f))
                    .border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🎯", fontSize = 14.sp)
                    Text(
                        text = "Daily Target: $targetText",
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = accentColor
                    )
                }
            }

            // One-liner
            Text(
                text = oneLiner,
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = ZivaaTheme.colors.inkSoft
            )

            // Visual Tier Progression Steps
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "POINT TIERS",
                    style = ZivaaTheme.typography.meta.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    color = ZivaaTheme.colors.inkMute
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tierBoxBg = if (isDark) Color.White.copy(alpha = 0.04f) else ZivaaTheme.colors.bg.copy(alpha = 0.8f)
                    val tierBoxBorder = if (isDark) accentColor.copy(alpha = 0.25f) else accentColor.copy(alpha = 0.35f)
                    tiers.forEach { tier ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tierBoxBg)
                                .border(1.dp, tierBoxBorder, RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tier.points,
                                    style = ZivaaTheme.typography.cardTitle.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = accentColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tier.label,
                                    style = ZivaaTheme.typography.meta.copy(fontSize = 9.5.sp),
                                    color = ZivaaTheme.colors.inkMute,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Friendly Takeaway
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "💡", fontSize = 13.sp, modifier = Modifier.padding(top = 1.dp))
                Text(
                    text = takeaway,
                    style = ZivaaTheme.typography.meta.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = ZivaaTheme.colors.inkSoft
                )
            }

            if (!footnote.isNullOrBlank()) {
                Text(
                    text = footnote,
                    style = ZivaaTheme.typography.meta.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    color = ZivaaTheme.colors.inkMute
                )
            }

            // Collapsible "How It's Calculated" Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide detailed scoring" else "See detailed scoring",
                    style = ZivaaTheme.typography.meta.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = accentColor
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Collapsible Details Table
            AnimatedVisibility(visible = isExpanded) {
                val tableBg = if (isDark) Color.White.copy(alpha = 0.03f) else ZivaaTheme.colors.bg.copy(alpha = 0.8f)
                val tableBorder = if (isDark) Color.White.copy(alpha = 0.06f) else ZivaaTheme.colors.line
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tableBg)
                        .border(1.dp, tableBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    detailedRows.forEach { (range, pts) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = range,
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = ZivaaTheme.colors.inkSoft,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = pts,
                                style = ZivaaTheme.typography.cardTitle.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ZivaaTheme.colors.ink
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Visual Mobility Tiers Card
// ---------------------------------------------------------------------------
@Composable
private fun VisualMobilityTiersCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(1.dp, ZivaaTheme.colors.line, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "WHAT YOUR DAILY SCORE MEANS",
                style = ZivaaTheme.typography.eyebrow.copy(fontSize = 11.sp),
                color = ZivaaTheme.colors.inkMute
            )

            Text(
                text = "The 4 Mobility Tiers",
                style = ZivaaTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ZivaaTheme.colors.ink
            )

            TierCardItem(
                badge = "85 – 100",
                emoji = "🌟",
                title = "Optimal Mobility",
                subtitle = "Peak vitality & independent everyday energy.",
                color = Color(0xFF4EAE7B)
            )

            TierCardItem(
                badge = "70 – 84",
                emoji = "🌿",
                title = "Steady & Active",
                subtitle = "Strong baseline with consistent healthy movement.",
                color = Color(0xFF658A73)
            )

            TierCardItem(
                badge = "50 – 69",
                emoji = "🌼",
                title = "Building Rhythm",
                subtitle = "Healthy base with easy room to add pace or breaks.",
                color = Color(0xFFD89B48)
            )

            TierCardItem(
                badge = "0 – 49",
                emoji = "🛋️",
                title = "Gentle Mobility",
                subtitle = "Rest or recovery day — gentle pacing for joints.",
                color = Color(0xFF8A9A86)
            )
        }
    }
}

@Composable
private fun TierCardItem(
    badge: String,
    emoji: String,
    title: String,
    subtitle: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.07f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 20.sp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ZivaaTheme.typography.cardTitle.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ZivaaTheme.colors.ink
            )
            Text(
                text = subtitle,
                style = ZivaaTheme.typography.meta.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                color = ZivaaTheme.colors.inkSoft
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                style = ZivaaTheme.typography.meta.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. Quick Action Tips (3 Ways to Lift Your Score)
// ---------------------------------------------------------------------------
@Composable
private fun VisualActionTipsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(1.dp, ZivaaTheme.colors.line, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ZivaaTheme.colors.sage,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "3 Simple Ways to Lift Your Score",
                    style = ZivaaTheme.typography.titleLarge.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ZivaaTheme.colors.ink
                )
            }

            ActionTipCard(
                emoji = "👟",
                headline = "Add a 10-Minute Walk",
                body = "Adds ~800 steps, lifting both your Step Volume and Active Time scores simultaneously."
            )

            ActionTipCard(
                emoji = "⚡",
                headline = "Step with Purpose",
                body = "Walking slightly brisker for just 2 to 3 minutes unlocks the full 20 Pace Quality points."
            )

            ActionTipCard(
                emoji = "⏰",
                headline = "Stand Once an Hour",
                body = "A 90-second stroll around the house breaks up sitting and locks in your Regularity hours."
            )
        }
    }
}

@Composable
private fun ActionTipCard(
    emoji: String,
    headline: String,
    body: String
) {
    val isDark = ZivaaTheme.colors.isDark
    val cardBg = if (isDark) Color.White.copy(alpha = 0.03f) else ZivaaTheme.colors.bg.copy(alpha = 0.8f)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.06f) else ZivaaTheme.colors.line
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = emoji, fontSize = 20.sp)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = headline,
                style = ZivaaTheme.typography.cardTitle.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ZivaaTheme.colors.ink
            )
            Text(
                text = body,
                style = ZivaaTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                ),
                color = ZivaaTheme.colors.inkSoft
            )
        }
    }
}
