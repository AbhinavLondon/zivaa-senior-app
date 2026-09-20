package com.zivaa.app.presentation.movement

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlin.math.roundToInt

import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.compose.component.marker.markerComponent
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovementScreen(
    viewModel: MovementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExplainer: () -> Unit = {}
) {
    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    LaunchedEffect(Unit) {
        viewModel.fetchMovementData()
    }

    val lazyListState = rememberLazyListState()
    var showStepsGoalPopup by remember { mutableStateOf(false) }

    if (showStepsGoalPopup) {
        com.zivaa.app.presentation.components.StepsGoalBottomSheet(
            initialGoal = viewModel.goalSteps,
            onDismiss = { showStepsGoalPopup = false },
            onSave = { newGoal -> 
                viewModel.saveStepsGoal(newGoal)
                showStepsGoalPopup = false 
            }
        )
    }

    Scaffold(
        containerColor = ZivaaTheme.colors.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
            state = lazyListState
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ZivaaTheme.colors.inkMute,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onNavigateBack() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Movement · Today",
                        style = ZivaaTheme.typography.eyebrow,
                        color = ZivaaTheme.colors.eyebrow
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mobility Score Card (Arc Dialer + Contributing Factors)
                MobilityScoreCard(
                    viewModel = viewModel,
                    onAddGoalClick = { showStepsGoalPopup = true },
                    onInfoClick = onNavigateToExplainer
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Hourly Steps Chart Card
                HourlyStepsChartCard(viewModel = viewModel)

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sticky Filter Bars (7 Days | 30 Days | 3 Months and Bar | Line)
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZivaaTheme.colors.bg)
                        .padding(vertical = 10.dp)
                ) {
                    Column {
                        // Time Range Filter (7 Days, 30 Days, 3 Months)
                        TimeRangeFilterBar(
                            selectedRange = viewModel.selectedTimeRange,
                            onRangeSelected = { viewModel.setTimeRange(it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Chart Type Selector (Bar vs Line)
                        ChartTypeFilterBar(
                            selectedType = viewModel.selectedChartType,
                            onTypeSelected = { viewModel.setChartType(it) }
                        )
                    }
                }
            }

            // 0. Mobility Score (Overall 0-100)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                WeeklyMobilityScoreChartCard(
                    viewModel = viewModel,
                    onInfoClick = onNavigateToExplainer
                )
            }

            // 1. Total Steps
            item {
                Spacer(modifier = Modifier.height(20.dp))
                WeeklyStepsChartCard(viewModel = viewModel)
            }

            // 2. Walking Cadence (spm)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                WeeklyCadenceChartCard(viewModel = viewModel)
            }

            // 3. Active Moving Time (minutes)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                WeeklyActiveMinutesChartCard(viewModel = viewModel)
            }

            // 4. Movement Regularity (active hours)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                WeeklyActiveHoursChartCard(viewModel = viewModel)
            }

            // Footer Text
            item {
                Text(
                    text = "Every step here counts itself — you just keep walking. We'll tally again tomorrow.",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = (12.5 * 1.55).sp),
                    color = ZivaaTheme.colors.inkMute,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 36.dp)
                )
                
                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp))
            }
        }
    }
}

