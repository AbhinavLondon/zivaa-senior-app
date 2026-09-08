package com.zivaa.app.presentation.movement

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import com.zivaa.app.ui.theme.ZivaaTheme

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

@Composable
fun MovementScreen(
    viewModel: MovementViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchMovementData()
    }

    val scrollState = rememberScrollState()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp))
                
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

                Spacer(modifier = Modifier.height(24.dp))

                // Hero Card
                MovementHeroCard(viewModel = viewModel, onAddGoalClick = { showStepsGoalPopup = true })

                // Hourly Chart
                Text(
                    text = "Your Steps, Hour By Hour",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.eyebrow,
                    modifier = Modifier.padding(top = 32.dp, start = 22.dp, end = 22.dp, bottom = 12.dp)
                )
                HourlyStepsChartCard(viewModel = viewModel)

                // Weekly Chart
                Text(
                    text = "Your Last Seven Days",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.eyebrow,
                    modifier = Modifier.padding(top = 32.dp, start = 22.dp, end = 22.dp, bottom = 12.dp)
                )
                WeeklyStepsChartCard(viewModel = viewModel)

                // Insights & Upcoming Plan (Removed)

                // Footer Text
                Text(
                    text = "Every step here counts itself — you just keep walking. We'll tally again tomorrow.",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = (12.5 * 1.55).sp),
                    color = ZivaaTheme.colors.inkMute,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 40.dp)
                )
                
                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp))
            }
    }
}

@Composable
fun MovementHeroCard(viewModel: MovementViewModel, onAddGoalClick: () -> Unit) {
    val colors = ZivaaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.15f),
                spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.sage)
            .padding(24.dp)
    ) {
        Column {
            // Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ZivaaTheme.colors.sageInk.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(ZivaaTheme.colors.sageInk.copy(alpha = 0.3f))
                    )
                    Text(
                        text = if (viewModel.isGoalMet) "Past Your Goal" else "Making Progress",
                        style = ZivaaTheme.typography.eyebrow,
                        color = ZivaaTheme.colors.sageInk
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            androidx.compose.animation.Crossfade(targetState = viewModel.isHeroInsightLoading, label = "heroInsightCrossfade") { isLoading ->
                if (isLoading) {
                    Text(
                        text = "Generating encouragement...",
                        style = ZivaaTheme.typography.titleLarge.copy(fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.01).em),
                        color = ZivaaTheme.colors.sageInk.copy(alpha = 0.5f)
                    )
                } else {
                    Text(
                        text = viewModel.heroInsightText.ifEmpty { viewModel.overGoalText },
                        style = ZivaaTheme.typography.titleLarge.copy(fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.01).em),
                        color = ZivaaTheme.colors.sageInk
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = viewModel.totalStepsToday,
                    style = ZivaaTheme.typography.displayLarge.copy(fontSize = 62.sp, letterSpacing = (-1.5).sp),
                    color = ZivaaTheme.colors.sageInk
                )
                if (viewModel.goalSteps != null) {
                    Text(
                        text = "Steps · Goal ${java.text.NumberFormat.getNumberInstance().format(viewModel.goalSteps)}",
                        style = ZivaaTheme.typography.eyebrow,
                        color = ZivaaTheme.colors.sageInk,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
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
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Exciting Animated Progress Bar
            val safeGoal = viewModel.goalSteps?.toFloat() ?: 10000f // fallback to avoid div by zero if animation runs
            val progress = if (viewModel.goalSteps == null) 0f else (viewModel.totalStepsToday.replace(",", "").toIntOrNull() ?: 0) / safeGoal
            val safeProgress = progress.coerceIn(0f, 1f)
            
            // 1. Smooth fill animation on load
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

            // 2. Infinite elegant shimmer effect
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
                    ZivaaTheme.colors.sageInk, // Bright center
                    ZivaaTheme.colors.sageInk.copy(alpha = 0.3f)
                ),
                start = Offset(shimmerX, 0f),
                end = Offset(shimmerX + 400f, 0f)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp) // Thicker for a more premium feel
                    .clip(RoundedCornerShape(999.dp))
                    .background(ZivaaTheme.colors.ink.copy(alpha = 0.15f)) // Darker inset track
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
            val hourlyData = if (viewModel.hourlySteps.isEmpty()) {
                val hours = listOf("6A", "", "", "9A", "", "", "12P", "", "", "3P", "", "", "6P", "", "", "9P")
                hours.map { it to 0 }
            } else {
                viewModel.hourlySteps
            }
            
            val maxSteps = hourlyData.maxOfOrNull { it.second } ?: 0
            val seriesList = hourlyData.mapIndexed { index, pair ->
                List(hourlyData.size) { i -> FloatEntry(x = i.toFloat(), y = if (i == index) pair.second.toFloat() else 0f) }
            }
            val chartEntryModel = entryModelOf(*seriesList.toTypedArray())

            val bottomAxisValueFormatter = AxisValueFormatter<com.patrykandpatrick.vico.core.axis.AxisPosition.Horizontal.Bottom> { value, _ ->
                hourlyData.getOrNull(value.toInt())?.first ?: ""
            }

            val columns = List(hourlyData.size) { 
                lineComponent(color = ZivaaTheme.colors.sage, thickness = 12.dp, shape = Shapes.pillShape)
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
                    spacing = 8.dp,
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
fun rememberMarker(): Marker {
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
                val total = markedEntries.sumOf { it.entry.y.toDouble() }.toInt()
                return java.text.NumberFormat.getNumberInstance().format(total)
            }
        }
    }
}

