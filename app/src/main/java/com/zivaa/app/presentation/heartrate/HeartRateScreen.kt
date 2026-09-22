package com.zivaa.app.presentation.heartrate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.zivaa.app.ui.theme.ZivaaTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HeartRateScreen(
    viewModel: HeartRateViewModel,
    onNavigateBack: () -> Unit,
    onInfoClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAllData()
    }

    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Heart Health", style = ZivaaTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZivaaTheme.colors.bg,
                    titleContentColor = ZivaaTheme.colors.ink,
                    navigationIconContentColor = ZivaaTheme.colors.ink
                )
            )
        },
        containerColor = ZivaaTheme.colors.bg
    ) { paddingValues ->
        if (isLoading && viewModel.heartScore == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ZivaaTheme.colors.sage)
            }
        } else {
            val score = viewModel.heartScore ?: 0
            val color = when {
                score == 0 -> Color(0xFF6B7280)
                score >= 85 -> Color(0xFF4EAE7B) // Optimal Green
                score >= 70 -> Color(0xFF3B82F6) // Steady Blue
                score >= 50 -> Color(0xFFE5A643) // Moderate Yellow/Orange
                else -> Color(0xFFEF4444)        // Attention Red
            }
            val text = when {
                score == 0 -> "No Readings Yet"
                score >= 85 -> "Optimal Rhythm"
                score >= 70 -> "Resilient & Stable"
                score >= 50 -> "Mild Cardiac Strain"
                else -> "Attention Advised"
            }

            val b = viewModel.heartBreakdown
            val rhrValNum = (b?.get("resting_heart_rate") as? Number)?.toDouble()
            val rhrBaseNum = (b?.get("rhr_baseline") as? Number)?.toDouble()
            val rhrVal = rhrValNum?.let { "${it.toInt()} bpm" } ?: "No data"
            val rhrScorePts = (b?.get("rhr_pts") as? Number)?.toInt() ?: 30
            val rhrStatus = when {
                rhrValNum == null -> "No data"
                rhrValNum < 48.0 -> "Low / Brady"
                rhrBaseNum != null && rhrValNum <= rhrBaseNum -> "Calm Baseline"
                rhrValNum <= 65.0 -> "Calm Baseline"
                rhrValNum <= 74.0 -> "Stable Baseline"
                else -> "Elevated Rest"
            }
            val rhrAccentColor = when (rhrStatus) {
                "Calm Baseline" -> Color(0xFF4EAE7B)
                "Stable Baseline" -> Color(0xFF3B82F6)
                "No data" -> Color(0xFF6B7280)
                else -> Color(0xFFE58B43)
            }

            val hrvValNum = (b?.get("hrv_rmssd") as? Number)?.toDouble()
            val hasHrv = hrvValNum != null && hrvValNum > 0
            val hrvVal = if (hasHrv) "${hrvValNum!!.toInt()} ms" else "No data"
            val hrvScorePts = (b?.get("hrv_pts") as? Number)?.toInt()
            val hrvStatus = when {
                !hasHrv -> "Sensor Inactive"
                hrvValNum!! >= 35.0 -> "High Resilience"
                hrvValNum!! >= 22.0 -> "Balanced Tone"
                else -> "Under Strain"
            }
            val hrvAccentColor = when (hrvStatus) {
                "High Resilience" -> Color(0xFF4EAE7B)
                "Balanced Tone" -> Color(0xFF06B6D4)
                "Sensor Inactive" -> Color(0xFF6B7280)
                else -> Color(0xFFE58B43)
            }

            val dippingPctNum = (b?.get("dipping_ratio_pct") as? Number)?.toDouble()
            val hasDipping = dippingPctNum != null
            val dippingVal = if (hasDipping) "${dippingPctNum!!.toInt()}% dip" else "No data"
            val dipScorePts = (b?.get("dipping_pts") as? Number)?.toInt()
            val dippingStatus = when {
                !hasDipping -> "No data"
                dippingPctNum!! in 10.0..22.0 -> "Healthy Dipper"
                dippingPctNum!! in 5.0..<10.0 -> "Mild Dipper"
                dippingPctNum!! in 0.0..<5.0 -> "Non-Dipper"
                dippingPctNum!! < 0.0 -> "Reverse Dipper"
                else -> "Extreme Dipper"
            }
            val dippingAccentColor = when (dippingStatus) {
                "Healthy Dipper" -> Color(0xFF4EAE7B)
                "Mild Dipper" -> Color(0xFF8B5CF6)
                "Extreme Dipper" -> Color(0xFF3B82F6)
                "Non-Dipper" -> Color(0xFFE5A643)
                "Reverse Dipper" -> Color(0xFFEF4444)
                else -> Color(0xFF6B7280)
            }

            val minHrNum = (b?.get("min_heart_rate") as? Number)?.toInt()
            val maxHrNum = (b?.get("max_heart_rate") as? Number)?.toInt()
            val hasRange = minHrNum != null && maxHrNum != null && maxHrNum >= minHrNum
            val spanNum = if (hasRange) maxHrNum!! - minHrNum!! else ((b?.get("hr_span") as? Number)?.toInt())
            val rangeVal = if (hasRange) "$minHrNum – $maxHrNum" else "No data"
            val rangeScorePts = (b?.get("range_pts") as? Number)?.toInt()
            val rangeStatus = when {
                !hasRange || spanNum == null -> "No data"
                spanNum in 38..70 -> "Adaptive Span"
                spanNum > 70 -> "Broad Excursion"
                else -> "Narrow Span"
            }
            val rangeAccentColor = when (rangeStatus) {
                "Adaptive Span" -> Color(0xFF10B981)
                "Broad Excursion" -> Color(0xFF3B82F6)
                "Narrow Span" -> Color(0xFFE58B43)
                else -> Color(0xFF6B7280)
            }

            val lazyListState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = lazyListState
            ) {
                item {
                    // Top Hero Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .shadow(
                                elevation = if (ZivaaTheme.colors.isDark) 20.dp else 12.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = if (ZivaaTheme.colors.isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x08000000),
                                spotColor = if (ZivaaTheme.colors.isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x10000000)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(ZivaaTheme.colors.surfaceCard)
                            .padding(24.dp)
                    ) {
                        Column {
                            // "Heart score [i]   Score / 100" header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Heart score",
                                        style = ZivaaTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                                        color = ZivaaTheme.colors.ink
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = onInfoClick,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Heart Score Info",
                                            tint = ZivaaTheme.colors.sage,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (viewModel.scoreDateLabel.isNotBlank()) viewModel.scoreDateLabel else "Score / 100",
                                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                    color = ZivaaTheme.colors.textBody
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dialer
                            HeartArcDialer(score = score, tierTitle = text, tierColor = color)

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Heart score factors",
                                style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                color = ZivaaTheme.colors.ink
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2x2 Factor Tiles (Row 1: Resting HR & HRV)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HeartFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Resting HR",
                                    scoreText = if (rhrValNum == null) "" else "$rhrScorePts/30",
                                    primaryValue = rhrVal,
                                    unitText = "",
                                    progress = if (rhrValNum == null) 0f else (rhrScorePts / 30f).coerceIn(0f, 1f),
                                    statusText = rhrStatus,
                                    accentColor = rhrAccentColor
                                )
                                HeartFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "HRV",
                                    scoreText = if (hasHrv && hrvScorePts != null) "$hrvScorePts/25" else if (hasHrv) "25/25" else "",
                                    primaryValue = hrvVal,
                                    unitText = "",
                                    progress = if (hasHrv) 0.85f else 0f,
                                    statusText = hrvStatus,
                                    accentColor = hrvAccentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2x2 Factor Tiles (Row 2: Night Dipping & HR Range)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HeartFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Night Dipping",
                                    scoreText = if (!hasDipping || dipScorePts == null) "" else "$dipScorePts/25",
                                    primaryValue = dippingVal,
                                    unitText = "",
                                    progress = if (!hasDipping || dipScorePts == null) 0f else (dipScorePts / 25f).coerceIn(0f, 1f),
                                    statusText = dippingStatus,
                                    accentColor = dippingAccentColor
                                )
                                HeartFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "HR Range",
                                    scoreText = if (!hasRange || rangeScorePts == null) "" else "$rangeScorePts/20",
                                    primaryValue = rangeVal,
                                    unitText = if (hasRange) "bpm" else "",
                                    progress = if (!hasRange || rangeScorePts == null) 0f else (rangeScorePts / 20f).coerceIn(0f, 1f),
                                    statusText = rangeStatus,
                                    accentColor = rangeAccentColor
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Intraday Beat by Beat Card (Range every 30 mins from midnight to midnight)
                    IntradayBeatByBeatCard(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Sticky filters at the top
                stickyHeader {
                    Surface(
                        color = ZivaaTheme.colors.bg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HeartTimeRangeFilterBar(
                                selectedRange = viewModel.selectedTimeRange,
                                onRangeSelected = { viewModel.setTimeRange(it) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HeartChartTypeFilterBar(
                                selectedType = viewModel.selectedChartType,
                                onTypeSelected = { viewModel.setChartType(it) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Graph 1: Heart Health Score
                        HeartChartCard(
                            title = "Heart Health Score",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartHeartScores,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFF4EAE7B),
                            showAverageLine = true,
                            averageValue = viewModel.averageHeartScore,
                            unitSuffix = "",
                            isDecimal = false
                        )

                        // Graph 2: Resting Heart Rate (Forced LINE like RestScreen)
                        HeartChartCard(
                            title = "Resting Heart Rate (bpm)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartRestingHr,
                            chartType = HeartChartType.LINE,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFFEF4444),
                            showAverageLine = true,
                            averageValue = viewModel.averageRestingHr,
                            unitSuffix = " bpm",
                            isDecimal = false
                        )

                        // Graph 3: Heart Rate Variability (Forced LINE like RestScreen)
                        HeartChartCard(
                            title = "Heart Rate Variability (ms)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartHrv,
                            chartType = HeartChartType.LINE,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFF06B6D4),
                            showAverageLine = true,
                            averageValue = viewModel.averageHrv,
                            isNoData = !viewModel.hasHrvData,
                            unitSuffix = " ms",
                            isDecimal = false
                        )

                        // Graph 4: Heart Rate Range (Floating min-max candles + daily avg HR overlay + period avg HR line)
                        HeartRangeChartCard(
                            title = "Heart Rate Range (bpm)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            minValues = viewModel.chartMinHr,
                            maxValues = viewModel.chartMaxHr,
                            avgValues = viewModel.chartAvgHr,
                            spanValues = viewModel.chartHrSpans,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            overallAverageAvgHr = viewModel.averageDailyAvgHr,
                            color = Color(0xFF8B5CF6)
                        )

                        // Graph 5: Where It Spent The Day (Stacked Days Distribution Chart)
                        HeartZonesChartCard(
                            title = "Where It Spent the Day",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            restingPct = viewModel.chartZoneResting,
                            moderatePct = viewModel.chartZoneModerate,
                            peakPct = viewModel.chartZonePeak
                        )

                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }
    }
}

fun getHeartScoreTierInfo(score: Int): Pair<String, Color> {
    return when {
        score == 0 -> "No Readings Yet" to Color(0xFF6B7280)
        score >= 85 -> "Optimal Rhythm" to Color(0xFF4EAE7B)
        score >= 70 -> "Resilient & Stable" to Color(0xFF3B82F6)
        score >= 50 -> "Mild Cardiac Strain" to Color(0xFFE5A643)
        else -> "Attention Advised" to Color(0xFFEF4444)
    }
}

@Composable
fun HeartArcDialer(
    score: Int,
    tierTitle: String,
    tierColor: Color
) {
    val isDark = ZivaaTheme.colors.isDark
    val lineStrongColor = ZivaaTheme.colors.lineStrong
    val inkColor = ZivaaTheme.colors.ink

    val animatedScoreFraction by animateFloatAsState(
        targetValue = (score.toFloat() / 100f).coerceIn(0f, 1f),
        animationSpec = tween(1500, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)),
        label = "dialerProgress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(165.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
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

                drawArc(
                    color = if (isDark) Color(0xFF2E3238) else lineStrongColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                if (animatedScoreFraction > 0f) {
                    drawArc(
                        color = tierColor,
                        startAngle = startAngle,
                        sweepAngle = sweepTotal * animatedScoreFraction,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    val currentAngleRad = Math.toRadians((startAngle + sweepTotal * animatedScoreFraction).toDouble())
                    val knobX = cx + (radius * Math.cos(currentAngleRad)).toFloat()
                    val knobY = cy + (radius * Math.sin(currentAngleRad)).toFloat()
                    val knobCenter = Offset(knobX, knobY)

                    drawCircle(color = tierColor.copy(alpha = 0.35f), radius = knobRadiusPx + 3.dp.toPx(), center = knobCenter)
                    drawCircle(color = tierColor, radius = knobRadiusPx, center = knobCenter)
                    drawCircle(color = inkColor.copy(alpha = 0.85f), radius = 2.5.dp.toPx(), center = knobCenter)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = if (score == 0) "--" else "$score",
                    style = ZivaaTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp
                    ),
                    color = inkColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tierTitle,
            style = ZivaaTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = tierColor
        )
    }
}

@Composable
fun HeartFactorTile(
    modifier: Modifier = Modifier,
    title: String,
    scoreText: String,
    primaryValue: String,
    unitText: String,
    progress: Float,
    statusText: String,
    accentColor: Color
) {
    val isDark = ZivaaTheme.colors.isDark
    val tileBg = if (isDark) Color(0xFF1C2025) else Color(0xFFF7F4EE)
    val tileBorder = if (isDark) Color(0xFF2E333D) else Color(0xFFE6E1D7)
    val titleColor = if (isDark) Color(0xFFD4D8E2) else Color(0xFF42474E)
    val scoreTextColor = if (isDark) ZivaaTheme.colors.inkMute.copy(alpha = 0.7f) else Color(0xFF7A7E85)
    val valueColor = if (isDark) Color.White else Color(0xFF14181B)
    val unitColor = if (isDark) Color(0xFFA0A6B2) else Color(0xFF6E727A)
    val trackColor = if (isDark) Color(0xFF282B33) else Color(0xFFE5E0D6)

    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(tileBg)
            .border(1.dp, tileBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
                    color = titleColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = scoreText,
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                    color = scoreTextColor,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = primaryValue,
                    style = ZivaaTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                    color = valueColor
                )
                if (unitText.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unitText,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                        color = unitColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun HeartTimeRangeFilterBar(
    selectedRange: HeartTimeRange,
    onRangeSelected: (HeartTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(999.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeartTimeRange.entries.forEach { range ->
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
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else ZivaaTheme.colors.inkMute
                )
            }
        }
    }
}

@Composable
fun HeartChartTypeFilterBar(
    selectedType: HeartChartType,
    onTypeSelected: (HeartChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(999.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeartChartType.entries.forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) ZivaaTheme.colors.sage else Color.Transparent)
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.label,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else ZivaaTheme.colors.inkMute
                )
            }
        }
    }
}