@Composable
fun MovementHeroCard(viewModel: MovementViewModel, onAddGoalClick: () -> Unit) {
    val mobility = viewModel.mobilitySummary
    val totalSteps = mobility?.formattedSteps ?: viewModel.totalStepsToday
    val goalSteps = mobility?.goalSteps ?: viewModel.goalSteps
    val safeGoal = goalSteps?.toFloat() ?: 10000f
    val currentStepsInt = mobility?.totalSteps ?: (totalSteps.replace(",", "").toIntOrNull() ?: 0)
    val progress = if (goalSteps == null) 0f else currentStepsInt / safeGoal
    val safeProgress = progress.coerceIn(0f, 1f)

    var targetProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(safeProgress) {
        targetProgress = safeProgress
    }

    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1800,
            easing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
        ),
        label = "progressAnim"
    )

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 2000f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(3000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    val shimmerBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(
            ZivaaTheme.colors.sageInk.copy(alpha = 0.3f),
            ZivaaTheme.colors.sageInk,
            ZivaaTheme.colors.sageInk.copy(alpha = 0.3f)
        ),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 400f, 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.15f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(ZivaaTheme.colors.sage)
            .padding(22.dp)
    ) {
        Column {
            // ── TOP HEADER: Pacing Tier & Primary Source ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cadence / Pacing Pill
                val cadenceTier = mobility?.cadenceTier ?: CadenceTier.RESTING
                val pillText = "${cadenceTier.icon} ${cadenceTier.title}"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ZivaaTheme.colors.sageInk.copy(alpha = 0.12f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = pillText,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ZivaaTheme.colors.sageInk
                    )
                }

                // Source Badge
                val sourceLabel = mobility?.primarySourceLabel ?: viewModel.hourlyStepsSource ?: "Phone Pedometer"
                Text(
                    text = sourceLabel,
                    style = ZivaaTheme.typography.eyebrow.copy(fontSize = 11.sp),
                    color = ZivaaTheme.colors.sageInk.copy(alpha = 0.65f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── HERO VOLUME: Big Steps & Goal ──
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = totalSteps,
                    style = ZivaaTheme.typography.displayLarge.copy(fontSize = 54.sp, letterSpacing = (-1.5).sp),
                    color = ZivaaTheme.colors.sageInk
                )
                if (goalSteps != null) {
                    Text(
                        text = "Steps · Goal ${java.text.NumberFormat.getNumberInstance().format(goalSteps)}",
                        style = ZivaaTheme.typography.eyebrow,
                        color = ZivaaTheme.colors.sageInk.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, ZivaaTheme.colors.sageInk.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                            .clickable { onAddGoalClick() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Add Step Goal",
                            style = ZivaaTheme.typography.eyebrow,
                            color = ZivaaTheme.colors.sageInk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Distance & Active Energy Subtitle
            val distanceKmStr = String.format(
                java.util.Locale.US,
                "%.1f",
                mobility?.distanceKm ?: (currentStepsInt * 0.00072f)
            )
            val caloriesVal = mobility?.activeCaloriesKcal ?: (currentStepsInt * 0.04f).toInt()
            Text(
                text = "$distanceKmStr km covered · $caloriesVal kcal active",
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                color = ZivaaTheme.colors.sageInk.copy(alpha = 0.80f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── ANIMATED SHIMMER PROGRESS BAR ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ZivaaTheme.colors.ink.copy(alpha = 0.18f))
            ) {
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(shimmerBrush)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── TRI-METRIC DASHBOARD INSET (The 3 Core Pillars) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ZivaaTheme.colors.sageInk.copy(alpha = 0.09f))
                    .border(1.dp, ZivaaTheme.colors.sageInk.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp, horizontal = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Col 1: Time on Feet
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TIME ON FEET",
                            style = ZivaaTheme.typography.eyebrow.copy(fontSize = 9.sp, letterSpacing = 0.08.em),
                            color = ZivaaTheme.colors.sageInk.copy(alpha = 0.60f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${mobility?.activeMinutes ?: 0}m",
                            style = ZivaaTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                            color = ZivaaTheme.colors.sageInk
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Active walking",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                            color = ZivaaTheme.colors.sageInk.copy(alpha = 0.75f)
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(ZivaaTheme.colors.sageInk.copy(alpha = 0.15f))
                    )

                    // Col 2: Cadence
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CADENCE",
                            style = ZivaaTheme.typography.eyebrow.copy(fontSize = 9.sp, letterSpacing = 0.08.em),
                            color = ZivaaTheme.colors.sageInk.copy(alpha = 0.60f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if ((mobility?.cadenceSpm ?: 0) > 0) "${mobility?.cadenceSpm} spm" else "—",
                            style = ZivaaTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                            color = ZivaaTheme.colors.sageInk
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val cadenceSub = mobility?.cadenceTier?.title?.substringBefore(" &") ?: "Resting"
                        Text(
                            text = cadenceSub,
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                            color = ZivaaTheme.colors.sageInk.copy(alpha = 0.75f)
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(ZivaaTheme.colors.sageInk.copy(alpha = 0.15f))
                    )

                    // Col 3: Day Regularity
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DAY SPREAD",
                            style = ZivaaTheme.typography.eyebrow.copy(fontSize = 9.sp, letterSpacing = 0.08.em),
                            color = ZivaaTheme.colors.sageInk.copy(alpha = 0.60f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val activeHrs = mobility?.activeHoursCount ?: 0
                        val elapsedHrs = mobility?.elapsedDaytimeHours ?: 1
                        Text(
                            text = "$activeHrs/${elapsedHrs}h",
                            style = ZivaaTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                            color = ZivaaTheme.colors.sageInk
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mobility?.regularityStatus ?: "Daytime hrs",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 10.5.sp),
                            color = ZivaaTheme.colors.sageInk.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── CLINICAL NARRATIVE FOOTER ──
            val narrative = mobility?.clinicalNarrative
                ?: viewModel.heroInsightText.ifEmpty { viewModel.overGoalText }
            Text(
                text = narrative,
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                color = ZivaaTheme.colors.sageInk.copy(alpha = 0.90f)
            )
        }
    }
}

@Composable
fun MobilityScoreCard(
    viewModel: MovementViewModel,
    onAddGoalClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null
) {
    val isDark = ZivaaTheme.colors.isDark
    val mobility = viewModel.mobilitySummary
    val mobilityScore = mobility?.mobilityScore
    val scoreVal = mobilityScore?.overallScore ?: 0
    val tier = mobilityScore?.tier ?: MobilityTier.GENTLE

    // Tier-specific accent color matching Samsung Health aesthetic
    val tierColor = when (tier) {
        MobilityTier.OPTIMAL -> if (isDark) Color(0xFF4EAE7B) else Color(0xFF2E8555)     // Vibrant Sage / Emerald
        MobilityTier.STEADY -> if (isDark) Color(0xFF7CB89E) else Color(0xFF2E6B56)      // Steady Sage
        MobilityTier.BUILDING -> if (isDark) Color(0xFFE5A643) else Color(0xFFC97A1E)    // Warm Amber
        MobilityTier.GENTLE -> if (isDark) Color(0xFFE58B43) else Color(0xFFC85A24)      // Attention Orange
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = if (isDark) 20.dp else 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x0A000000),
                spotColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x0E000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(1.dp, ZivaaTheme.colors.line, RoundedCornerShape(24.dp))
            .padding(22.dp)
    ) {
        Column {
            // Card Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Mobility score",
                        style = ZivaaTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ZivaaTheme.colors.ink
                    )
                    if (onInfoClick != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.White.copy(alpha = 0.06f) else ZivaaTheme.colors.lineStrong.copy(alpha = 0.15f))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else ZivaaTheme.colors.lineStrong.copy(alpha = 0.35f), CircleShape)
                                .clickable { onInfoClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Mobility score calculation explanation",
                                tint = if (isDark) ZivaaTheme.colors.sage else Color(0xFF234B3F),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color(0x0D111111))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Score / 100",
                        style = ZivaaTheme.typography.eyebrow.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ZivaaTheme.colors.inkMute
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dialer Section (Horseshoe Arc + Center Score + Underneath Tag)
            MobilityArcDialer(
                score = scoreVal,
                tierTitle = tier.title,
                tierColor = tierColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Contributing Factors Header
            Text(
                text = "Mobility score factors",
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = ZivaaTheme.colors.ink
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Contributing Factors (2x2 Grid)
            val volFraction = ((mobilityScore?.volumeScore ?: 0) / 40f).coerceIn(0f, 1f)
            val paceFraction = ((mobilityScore?.paceScore ?: 0) / 20f).coerceIn(0f, 1f)
            val activeFraction = ((mobilityScore?.activeTimeScore ?: 0) / 20f).coerceIn(0f, 1f)
            val breaksFraction = ((mobilityScore?.regularityScore ?: 0) / 20f).coerceIn(0f, 1f)

            fun factorColor(fraction: Float): Color = when {
                fraction >= 0.75f -> if (isDark) Color(0xFF4EAE7B) else Color(0xFF2E8555) // Optimal Green
                fraction >= 0.45f -> if (isDark) Color(0xFFE5A643) else Color(0xFFC97A1E) // Warm Amber
                else -> if (isDark) Color(0xFFE58B43) else Color(0xFFC85A24)              // Attention Orange
            }

            fun factorStatus(fraction: Float): String = when {
                fraction >= 0.75f -> "Optimal"
                fraction >= 0.45f -> "Steady"
                else -> "Attention"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MobilityFactorTile(
                    modifier = Modifier.weight(1f),
                    title = "Step volume",
                    scoreText = "${mobilityScore?.volumeScore ?: 0}/40",
                    primaryValue = java.text.NumberFormat.getNumberInstance().format(mobility?.totalSteps ?: 0),
                    unitText = "steps",
                    progress = volFraction,
                    statusText = factorStatus(volFraction),
                    accentColor = factorColor(volFraction),
                    onClick = onAddGoalClick
                )
                MobilityFactorTile(
                    modifier = Modifier.weight(1f),
                    title = "Pace quality",
                    scoreText = "${mobilityScore?.paceScore ?: 0}/20",
                    primaryValue = if ((mobility?.cadenceSpm ?: 0) > 0) "${mobility?.cadenceSpm}" else "0",
                    unitText = if ((mobility?.cadenceSpm ?: 0) > 0) "spm" else "Resting",
                    progress = paceFraction,
                    statusText = factorStatus(paceFraction),
                    accentColor = factorColor(paceFraction)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val activeMins = mobility?.activeMinutes ?: 0
                val activePrimary = if (activeMins >= 60) "${activeMins / 60}h ${activeMins % 60}m" else "$activeMins"
                val activeUnit = if (activeMins >= 60) "" else "mins"

                MobilityFactorTile(
                    modifier = Modifier.weight(1f),
                    title = "Active time",
                    scoreText = "${mobilityScore?.activeTimeScore ?: 0}/20",
                    primaryValue = activePrimary,
                    unitText = activeUnit,
                    progress = activeFraction,
                    statusText = factorStatus(activeFraction),
                    accentColor = factorColor(activeFraction)
                )
                MobilityFactorTile(
                    modifier = Modifier.weight(1f),
                    title = "Regularity",
                    scoreText = "${mobilityScore?.regularityScore ?: 0}/20",
                    primaryValue = "${mobility?.activeHoursCount ?: 0}",
                    unitText = "of ${mobility?.targetActiveHours ?: 8} active hrs",
                    progress = breaksFraction,
                    statusText = factorStatus(breaksFraction),
                    accentColor = factorColor(breaksFraction)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Coaching Takeaway Sentence
            val takeaway = mobilityScore?.coachingTakeaway
                ?: "Regular daily walking bouts and movement regularity maintain functional mobility."
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0xFF19231F) else Color(0xFFEFF5F1))
                    .border(1.dp, if (isDark) Color(0xFF2E4239) else Color(0xFFD4E6DC), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "🌿", fontSize = 16.sp)
                    Text(
                        text = takeaway,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isDark) Color(0xFFBCE3D2) else Color(0xFF234B3F)
                    )
                }
            }
        }
    }
}

@Composable
private fun MobilityArcDialer(
    score: Int,
    tierTitle: String,
    tierColor: Color
) {
    val isDark = ZivaaTheme.colors.isDark
    val animatedScoreFraction by androidx.compose.animation.core.animateFloatAsState(
        targetValue = (score.toFloat() / 100f).coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1500,
            easing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
        ),
        label = "dialerProgress"
    )

    val trackColor = if (isDark) Color(0xFF2E3238) else Color(0xFFEBE7DF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(175.dp)) {
            val strokeWidthPx = 13.dp.toPx()
            val knobRadiusPx = 8.dp.toPx()
            val arcPadding = strokeWidthPx / 2f + knobRadiusPx
            val arcDiameter = size.minDimension - 2 * arcPadding
            val arcTopLeft = Offset(
                (size.width - arcDiameter) / 2f,
                (size.height - arcDiameter) / 2f
            )
            val arcSize = Size(arcDiameter, arcDiameter)
            val radius = arcDiameter / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f

            val startAngle = 135f
            val sweepTotal = 270f

            // 1. Background Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            )

            // 2. Active Progress Arc
            if (animatedScoreFraction > 0f) {
                drawArc(
                    color = tierColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal * animatedScoreFraction,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                )

                // 3. Indicator Knob Dot at current progress
                val currentAngleRad = Math.toRadians((startAngle + sweepTotal * animatedScoreFraction).toDouble())
                val knobX = cx + (radius * Math.cos(currentAngleRad)).toFloat()
                val knobY = cy + (radius * Math.sin(currentAngleRad)).toFloat()
                val knobCenter = Offset(knobX, knobY)

                // Outer halo
                drawCircle(
                    color = tierColor.copy(alpha = if (isDark) 0.35f else 0.22f),
                    radius = knobRadiusPx + 3.dp.toPx(),
                    center = knobCenter
                )
                // Main knob
                drawCircle(
                    color = tierColor,
                    radius = knobRadiusPx,
                    center = knobCenter
                )
                // Inner white highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 2.5.dp.toPx(),
                    center = knobCenter
                )
            }
        }

        // Center Content: Score Number and Underneath Pill Tag
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "$score",
                style = ZivaaTheme.typography.displayLarge.copy(
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.5).sp
                ),
                color = ZivaaTheme.colors.ink
            )
            Text(
                text = tierTitle,
                style = ZivaaTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = tierColor
            )
        }
    }
}