@Composable
fun WeeklyStepsChartCard(viewModel: MovementViewModel) {
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
            val stepsData = if (viewModel.weeklySteps.isEmpty()) List(7) { "-" to 0 } else viewModel.weeklySteps
            val series = stepsData.mapIndexed { index, pair ->
                List(7) { i -> FloatEntry(x = i.toFloat(), y = if (i == index) pair.second.toFloat() else 0f) }
            }
            val chartEntryModel = entryModelOf(*series.toTypedArray())

            val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                stepsData.getOrNull(value.toInt())?.first ?: ""
            }

            // Chart with right-side labels aligned to threshold lines
            val maxDataValue = stepsData.maxOf { it.second }
            val currentGoal = viewModel.goalSteps
            val validGoal = currentGoal != null && currentGoal > 0
            val chartMax = (if (validGoal) maxOf(maxDataValue, currentGoal!!) else maxOf(maxDataValue, 1)).toFloat() * 1.05f
            val chartHeightDp = 180.dp
            val bottomAxisHeightDp = 22.dp // approximate space for bottom axis labels
            val plotAreaHeightDp = chartHeightDp - bottomAxisHeightDp

            val goalLine = ThresholdLine(
                thresholdValue = (currentGoal ?: 0).toFloat(),
                lineComponent = lineComponent(
                    color = ZivaaTheme.colors.sage.copy(alpha = 0.4f), 
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

            val columns = List(7) { index ->
                if (index == 6) {
                    lineComponent(color = ZivaaTheme.colors.sage, thickness = 20.dp, shape = Shapes.pillShape)
                } else {
                    lineComponent(color = ZivaaTheme.colors.lineStrong, thickness = 20.dp, shape = Shapes.pillShape)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Chart(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeightDp),
                    chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                    chart = columnChart(
                        columns = columns,
                        mergeMode = MergeMode.Stack,
                        decorations = if (validGoal) listOf(goalLine, avgLine) else listOf(avgLine),
                        axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = 0f, maxY = chartMax),
                        spacing = 12.dp
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

                // Right-side labels pinned to threshold line Y positions
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .width(50.dp)
                        .height(chartHeightDp)
                ) {
                    // GOAL label — positioned at its threshold Y
                    if (validGoal && currentGoal != null) {
                        val goalFraction = if (chartMax > 0f) 1f - (currentGoal.toFloat() / chartMax) else 0.5f
                        val goalOffsetY = plotAreaHeightDp * goalFraction
                        Column(
                            modifier = Modifier.offset(y = goalOffsetY - 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Goal",
                                style = ZivaaTheme.typography.eyebrow,
                                color = ZivaaTheme.colors.eyebrow,
                                fontSize = 10.sp
                            )
                            Text(
                                text = java.text.NumberFormat.getNumberInstance().format(currentGoal),
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = ZivaaTheme.colors.sage
                            )
                        }
                    }

                    // AVG label — positioned at its threshold Y
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
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = ZivaaTheme.colors.inkMute
                        )
                    }
                }
            }
            
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
                    text = viewModel.insightText.ifEmpty { "Averaging ${java.text.NumberFormat.getNumberInstance().format(viewModel.averageSteps)} a day — active all week, even through Saturday's rain." },
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = ZivaaTheme.colors.ink
                )
            }
        }
    }
}


