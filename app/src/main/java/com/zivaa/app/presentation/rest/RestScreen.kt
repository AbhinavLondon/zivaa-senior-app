package com.zivaa.app.presentation.rest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em

import androidx.compose.foundation.clickable
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import java.util.Locale

import androidx.lifecycle.viewmodel.compose.viewModel
import com.zivaa.app.ui.theme.ZivaaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RestScreen(
    onNavigateBack: () -> Unit,
    onInfoClick: () -> Unit,
    viewModel: RestViewModel = viewModel(factory = RestViewModelFactory())
) {
    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rest & Recovery", style = ZivaaTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZivaaTheme.colors.bg, // Dark BG
                    titleContentColor = ZivaaTheme.colors.ink,
                    navigationIconContentColor = ZivaaTheme.colors.ink
                )
            )
        },
        containerColor = ZivaaTheme.colors.bg // Dark BG from screenshot
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ZivaaTheme.colors.sage)
            }
        } else {
            val score = viewModel.restScore ?: 0
            val color = when {
                score == 0 -> Color(0xFF6B7280) // Gray for no data
                score >= 85 -> Color(0xFF4EAE7B) // Optimal Green
                score >= 60 -> Color(0xFFE5A643) // Steady Yellow
                else -> Color(0xFFE58B43)        // Attention Orange
            }
            val text = when {
                score == 0 -> "No Data Yet"
                score >= 85 -> "Excellent Rest"
                score >= 60 -> "Fair Rest"
                else -> "Needs Attention"
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
                                ambientColor = if (ZivaaTheme.colors.isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color(0x08000000),
                                spotColor = if (ZivaaTheme.colors.isDark) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color(0x10000000)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(ZivaaTheme.colors.surfaceCard)
                            .padding(24.dp)
                    ) {
                        Column {
                            // "Rest Score [i]   Score / 100" header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                      Text(
                                          text = "Rest score",
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
                                            contentDescription = "Rest Score Info",
                                            tint = ZivaaTheme.colors.sage,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Score / 100",
                                    style = ZivaaTheme.typography.eyebrow.copy(fontSize = 11.sp),
                                    color = ZivaaTheme.colors.textBody
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Dialer
                            RestArcDialer(score = score, tierTitle = text, tierColor = color)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Rest score factors",
                                style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                color = ZivaaTheme.colors.ink
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2x2 Grid of factors
                            // 2x2 Grid of factors (4 rows of 2 tiles)
                            val b = viewModel.restBreakdown
                            
                            // Values
                            val durPts = (b?.get("duration_pts") as? Number)?.toInt() ?: 0
                            val durValNum = (b?.get("sleep_hours") as? Number)?.toDouble()
                            val durVal = durValNum?.let { String.format(Locale.US, "%.1fh", it) } ?: "No data"
                            
                            // WASO replaces Efficiency
                            val wasoPts = (b?.get("waso_pts") as? Number)?.toInt()
                            val wasoValNum = (b?.get("waso_mins") as? Number)?.toDouble()
                            val wasoVal = wasoValNum?.let { "${it.toInt()} min" } ?: "No data"
                            
                            val deepValNum = (b?.get("sleep_stage_5_hours") as? Number)?.toDouble()
                            val deepVal = deepValNum?.let { String.format(Locale.US, "%.1fh", it) } ?: "No data"
                            
                            val remValNum = (b?.get("sleep_stage_6_hours") as? Number)?.toDouble()
                            val remVal = remValNum?.let { String.format(Locale.US, "%.1fh", it) } ?: "No data"
                            
                            // Resting HR & HRV
                            val rhrValNum = (b?.get("resting_heart_rate") as? Number)?.toInt()
                            val rhrVal = rhrValNum?.let { "$it bpm" } ?: "No data"
                            val hrvScorePts = (b?.get("hrv_pts") as? Number)?.toInt()
                            val rhrMax = if (hrvScorePts != null) 15 else 30
                            val rhrScorePts = (b?.get("rhr_pts") as? Number)?.toInt() ?: (b?.get("vitals_pts") as? Number)?.toInt() ?: 0

                            val hrvValNum = (b?.get("hrv_rmssd") as? Number)?.toDouble()
                            val hrvBaseNum = (b?.get("hrv_rmssd_baseline") as? Number)?.toDouble()
                            val hasHrv = hrvValNum != null && hrvValNum > 0
                            val hrvPrimaryVal = if (hasHrv) "${hrvValNum!!.toInt()} ms" else "No Data"
                            val hrvScoreText = if (hasHrv && hrvScorePts != null) "$hrvScorePts/15" else ""
                            val hrvProgress = if (hasHrv) {
                                if (hrvBaseNum != null && hrvBaseNum > 0) (hrvValNum!! / hrvBaseNum).toFloat().coerceIn(0f, 1f)
                                else (hrvValNum!!.toFloat() / 50f).coerceIn(0f, 1f)
                            } else 0f
                            val hrvStatusText = if (!hasHrv) "No Data" else if (hrvScorePts != null && hrvScorePts >= 12) "Optimal" else if (hrvScorePts != null && hrvScorePts >= 7) "Steady" else "Attention"
                            val hrvAccentColor = if (!hasHrv) Color(0xFF6B7280) else if (hrvScorePts != null && hrvScorePts >= 12) Color(0xFF4EAE7B) else if (hrvScorePts != null && hrvScorePts >= 7) Color(0xFFE5A643) else Color(0xFFE58B43)
                            
                            val tempPts = (b?.get("penalty_pts") as? Number)?.toInt() ?: 0
                            val tempVal = (b?.get("skin_temp_delta") as? Number)?.toDouble()?.let { String.format(Locale.US, "%+.1f°C", it) } ?: "--"

                            // Row 1: Sleep time & WASO
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Sleep time",
                                    scoreText = if (durValNum == null) "" else "$durPts/35",
                                    primaryValue = durVal,
                                    unitText = "",
                                    progress = if (durValNum == null) 0f else (durPts / 35f).coerceIn(0f, 1f),
                                    statusText = if (durValNum == null) "No data" else if (durPts >= 30) "Optimal" else if (durPts >= 20) "Steady" else "Attention",
                                    accentColor = if (durValNum == null) Color(0xFF6B7280) else if (durPts >= 30) Color(0xFF4EAE7B) else if (durPts >= 20) Color(0xFFE5A643) else Color(0xFFE58B43)
                                )
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "WASO",
                                    scoreText = if (wasoValNum == null) "" else "${wasoPts ?: 0}/15",
                                    primaryValue = wasoVal,
                                    unitText = "",
                                    progress = if (wasoValNum == null) 0f else if (wasoPts != null) (wasoPts / 15f).coerceIn(0f, 1f) else if (wasoValNum <= 30) 1f else (1f - ((wasoValNum.toFloat() - 30f) / 45f)).coerceIn(0f, 1f),
                                    statusText = if (wasoValNum == null) "No data" else if (wasoValNum <= 30) "Optimal" else if (wasoValNum <= 60) "Steady" else "Attention",
                                    accentColor = if (wasoValNum == null) Color(0xFF6B7280) else if (wasoValNum <= 30) Color(0xFF4EAE7B) else if (wasoValNum <= 60) Color(0xFFE5A643) else Color(0xFFE58B43)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 2: Deep sleep & REM sleep
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Deep sleep",
                                    scoreText = if (deepValNum == null) "" else "Max 10",
                                    primaryValue = deepVal,
                                    unitText = "",
                                    progress = ((deepValNum?.toFloat() ?: 0f) / 1.5f).coerceIn(0f, 1f),
                                    statusText = if (deepValNum == null) "No data" else if (deepValNum >= 1.5) "Optimal" else if (deepValNum >= 1.0) "Steady" else "Attention",
                                    accentColor = if (deepValNum == null) Color(0xFF6B7280) else if (deepValNum >= 1.5) Color(0xFF4EAE7B) else if (deepValNum >= 1.0) Color(0xFFE5A643) else Color(0xFFE58B43)
                                )
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "REM sleep",
                                    scoreText = if (remValNum == null) "" else "Max 10",
                                    primaryValue = remVal,
                                    unitText = "",
                                    progress = ((remValNum?.toFloat() ?: 0f) / 1.5f).coerceIn(0f, 1f),
                                    statusText = if (remValNum == null) "No data" else if (remValNum >= 1.5) "Optimal" else if (remValNum >= 1.0) "Steady" else "Attention",
                                    accentColor = if (remValNum == null) Color(0xFF6B7280) else if (remValNum >= 1.5) Color(0xFF4EAE7B) else if (remValNum >= 1.0) Color(0xFFE5A643) else Color(0xFFE58B43)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 3: Resting HR & HRV (next to Resting HR)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Resting HR",
                                    scoreText = if (rhrValNum == null) "" else "$rhrScorePts/$rhrMax",
                                    primaryValue = rhrVal,
                                    unitText = "",
                                    progress = if (rhrValNum == null) 0f else (rhrScorePts.toFloat() / rhrMax.toFloat()).coerceIn(0f, 1f),
                                    statusText = if (rhrValNum == null) "No data" else if (rhrScorePts >= (rhrMax * 0.85f)) "Optimal" else if (rhrScorePts >= (rhrMax * 0.5f)) "Steady" else "Attention",
                                    accentColor = if (rhrValNum == null) Color(0xFF6B7280) else if (rhrScorePts >= (rhrMax * 0.85f)) Color(0xFF4EAE7B) else if (rhrScorePts >= (rhrMax * 0.5f)) Color(0xFFE5A643) else Color(0xFFE58B43)
                                )
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "HRV",
                                    scoreText = hrvScoreText,
                                    primaryValue = hrvPrimaryVal,
                                    unitText = "",
                                    progress = hrvProgress,
                                    statusText = hrvStatusText,
                                    accentColor = hrvAccentColor
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 4: Skin temp & Resp. rate
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Skin temp",
                                    scoreText = if (b?.get("skin_temp_delta") == null) "" else if (tempPts > 0) "-15 pts" else "0 pts",
                                    primaryValue = if (b?.get("skin_temp_delta") == null) "No data" else tempVal,
                                    unitText = "",
                                    progress = if (b?.get("skin_temp_delta") == null) 0f else if (tempPts > 0) 0.1f else 1f,
                                    statusText = if (b?.get("skin_temp_delta") == null) "No data" else if (tempPts > 0) "Elevated" else "Normal",
                                    accentColor = if (b?.get("skin_temp_delta") == null) Color(0xFF6B7280) else if (tempPts > 0) Color(0xFFE58B43) else Color(0xFF4EAE7B)
                                )
                                val rrValNum = (b?.get("respiratory_rate") as? Number)?.toDouble()
                                val rrVal = rrValNum?.let { String.format(Locale.US, "%.1f brpm", it) } ?: "No data"
                                val totalPenalties = (b?.get("penalty_pts") as? Number)?.toInt() ?: 0
                                val rrPenalty = if (totalPenalties == 30 || (totalPenalties == 15 && tempPts == 0)) 15 else 0
                                
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Resp. rate",
                                    scoreText = if (rrValNum == null) "" else if (rrPenalty > 0) "-15 pts" else "0 pts",
                                    primaryValue = rrVal,
                                    unitText = "",
                                    progress = if (rrValNum == null) 0f else if (rrPenalty > 0) 0.1f else 1f,
                                    statusText = if (rrValNum == null) "No data" else if (rrPenalty > 0) "Elevated" else "Normal",
                                    accentColor = if (rrValNum == null) Color(0xFF6B7280) else if (rrPenalty > 0) Color(0xFFE58B43) else Color(0xFF4EAE7B)
                                )
                            }
                        }
                    }
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
                            RestTimeRangeFilterBar(
                                selectedRange = viewModel.selectedTimeRange,
                                onRangeSelected = { viewModel.setTimeRange(it) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            RestChartTypeFilterBar(
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
                        RestChartCard(
                            title = "Rest Score",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartRestScores,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFF4EAE7B),
                            showAverageLine = true,
                            averageValue = viewModel.averageRestScore,
                            unitSuffix = "",
                            isDecimal = false
                        )
                        
                        RestChartCard(
                            title = "Total Sleep (hours)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartTotalSleep,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFF3B82F6),
                            showAverageLine = true,
                            averageValue = viewModel.averageTotalSleep,
                            unitSuffix = "h",
                            isDecimal = true
                        )
                        
                        RestSleepStagesChartCard(
                            title = "Sleep Stages",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            deep = viewModel.chartSleepDeep,
                            rem = viewModel.chartSleepRem,
                            light = viewModel.chartSleepLight
                        )
                        
                        RestChartCard(
                            title = "Resting Heart Rate (bpm)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartRestingHr,
                            chartType = RestChartType.LINE, // Forced line
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFFEF4444),
                            showAverageLine = true,
                            averageValue = viewModel.averageRestingHr,
                            unitSuffix = " bpm",
                            isDecimal = false
                        )
                        
                        RestChartCard(
                            title = "Heart Rate Variability (ms)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartHrv,
                            chartType = RestChartType.LINE, // Forced line, identical look & feel to RHR
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFF06B6D4), // Cyan/Teal
                            showAverageLine = true,
                            averageValue = viewModel.averageHrv,
                            isNoData = !viewModel.hasHrvData,
                            unitSuffix = " ms",
                            isDecimal = false
                        )
                        
                        RestChartCard(
                            title = "Skin Temp Change (°C)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartSkinTemp,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFFF59E0B),
                            showAverageLine = false,
                            isNoData = !viewModel.hasSkinTempData,
                            unitSuffix = "°C",
                            isDecimal = true
                        )
                        
                        RestChartCard(
                            title = "Respiratory Rate (brpm)",
                            dates = viewModel.chartDates,
                            detailedDates = viewModel.chartDetailedDates,
                            values = viewModel.chartRespRate,
                            chartType = viewModel.selectedChartType,
                            timeRange = viewModel.selectedTimeRange,
                            startDateLabel = viewModel.startDateLabel,
                            color = Color(0xFF8B5CF6),
                            showAverageLine = false,
                            isNoData = !viewModel.hasRespRateData,
                            unitSuffix = " brpm",
                            isDecimal = true
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RestArcDialer(
    score: Int,
    tierTitle: String,
    tierColor: Color
) {
    val isDark = ZivaaTheme.colors.isDark
    val lineStrongColor = ZivaaTheme.colors.lineStrong
    val inkColor = ZivaaTheme.colors.ink

    val animatedScoreFraction by androidx.compose.animation.core.animateFloatAsState(
        targetValue = (score.toFloat() / 100f).coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)),
        label = "dialerProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(170.dp)) {
            val strokeWidthPx = 13.dp.toPx()
            val knobRadiusPx = 8.dp.toPx()
            val arcPadding = strokeWidthPx / 2f + knobRadiusPx
            val arcDiameter = size.minDimension - 2 * arcPadding
            val arcTopLeft = androidx.compose.ui.geometry.Offset(
                (size.width - arcDiameter) / 2f,
                (size.height - arcDiameter) / 2f
            )
            val arcSize = androidx.compose.ui.geometry.Size(arcDiameter, arcDiameter)
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
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            if (animatedScoreFraction > 0f) {
                drawArc(
                    color = tierColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal * animatedScoreFraction,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                val currentAngleRad = Math.toRadians((startAngle + sweepTotal * animatedScoreFraction).toDouble())
                val knobX = cx + (radius * Math.cos(currentAngleRad)).toFloat()
                val knobY = cy + (radius * Math.sin(currentAngleRad)).toFloat()
                val knobCenter = androidx.compose.ui.geometry.Offset(knobX, knobY)

                drawCircle(color = tierColor.copy(alpha = 0.35f), radius = knobRadiusPx + 3.dp.toPx(), center = knobCenter)
                drawCircle(color = tierColor, radius = knobRadiusPx, center = knobCenter)
                drawCircle(color = inkColor.copy(alpha = 0.85f), radius = 2.5.dp.toPx(), center = knobCenter)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "$score",
                style = ZivaaTheme.typography.displayLarge.copy(fontSize = 46.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp),
                color = inkColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tierTitle,
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                color = tierColor
            )
        }
    }
}