@Composable
fun HeartChartAxisRow(
    timeRange: HeartTimeRange,
    startLabel: String,
    labels: List<String> = emptyList(),
    selectedIndex: Int? = null,
    onIndexSelected: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .padding(top = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when (timeRange) {
            HeartTimeRange.SEVEN_DAYS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    labels.forEachIndexed { index, label ->
                        val isSelected = selectedIndex != null && index == selectedIndex
                        val isToday = selectedIndex == null && index == labels.size - 1
                        Text(
                            text = label,
                            style = ZivaaTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = when {
                                isSelected -> ZivaaTheme.colors.sage
                                isToday -> ZivaaTheme.colors.sage.copy(alpha = 0.9f)
                                else -> ZivaaTheme.colors.textBody
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (onIndexSelected != null) {
                                        Modifier.clickable { onIndexSelected(index) }
                                    } else Modifier
                                )
                        )
                    }
                }
            }
            HeartTimeRange.THIRTY_DAYS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (startLabel.isNotBlank()) startLabel else (labels.firstOrNull() ?: ""),
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = ZivaaTheme.colors.textBody
                    )
                    Text(
                        text = "Today",
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                        color = ZivaaTheme.colors.sage
                    )
                }
            }
            HeartTimeRange.THREE_MONTHS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (startLabel.isNotBlank()) startLabel else (labels.firstOrNull() ?: ""),
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = ZivaaTheme.colors.textBody
                    )
                    Text(
                        text = "6 weeks ago",
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                        color = ZivaaTheme.colors.textBody.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Today",
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                        color = ZivaaTheme.colors.sage
                    )
                }
            }
        }
    }
}