@Composable
private fun MobilityFactorTile(
    modifier: Modifier = Modifier,
    title: String,
    scoreText: String,
    primaryValue: String,
    unitText: String,
    progress: Float,
    statusText: String,
    accentColor: Color,
    onClick: (() -> Unit)? = null
) {
    val isDark = ZivaaTheme.colors.isDark
    val containerModifier = if (onClick != null) {
        modifier
            .height(132.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    } else {
        modifier
            .height(132.dp)
            .clip(RoundedCornerShape(18.dp))
    }

    val tileBg = if (isDark) Color(0xFF1C2025) else Color(0xFFF7F4EE)
    val tileBorder = if (isDark) Color(0xFF2E333D) else Color(0xFFE6E1D7)
    val titleColor = if (isDark) Color(0xFFD4D8E2) else Color(0xFF42474E)
    val scoreTextColor = if (isDark) ZivaaTheme.colors.inkMute.copy(alpha = 0.7f) else Color(0xFF7A7E85)
    val valueColor = if (isDark) Color.White else Color(0xFF14181B)
    val unitColor = if (isDark) Color(0xFFA0A6B2) else Color(0xFF6E727A)
    val trackColor = if (isDark) Color(0xFF282B33) else Color(0xFFE5E0D6)

    Box(
        modifier = containerModifier
            .background(tileBg)
            .border(1.dp, tileBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Row: Title & Score Contribution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = titleColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = scoreText,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = scoreTextColor,
                    maxLines = 1
                )
            }

            // 2. Big Primary Metric
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = primaryValue,
                    style = ZivaaTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = valueColor
                )
                if (unitText.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unitText,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = unitColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // 3. Progress Bar Line ("Keep the progress bar")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (progress <= 0f) 0f else progress.coerceIn(0.06f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor)
                )
            }

            // 4. Status Pill Tag (mirroring Samsung Health "Attention" pill)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) accentColor.copy(alpha = 0.18f) else accentColor.copy(alpha = 0.12f))
                    .border(1.dp, if (isDark) accentColor.copy(alpha = 0.35f) else accentColor.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusText,
                    style = ZivaaTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun HourlyStepsChartCard(viewModel: MovementViewModel) {
    val colors = ZivaaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            val default24Hours = listOf(
                "12A", "", "", "3A", "", "",
                "6A", "", "", "9A", "", "",
                "12P", "", "", "3P", "", "",
                "6P", "", "", "9P", "", ""
            )
            val hourlyData = if (viewModel.hourlySteps.isEmpty()) {
                default24Hours.map { it to 0 }
            } else {
                viewModel.hourlySteps
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Steps, Hour By Hour",
                    style = ZivaaTheme.typography.eyebrow.copy(
                        fontSize = 12.5.sp,
                        letterSpacing = 0.06.em
                    ),
                    color = ZivaaTheme.colors.eyebrow
                )
                viewModel.hourlyStepsSource?.let { src ->
                    Text(
                        text = "Source: $src",
                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        color = ZivaaTheme.colors.sage
                    )
                }
            }
            
            val maxSteps = hourlyData.maxOfOrNull { if (it.second.toDouble() < 0) 0 else it.second.toInt() } ?: 0
            val seriesList = hourlyData.mapIndexed { index, pair ->
                List(hourlyData.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index && pair.second.toDouble() >= 0) pair.second.toFloat() else 0f) }
            }
            val chartEntryModel = entryModelOf(*seriesList.toTypedArray())

            val bottomAxisValueFormatter = AxisValueFormatter<com.patrykandpatrick.vico.core.axis.AxisPosition.Horizontal.Bottom> { value, _ ->
                hourlyData.getOrNull(value.toInt())?.first ?: ""
            }

            val columns = List(hourlyData.size) { 
                lineComponent(color = ZivaaTheme.colors.sage, thickness = 7.dp, shape = Shapes.pillShape)
            }
            
            val labelFormatter = object : com.patrykandpatrick.vico.core.formatter.ValueFormatter {
                override fun formatValue(
                    value: Float,
                    chartValues: com.patrykandpatrick.vico.core.chart.values.ChartValues
                ): CharSequence {
                    return if (value.toInt() == maxSteps && maxSteps > 0) {
                        java.text.NumberFormat.getNumberInstance().format(value.toInt())
                    } else {
                        ""
                    }
                }
            }

            Chart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                chart = columnChart(
                    columns = columns,
                    spacing = 4.dp,
                    mergeMode = MergeMode.Stack,
                    dataLabel = textComponent(
                        color = ZivaaTheme.colors.sage,
                        textSize = 11.sp,
                        typeface = android.graphics.Typeface.DEFAULT_BOLD,
                        margins = dimensionsOf(bottom = 4.dp)
                    ),
                    dataLabelValueFormatter = labelFormatter
                ),
                model = chartEntryModel,
                marker = rememberMarker(),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = bottomAxisValueFormatter,
                    tickLength = 0.dp,
                    guideline = null,
                    label = textComponent(
                        color = ZivaaTheme.colors.inkMute.copy(alpha = 0.6f),
                        textSize = 10.sp
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(20.dp))
            
            if (viewModel.isHourlyInsightLoading) {
                Text(
                    text = "Generating insights...",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = ZivaaTheme.colors.inkMute
                )
            } else {
                Text(
                    text = viewModel.hourlyInsightText.ifEmpty { "Consistent steps throughout the afternoon. Keep that steady pace up!" },
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = ZivaaTheme.colors.ink
                )
            }
        }
    }
}

@Composable
fun TimeRangeFilterBar(
    selectedRange: MovementTimeRange,
    onRangeSelected: (MovementTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(999.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MovementTimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) ZivaaTheme.colors.sage else Color.Transparent)
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSelected) ZivaaTheme.colors.sageInk else ZivaaTheme.colors.inkMute
                )
            }
        }
    }
}

@Composable
fun ChartTypeFilterBar(
    selectedType: MovementChartType,
    onTypeSelected: (MovementChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(ZivaaTheme.colors.surfaceCard)
                .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(999.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MovementChartType.entries.forEach { type ->
                val isSelected = type == selectedType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) ZivaaTheme.colors.sage else Color.Transparent)
                        .clickable { onTypeSelected(type) }
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) ZivaaTheme.colors.sageInk else ZivaaTheme.colors.inkMute
                    )
                }
            }
        }
    }
}

@Composable
fun rememberMovementMarker(unitSuffix: String = "", isDecimal: Boolean = false): Marker {
    val labelBackground = shapeComponent(Shapes.pillShape, ZivaaTheme.colors.sage)
    val label = textComponent(
        background = labelBackground,
        padding = dimensionsOf(8.dp, 4.dp),
        color = ZivaaTheme.colors.sageInk,
        textSize = 12.sp,
    )
    val indicator = shapeComponent(Shapes.pillShape, ZivaaTheme.colors.sage)
    val guideline = lineComponent(
        color = ZivaaTheme.colors.sage.copy(alpha = 0.5f),
        thickness = 2.dp,
    )
    return com.patrykandpatrick.vico.core.component.marker.MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline
    ).apply {
        labelFormatter = object : MarkerLabelFormatter {
            override fun getLabel(
                markedEntries: List<Marker.EntryModel>,
                chartValues: ChartValues
            ): CharSequence {
                val total = markedEntries.sumOf { it.entry.y.toDouble() }
                return if (isDecimal) {
                    String.format(java.util.Locale.US, "%.1f%s", total, unitSuffix)
                } else {
                    "${java.text.NumberFormat.getNumberInstance().format(total.toInt())}$unitSuffix"
                }
            }
        }
    }
}

@Composable
fun MovementChartAxisRow(
    timeRange: MovementTimeRange,
    startLabel: String,
    labels: List<String> = emptyList(),
    endPadding: androidx.compose.ui.unit.Dp = 54.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(top = 8.dp, end = endPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        when (timeRange) {
            MovementTimeRange.SEVEN_DAYS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    labels.forEachIndexed { index, label ->
                        val isToday = index == labels.size - 1
                        Text(
                            text = label,
                            style = ZivaaTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isToday) ZivaaTheme.colors.sage else ZivaaTheme.colors.inkMute.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            MovementTimeRange.THIRTY_DAYS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = startLabel,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = ZivaaTheme.colors.inkMute.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Today",
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                        color = ZivaaTheme.colors.sage
                    )
                }
            }
            MovementTimeRange.THREE_MONTHS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = startLabel,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = ZivaaTheme.colors.inkMute.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "6w ago",
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = ZivaaTheme.colors.inkMute.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "This week",
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                        color = ZivaaTheme.colors.sage
                    )
                }
            }
        }
    }
}

@Composable
fun rememberMarker(): Marker = rememberMovementMarker()