@Composable
fun RestFactorTile(
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
fun RestTimeRangeFilterBar(
    selectedRange: RestTimeRange,
    onRangeSelected: (RestTimeRange) -> Unit,
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
        RestTimeRange.entries.forEach { range ->
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
fun RestChartTypeFilterBar(
    selectedType: RestChartType,
    onTypeSelected: (RestChartType) -> Unit,
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
        RestChartType.entries.forEach { type ->
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
fun RestChartAxisRow(
    timeRange: RestTimeRange,
    startLabel: String,
    labels: List<String> = emptyList(),
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
            RestTimeRange.SEVEN_DAYS -> {
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
                            color = if (isToday) ZivaaTheme.colors.sage else ZivaaTheme.colors.textBody,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            RestTimeRange.THIRTY_DAYS -> {
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
            RestTimeRange.THREE_MONTHS -> {
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
fun rememberRestMarker(
    dates: List<String>,
    unitSuffix: String = "",
    isDecimal: Boolean = false,
    title: String = "",
    chartType: RestChartType = RestChartType.BAR
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
                val total = if (title.contains("Sleep Stages", ignoreCase = true) && chartType == RestChartType.LINE) {
                    markedEntries.maxOfOrNull { it.entry.y.toDouble() } ?: 0.0
                } else {
                    markedEntries.sumOf { it.entry.y.toDouble() }
                }

                if (total <= 0.0 && !title.contains("Skin Temp", ignoreCase = true)) {
                    return if (dateLabel.isNotBlank()) "No data · $dateLabel" else "No data"
                }

                val formattedVal = when {
                    title.contains("Skin Temp", ignoreCase = true) -> String.format(Locale.US, "%+.1f%s", total, unitSuffix)
                    isDecimal -> String.format(Locale.US, "%.1f%s", total, unitSuffix)
                    else -> "${total.toInt()}$unitSuffix"
                }

                return if (dateLabel.isNotBlank()) "$formattedVal · $dateLabel" else formattedVal
            }
        }
    }
}

@Composable
fun RestChartCard(
    title: String,
    dates: List<String>,
    detailedDates: List<String> = emptyList(),
    values: List<Float>,
    chartType: RestChartType,
    timeRange: RestTimeRange = RestTimeRange.SEVEN_DAYS,
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
    val isSevenDays = timeRange == RestTimeRange.SEVEN_DAYS
    val noDataState = isNoData || (values.all { it <= 0f } && (title.contains("Skin Temp", ignoreCase = true) || title.contains("Respiratory", ignoreCase = true) || title.contains("HRV", ignoreCase = true) || title.contains("Variability", ignoreCase = true)))

    val barThickness = when (timeRange) {
        RestTimeRange.SEVEN_DAYS -> 20.dp
        RestTimeRange.THIRTY_DAYS -> 7.dp
        RestTimeRange.THREE_MONTHS -> 16.dp
    }
    val barSpacing = when (timeRange) {
        RestTimeRange.SEVEN_DAYS -> 14.dp
        RestTimeRange.THIRTY_DAYS -> 2.5.dp
        RestTimeRange.THREE_MONTHS -> 6.dp
    }

    val chartEntryModel = if (chartType == RestChartType.LINE) {
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
        if (seriesList.isEmpty()) seriesList.add(listOf(FloatEntry(0f, 0f))) // Safe fallback
        entryModelOf(*seriesList.toTypedArray())
    } else {
        val series = values.mapIndexed { index, value ->
            List(values.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index) value else 0f) }
        }
        entryModelOf(*series.toTypedArray())
    }

    val pastBarColor = if (isDark) Color(0xFF4A4E58) else Color(0xFFB8B0A2)
    val columns = List(values.size) { index ->
        if (index == values.size - 1) {
            lineComponent(color = color, thickness = barThickness, shape = Shapes.pillShape)
        } else {
            lineComponent(color = pastBarColor, thickness = barThickness, shape = Shapes.pillShape)
        }
    }

    val chartMax = (values.maxOrNull() ?: 0f) * 1.15f
    val plotAreaHeightDp = 150.dp
    val rangeLabel = when (timeRange) {
        RestTimeRange.SEVEN_DAYS -> "7 Days"
        RestTimeRange.THIRTY_DAYS -> "30 Days"
        RestTimeRange.THREE_MONTHS -> "3 Months"
    }

    val heroValue = if (isSevenDays) (values.lastOrNull() ?: 0f) else {
        val nonZeros = values.filter { it > 0f }
        if (nonZeros.isNotEmpty()) nonZeros.average().toFloat() else 0f
    }
    val heroSubtitle = if (isSevenDays) "latest" else "average"

    val avgLine = if (showAverageLine && averageValue > 0f) {
        val avgLabelText = when {
            title.contains("Rest Score", ignoreCase = true) -> "Avg ${averageValue.toInt()}"
            title.contains("Sleep", ignoreCase = true) -> "Avg ${String.format(Locale.US, "%.1fh", averageValue)}"
            title.contains("Heart Rate", ignoreCase = true) -> "Avg ${averageValue.toInt()} bpm"
            title.contains("HRV", ignoreCase = true) || title.contains("Variability", ignoreCase = true) -> "Avg ${averageValue.toInt()} ms"
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
                    val heroText = if (title.contains("Skin Temp", ignoreCase = true)) {
                        String.format(Locale.US, "%+.1f", heroValue)
                    } else if (heroValue % 1 == 0f) {
                        heroValue.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.1f", heroValue)
                    }
                    Text(
                        text = heroText,
                        style = ZivaaTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
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
                        chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                        chart = if (chartType == RestChartType.BAR) {
                            columnChart(
                                columns = columns,
                                mergeMode = MergeMode.Stack,
                                spacing = barSpacing,
                                decorations = decorations,
                                axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                                    minY = 0f,
                                    maxY = if (chartMax > 0f) chartMax else 10f
                                )
                            )
                        } else {
                            lineChart(
                                lines = List(chartEntryModel.entries.size) {
                                    com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                                        lineColor = color,
                                        lineBackgroundShader = null,
                                        lineThickness = 3.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = color),
                                        pointSize = 6.dp
                                    )
                                },
                                decorations = decorations,
                                axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                                    minY = 0f,
                                    maxY = if (chartMax > 0f) chartMax else 10f
                                )
                            )
                        },
                        model = chartEntryModel,
                        marker = rememberRestMarker(
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
                RestChartAxisRow(
                    timeRange = timeRange,
                    startLabel = startDateLabel,
                    labels = dates
                )
            }
        }
    }
}

@Composable
fun RestSleepStagesChartCard(
    title: String,
    dates: List<String>,
    detailedDates: List<String> = emptyList(),
    chartType: RestChartType = RestChartType.BAR,
    timeRange: RestTimeRange = RestTimeRange.SEVEN_DAYS,
    startDateLabel: String = "",
    deep: List<Float>,
    rem: List<Float>,
    light: List<Float>
) {
    if (dates.isEmpty() || deep.isEmpty()) return

    val isDark = ZivaaTheme.colors.isDark

    val barThickness = when (timeRange) {
        RestTimeRange.SEVEN_DAYS -> 20.dp
        RestTimeRange.THIRTY_DAYS -> 7.dp
        RestTimeRange.THREE_MONTHS -> 16.dp
    }
    val barSpacing = when (timeRange) {
        RestTimeRange.SEVEN_DAYS -> 14.dp
        RestTimeRange.THIRTY_DAYS -> 2.5.dp
        RestTimeRange.THREE_MONTHS -> 6.dp
    }

    val pointSize = when (timeRange) {
        RestTimeRange.SEVEN_DAYS -> 5.dp
        RestTimeRange.THIRTY_DAYS -> 3.dp
        RestTimeRange.THREE_MONTHS -> 4.dp
    }

    val maxTotal = (0 until deep.size).maxOfOrNull { i ->
        deep.getOrElse(i) { 0f } + rem.getOrElse(i) { 0f } + light.getOrElse(i) { 0f }
    } ?: 0f
    val chartMax = if (maxTotal > 0f) maxTotal * 1.15f else 10f

    val rangeLabel = when (timeRange) {
        RestTimeRange.SEVEN_DAYS -> "7 Days"
        RestTimeRange.THIRTY_DAYS -> "30 Days"
        RestTimeRange.THREE_MONTHS -> "3 Months"
    }
    val plotAreaHeightDp = 150.dp

    val lightLineColor = Color(0xFF9CA3AF)
    val remLineColor = Color(0xFF3B82F6)
    val deepLineColor = Color(0xFF4EAE7B)

    val totalSeriesList = mutableListOf<List<FloatEntry>>()
    val remSeriesList = mutableListOf<List<FloatEntry>>()
    val deepSeriesList = mutableListOf<List<FloatEntry>>()
    val chartModel = if (chartType == RestChartType.BAR) {
        val deepEntries = deep.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        val remEntries = rem.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        val lightEntries = light.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        entryModelOf(deepEntries, remEntries, lightEntries)
    } else {
        var curTotal = mutableListOf<FloatEntry>()
        var curRem = mutableListOf<FloatEntry>()
        var curDeep = mutableListOf<FloatEntry>()
        
        (0 until deep.size).forEach { index ->
            val d = deep.getOrElse(index) { 0f }
            val r = rem.getOrElse(index) { 0f }
            val l = light.getOrElse(index) { 0f }
            
            if (d >= -0.1f && r >= -0.1f && l >= -0.1f) {
                curTotal.add(FloatEntry(x = index.toFloat(), y = d + r + l))
            } else {
                if (curTotal.isNotEmpty()) { totalSeriesList.add(curTotal); curTotal = mutableListOf() }
            }
            if (d >= -0.1f && r >= -0.1f) {
                curRem.add(FloatEntry(x = index.toFloat(), y = d + r))
            } else {
                if (curRem.isNotEmpty()) { remSeriesList.add(curRem); curRem = mutableListOf() }
            }
            if (d >= -0.1f) {
                curDeep.add(FloatEntry(x = index.toFloat(), y = d))
            } else {
                if (curDeep.isNotEmpty()) { deepSeriesList.add(curDeep); curDeep = mutableListOf() }
            }
        }
        if (curTotal.isNotEmpty()) totalSeriesList.add(curTotal)
        if (curRem.isNotEmpty()) remSeriesList.add(curRem)
        if (curDeep.isNotEmpty()) deepSeriesList.add(curDeep)
        
        if (totalSeriesList.isEmpty()) totalSeriesList.add(listOf(FloatEntry(0f, 0f)))
        if (remSeriesList.isEmpty()) remSeriesList.add(listOf(FloatEntry(0f, 0f)))
        if (deepSeriesList.isEmpty()) deepSeriesList.add(listOf(FloatEntry(0f, 0f)))
        
        entryModelOf(*(totalSeriesList + remSeriesList + deepSeriesList).toTypedArray())
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
            Row(modifier = Modifier.padding(bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(deepLineColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deep", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(remLineColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REM", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(lightLineColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Light", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(plotAreaHeightDp)
            ) {
                Chart(
                    modifier = Modifier.fillMaxSize(),
                    chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                    chart = if (chartType == RestChartType.BAR) {
                        columnChart(
                            columns = listOf(
                                lineComponent(color = deepLineColor, thickness = barThickness),
                                lineComponent(color = remLineColor, thickness = barThickness),
                                lineComponent(
                                    color = lightLineColor,
                                    thickness = barThickness,
                                    shape = Shapes.roundedCornerShape(topLeftPercent = 50, topRightPercent = 50)
                                )
                            ),
                            mergeMode = MergeMode.Stack,
                            spacing = barSpacing,
                            axisValuesOverrider = AxisValuesOverrider.fixed(
                                minY = 0f,
                                maxY = chartMax
                            )
                        )
                    } else {
                        lineChart(
                            lines = buildList {
                                repeat(totalSeriesList.size) {
                                    add(lineSpec(
                                        lineColor = lightLineColor,
                                        lineBackgroundShader = verticalGradient(
                                            colors = arrayOf(lightLineColor.copy(alpha = 0.3f), lightLineColor.copy(alpha = 0.05f))
                                        ),
                                        lineThickness = 2.5.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = lightLineColor),
                                        pointSize = pointSize
                                    ))
                                }
                                repeat(remSeriesList.size) {
                                    add(lineSpec(
                                        lineColor = remLineColor,
                                        lineBackgroundShader = verticalGradient(
                                            colors = arrayOf(remLineColor.copy(alpha = 0.6f), remLineColor.copy(alpha = 0.1f))
                                        ),
                                        lineThickness = 2.5.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = remLineColor),
                                        pointSize = pointSize
                                    ))
                                }
                                repeat(deepSeriesList.size) {
                                    add(lineSpec(
                                        lineColor = deepLineColor,
                                        lineBackgroundShader = verticalGradient(
                                            colors = arrayOf(deepLineColor.copy(alpha = 0.8f), deepLineColor.copy(alpha = 0.2f))
                                        ),
                                        lineThickness = 2.5.dp,
                                        point = shapeComponent(shape = Shapes.pillShape, color = deepLineColor),
                                        pointSize = pointSize
                                    ))
                                }
                            },
                            axisValuesOverrider = AxisValuesOverrider.fixed(
                                minY = 0f,
                                maxY = chartMax
                            )
                        )
                    },
                    model = chartModel,
                    marker = rememberRestMarker(
                        dates = detailedDates.ifEmpty { dates },
                        unitSuffix = "h",
                        isDecimal = true,
                        title = "Sleep Stages",
                        chartType = chartType
                    ),
                    startAxis = null,
                    bottomAxis = null
                )
            }

            RestChartAxisRow(
                timeRange = timeRange,
                startLabel = startDateLabel,
                labels = dates
            )
        }
    }
}