@Composable
fun rememberHeartMarker(
    dates: List<String>,
    unitSuffix: String = "",
    isDecimal: Boolean = false,
    title: String = "",
    chartType: HeartChartType = HeartChartType.BAR
): Marker {
    val isDark = ZivaaTheme.colors.isDark
    val pillBgColor = if (isDark) Color(0xFF282B33) else ZivaaTheme.colors.surfaceCard
    val labelBackground = shapeComponent(Shapes.pillShape, pillBgColor)
    val label = textComponent(
        background = labelBackground,
        padding = dimensionsOf(horizontal = 10.dp, vertical = 5.dp),
        color = ZivaaTheme.colors.ink,
        textSize = 11.5.sp,
        margins = dimensionsOf(bottom = 6.dp)
    )
    val indicator = shapeComponent(Shapes.pillShape, ZivaaTheme.colors.sage)
    val guideline = lineComponent(
        color = ZivaaTheme.colors.sage.copy(alpha = 0.4f),
        thickness = 1.5.dp,
        shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
    )

    return MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline
    ).apply {
        labelFormatter = object : MarkerLabelFormatter {
            override fun getLabel(
                markedEntries: List<Marker.EntryModel>,
                chartValues: ChartValues
            ): CharSequence {
                if (markedEntries.isEmpty()) return ""
                val index = markedEntries.first().entry.x.toInt()
                val dateLabel = if (index in dates.indices) dates[index] else ""
                val total = markedEntries.sumOf { it.entry.y.toDouble() }

                if (total <= 0.0) {
                    return if (dateLabel.isNotBlank()) "No data · $dateLabel" else "No data"
                }

                val formattedVal = when {
                    isDecimal -> String.format(Locale.US, "%.1f%s", total, unitSuffix)
                    else -> "${total.toInt()}$unitSuffix"
                }

                return if (dateLabel.isNotBlank()) "$formattedVal · $dateLabel" else formattedVal
            }
        }
    }
}

@Composable
fun rememberHeartZonesMarker(
    dates: List<String>,
    restingPct: List<Float>,
    moderatePct: List<Float>,
    peakPct: List<Float>
): Marker {
    val isDark = ZivaaTheme.colors.isDark
    val pillBgColor = if (isDark) Color(0xFF282B33) else ZivaaTheme.colors.surfaceCard
    val labelBackground = shapeComponent(Shapes.pillShape, pillBgColor)
    val label = textComponent(
        background = labelBackground,
        padding = dimensionsOf(horizontal = 10.dp, vertical = 5.dp),
        color = ZivaaTheme.colors.ink,
        textSize = 11.sp,
        margins = dimensionsOf(bottom = 6.dp)
    )
    val indicator = shapeComponent(Shapes.pillShape, ZivaaTheme.colors.sage)
    val guideline = lineComponent(
        color = ZivaaTheme.colors.sage.copy(alpha = 0.4f),
        thickness = 1.5.dp,
        shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
    )

    return MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline
    ).apply {
        labelFormatter = object : MarkerLabelFormatter {
            override fun getLabel(
                markedEntries: List<Marker.EntryModel>,
                chartValues: ChartValues
            ): CharSequence {
                if (markedEntries.isEmpty()) return ""
                val index = markedEntries.first().entry.x.toInt()
                val dateLabel = if (index in dates.indices) dates[index] else ""
                val r = restingPct.getOrNull(index)?.toInt() ?: 0
                val m = moderatePct.getOrNull(index)?.toInt() ?: 0
                val p = peakPct.getOrNull(index)?.toInt() ?: 0
                val text = "Rest $r% · Active $m% · Peak $p%"
                return if (dateLabel.isNotBlank()) "$text · $dateLabel" else text
            }
        }
    }
}