@Composable
fun WeeklyMobilityScoreChartCard(
    viewModel: MovementViewModel,
    onInfoClick: (() -> Unit)? = null
) {
    val isDark = ZivaaTheme.colors.isDark
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = if (isDark) 20.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x0C000000),
                spotColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(1.dp, ZivaaTheme.colors.line, RoundedCornerShape(22.dp))
            .padding(24.dp)
    ) {
        Column {
            val scoreData = if (viewModel.weeklyMobilityScore.isEmpty()) List(7) { "-" to 0 } else viewModel.weeklyMobilityScore
            val chartEntryModel = if (viewModel.selectedChartType == MovementChartType.LINE) {
                val seriesList = mutableListOf<List<FloatEntry>>()
                var currentSeries = mutableListOf<FloatEntry>()
                scoreData.forEachIndexed { i, p ->
                    if (p.second.toDouble() >= 0) {
                        currentSeries.add(FloatEntry(x = i.toFloat(), y = p.second.toFloat()))
                    } else {
                        if (currentSeries.isNotEmpty()) {
                            seriesList.add(currentSeries)
                            currentSeries = mutableListOf()
                        }
                    }
                }
                if (currentSeries.isNotEmpty()) seriesList.add(currentSeries)
                if (seriesList.isEmpty()) seriesList.add(listOf(FloatEntry(0f, 0f))) // Safe fallback
                entryModelOf(*seriesList.toTypedArray())
            } else {
                val series = scoreData.mapIndexed { index, pair ->
                    List(scoreData.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index && pair.second.toDouble() >= 0) pair.second.toFloat() else 0f) }
                }
                entryModelOf(*series.toTypedArray())
            }

            val maxDataValue = scoreData.maxOfOrNull { if (it.second.toDouble() < 0) 0 else it.second.toInt() } ?: 0
            // Fixed reference scale for 85+ Optimal Zone (85 / 145 = 58.6% from bottom) with headroom
            val chartMax = 145f
            val plotAreaHeightDp = 150.dp

            val isSevenDays = viewModel.selectedTimeRange == MovementTimeRange.SEVEN_DAYS
            val heroScore = if (isSevenDays) {
                val mobilityScore = viewModel.mobilitySummary?.mobilityScore?.overallScore
                if (mobilityScore != null && mobilityScore > 0) {
                    mobilityScore
                } else {
                    scoreData.lastOrNull()?.second ?: 0
                }
            } else {
                viewModel.averageMobilityScore
            }
            val heroSubtitle = if (isSevenDays) "mobility score today" else "daily average score"

            val diffBadgeText = if (isSevenDays && heroScore >= 85) {
                "Optimal Tier"
            } else if (isSevenDays && heroScore >= 70) {
                "Steady Tier"
            } else if (isSevenDays && viewModel.averageMobilityScore > 0 && heroScore > 0) {
                val diff = heroScore - viewModel.averageMobilityScore
                if (diff >= 0) "+$diff vs Avg" else "$diff vs Avg"
            } else if (!isSevenDays && viewModel.averageMobilityScore >= 85) {
                "Optimal Avg"
            } else if (!isSevenDays && viewModel.averageMobilityScore >= 70) {
                "Steady Avg"
            } else if (!isSevenDays && viewModel.averageMobilityScore >= 50) {
                "Building Avg"
            } else if (!isSevenDays && viewModel.averageMobilityScore > 0) {
                "Gentle Avg"
            } else ""

            val heroScoreColor = when {
                heroScore >= 85 -> if (isDark) Color(0xFF4EAE7B) else Color(0xFF2E8555)
                heroScore >= 70 -> if (isDark) Color(0xFF7CB89E) else Color(0xFF2E6B56)
                heroScore >= 50 -> if (isDark) Color(0xFFE5A643) else Color(0xFFC97A1E)
                else -> if (isDark) Color(0xFFE58B43) else Color(0xFFC85A24)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Mobility Score · ${viewModel.selectedTimeRange.label}",
                        style = ZivaaTheme.typography.eyebrow.copy(
                            fontSize = 12.5.sp,
                            letterSpacing = 0.06.em
                        ),
                        color = ZivaaTheme.colors.eyebrow
                    )
                    if (onInfoClick != null) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.White.copy(alpha = 0.06f) else ZivaaTheme.colors.lineStrong.copy(alpha = 0.15f))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else ZivaaTheme.colors.lineStrong.copy(alpha = 0.35f), CircleShape)
                                .clickable { onInfoClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Mobility score calculation explanation",
                                tint = if (isDark) ZivaaTheme.colors.sage else Color(0xFF234B3F),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (heroScore > 0) "$heroScore" else "—",
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = heroScoreColor
                        )
                    )
                    Text(
                        text = heroSubtitle,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                }

                if (diffBadgeText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(heroScoreColor.copy(alpha = 0.14f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = diffBadgeText,
                            style = ZivaaTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = heroScoreColor
                        )
                    }
                }
            }

            val optimalLine = ThresholdLine(
                thresholdValue = 85f,
                lineComponent = lineComponent(
                    color = Color(0xFF4EAE7B).copy(alpha = 0.45f),
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val avgLine = ThresholdLine(
                thresholdValue = viewModel.averageMobilityScore.toFloat(),
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.inkMute.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val barThickness = when (scoreData.size) {
                in 25..35 -> 4.5.dp
                in 10..15 -> 12.dp
                else -> 20.dp
            }
            val barSpacing = when (scoreData.size) {
                in 25..35 -> 3.dp
                in 10..15 -> 8.dp
                else -> 12.dp
            }

            val pastBarColor = if (isDark) Color(0xFF4A4E58) else Color(0xFFD4CDC2)
            val columns = List(scoreData.size) { index ->
                if (index == scoreData.size - 1) {
                    lineComponent(color = heroScoreColor, thickness = barThickness, shape = Shapes.pillShape)
                } else {
                    lineComponent(color = pastBarColor, thickness = barThickness, shape = Shapes.pillShape)
                }
            }

            val chart = if (viewModel.selectedChartType == MovementChartType.LINE) {
                val chartLineColor = if (isDark) ZivaaTheme.colors.sage else Color(0xFF2E6B56)
                lineChart(
                    lines = List(chartEntryModel.entries.size) {
                        LineChart.LineSpec(
                            lineColor = chartLineColor.toArgb(),
                            lineThicknessDp = 3f,
                            point = shapeComponent(shape = Shapes.pillShape, color = chartLineColor),
                            pointSizeDp = when (scoreData.size) {
                                in 25..35 -> 4f
                                in 10..15 -> 6f
                                else -> 8f
                            }
                        )
                    },
                    decorations = listOf(optimalLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                        minY = 0f,
                        maxY = chartMax,
                        minX = 0f,
                        maxX = (scoreData.size - 1).toFloat()
                    )
                )
            } else {
                columnChart(
                    columns = columns,
                    mergeMode = MergeMode.Stack,
                    decorations = listOf(optimalLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = chartMax),
                    spacing = barSpacing
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(plotAreaHeightDp)
                ) {
                    // Soft Translucent "Optimal Zone" Shaded Band across 85 to 100
                    val optimalZoneTopVal = 100f
                    val optimalZoneBottomVal = 85f
                    val topFraction = (1f - (optimalZoneTopVal / chartMax)).coerceIn(0f, 1f)
                    val bottomFraction = (1f - (optimalZoneBottomVal / chartMax)).coerceIn(0f, 1f)
                    val bandHeight = (plotAreaHeightDp * (bottomFraction - topFraction)).coerceAtLeast(18.dp)
                    val bandOffsetY = plotAreaHeightDp * topFraction

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = bandOffsetY)
                            .height(bandHeight)
                            .background(
                                color = Color(0xFF4EAE7B).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    Chart(
                        modifier = Modifier.fillMaxSize(),
                        chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                        chart = chart,
                        model = chartEntryModel,
                        marker = rememberMovementMarker(unitSuffix = "/100"),
                        bottomAxis = null
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .width(52.dp)
                        .height(plotAreaHeightDp)
                ) {
                    val targetFraction = 1f - (85f / chartMax)
                    val rawTargetOffsetY = plotAreaHeightDp * targetFraction
                    val rawAvgOffsetY = if (viewModel.averageMobilityScore > 0) {
                        val avgFraction = 1f - (viewModel.averageMobilityScore.toFloat() / chartMax)
                        plotAreaHeightDp * avgFraction
                    } else null

                    val isColliding = rawAvgOffsetY != null && kotlin.math.abs((rawTargetOffsetY - rawAvgOffsetY).value) < 24f
                    val targetOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawTargetOffsetY <= rawAvgOffsetY) rawTargetOffsetY - 7.dp else rawTargetOffsetY + 11.dp
                    } else rawTargetOffsetY

                    val avgOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawTargetOffsetY <= rawAvgOffsetY) rawAvgOffsetY + 11.dp else rawAvgOffsetY - 7.dp
                    } else rawAvgOffsetY

                    Column(
                        modifier = Modifier.offset(y = targetOffsetY - 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Optimal",
                            style = ZivaaTheme.typography.eyebrow,
                            color = Color(0xFF4EAE7B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "85+",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = Color(0xFF4EAE7B),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // AVG label
                    if (avgOffsetY != null) {
                        Column(
                            modifier = Modifier.offset(y = avgOffsetY - 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg",
                                style = ZivaaTheme.typography.eyebrow,
                                color = ZivaaTheme.colors.eyebrow,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${viewModel.averageMobilityScore}",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = ZivaaTheme.colors.inkMute
                            )
                        }
                    }
                }
            }

            val defaultScoreStart = if (viewModel.selectedTimeRange == MovementTimeRange.THREE_MONTHS) "3m ago" else "30d ago"
            MovementChartAxisRow(
                timeRange = viewModel.selectedTimeRange,
                startLabel = scoreData.firstOrNull()?.first?.ifEmpty { defaultScoreStart } ?: defaultScoreStart,
                labels = scoreData.map { it.first },
                endPadding = 58.dp
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = viewModel.mobilityScoreInsightText.ifEmpty { "Averaging ${viewModel.averageMobilityScore}/100 mobility score across the ${viewModel.selectedTimeRange.label.lowercase()}." },
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = ZivaaTheme.colors.ink
            )
        }
    }
}

