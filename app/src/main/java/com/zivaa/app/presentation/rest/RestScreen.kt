package com.zivaa.app.presentation.rest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em

import androidx.compose.foundation.clickable
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent

import androidx.lifecycle.viewmodel.compose.viewModel
import com.zivaa.app.ui.theme.ZivaaTheme

@OptIn(ExperimentalMaterial3Api::class)
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
                    containerColor = Color(0xFF13151A), // Dark BG
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF13151A) // Dark BG from screenshot
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Hero Card (Dark Gray Box)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF1B1D23))
                            .padding(24.dp)
                    ) {
                        Column {
                            // "Mobility score [i]   Score / 100" header -> "Rest Score [i]   Score / 100"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Rest score",
                                        style = ZivaaTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                                        color = Color.White
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
                                    color = Color(0xFFA0A6B2)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Dialer
                            RestArcDialer(score = score, tierTitle = text, tierColor = color)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Rest score factors",
                                style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2x2 Grid of factors
                            val b = viewModel.restBreakdown
                            
                            // Values
                            val durPts = (b?.get("duration_pts") as? Number)?.toInt() ?: 0
                            val durVal = (b?.get("sleep_hours") as? Number)?.toDouble()?.let { String.format("%.1fh", it) } ?: "--h"
                            
                            val qualPts = (b?.get("quality_pts") as? Number)?.toInt() ?: 0
                            val effVal = (b?.get("sleep_efficiency_pct") as? Number)?.toInt()?.let { "${it}%" } ?: "--%"
                            
                            val deepValNum = (b?.get("sleep_stage_5_hours") as? Number)?.toDouble()
                            val deepVal = deepValNum?.let { String.format("%.1fh", it) } ?: "No data"
                            
                            val remValNum = (b?.get("sleep_stage_6_hours") as? Number)?.toDouble()
                            val remVal = remValNum?.let { String.format("%.1fh", it) } ?: "No data"
                            
                            val rhrPts = (b?.get("vitals_pts") as? Number)?.toInt() ?: 0
                            val rhrVal = (b?.get("resting_heart_rate") as? Number)?.toInt()?.let { "$it bpm" } ?: "-- bpm"
                            
                            val tempPts = (b?.get("penalty_pts") as? Number)?.toInt() ?: 0
                            val tempVal = (b?.get("skin_temp_delta") as? Number)?.toDouble()?.let { String.format("%+.1f°C", it) } ?: "--"

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Sleep time",
                                    scoreText = "$durPts/40",
                                    primaryValue = durVal,
                                    unitText = "",
                                    progress = (durPts / 40f).coerceIn(0f, 1f),
                                    statusText = if (durPts >= 35) "Optimal" else "Attention",
                                    accentColor = if (durPts >= 35) Color(0xFF4EAE7B) else Color(0xFFE5A643)
                                )
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Efficiency",
                                    scoreText = "$qualPts/30",
                                    primaryValue = effVal,
                                    unitText = "",
                                    progress = (qualPts / 30f).coerceIn(0f, 1f),
                                    statusText = if (qualPts >= 20) "Steady" else "Attention",
                                    accentColor = if (qualPts >= 20) Color(0xFFE5A643) else Color(0xFFE58B43)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
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
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RestFactorTile(
                                    modifier = Modifier.weight(1f),
                                    title = "Resting HR",
                                    scoreText = "$rhrPts/30",
                                    primaryValue = rhrVal,
                                    unitText = "",
                                    progress = (rhrPts / 30f).coerceIn(0f, 1f),
                                    statusText = if (rhrPts == 30) "Optimal" else "Attention",
                                    accentColor = if (rhrPts == 30) Color(0xFF4EAE7B) else Color(0xFFE58B43)
                                )
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
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val rrValNum = (b?.get("respiratory_rate") as? Number)?.toDouble()
                                val rrVal = rrValNum?.let { String.format("%.1f brpm", it) } ?: "No data"
                                // If penalties is 30 (both triggered) or 15 but temp was 0, it means respiratory triggered it.
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
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Charts section
                            RestTimeRangeFilterBar(
                                selectedRange = viewModel.selectedTimeRange,
                                onRangeSelected = { viewModel.setTimeRange(it) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            RestChartTypeFilterBar(
                                selectedType = viewModel.selectedChartType,
                                onTypeSelected = { viewModel.setChartType(it) }
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            RestChartCard(
                                title = "Rest Score",
                                dates = viewModel.chartDates,
                                values = viewModel.chartRestScores,
                                chartType = viewModel.selectedChartType,
                                color = Color(0xFF4EAE7B)
                            )
                            
                            RestChartCard(
                                title = "Total Sleep (hours)",
                                dates = viewModel.chartDates,
                                values = viewModel.chartTotalSleep,
                                chartType = viewModel.selectedChartType,
                                color = Color(0xFF3B82F6)
                            )
                            
                            RestSleepStagesChartCard(
                                title = "Sleep Stages",
                                dates = viewModel.chartDates,
                                deep = viewModel.chartSleepDeep,
                                rem = viewModel.chartSleepRem,
                                light = viewModel.chartSleepLight
                            )
                            
                            RestChartCard(
                                title = "Resting Heart Rate (bpm)",
                                dates = viewModel.chartDates,
                                values = viewModel.chartRestingHr,
                                chartType = RestChartType.LINE, // Forced line
                                color = Color(0xFFEF4444)
                            )
                            
                            RestChartCard(
                                title = "Skin Temp Change (°C)",
                                dates = viewModel.chartDates,
                                values = viewModel.chartSkinTemp,
                                chartType = viewModel.selectedChartType,
                                color = Color(0xFFF59E0B)
                            )
                            
                            RestChartCard(
                                title = "Respiratory Rate (brpm)",
                                dates = viewModel.chartDates,
                                values = viewModel.chartRespRate,
                                chartType = viewModel.selectedChartType,
                                color = Color(0xFF8B5CF6)
                            )
                            
                            Spacer(modifier = Modifier.height(48.dp))
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
                color = Color(0xFF2E3238),
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
                drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 2.5.dp.toPx(), center = knobCenter)
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
                color = Color.White
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
    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1B1D23))
            .border(1.dp, Color(0xFF383C46).copy(alpha = 0.8f), RoundedCornerShape(18.dp))
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
                    color = Color(0xFFD4D8E2),
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = scoreText,
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFA0A6B2),
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = primaryValue,
                    style = ZivaaTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                    color = Color.White
                )
                if (unitText.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unitText,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                        color = Color(0xFFA0A6B2),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF282B33))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.06f, 1f))
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
    val isDark = ZivaaTheme.colors.isDark
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
    val isDark = ZivaaTheme.colors.isDark
    Row(
        modifier = modifier
            .width(200.dp)
            .padding(horizontal = 22.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(999.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RestChartType.entries.forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) ZivaaTheme.colors.sage else Color.Transparent)
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.label,
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
fun RestChartCard(
    title: String,
    dates: List<String>,
    values: List<Float>,
    chartType: RestChartType,
    color: Color
) {
    if (dates.isEmpty() || values.isEmpty()) return
    
    val isSevenDays = values.size <= 7
    val barThickness = when (values.size) {
        in 25..90 -> 4.5.dp
        in 10..24 -> 12.dp
        else -> 20.dp
    }
    
    val chartEntryModel = if (chartType == RestChartType.LINE) {
        val entries = values.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        entryModelOf(entries)
    } else {
        val series = values.mapIndexed { index, value ->
            List(values.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index) value else 0f) }
        }
        entryModelOf(*series.toTypedArray())
    }
    
    val axisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val i = value.toInt()
        if (i in dates.indices) dates[i] else ""
    }

    val isDark = ZivaaTheme.colors.isDark
    val pastBarColor = if (isDark) Color(0xFF4A4E58) else Color(0xFFB8B0A2)
    
    val columns = List(values.size) { index ->
        if (index == values.size - 1) {
            com.patrykandpatrick.vico.compose.component.lineComponent(color = color, thickness = barThickness, shape = Shapes.pillShape)
        } else {
            com.patrykandpatrick.vico.compose.component.lineComponent(color = pastBarColor, thickness = barThickness, shape = Shapes.pillShape)
        }
    }

    val chartMax = (values.maxOrNull() ?: 0f) * 1.15f
    val plotAreaHeightDp = 150.dp
    val rangeLabel = if (values.size <= 7) "7 Days" else if (values.size <= 30) "30 Days" else "3 Months"
    val heroValue = if (isSevenDays) values.last() else (values.sum() / values.size)
    val heroSubtitle = if (isSevenDays) "latest" else "average"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
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
                Text(
                    text = if (heroValue % 1 == 0f) heroValue.toInt().toString() else String.format("%.1f", heroValue),
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
                            columns = columns,
                            mergeMode = MergeMode.Stack,
                            axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = if (chartMax > 0f) chartMax else 10f)
                        )
                    } else {
                        lineChart(
                            lines = listOf(
                                com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                                    lineColor = color,
                                    lineBackgroundShader = null,
                                    lineThickness = 3.dp,
                                    point = shapeComponent(shape = Shapes.pillShape, color = color),
                                    pointSize = 6.dp
                                )
                            ),
                            axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = if (chartMax > 0f) chartMax else 10f)
                        )
                    },
                    model = chartEntryModel,
                    startAxis = null,
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = axisFormatter,
                        label = textComponent(color = ZivaaTheme.colors.inkMute, textSize = 11.sp),
                        axis = null,
                        tick = null,
                        guideline = null
                    )
                )
            }
        }
    }
}