@Composable
fun HeartChartCard(
    title: String,
    dates: List<String>,
    detailedDates: List<String> = emptyList(),
    values: List<Float>,
    chartType: HeartChartType,
    timeRange: HeartTimeRange = HeartTimeRange.SEVEN_DAYS,
    startDateLabel: String = "",
    color: Color,
    showAverageLine: Boolean = false,
    averageValue: Float = 0f,
    isNoData: Boolean = false,
    unitSuffix: String = "",
    isDecimal: Boolean = false
) {
    if (dates.isEmpty() && values.isEmpty()) return

    val isDark = ZivaaTheme.colors.isDark
    val isSevenDays = timeRange == HeartTimeRange.SEVEN_DAYS
    val noDataState = isNoData || (values.all { it <= 0f } && title.contains("HRV", ignoreCase = true))

    val barThickness = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> 20.dp
        HeartTimeRange.THIRTY_DAYS -> 7.dp
        HeartTimeRange.THREE_MONTHS -> 16.dp
    }
    val barSpacing = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> 14.dp
        HeartTimeRange.THIRTY_DAYS -> 2.5.dp
        HeartTimeRange.THREE_MONTHS -> 6.dp
    }

    val chartEntryModel = if (chartType == HeartChartType.LINE) {
        val seriesList = mutableListOf<List<FloatEntry>>()
        var currentSeries = mutableListOf<FloatEntry>()
        values.forEachIndexed { index, value ->
            if (value >= -0.1f) {
                currentSeries.add(FloatEntry(x = index.toFloat(), y = value))
            } else {
                if (currentSeries.isNotEmpty()) {
                    seriesList.add(currentSeries)
                    currentSeries = mutableListOf()
                }
            }
        }
        if (currentSeries.isNotEmpty()) seriesList.add(currentSeries)
        if (seriesList.isEmpty()) seriesList.add(listOf(FloatEntry(0f, 0f)))
        entryModelOf(*seriesList.toTypedArray())
    } else {
        val series = values.mapIndexed { index, value ->
            List(values.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index) value else 0f) }
        }
        entryModelOf(*series.toTypedArray())
    }

    val isHeartScoreCard = title.contains("Heart Health Score", ignoreCase = true) || title.contains("Heart Score", ignoreCase = true)

    val pastBarColor = if (isDark) Color(0xFF4A4E58) else Color(0xFFB8B0A2)
    val columns = List(values.size) { index ->
        val barColor = if (isHeartScoreCard) {
            val barVal = values.getOrNull(index)?.roundToInt() ?: 0
            if (barVal <= 0) {
                pastBarColor
            } else {
                getHeartScoreTierInfo(barVal).second
            }
        } else {
            if (index == values.size - 1) color else pastBarColor
        }
        lineComponent(color = barColor, thickness = barThickness, shape = Shapes.pillShape)
    }

    val chartMax = (values.maxOrNull() ?: 0f) * 1.15f
    val plotAreaHeightDp = 150.dp
    val rangeLabel = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> "7 Days"
        HeartTimeRange.THIRTY_DAYS -> "30 Days"
        HeartTimeRange.THREE_MONTHS -> "3 Months"
    }

    val heroValue = if (isHeartScoreCard) {
        if (averageValue > 0f) averageValue else {
            val nonZeros = values.filter { it > 0f }
            if (nonZeros.isNotEmpty()) nonZeros.average().toFloat() else (values.lastOrNull() ?: 0f)
        }
    } else if (isSevenDays) {
        values.lastOrNull() ?: 0f
    } else {
        val nonZeros = values.filter { it > 0f }
        if (nonZeros.isNotEmpty()) nonZeros.average().toFloat() else 0f
    }

    val scoreInt = heroValue.roundToInt()
    val (tagText, tagColor) = if (isHeartScoreCard) getHeartScoreTierInfo(scoreInt) else "" to color

    val heroSubtitle = if (isHeartScoreCard) {
        when (timeRange) {
            HeartTimeRange.SEVEN_DAYS -> "7-day average score"
            HeartTimeRange.THIRTY_DAYS -> "30-day average score"
            HeartTimeRange.THREE_MONTHS -> "3-month average score"
        }
    } else if (isSevenDays) {
        when {
            title.contains("Score", ignoreCase = true) -> "latest score"
            title.contains("Resting", ignoreCase = true) -> "latest resting rate"
            title.contains("Variability", ignoreCase = true) -> "latest HRV"
            else -> "latest reading"
        }
    } else {
        when {
            title.contains("Score", ignoreCase = true) -> "period average score"
            title.contains("Resting", ignoreCase = true) -> "period average rest"
            title.contains("Variability", ignoreCase = true) -> "period average HRV"
            else -> "period average"
        }
    }

    val avgLine = if (showAverageLine && averageValue > 0f) {
        val avgLabelText = when {
            title.contains("Heart Health Score", ignoreCase = true) -> "Avg ${averageValue.toInt()}"
            title.contains("Heart Rate", ignoreCase = true) -> "Avg ${averageValue.toInt()} bpm"
            title.contains("HRV", ignoreCase = true) -> "Avg ${averageValue.toInt()} ms"
            else -> "Avg ${String.format(Locale.US, "%.1f", averageValue)}"
        }
        ThresholdLine(
            thresholdValue = averageValue,
            lineComponent = lineComponent(
                color = ZivaaTheme.colors.ink.copy(alpha = 0.35f),
                thickness = 1.dp,
                shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
            ),
            labelComponent = textComponent(
                color = ZivaaTheme.colors.textBody,
                textSize = 9.5.sp,
                margins = dimensionsOf(bottom = 4.dp)
            ),
            labelHorizontalPosition = ThresholdLine.LabelHorizontalPosition.Start,
            thresholdLabel = avgLabelText
        )
    } else null

    val decorations = if (avgLine != null) listOf(avgLine) else emptyList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 20.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x0C000000),
                spotColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "${title} · ${rangeLabel}",
                style = ZivaaTheme.typography.eyebrow.copy(
                    fontSize = 12.5.sp,
                    letterSpacing = 0.06.em
                ),
                color = ZivaaTheme.colors.eyebrow
            )
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.padding(bottom = 18.dp)) {
                if (noDataState) {
                    Text(
                        text = "No data",
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        )
                    )
                    Text(
                        text = "no readings recorded",
                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                } else {
                    val heroText = if (isDecimal) {
                        String.format(Locale.US, "%.1f", heroValue)
                    } else if (heroValue % 1 == 0f) {
                        heroValue.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.1f", heroValue)
                    }
                    if (isHeartScoreCard && scoreInt > 0) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "$heroText$unitSuffix",
                                style = ZivaaTheme.typography.displayLarge.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tagColor
                                )
                            )
                            Text(
                                text = tagText,
                                style = ZivaaTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tagColor
                                ),
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "$heroText$unitSuffix",
                            style = ZivaaTheme.typography.displayLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = heroSubtitle,
                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(plotAreaHeightDp)
            ) {
                if (noDataState) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded readings for this period",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = ZivaaTheme.colors.textBody.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Chart(
                        modifier = Modifier.fillMaxSize(),
                        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                        chart = if (chartType == HeartChartType.BAR) {
                            columnChart(
                                columns = columns,
                                mergeMode = MergeMode.Stack,
                                spacing = barSpacing,
                                decorations = decorations,
                                axisValuesOverrider = AxisValuesOverrider.fixed(
                                    minY = 0f,
                                    maxY = if (chartMax > 0f) chartMax else 10f
                                )
                            )
                        } else {
                            lineChart(
                                lines = List(chartEntryModel.entries.size) {
                                    lineSpec(
                                        lineColor = color,
                                        lineBackgroundShader = null,
                                        lineThickness = 3.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = color),
                                        pointSize = 6.dp
                                    )
                                },
                                decorations = decorations,
                                axisValuesOverrider = AxisValuesOverrider.fixed(
                                    minY = 0f,
                                    maxY = if (chartMax > 0f) chartMax else 10f
                                )
                            )
                        },
                        model = chartEntryModel,
                        marker = rememberHeartMarker(
                            dates = detailedDates.ifEmpty { dates },
                            unitSuffix = unitSuffix,
                            isDecimal = isDecimal,
                            title = title,
                            chartType = chartType
                        ),
                        startAxis = null,
                        bottomAxis = null
                    )
                }
            }

            if (!noDataState) {
                HeartChartAxisRow(
                    timeRange = timeRange,
                    startLabel = startDateLabel,
                    labels = dates
                )
            }
        }
    }
}