@Composable
fun WeeklyStepsChartCard(viewModel: MovementViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            val stepsData = if (viewModel.weeklySteps.isEmpty()) List(7) { "-" to 0 } else viewModel.weeklySteps
            val chartEntryModel = if (viewModel.selectedChartType == MovementChartType.LINE) {
                val seriesList = mutableListOf<List<FloatEntry>>()
                var currentSeries = mutableListOf<FloatEntry>()
                stepsData.forEachIndexed { i, p ->
                    if (p.second.toDouble() >= 0) {
                        currentSeries.add(FloatEntry(x = i.toFloat(), y = p.second.toFloat()))
                    } else {
                        if (currentSeries.isNotEmpty()) {
                            seriesList.add(currentSeries)
                            currentSeries = mutableListOf()
                        }
                    }
                }
                if (currentSeries.isNotEmpty()) seriesList.add(currentSeries)
                if (seriesList.isEmpty()) seriesList.add(listOf(FloatEntry(0f, 0f))) // Safe fallback
                entryModelOf(*seriesList.toTypedArray())
            } else {
                val series = stepsData.mapIndexed { index, pair ->
                    List(stepsData.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index && pair.second.toDouble() >= 0) pair.second.toFloat() else 0f) }
                }
                entryModelOf(*series.toTypedArray())
            }

            // Chart with right-side labels aligned to threshold lines
            val maxDataValue = stepsData.maxOfOrNull { if (it.second.toDouble() < 0) 0 else it.second.toInt() } ?: 0
            val currentGoal = viewModel.mobilitySummary?.goalSteps ?: viewModel.goalSteps ?: 6000
            val validGoal = currentGoal > 0
            // Fixed reference scale for target zone (goal / 0.5862f) with headroom for bars exceeding goal
            val chartMax = maxOf(currentGoal.toFloat() / 0.5862f, maxDataValue.toFloat() * 1.15f)
            val plotAreaHeightDp = 150.dp

            // 1. Option 1 Hero Step Counter & Comparative Badge
            val isSevenDays = viewModel.selectedTimeRange == MovementTimeRange.SEVEN_DAYS
            val heroSteps = if (isSevenDays) {
                val mobilityInt = viewModel.mobilitySummary?.totalSteps
                if (mobilityInt != null && mobilityInt > 0) {
                    mobilityInt
                } else {
                    viewModel.totalStepsToday.replace(",", "").toIntOrNull()
                        ?: stepsData.lastOrNull()?.second
                        ?: 0
                }
            } else {
                viewModel.averageSteps
            }
            val heroStepsFormatted = java.text.NumberFormat.getNumberInstance().format(heroSteps)
            val heroSubtitle = if (isSevenDays) "steps today" else "daily average"

            val diffBadgeText = if (isSevenDays && viewModel.averageSteps > 0 && heroSteps > 0) {
                val diffPct = ((heroSteps - viewModel.averageSteps) * 100) / viewModel.averageSteps
                if (diffPct >= 0) "+$diffPct% vs Avg" else "$diffPct% vs Avg"
            } else if (validGoal && heroSteps >= currentGoal) {
                "Goal Met"
            } else ""

            Text(
                text = "Total Steps · ${viewModel.selectedTimeRange.label}",
                style = ZivaaTheme.typography.eyebrow.copy(
                    fontSize = 12.5.sp,
                    letterSpacing = 0.06.em
                ),
                color = ZivaaTheme.colors.eyebrow
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = heroStepsFormatted,
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZivaaTheme.colors.sage
                        )
                    )
                    Text(
                        text = heroSubtitle,
                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                }

                if (diffBadgeText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = ZivaaTheme.colors.sage.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = diffBadgeText,
                            style = ZivaaTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ZivaaTheme.colors.sage
                        )
                    }
                }
            }

            val goalLine = ThresholdLine(
                thresholdValue = currentGoal.toFloat(),
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.sage.copy(alpha = 0.45f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(
                    color = Color.Transparent,
                    textSize = 0.sp
                ),
                thresholdLabel = ""
            )

            val avgLine = ThresholdLine(
                thresholdValue = viewModel.averageSteps.toFloat(),
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.inkMute.copy(alpha = 0.4f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(
                    color = Color.Transparent,
                    textSize = 0.sp
                ),
                thresholdLabel = ""
            )

            val barThickness = when (stepsData.size) {
                in 25..35 -> 4.5.dp
                in 10..15 -> 12.dp
                else -> 20.dp
            }
            val barSpacing = when (stepsData.size) {
                in 25..35 -> 3.dp
                in 10..15 -> 8.dp
                else -> 12.dp
            }

            val pastBarColor = Color(0xFFB8B0A2)
            val columns = List(stepsData.size) { index ->
                if (index == stepsData.size - 1) {
                    lineComponent(color = ZivaaTheme.colors.sage, thickness = barThickness, shape = Shapes.pillShape)
                } else {
                    lineComponent(color = pastBarColor, thickness = barThickness, shape = Shapes.pillShape)
                }
            }

            val chart = if (viewModel.selectedChartType == MovementChartType.LINE) {
                lineChart(
                    lines = List(chartEntryModel.entries.size) {
                        LineChart.LineSpec(
                            lineColor = ZivaaTheme.colors.sage.toArgb(),
                            lineThicknessDp = 3f,
                            point = shapeComponent(shape = Shapes.pillShape, color = ZivaaTheme.colors.sage),
                            pointSizeDp = when (stepsData.size) {
                                in 25..35 -> 4f
                                in 10..15 -> 6f
                                else -> 8f
                            }
                        )
                    },
                    decorations = if (validGoal) listOf(goalLine, avgLine) else listOf(avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                        minY = 0f,
                        maxY = chartMax,
                        minX = 0f,
                        maxX = (stepsData.size - 1).toFloat()
                    )
                )
            } else {
                columnChart(
                    columns = columns,
                    mergeMode = MergeMode.Stack,
                    decorations = if (validGoal) listOf(goalLine, avgLine) else listOf(avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = chartMax),
                    spacing = barSpacing
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(plotAreaHeightDp)
                ) {
                    // Soft Translucent "Goal Zone" Shaded Band across the target range (from goal up to goal * 1.18)
                    if (validGoal && chartMax > 0f) {
                        val goalZoneTopVal = currentGoal.toFloat() * 1.18f
                        val goalZoneBottomVal = currentGoal.toFloat()
                        val topFraction = (1f - (goalZoneTopVal / chartMax)).coerceIn(0f, 1f)
                        val bottomFraction = (1f - (goalZoneBottomVal / chartMax)).coerceIn(0f, 1f)
                        val bandHeight = (plotAreaHeightDp * (bottomFraction - topFraction)).coerceAtLeast(18.dp)
                        val bandOffsetY = plotAreaHeightDp * topFraction

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = bandOffsetY)
                                .height(bandHeight)
                                .background(
                                    color = ZivaaTheme.colors.sage.copy(alpha = 0.09f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }

                    Chart(
                        modifier = Modifier
                            .fillMaxSize(),
                        chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                        chart = chart,
                        model = chartEntryModel,
                        marker = rememberMovementMarker(),
                        bottomAxis = null
                    )
                }

                // Right-side labels pinned to threshold line Y positions
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .width(52.dp)
                        .height(plotAreaHeightDp)
                ) {
                    // GOAL ZONE label
                    if (validGoal) {
                        val goalFraction = if (chartMax > 0f) 1f - (currentGoal.toFloat() / chartMax) else 0.5f
                        val goalOffsetY = plotAreaHeightDp * goalFraction
                        Column(
                            modifier = Modifier.offset(y = goalOffsetY - 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Goal Zone",
                                style = ZivaaTheme.typography.eyebrow,
                                color = ZivaaTheme.colors.sage,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = java.text.NumberFormat.getNumberInstance().format(currentGoal),
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = ZivaaTheme.colors.sage,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // AVG label
                    if (viewModel.averageSteps > 0) {
                        val avgFraction = if (chartMax > 0f) 1f - (viewModel.averageSteps.toFloat() / chartMax) else 0.5f
                        val avgOffsetY = plotAreaHeightDp * avgFraction
                        Column(
                            modifier = Modifier.offset(y = avgOffsetY - 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg",
                                style = ZivaaTheme.typography.eyebrow,
                                color = ZivaaTheme.colors.eyebrow,
                                fontSize = 10.sp
                            )
                            Text(
                                text = java.text.NumberFormat.getNumberInstance().format(viewModel.averageSteps),
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = ZivaaTheme.colors.inkMute
                            )
                        }
                    }
                }
            }

            val defaultStepsStart = if (viewModel.selectedTimeRange == MovementTimeRange.THREE_MONTHS) "3m ago" else "30d ago"
            MovementChartAxisRow(
                timeRange = viewModel.selectedTimeRange,
                startLabel = stepsData.firstOrNull()?.first?.ifEmpty { defaultStepsStart } ?: defaultStepsStart,
                labels = stepsData.map { it.first },
                endPadding = 58.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(20.dp))
            
            if (viewModel.isInsightLoading) {
                Text(
                    text = "Generating insights...",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = ZivaaTheme.colors.inkMute
                )
            } else {
                Text(
                    text = viewModel.insightText.ifEmpty { "Averaging ${java.text.NumberFormat.getNumberInstance().format(viewModel.averageSteps)} steps a day across the week." },
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = ZivaaTheme.colors.ink
                )
            }
        }
    }
}

@Composable
fun WeeklyCadenceChartCard(viewModel: MovementViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            val cadenceData = if (viewModel.weeklyCadence.isEmpty()) List(7) { "-" to 0 } else viewModel.weeklyCadence
            val chartEntryModel = if (viewModel.selectedChartType == MovementChartType.LINE) {
                val seriesList = mutableListOf<List<FloatEntry>>()
                var currentSeries = mutableListOf<FloatEntry>()
                cadenceData.forEachIndexed { i, p ->
                    if (p.second.toDouble() >= 0) {
                        currentSeries.add(FloatEntry(x = i.toFloat(), y = p.second.toFloat()))
                    } else {
                        if (currentSeries.isNotEmpty()) {
                            seriesList.add(currentSeries)
                            currentSeries = mutableListOf()
                        }
                    }
                }
                if (currentSeries.isNotEmpty()) seriesList.add(currentSeries)
                if (seriesList.isEmpty()) seriesList.add(listOf(FloatEntry(0f, 0f))) // Safe fallback
                entryModelOf(*seriesList.toTypedArray())
            } else {
                val series = cadenceData.mapIndexed { index, pair ->
                    List(cadenceData.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index && pair.second.toDouble() >= 0) pair.second.toFloat() else 0f) }
                }
                entryModelOf(*series.toTypedArray())
            }

            val maxDataValue = cadenceData.maxOfOrNull { if (it.second.toDouble() < 0) 0 else it.second.toInt() } ?: 0
            // Fixed reference scale for 80 spm Brisk Zone (80 / 136.5 = 58.6% from bottom) with headroom
            val chartMax = maxOf(136.5f, maxDataValue.toFloat() * 1.15f)
            val plotAreaHeightDp = 150.dp

            // Option 1 Hero Cadence Counter & Comparative Badge
            val isSevenDays = viewModel.selectedTimeRange == MovementTimeRange.SEVEN_DAYS
            val heroCadence = if (isSevenDays) {
                val mobilityCadence = viewModel.mobilitySummary?.cadenceSpm
                if (mobilityCadence != null && mobilityCadence > 0) {
                    mobilityCadence
                } else {
                    cadenceData.lastOrNull()?.second ?: 0
                }
            } else {
                viewModel.averageCadence
            }
            val heroSubtitle = if (isSevenDays) "steps/min today" else "daily average pace"

            val diffBadgeText = if (isSevenDays && heroCadence >= 80) {
                "Brisk Pace"
            } else if (isSevenDays && viewModel.averageCadence > 0 && heroCadence > 0) {
                val diff = heroCadence - viewModel.averageCadence
                if (diff >= 0) "+$diff spm vs Avg" else "$diff spm vs Avg"
            } else if (!isSevenDays && viewModel.averageCadence >= 80) {
                "Brisk Avg"
            } else if (!isSevenDays && viewModel.averageCadence > 0) {
                "Steady Rhythm"
            } else ""

            Text(
                text = "Walking Cadence · ${viewModel.selectedTimeRange.label}",
                style = ZivaaTheme.typography.eyebrow.copy(
                    fontSize = 12.5.sp,
                    letterSpacing = 0.06.em
                ),
                color = ZivaaTheme.colors.eyebrow
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$heroCadence",
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZivaaTheme.colors.sage
                        )
                    )
                    Text(
                        text = heroSubtitle,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                }

                if (diffBadgeText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZivaaTheme.colors.sage.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = diffBadgeText,
                            style = ZivaaTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ZivaaTheme.colors.sage
                        )
                    }
                }
            }

            val briskLine = ThresholdLine(
                thresholdValue = 80f,
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.sage.copy(alpha = 0.4f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val avgLine = ThresholdLine(
                thresholdValue = viewModel.averageCadence.toFloat(),
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.inkMute.copy(alpha = 0.4f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val barThickness = when (cadenceData.size) {
                in 25..35 -> 4.5.dp
                in 10..15 -> 12.dp
                else -> 20.dp
            }
            val barSpacing = when (cadenceData.size) {
                in 25..35 -> 3.dp
                in 10..15 -> 8.dp
                else -> 12.dp
            }

            val pastBarColor = Color(0xFFB8B0A2)
            val columns = List(cadenceData.size) { index ->
                if (index == cadenceData.size - 1) {
                    lineComponent(color = ZivaaTheme.colors.sage, thickness = barThickness, shape = Shapes.pillShape)
                } else {
                    lineComponent(color = pastBarColor, thickness = barThickness, shape = Shapes.pillShape)
                }
            }

            val chart = if (viewModel.selectedChartType == MovementChartType.LINE) {
                lineChart(
                    lines = List(chartEntryModel.entries.size) {
                        LineChart.LineSpec(
                            lineColor = ZivaaTheme.colors.sage.toArgb(),
                            lineThicknessDp = 3f,
                            point = shapeComponent(shape = Shapes.pillShape, color = ZivaaTheme.colors.sage),
                            pointSizeDp = when (cadenceData.size) {
                                in 25..35 -> 4f
                                in 10..15 -> 6f
                                else -> 8f
                            }
                        )
                    },
                    decorations = listOf(briskLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                        minY = 0f,
                        maxY = chartMax,
                        minX = 0f,
                        maxX = (cadenceData.size - 1).toFloat()
                    )
                )
            } else {
                columnChart(
                    columns = columns,
                    mergeMode = MergeMode.Stack,
                    decorations = listOf(briskLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = chartMax),
                    spacing = barSpacing
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(plotAreaHeightDp)
                ) {
                    // Soft Translucent "Brisk Zone" Shaded Band across the target range (80 to 95 spm)
                    val briskZoneTopVal = 95f
                    val briskZoneBottomVal = 80f
                    val topFraction = (1f - (briskZoneTopVal / chartMax)).coerceIn(0f, 1f)
                    val bottomFraction = (1f - (briskZoneBottomVal / chartMax)).coerceIn(0f, 1f)
                    val bandHeight = (plotAreaHeightDp * (bottomFraction - topFraction)).coerceAtLeast(18.dp)
                    val bandOffsetY = plotAreaHeightDp * topFraction

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = bandOffsetY)
                            .height(bandHeight)
                            .background(
                                color = ZivaaTheme.colors.sage.copy(alpha = 0.09f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    Chart(
                        modifier = Modifier.fillMaxSize(),
                        chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                        chart = chart,
                        model = chartEntryModel,
                        marker = rememberMovementMarker(unitSuffix = " spm"),
                        bottomAxis = null
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .width(52.dp)
                        .height(plotAreaHeightDp)
                ) {
                    // Brisk guideline label (80 spm)
                    val briskFraction = if (chartMax > 0f) 1f - (80f / chartMax) else 0.5f
                    val rawBriskOffsetY = plotAreaHeightDp * briskFraction
                    val rawAvgOffsetY = if (viewModel.averageCadence > 0) {
                        val avgFraction = if (chartMax > 0f) 1f - (viewModel.averageCadence.toFloat() / chartMax) else 0.5f
                        plotAreaHeightDp * avgFraction
                    } else null

                    val isColliding = rawAvgOffsetY != null && kotlin.math.abs((rawBriskOffsetY - rawAvgOffsetY).value) < 24f
                    val briskOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawBriskOffsetY <= rawAvgOffsetY) rawBriskOffsetY - 7.dp else rawBriskOffsetY + 11.dp
                    } else rawBriskOffsetY

                    val avgOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawBriskOffsetY <= rawAvgOffsetY) rawAvgOffsetY + 11.dp else rawAvgOffsetY - 7.dp
                    } else rawAvgOffsetY

                    Column(
                        modifier = Modifier.offset(y = briskOffsetY - 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Brisk Zone",
                            style = ZivaaTheme.typography.eyebrow,
                            color = ZivaaTheme.colors.sage,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "80 spm",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = ZivaaTheme.colors.sage,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // AVG label
                    if (avgOffsetY != null) {
                        Column(
                            modifier = Modifier.offset(y = avgOffsetY - 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg",
                                style = ZivaaTheme.typography.eyebrow,
                                color = ZivaaTheme.colors.eyebrow,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${viewModel.averageCadence} spm",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = ZivaaTheme.colors.inkMute
                            )
                        }
                    }
                }
            }

            val defaultCadenceStart = if (viewModel.selectedTimeRange == MovementTimeRange.THREE_MONTHS) "3m ago" else "30d ago"
            MovementChartAxisRow(
                timeRange = viewModel.selectedTimeRange,
                startLabel = cadenceData.firstOrNull()?.first?.ifEmpty { defaultCadenceStart } ?: defaultCadenceStart,
                labels = cadenceData.map { it.first },
                endPadding = 58.dp
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = viewModel.cadenceInsightText.ifEmpty { "Averaging consistent pacing across your walking bouts." },
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = ZivaaTheme.colors.ink
            )
        }
    }
}

@Composable
fun WeeklyActiveMinutesChartCard(viewModel: MovementViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            val minutesData = if (viewModel.weeklyActiveMinutes.isEmpty()) List(7) { "-" to 0.0 } else viewModel.weeklyActiveMinutes
            val chartEntryModel = if (viewModel.selectedChartType == MovementChartType.LINE) {
                val seriesList = mutableListOf<List<FloatEntry>>()
                var currentSeries = mutableListOf<FloatEntry>()
                minutesData.forEachIndexed { i, p ->
                    if (p.second.toDouble() >= 0) {
                        currentSeries.add(FloatEntry(x = i.toFloat(), y = p.second.toFloat()))
                    } else {
                        if (currentSeries.isNotEmpty()) {
                            seriesList.add(currentSeries)
                            currentSeries = mutableListOf()
                        }
                    }
                }
                if (currentSeries.isNotEmpty()) seriesList.add(currentSeries)
                if (seriesList.isEmpty()) seriesList.add(listOf(FloatEntry(0f, 0f))) // Safe fallback
                entryModelOf(*seriesList.toTypedArray())
            } else {
                val series = minutesData.mapIndexed { index, pair ->
                    List(minutesData.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index && pair.second.toDouble() >= 0) pair.second.toFloat() else 0f) }
                }
                entryModelOf(*series.toTypedArray())
            }

            val maxDataValue = (minutesData.maxOfOrNull { if (it.second.toDouble() < 0) 0 else it.second.toInt() } ?: 0.0).toFloat()
            // Fixed reference scale for 30m Target Zone (30 / 51.2 = 58.6% from bottom) with headroom
            val chartMax = maxOf(51.2f, maxDataValue * 1.15f)
            val plotAreaHeightDp = 150.dp

            // Option 1 Hero Active Minutes Counter & Comparative Badge
            val isSevenDays = viewModel.selectedTimeRange == MovementTimeRange.SEVEN_DAYS
            val heroMinutes = if (isSevenDays) {
                viewModel.mobilitySummary?.activeMinutes?.toInt()
                    ?: minutesData.lastOrNull()?.second?.roundToInt()
                    ?: 0
            } else {
                viewModel.averageActiveMinutes.roundToInt()
            }
            val heroSubtitle = if (isSevenDays) "active mins today" else "daily average active time"

            val diffBadgeText = if (isSevenDays && heroMinutes >= 30) {
                "Target Met"
            } else if (isSevenDays && viewModel.averageActiveMinutes > 0 && heroMinutes > 0) {
                val diffPct = (((heroMinutes - viewModel.averageActiveMinutes) * 100) / viewModel.averageActiveMinutes).toInt()
                if (diffPct >= 0) "+$diffPct% vs Avg" else "$diffPct% vs Avg"
            } else if (!isSevenDays && viewModel.averageActiveMinutes >= 30) {
                "Target Met"
            } else if (!isSevenDays && viewModel.averageActiveMinutes > 0) {
                "Consistent"
            } else ""

            Text(
                text = "Active Moving Time · ${viewModel.selectedTimeRange.label}",
                style = ZivaaTheme.typography.eyebrow.copy(
                    fontSize = 12.5.sp,
                    letterSpacing = 0.06.em
                ),
                color = ZivaaTheme.colors.eyebrow
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${heroMinutes}m",
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZivaaTheme.colors.sage
                        )
                    )
                    Text(
                        text = heroSubtitle,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                }

                if (diffBadgeText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZivaaTheme.colors.sage.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = diffBadgeText,
                            style = ZivaaTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ZivaaTheme.colors.sage
                        )
                    }
                }
            }

            val targetLine = ThresholdLine(
                thresholdValue = 30f,
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.sage.copy(alpha = 0.45f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val avgLine = ThresholdLine(
                thresholdValue = viewModel.averageActiveMinutes.toFloat(),
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.inkMute.copy(alpha = 0.4f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val barThickness = when (minutesData.size) {
                in 25..35 -> 4.5.dp
                in 10..15 -> 12.dp
                else -> 20.dp
            }
            val barSpacing = when (minutesData.size) {
                in 25..35 -> 3.dp
                in 10..15 -> 8.dp
                else -> 12.dp
            }

            val pastBarColor = Color(0xFFB8B0A2)
            val columns = List(minutesData.size) { index ->
                if (index == minutesData.size - 1) {
                    lineComponent(color = ZivaaTheme.colors.sage, thickness = barThickness, shape = Shapes.pillShape)
                } else {
                    lineComponent(color = pastBarColor, thickness = barThickness, shape = Shapes.pillShape)
                }
            }

            val chart = if (viewModel.selectedChartType == MovementChartType.LINE) {
                lineChart(
                    lines = List(chartEntryModel.entries.size) {
                        LineChart.LineSpec(
                            lineColor = ZivaaTheme.colors.sage.toArgb(),
                            lineThicknessDp = 3f,
                            point = shapeComponent(shape = Shapes.pillShape, color = ZivaaTheme.colors.sage),
                            pointSizeDp = when (minutesData.size) {
                                in 25..35 -> 4f
                                in 10..15 -> 6f
                                else -> 8f
                            }
                        )
                    },
                    decorations = listOf(targetLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                        minY = 0f,
                        maxY = chartMax,
                        minX = 0f,
                        maxX = (minutesData.size - 1).toFloat()
                    )
                )
            } else {
                columnChart(
                    columns = columns,
                    mergeMode = MergeMode.Stack,
                    decorations = listOf(targetLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = chartMax),
                    spacing = barSpacing
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(plotAreaHeightDp)
                ) {
                    // Soft Translucent "Target Zone" Shaded Band across the target range (30 to 36m)
                    val targetZoneTopVal = 36f
                    val targetZoneBottomVal = 30f
                    val topFraction = (1f - (targetZoneTopVal / chartMax)).coerceIn(0f, 1f)
                    val bottomFraction = (1f - (targetZoneBottomVal / chartMax)).coerceIn(0f, 1f)
                    val bandHeight = (plotAreaHeightDp * (bottomFraction - topFraction)).coerceAtLeast(18.dp)
                    val bandOffsetY = plotAreaHeightDp * topFraction

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = bandOffsetY)
                            .height(bandHeight)
                            .background(
                                color = ZivaaTheme.colors.sage.copy(alpha = 0.09f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    Chart(
                        modifier = Modifier.fillMaxSize(),
                        chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                        chart = chart,
                        model = chartEntryModel,
                        marker = rememberMovementMarker(unitSuffix = "m"),
                        bottomAxis = null
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .width(52.dp)
                        .height(plotAreaHeightDp)
                ) {
                    // Daily Target label (30m)
                    val targetFraction = if (chartMax > 0f) 1f - (30f / chartMax) else 0.5f
                    val rawTargetOffsetY = plotAreaHeightDp * targetFraction
                    val rawAvgOffsetY = if (viewModel.averageActiveMinutes > 0) {
                        val avgFraction = if (chartMax > 0f) 1f - (viewModel.averageActiveMinutes.toFloat() / chartMax) else 0.5f
                        plotAreaHeightDp * avgFraction
                    } else null

                    val isColliding = rawAvgOffsetY != null && kotlin.math.abs((rawTargetOffsetY - rawAvgOffsetY).value) < 24f
                    val targetOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawTargetOffsetY <= rawAvgOffsetY) rawTargetOffsetY - 7.dp else rawTargetOffsetY + 11.dp
                    } else rawTargetOffsetY

                    val avgOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawTargetOffsetY <= rawAvgOffsetY) rawAvgOffsetY + 11.dp else rawAvgOffsetY - 7.dp
                    } else rawAvgOffsetY

                    Column(
                        modifier = Modifier.offset(y = targetOffsetY - 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Target Zone",
                            style = ZivaaTheme.typography.eyebrow,
                            color = ZivaaTheme.colors.sage,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "30m",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = ZivaaTheme.colors.sage,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // AVG label
                    if (avgOffsetY != null) {
                        Column(
                            modifier = Modifier.offset(y = avgOffsetY - 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg",
                                style = ZivaaTheme.typography.eyebrow,
                                color = ZivaaTheme.colors.eyebrow,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${viewModel.averageActiveMinutes.roundToInt()}m",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = ZivaaTheme.colors.inkMute
                            )
                        }
                    }
                }
            }

            val defaultMinsStart = if (viewModel.selectedTimeRange == MovementTimeRange.THREE_MONTHS) "3m ago" else "30d ago"
            MovementChartAxisRow(
                timeRange = viewModel.selectedTimeRange,
                startLabel = minutesData.firstOrNull()?.first?.ifEmpty { defaultMinsStart } ?: defaultMinsStart,
                labels = minutesData.map { it.first },
                endPadding = 58.dp
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = viewModel.activeMinutesInsightText.ifEmpty { "Upright active minutes spent walking on your feet each day." },
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = ZivaaTheme.colors.ink
            )
        }
    }
}

@Composable
fun WeeklyActiveHoursChartCard(viewModel: MovementViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            val hoursData = if (viewModel.weeklyActiveHours.isEmpty()) List(7) { "-" to 0 } else viewModel.weeklyActiveHours
            val chartEntryModel = if (viewModel.selectedChartType == MovementChartType.LINE) {
                val seriesList = mutableListOf<List<FloatEntry>>()
                var currentSeries = mutableListOf<FloatEntry>()
                hoursData.forEachIndexed { i, p ->
                    if (p.second.toDouble() >= 0) {
                        currentSeries.add(FloatEntry(x = i.toFloat(), y = p.second.toFloat()))
                    } else {
                        if (currentSeries.isNotEmpty()) {
                            seriesList.add(currentSeries)
                            currentSeries = mutableListOf()
                        }
                    }
                }
                if (currentSeries.isNotEmpty()) seriesList.add(currentSeries)
                if (seriesList.isEmpty()) seriesList.add(listOf(FloatEntry(0f, 0f))) // Safe fallback
                entryModelOf(*seriesList.toTypedArray())
            } else {
                val series = hoursData.mapIndexed { index, pair ->
                    List(hoursData.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index && pair.second.toDouble() >= 0) pair.second.toFloat() else 0f) }
                }
                entryModelOf(*series.toTypedArray())
            }

            val maxDataValue = hoursData.maxOfOrNull { if (it.second.toDouble() < 0) 0 else it.second.toInt() } ?: 0
            // Fixed reference scale for 8h Target Zone (8 / 13.65 = 58.6% from bottom) with headroom
            val chartMax = maxOf(13.65f, maxDataValue.toFloat() * 1.15f)
            val plotAreaHeightDp = 150.dp

            // Option 1 Hero Regularity Counter & Comparative Badge
            val isSevenDays = viewModel.selectedTimeRange == MovementTimeRange.SEVEN_DAYS
            val heroHoursToday = if (isSevenDays) {
                val mobilityHours = viewModel.mobilitySummary?.activeHoursCount
                if (mobilityHours != null && mobilityHours > 0) {
                    mobilityHours
                } else {
                    hoursData.lastOrNull()?.second ?: 0
                }
            } else 0

            val heroHoursFormatted = if (isSevenDays) "${heroHoursToday}h" else "${String.format(java.util.Locale.US, "%.1f", viewModel.averageActiveHours)}h"
            val heroSubtitle = if (isSevenDays) "active hours today (8a–8p)" else "daily average active hours"

            val diffBadgeText = if (isSevenDays && heroHoursToday >= 8) {
                "Regular Rhythm"
            } else if (isSevenDays && viewModel.averageActiveHours > 0 && heroHoursToday > 0) {
                val diff = heroHoursToday - viewModel.averageActiveHours
                if (diff >= 0) String.format(java.util.Locale.US, "+%.1fh vs Avg", diff) else String.format(java.util.Locale.US, "%.1fh vs Avg", diff)
            } else if (!isSevenDays && viewModel.averageActiveHours >= 8.0) {
                "Target Met"
            } else if (!isSevenDays && viewModel.averageActiveHours >= 6.0) {
                "Steady"
            } else ""

            Text(
                text = "Movement Regularity · ${viewModel.selectedTimeRange.label}",
                style = ZivaaTheme.typography.eyebrow.copy(
                    fontSize = 12.5.sp,
                    letterSpacing = 0.06.em
                ),
                color = ZivaaTheme.colors.eyebrow
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = heroHoursFormatted,
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZivaaTheme.colors.sage
                        )
                    )
                    Text(
                        text = heroSubtitle,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                }

                if (diffBadgeText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZivaaTheme.colors.sage.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = diffBadgeText,
                            style = ZivaaTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ZivaaTheme.colors.sage
                        )
                    }
                }
            }

            val targetLine = ThresholdLine(
                thresholdValue = 8f,
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.sage.copy(alpha = 0.45f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val avgLine = ThresholdLine(
                thresholdValue = viewModel.averageActiveHours.toFloat(),
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.inkMute.copy(alpha = 0.4f), 
                    thickness = 1.dp,
                    shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
                ),
                labelComponent = textComponent(color = Color.Transparent, textSize = 0.sp),
                thresholdLabel = ""
            )

            val barThickness = when (hoursData.size) {
                in 25..35 -> 4.5.dp
                in 10..15 -> 12.dp
                else -> 20.dp
            }
            val barSpacing = when (hoursData.size) {
                in 25..35 -> 3.dp
                in 10..15 -> 8.dp
                else -> 12.dp
            }

            val pastBarColor = Color(0xFFB8B0A2)
            val columns = List(hoursData.size) { index ->
                if (index == hoursData.size - 1) {
                    lineComponent(color = ZivaaTheme.colors.sage, thickness = barThickness, shape = Shapes.pillShape)
                } else {
                    lineComponent(color = pastBarColor, thickness = barThickness, shape = Shapes.pillShape)
                }
            }

            val chart = if (viewModel.selectedChartType == MovementChartType.LINE) {
                lineChart(
                    lines = List(chartEntryModel.entries.size) {
                        LineChart.LineSpec(
                            lineColor = ZivaaTheme.colors.sage.toArgb(),
                            lineThicknessDp = 3f,
                            point = shapeComponent(shape = Shapes.pillShape, color = ZivaaTheme.colors.sage),
                            pointSizeDp = when (hoursData.size) {
                                in 25..35 -> 4f
                                in 10..15 -> 6f
                                else -> 8f
                            }
                        )
                    },
                    decorations = listOf(targetLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                        minY = 0f,
                        maxY = chartMax,
                        minX = 0f,
                        maxX = (hoursData.size - 1).toFloat()
                    )
                )
            } else {
                columnChart(
                    columns = columns,
                    mergeMode = MergeMode.Stack,
                    decorations = listOf(targetLine, avgLine),
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = chartMax),
                    spacing = barSpacing
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(plotAreaHeightDp)
                ) {
                    // Soft Translucent "Target Zone" Shaded Band across 8h to 9.5h
                    val targetZoneTopVal = 9.5f
                    val targetZoneBottomVal = 8f
                    val topFraction = (1f - (targetZoneTopVal / chartMax)).coerceIn(0f, 1f)
                    val bottomFraction = (1f - (targetZoneBottomVal / chartMax)).coerceIn(0f, 1f)
                    val bandHeight = (plotAreaHeightDp * (bottomFraction - topFraction)).coerceAtLeast(18.dp)
                    val bandOffsetY = plotAreaHeightDp * topFraction

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = bandOffsetY)
                            .height(bandHeight)
                            .background(
                                color = ZivaaTheme.colors.sage.copy(alpha = 0.09f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    Chart(
                        modifier = Modifier.fillMaxSize(),
                        chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                        chart = chart,
                        model = chartEntryModel,
                        marker = rememberMovementMarker(unitSuffix = "/12h"),
                        bottomAxis = null
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .width(52.dp)
                        .height(plotAreaHeightDp)
                ) {
                    val targetFraction = 1f - (8f / chartMax)
                    val rawTargetOffsetY = plotAreaHeightDp * targetFraction
                    val rawAvgOffsetY = if (viewModel.averageActiveHours > 0) {
                        val avgFraction = 1f - (viewModel.averageActiveHours.toFloat() / chartMax)
                        plotAreaHeightDp * avgFraction
                    } else null

                    val isColliding = rawAvgOffsetY != null && kotlin.math.abs((rawTargetOffsetY - rawAvgOffsetY).value) < 24f
                    val targetOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawTargetOffsetY <= rawAvgOffsetY) rawTargetOffsetY - 7.dp else rawTargetOffsetY + 11.dp
                    } else rawTargetOffsetY

                    val avgOffsetY = if (isColliding && rawAvgOffsetY != null) {
                        if (rawTargetOffsetY <= rawAvgOffsetY) rawAvgOffsetY + 11.dp else rawAvgOffsetY - 7.dp
                    } else rawAvgOffsetY

                    Column(
                        modifier = Modifier.offset(y = targetOffsetY - 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Target Zone",
                            style = ZivaaTheme.typography.eyebrow,
                            color = ZivaaTheme.colors.sage,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "8h+",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = ZivaaTheme.colors.sage,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // AVG label
                    if (avgOffsetY != null) {
                        Column(
                            modifier = Modifier.offset(y = avgOffsetY - 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg",
                                style = ZivaaTheme.typography.eyebrow,
                                color = ZivaaTheme.colors.eyebrow,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", viewModel.averageActiveHours)}h",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = ZivaaTheme.colors.inkMute
                            )
                        }
                    }
                }
            }

            val defaultHoursStart = if (viewModel.selectedTimeRange == MovementTimeRange.THREE_MONTHS) "3m ago" else "30d ago"
            MovementChartAxisRow(
                timeRange = viewModel.selectedTimeRange,
                startLabel = hoursData.firstOrNull()?.first?.ifEmpty { defaultHoursStart } ?: defaultHoursStart,
                labels = hoursData.map { it.first },
                endPadding = 58.dp
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = viewModel.activeHoursInsightText.ifEmpty { "Active hours (>= 150 steps) between 8 AM and 8 PM." },
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = ZivaaTheme.colors.ink
            )
        }
    }
}