@Composable
fun RestSleepStagesChartCard(
    title: String,
    dates: List<String>,
    deep: List<Float>,
    rem: List<Float>,
    light: List<Float>
) {
    if (dates.isEmpty() || deep.isEmpty()) return
    
    val deepEntries = deep.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
    val remEntries = rem.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
    val lightEntries = light.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
    
    val model = entryModelOf(deepEntries, remEntries, lightEntries)
    
    val axisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val i = value.toInt()
        if (i in dates.indices) dates[i] else ""
    }

    val isDark = ZivaaTheme.colors.isDark
    
    val barThickness = when (deep.size) {
        in 25..90 -> 4.5.dp
        in 10..24 -> 12.dp
        else -> 20.dp
    }
    
    val rangeLabel = if (deep.size <= 7) "7 Days" else if (deep.size <= 30) "30 Days" else "3 Months"
    val plotAreaHeightDp = 150.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
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
                    Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF4EAE7B)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deep", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF3B82F6)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REM", color = ZivaaTheme.colors.inkMute, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF9CA3AF)))
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
                    chart = columnChart(
                        columns = listOf(
                            com.patrykandpatrick.vico.compose.component.lineComponent(color = Color(0xFF4EAE7B), thickness = barThickness),
                            com.patrykandpatrick.vico.compose.component.lineComponent(color = Color(0xFF3B82F6), thickness = barThickness),
                            com.patrykandpatrick.vico.compose.component.lineComponent(color = Color(0xFF9CA3AF), thickness = barThickness, shape = Shapes.roundedCornerShape(topLeftPercent = 50, topRightPercent = 50))
                        ),
                        mergeMode = MergeMode.Stack
                    ),
                    model = model,
                    startAxis = null,
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = axisFormatter,
                        label = textComponent(color = ZivaaTheme.colors.inkMute, textSize = 11.sp),
                        axis = null,
                        tick = null,
                        guideline = null
                    )
                )
            }
        }
    }
}