@Composable
fun HeartRangeChartCard(
    title: String,
    dates: List<String>,
    detailedDates: List<String> = emptyList(),
    minValues: List<Float>,
    maxValues: List<Float>,
    avgValues: List<Float>,
    spanValues: List<Float>,
    chartType: HeartChartType = HeartChartType.BAR,
    timeRange: HeartTimeRange = HeartTimeRange.SEVEN_DAYS,
    startDateLabel: String = "",
    overallAverageAvgHr: Float = 0f,
    color: Color = Color(0xFF8B5CF6)
) {
    if (dates.isEmpty() && minValues.isEmpty()) return

    val isDark = ZivaaTheme.colors.isDark
    val isSevenDays = timeRange == HeartTimeRange.SEVEN_DAYS

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(timeRange) {
        selectedIndex = null
    }

    val validSpans = spanValues.filter { it > 0f }
    val latestSpan = validSpans.lastOrNull() ?: 0f
    val avgSpan = if (validSpans.isNotEmpty()) validSpans.average().toFloat() else 0f
    val heroSpan = if (isSevenDays) latestSpan else avgSpan

    val validMins = minValues.filter { it > 0f }
    val validMaxs = maxValues.filter { it > 0f }
    val yMin = maxOf(40f, (validMins.minOrNull() ?: 50f) - 6f)
    val yMax = minOf(160f, (validMaxs.maxOrNull() ?: 120f) + 8f)

    val rangeLabel = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> "7 Days"
        HeartTimeRange.THIRTY_DAYS -> "30 Days"
        HeartTimeRange.THREE_MONTHS -> "3 Months"
    }

    val selMin = selectedIndex?.let { minValues.getOrNull(it) }?.takeIf { it > 0f }
    val selMax = selectedIndex?.let { maxValues.getOrNull(it) }?.takeIf { it > 0f }
    val selAvg = selectedIndex?.let { avgValues.getOrNull(it) }?.takeIf { it > 0f }
    val selSpan = selectedIndex?.let { spanValues.getOrNull(it) }?.takeIf { it > 0f }
    val selDate = selectedIndex?.let { idx ->
        detailedDates.getOrNull(idx) ?: dates.getOrNull(idx) ?: ""
    } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 20.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x0C000000),
                spotColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "${title} · ${rangeLabel}",
                style = ZivaaTheme.typography.eyebrow.copy(
                    fontSize = 12.5.sp,
                    letterSpacing = 0.06.em
                ),
                color = ZivaaTheme.colors.eyebrow
            )
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.padding(bottom = 18.dp)) {
                if (selectedIndex != null) {
                    if (selMin != null && selMax != null && selMax >= selMin) {
                        val span = selSpan ?: (selMax - selMin)
                        Text(
                            text = "${span.roundToInt()} bpm",
                            style = ZivaaTheme.typography.displayLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        )
                        Text(
                            text = buildString {
                                append(selDate)
                                append(" · Range: ${selMin.roundToInt()} – ${selMax.roundToInt()} bpm")
                                if (selAvg != null && selAvg > 0f) {
                                    append(" · Avg: ${selAvg.roundToInt()} bpm")
                                }
                            },
                            style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = ZivaaTheme.colors.inkMute
                        )
                    } else {
                        Text(
                            text = "No data",
                            style = ZivaaTheme.typography.displayLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            )
                        )
                        Text(
                            text = "$selDate · no readings recorded",
                            style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = ZivaaTheme.colors.inkMute
                        )
                    }
                } else {
                    Text(
                        text = "${heroSpan.toInt()} bpm",
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                    Text(
                        text = if (isSevenDays) "today's dynamic span" else "period average span",
                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = ZivaaTheme.colors.inkMute
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val density = LocalDensity.current
                val sageColor = ZivaaTheme.colors.sage
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()
                val n = minValues.size
                if (n == 0) return@BoxWithConstraints

                val barWidthPx = with(density) {
                    when (timeRange) {
                        HeartTimeRange.SEVEN_DAYS -> 20.dp.toPx()
                        HeartTimeRange.THIRTY_DAYS -> 7.dp.toPx()
                        HeartTimeRange.THREE_MONTHS -> 16.dp.toPx()
                    }
                }

                val topPad = with(density) { 8.dp.toPx() }
                val bottomPad = with(density) { 6.dp.toPx() }
                val plotH = heightPx - topPad - bottomPad

                fun getY(bpm: Float): Float {
                    val fraction = ((bpm - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
                    return topPad + (plotH - (fraction * plotH))
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(n) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val colW = size.width / n.toFloat()
                                    val idx = (offset.x / colW).toInt().coerceIn(0, n - 1)
                                    selectedIndex = idx
                                    tryAwaitRelease()
                                },
                                onTap = { offset ->
                                    val colW = size.width / n.toFloat()
                                    val idx = (offset.x / colW).toInt().coerceIn(0, n - 1)
                                    selectedIndex = idx
                                }
                            )
                        }
                        .pointerInput(n) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val colW = size.width / n.toFloat()
                                    val idx = (offset.x / colW).toInt().coerceIn(0, n - 1)
                                    selectedIndex = idx
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    val colW = size.width / n.toFloat()
                                    val idx = (change.position.x / colW).toInt().coerceIn(0, n - 1)
                                    selectedIndex = idx
                                }
                            )
                        }
                ) {
                    // Period Average Dashed Line
                    if (overallAverageAvgHr > 0f) {
                        val avgY = getY(overallAverageAvgHr)
                        drawLine(
                            color = Color(0xFFEF4444).copy(alpha = 0.45f),
                            start = Offset(0f, avgY),
                            end = Offset(widthPx, avgY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }

                    // Active highlight stripe behind the selected bar
                    if (selectedIndex != null) {
                        val selIdx = selectedIndex!!
                        val colW = widthPx / n.toFloat()
                        val selCx = colW * (selIdx.toFloat() + 0.5f)
                        drawRoundRect(
                            color = color.copy(alpha = 0.09f),
                            topLeft = Offset(selCx - colW * 0.46f, topPad),
                            size = Size(colW * 0.92f, plotH),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        // Vertical dashed guideline through cx
                        drawLine(
                            color = sageColor.copy(alpha = 0.55f),
                            start = Offset(selCx, topPad),
                            end = Offset(selCx, topPad + plotH),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // Vertical Floating Range Candles
                    val avgPoints = mutableListOf<Offset>()
                    val pastBarColor = if (isDark) Color(0xFF4A4E58) else Color(0xFFB8B0A2)

                    for (i in 0 until n) {
                        val minHr = minValues.getOrNull(i) ?: 0f
                        val maxHr = maxValues.getOrNull(i) ?: 0f
                        val avgHr = avgValues.getOrNull(i) ?: 0f

                        // Exact alignment matching HeartChartAxisRow (widthPx / n) * (i + 0.5f)
                        val colW = widthPx / n.toFloat()
                        val cx = colW * (i.toFloat() + 0.5f)

                        if (minHr > 0f && maxHr >= minHr) {
                            val topY = getY(maxHr)
                            val botY = getY(minHr)
                            val isSelected = (selectedIndex != null && i == selectedIndex)
                            val isToday = (selectedIndex == null && i == n - 1)

                            val candleColor = when {
                                isSelected -> color
                                isToday -> color
                                selectedIndex != null -> pastBarColor.copy(alpha = 0.55f)
                                else -> pastBarColor
                            }

                            drawRoundRect(
                                color = candleColor,
                                topLeft = Offset(cx - barWidthPx / 2f, topY),
                                size = Size(barWidthPx, maxOf(botY - topY, barWidthPx)),
                                cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
                            )

                            // Subtle halo around selected candle
                            if (isSelected) {
                                drawRoundRect(
                                    color = color.copy(alpha = 0.25f),
                                    topLeft = Offset(cx - (barWidthPx + 4.dp.toPx()) / 2f, topY - 2.dp.toPx()),
                                    size = Size(barWidthPx + 4.dp.toPx(), maxOf(botY - topY, barWidthPx) + 4.dp.toPx()),
                                    cornerRadius = CornerRadius((barWidthPx + 4.dp.toPx()) / 2f, (barWidthPx + 4.dp.toPx()) / 2f),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }

                            if (avgHr > 0f) {
                                val cy = getY(avgHr)
                                avgPoints.add(Offset(cx, cy))
                            }
                        }
                    }

                    // Average Line
                    if (avgPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(avgPoints.first().x, avgPoints.first().y)
                            for (p in avgPoints.drop(1)) {
                                lineTo(p.x, p.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFEF4444).copy(alpha = if (selectedIndex != null) 0.6f else 1f),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    // Average Points
                    for (p in avgPoints) {
                        val colW = widthPx / n.toFloat()
                        val isPointSelected = selectedIndex != null && kotlin.math.abs(p.x - (colW * (selectedIndex!!.toFloat() + 0.5f))) < 2f
                        if (isPointSelected) {
                            drawCircle(
                                color = Color.White,
                                radius = 5.5.dp.toPx(),
                                center = p
                            )
                            drawCircle(
                                color = Color(0xFFEF4444),
                                radius = 3.5.dp.toPx(),
                                center = p
                            )
                        } else {
                            drawCircle(
                                color = Color.White,
                                radius = 3.5.dp.toPx(),
                                center = p
                            )
                            drawCircle(
                                color = Color(0xFFEF4444),
                                radius = 2.2.dp.toPx(),
                                center = p
                            )
                        }
                    }
                }

                // Floating Tooltip Marker Overlay
                if (selectedIndex != null) {
                    val selIdx = selectedIndex!!
                    val colW = widthPx / n.toFloat()
                    val cxPx = colW * (selIdx.toFloat() + 0.5f)
                    val cxDp = with(density) { cxPx.toDp() }
                    val tooltipWidth = 140.dp
                    val leftOffsetDp = (cxDp - tooltipWidth / 2f).coerceIn(0.dp, maxWidth - tooltipWidth)

                    val maxHr = selMax
                    val topY = if (maxHr != null) getY(maxHr) else 0f
                    val tooltipHeightPx = with(density) { 46.dp.toPx() }
                    val minGapPx = with(density) { 6.dp.toPx() }
                    val minTopPx = with(density) { 2.dp.toPx() }
                    val targetTopY = (topY - tooltipHeightPx - minGapPx).coerceAtLeast(minTopPx)
                    val topOffsetDp = with(density) { targetTopY.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = leftOffsetDp, y = topOffsetDp)
                            .width(tooltipWidth)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(10.dp),
                                ambientColor = if (isDark) Color.Black else Color(0x18000000),
                                spotColor = if (isDark) Color.Black else Color(0x28000000)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF242830) else Color(0xFFFFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (isDark) Color(0xFF3F4450) else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (selMin != null && selMax != null && selMax >= selMin) {
                                Text(
                                    text = "${selMin.roundToInt()} – ${selMax.roundToInt()} bpm",
                                    style = ZivaaTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = color,
                                    maxLines = 1
                                )
                                Text(
                                    text = buildString {
                                        if (selAvg != null && selAvg > 0f) {
                                            append("Avg ${selAvg.roundToInt()} · ")
                                        }
                                        append(selDate)
                                    },
                                    style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp),
                                    color = ZivaaTheme.colors.inkMute,
                                    maxLines = 1
                                )
                            } else {
                                Text(
                                    text = "No readings",
                                    style = ZivaaTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = ZivaaTheme.colors.inkMute,
                                    maxLines = 1
                                )
                                Text(
                                    text = selDate,
                                    style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp),
                                    color = ZivaaTheme.colors.inkMute,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            HeartChartAxisRow(
                timeRange = timeRange,
                startLabel = startDateLabel,
                labels = dates,
                selectedIndex = selectedIndex,
                onIndexSelected = { idx ->
                    selectedIndex = if (selectedIndex == idx) null else idx
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Range & Average Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Range",
                    style = ZivaaTheme.typography.meta.copy(fontSize = 11.5.sp),
                    color = ZivaaTheme.colors.inkMute
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Daily Avg HR",
                    style = ZivaaTheme.typography.meta.copy(fontSize = 11.5.sp),
                    color = ZivaaTheme.colors.inkMute
                )
                if (overallAverageAvgHr > 0f) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Period Avg: ${overallAverageAvgHr.toInt()} bpm",
                        style = ZivaaTheme.typography.meta.copy(fontSize = 11.5.sp),
                        color = ZivaaTheme.colors.textBody
                    )
                }
                if (selectedIndex != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Reset",
                        style = ZivaaTheme.typography.meta.copy(
                            fontSize = 11.5.sp,
                            color = ZivaaTheme.colors.sage,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { selectedIndex = null }
                    )
                }
            }
        }
    }
}

@Composable
fun HeartZonesChartCard(
    title: String = "Where It Spent the Day",
    dates: List<String>,
    detailedDates: List<String> = emptyList(),
    chartType: HeartChartType = HeartChartType.BAR,
    timeRange: HeartTimeRange = HeartTimeRange.SEVEN_DAYS,
    startDateLabel: String = "",
    restingPct: List<Float>,
    moderatePct: List<Float>,
    peakPct: List<Float>
) {
    if (dates.isEmpty() || restingPct.isEmpty()) return

    val isDark = ZivaaTheme.colors.isDark

    val barThickness = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> 20.dp
        HeartTimeRange.THIRTY_DAYS -> 7.dp
        HeartTimeRange.THREE_MONTHS -> 16.dp
    }
    val barSpacing = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> 14.dp
        HeartTimeRange.THIRTY_DAYS -> 2.5.dp
        HeartTimeRange.THREE_MONTHS -> 6.dp
    }

    val pointSize = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> 5.dp
        HeartTimeRange.THIRTY_DAYS -> 3.dp
        HeartTimeRange.THREE_MONTHS -> 4.dp
    }

    val rangeLabel = when (timeRange) {
        HeartTimeRange.SEVEN_DAYS -> "7 Days"
        HeartTimeRange.THIRTY_DAYS -> "30 Days"
        HeartTimeRange.THREE_MONTHS -> "3 Months"
    }
    val plotAreaHeightDp = 150.dp

    val restingColor = Color(0xFF4EAE7B) // Optimal Green (<75 bpm)
    val moderateColor = Color(0xFF3B82F6) // Steady Blue (75–105 bpm)
    val peakColor = Color(0xFFEF4444) // Elevated Red (>105 bpm)

    val totalSeriesList = mutableListOf<List<FloatEntry>>()
    val moderateSeriesList = mutableListOf<List<FloatEntry>>()
    val restingSeriesList = mutableListOf<List<FloatEntry>>()

    val chartModel = if (chartType == HeartChartType.BAR) {
        val restingEntries = restingPct.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        val moderateEntries = moderatePct.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        val peakEntries = peakPct.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        entryModelOf(restingEntries, moderateEntries, peakEntries)
    } else {
        var curTotal = mutableListOf<FloatEntry>()
        var curMod = mutableListOf<FloatEntry>()
        var curRest = mutableListOf<FloatEntry>()

        (0 until restingPct.size).forEach { index ->
            val r = restingPct.getOrElse(index) { 0f }
            val m = moderatePct.getOrElse(index) { 0f }
            val p = peakPct.getOrElse(index) { 0f }

            if (r >= 0f && m >= 0f && p >= 0f) {
                curTotal.add(FloatEntry(x = index.toFloat(), y = r + m + p))
            } else {
                if (curTotal.isNotEmpty()) { totalSeriesList.add(curTotal); curTotal = mutableListOf() }
            }
            if (r >= 0f && m >= 0f) {
                curMod.add(FloatEntry(x = index.toFloat(), y = r + m))
            } else {
                if (curMod.isNotEmpty()) { moderateSeriesList.add(curMod); curMod = mutableListOf() }
            }
            if (r >= 0f) {
                curRest.add(FloatEntry(x = index.toFloat(), y = r))
            } else {
                if (curRest.isNotEmpty()) { restingSeriesList.add(curRest); curRest = mutableListOf() }
            }
        }
        if (curTotal.isNotEmpty()) totalSeriesList.add(curTotal)
        if (curMod.isNotEmpty()) moderateSeriesList.add(curMod)
        if (curRest.isNotEmpty()) restingSeriesList.add(curRest)

        if (totalSeriesList.isEmpty()) totalSeriesList.add(listOf(FloatEntry(0f, 0f)))
        if (moderateSeriesList.isEmpty()) moderateSeriesList.add(listOf(FloatEntry(0f, 0f)))
        if (restingSeriesList.isEmpty()) restingSeriesList.add(listOf(FloatEntry(0f, 0f)))

        entryModelOf(*(totalSeriesList + moderateSeriesList + restingSeriesList).toTypedArray())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 20.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x0C000000),
                spotColor = if (isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "${title} · ${rangeLabel}",
                style = ZivaaTheme.typography.eyebrow.copy(
                    fontSize = 12.5.sp,
                    letterSpacing = 0.06.em
                ),
                color = ZivaaTheme.colors.eyebrow
            )
            Spacer(modifier = Modifier.height(14.dp))
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(restingColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resting (<75 bpm)", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(moderateColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Active (75–105 bpm)", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(peakColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Peak (>105 bpm)", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(plotAreaHeightDp)
            ) {
                Chart(
                    modifier = Modifier.fillMaxSize(),
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                    chart = if (chartType == HeartChartType.BAR) {
                        columnChart(
                            columns = listOf(
                                lineComponent(color = restingColor, thickness = barThickness),
                                lineComponent(color = moderateColor, thickness = barThickness),
                                lineComponent(
                                    color = peakColor,
                                    thickness = barThickness,
                                    shape = Shapes.roundedCornerShape(topLeftPercent = 50, topRightPercent = 50)
                                )
                            ),
                            mergeMode = MergeMode.Stack,
                            spacing = barSpacing,
                            axisValuesOverrider = AxisValuesOverrider.fixed(
                                minY = 0f,
                                maxY = 100f
                            )
                        )
                    } else {
                        lineChart(
                            lines = buildList {
                                repeat(totalSeriesList.size) {
                                    add(lineSpec(
                                        lineColor = peakColor,
                                        lineBackgroundShader = verticalGradient(
                                            colors = arrayOf(peakColor.copy(alpha = 0.4f), peakColor.copy(alpha = 0.05f))
                                        ),
                                        lineThickness = 2.5.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = peakColor),
                                        pointSize = pointSize
                                    ))
                                }
                                repeat(moderateSeriesList.size) {
                                    add(lineSpec(
                                        lineColor = moderateColor,
                                        lineBackgroundShader = verticalGradient(
                                            colors = arrayOf(moderateColor.copy(alpha = 0.6f), moderateColor.copy(alpha = 0.1f))
                                        ),
                                        lineThickness = 2.5.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = moderateColor),
                                        pointSize = pointSize
                                    ))
                                }
                                repeat(restingSeriesList.size) {
                                    add(lineSpec(
                                        lineColor = restingColor,
                                        lineBackgroundShader = verticalGradient(
                                            colors = arrayOf(restingColor.copy(alpha = 0.8f), restingColor.copy(alpha = 0.2f))
                                        ),
                                        lineThickness = 2.5.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = restingColor),
                                        pointSize = pointSize
                                    ))
                                }
                            },
                            axisValuesOverrider = AxisValuesOverrider.fixed(
                                minY = 0f,
                                maxY = 100f
                            )
                        )
                    },
                    model = chartModel,
                    marker = rememberHeartZonesMarker(
                        dates = detailedDates.ifEmpty { dates },
                        restingPct = restingPct,
                        moderatePct = moderatePct,
                        peakPct = peakPct
                    ),
                    startAxis = null,
                    bottomAxis = null
                )
            }

            HeartChartAxisRow(
                timeRange = timeRange,
                startLabel = startDateLabel,
                labels = dates
            )
        }
    }
}

@Composable
fun IntradayBeatByBeatCard(
    viewModel: HeartRateViewModel,
    modifier: Modifier = Modifier
) {
    val buckets = viewModel.intradayBuckets
    val hasData = viewModel.hasData.collectAsState().value
    val isDark = ZivaaTheme.colors.isDark
    val colors = ZivaaTheme.colors

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 20.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isDark) colors.sage.copy(alpha = 0.08f) else Color(0x0C000000),
                spotColor = if (isDark) colors.sage.copy(alpha = 0.08f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceCard)
            .padding(22.dp)
    ) {
        if (!hasData || buckets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR DAY, BEAT BY BEAT",
                    style = ZivaaTheme.typography.eyebrow,
                    color = colors.eyebrow
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No intraday data recorded yet today.\nWear your watch/ring to capture continuous 30-minute heart rate ranges.",
                    style = ZivaaTheme.typography.bodyMedium,
                    color = colors.inkMute,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            var selectedBucketIndex by remember { mutableStateOf<Int?>(null) }
            val activeBucket = selectedBucketIndex?.let { buckets.getOrNull(it) }

            Column {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR DAY, BEAT BY BEAT",
                        style = ZivaaTheme.typography.eyebrow.copy(
                            fontSize = 12.5.sp,
                            letterSpacing = 0.06.em
                        ),
                        color = colors.eyebrow
                    )
                    Text(
                        text = viewModel.intradayDateLabel,
                        style = ZivaaTheme.typography.meta.copy(fontWeight = FontWeight.Medium),
                        color = colors.inkMute
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Inspector Summary Row
                if (activeBucket != null && activeBucket.hasData) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isDark) Color(0xFF1F242C) else Color(0xFFF7F4EE),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = activeBucket.timeLabel,
                                style = ZivaaTheme.typography.cardTitle.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                color = colors.textStrong,
                                maxLines = 1
                            )
                            Text(
                                text = "${activeBucket.count} samples",
                                style = ZivaaTheme.typography.meta,
                                color = colors.inkMute,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${activeBucket.minBpm.roundToInt()} – ${activeBucket.maxBpm.roundToInt()}",
                                    style = ZivaaTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp),
                                    color = Color(0xFFEF4444),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "bpm",
                                    style = ZivaaTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = colors.inkMute,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = "Avg ${activeBucket.avgBpm.roundToInt()} bpm",
                                style = ZivaaTheme.typography.meta.copy(fontWeight = FontWeight.Medium),
                                color = colors.textBody,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isDark) Color(0xFF1F242C) else Color(0xFFF7F4EE),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Day Range (30-min)",
                                style = ZivaaTheme.typography.meta,
                                color = colors.inkMute,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${viewModel.intradayMinHr} – ${viewModel.intradayMaxHr}",
                                    style = ZivaaTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                                    color = colors.textStrong,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "bpm",
                                    style = ZivaaTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = colors.inkMute,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Day Average",
                                style = ZivaaTheme.typography.meta,
                                color = colors.inkMute,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${viewModel.intradayAvgHr}",
                                    style = ZivaaTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                                    color = Color(0xFFEF4444),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "bpm",
                                    style = ZivaaTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = colors.inkMute,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 48-Bucket Range Chart (Canvas)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val heightPx = constraints.maxHeight.toFloat()
                    val density = LocalDensity.current
                    val bottomAxisHeightPx = with(density) { 24.dp.toPx() }
                    val leftLabelWidthPx = with(density) { 30.dp.toPx() }
                    val topPaddingPx = with(density) { 8.dp.toPx() }
                    val plotWidth = widthPx - leftLabelWidthPx
                    val plotHeight = heightPx - bottomAxisHeightPx - topPaddingPx
                    val bucketWidth = plotWidth / 48f

                    val minDataBpm = remember(buckets) {
                        val mins = buckets.filter { it.hasData }.map { it.minBpm }
                        if (mins.isNotEmpty()) mins.minOrNull()!! else 50f
                    }
                    val maxDataBpm = remember(buckets) {
                        val maxs = buckets.filter { it.hasData }.map { it.maxBpm }
                        if (maxs.isNotEmpty()) maxs.maxOrNull()!! else 110f
                    }

                    // Rounded grid intervals e.g. 50 to 120
                    val yMin = remember(minDataBpm) { (((minDataBpm - 8f) / 10f).toInt() * 10f).coerceAtLeast(30f) }
                    val yMax = remember(maxDataBpm, yMin) { (((maxDataBpm + 12f) / 10f).toInt() * 10f).coerceAtLeast(yMin + 40f) }

                    fun getY(bpm: Float): Float {
                        val norm = (bpm - yMin) / (yMax - yMin)
                        return topPaddingPx + (1f - norm.coerceIn(0f, 1f)) * plotHeight
                    }
                    fun getX(bucketIdx: Int): Float {
                        return leftLabelWidthPx + (bucketIdx + 0.5f) * bucketWidth
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(buckets) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        val relX = offset.x - leftLabelWidthPx
                                        if (relX in 0f..plotWidth) {
                                            selectedBucketIndex = (relX / bucketWidth).toInt().coerceIn(0, 47)
                                        }
                                        tryAwaitRelease()
                                    },
                                    onTap = { offset ->
                                        val relX = offset.x - leftLabelWidthPx
                                        if (relX in 0f..plotWidth) {
                                            selectedBucketIndex = (relX / bucketWidth).toInt().coerceIn(0, 47)
                                        }
                                    }
                                )
                            }
                            .pointerInput(buckets) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val relX = offset.x - leftLabelWidthPx
                                        if (relX in 0f..plotWidth) {
                                            selectedBucketIndex = (relX / bucketWidth).toInt().coerceIn(0, 47)
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val relX = change.position.x - leftLabelWidthPx
                                        if (relX in 0f..plotWidth) {
                                            selectedBucketIndex = (relX / bucketWidth).toInt().coerceIn(0, 47)
                                        }
                                    }
                                )
                            }
                    ) {
                        val lineStroke = 1.dp.toPx()
                        val gridColor = if (isDark) Color(0xFF262A30) else Color(0xFFF0EBE1)
                        val textPaint = android.graphics.Paint().apply {
                            color = if (isDark) android.graphics.Color.parseColor("#8E95A3") else android.graphics.Color.parseColor("#9E9E9E")
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }

                        // Horizontal grid lines (4 steps)
                        val stepCount = 4
                        for (s in 0..stepCount) {
                            val bpmVal = yMin + (yMax - yMin) * (s.toFloat() / stepCount)
                            val y = getY(bpmVal)
                            drawLine(
                                color = gridColor,
                                start = Offset(leftLabelWidthPx, y),
                                end = Offset(widthPx, y),
                                strokeWidth = lineStroke
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${bpmVal.roundToInt()}",
                                leftLabelWidthPx - 4.dp.toPx(),
                                y + 3.dp.toPx(),
                                textPaint
                            )
                        }

                        // Vertical grid lines & labels at midnight, 6 AM, noon, 6 PM, midnight
                        val timeDivisions = listOf(
                            Pair(0, "12 AM"),
                            Pair(12, "6 AM"),
                            Pair(24, "12 PM"),
                            Pair(36, "6 PM"),
                            Pair(48, "12 AM")
                        )

                        timeDivisions.forEachIndexed { index, (bucketOffset, label) ->
                            val x = leftLabelWidthPx + bucketOffset * bucketWidth
                            drawLine(
                                color = gridColor,
                                start = Offset(x, topPaddingPx),
                                end = Offset(x, topPaddingPx + plotHeight),
                                strokeWidth = lineStroke
                            )
                            val labelPaint = android.graphics.Paint(textPaint).apply {
                                textAlign = when (index) {
                                    0 -> android.graphics.Paint.Align.LEFT
                                    timeDivisions.size - 1 -> android.graphics.Paint.Align.RIGHT
                                    else -> android.graphics.Paint.Align.CENTER
                                }
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x,
                                heightPx - 4.dp.toPx(),
                                labelPaint
                            )
                        }

                        // Active scrub highlight stripe
                        selectedBucketIndex?.let { selIdx ->
                            val selX = getX(selIdx)
                            drawRoundRect(
                                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                topLeft = Offset(selX - bucketWidth / 2f, topPaddingPx),
                                size = Size(bucketWidth, plotHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }

                        // Draw 48 range candles
                        val barThickness = (bucketWidth * 0.65f).coerceIn(3.dp.toPx(), 6.5.dp.toPx())

                        buckets.forEachIndexed { idx, bucket ->
                            if (!bucket.hasData) return@forEachIndexed

                            val cx = getX(idx)
                            val isSelected = (selectedBucketIndex == idx)
                            val topY = getY(bucket.maxBpm)
                            val bottomY = getY(bucket.minBpm)
                            val barHeight = maxOf(bottomY - topY, barThickness)

                            val candleColor = if (isSelected) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.65f)

                            drawRoundRect(
                                color = candleColor,
                                topLeft = Offset(cx - barThickness / 2f, topY),
                                size = Size(barThickness, barHeight),
                                cornerRadius = CornerRadius(barThickness / 2f, barThickness / 2f)
                            )

                            // Avg dot in the center
                            val avgY = getY(bucket.avgBpm)
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = Offset(cx, avgY)
                                )
                                drawCircle(
                                    color = Color(0xFFEF4444),
                                    radius = 1.8.dp.toPx(),
                                    center = Offset(cx, avgY)
                                )
                            } else {
                                drawCircle(
                                    color = if (isDark) Color(0xFFFFD4D4) else Color(0xFFFFFFFF),
                                    radius = 1.4.dp.toPx(),
                                    center = Offset(cx, avgY)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer / Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(11.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("30m range", style = ZivaaTheme.typography.meta, color = colors.inkMute)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFFFFD4D4) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Avg BPM", style = ZivaaTheme.typography.meta, color = colors.inkMute)
                        }
                    }

                    if (selectedBucketIndex != null) {
                        Text(
                            text = "Reset",
                            style = ZivaaTheme.typography.meta.copy(
                                color = colors.sage,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier
                                .clickable { selectedBucketIndex = null }
                                .padding(4.dp)
                        )
                    } else {
                        Text(
                            text = "Tap & drag to inspect",
                            style = ZivaaTheme.typography.meta,
                            color = colors.inkMute
                        )
                    }
                }
            }
        }
    }
}
