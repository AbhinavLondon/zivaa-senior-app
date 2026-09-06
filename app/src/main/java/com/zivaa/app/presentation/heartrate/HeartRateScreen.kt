package com.zivaa.app.presentation.heartrate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.ZivaaTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.animation.core.animateFloat

@Composable
fun HeartRateScreen(
    viewModel: HeartRateViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ZivaaTheme.colors.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // We handle insets manually to allow edge-to-edge for hero
    ) { paddingValues ->
        LaunchedEffect(Unit) {
            viewModel.fetchTodayHeartRate()
            viewModel.fetchWeeklyHeartRate()
        }

        val hasData by viewModel.hasData.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val heartRateZones by viewModel.heartRateZones.collectAsState()
        val weeklyData by viewModel.weeklyData.collectAsState()
        val monthAvg by viewModel.monthAvg.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Usually 0 since we set contentWindowInsets to 0
            contentPadding = WindowInsets.systemBars.asPaddingValues() // Add system bars padding at the top and bottom of content
        ) {
            item {
                HeroHeartRateCard(onNavigateBack = onNavigateBack)
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "YOUR DAY, BEAT BY BEAT",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.textMeta,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ZivaaTheme.colors.accent)
                    }
                } else if (!hasData) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data for today. Wear your watch/ring to measure your heart rate.",
                            style = ZivaaTheme.typography.bodyMedium,
                            color = ZivaaTheme.colors.inkMute,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    BeatByBeatChartSection(viewModel)
                }
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "WHERE IT SPENT THE DAY",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.textMeta,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                DistributionSection(zones = heartRateZones)
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "YOUR LAST SEVEN DAYS",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.textMeta,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                val weeklyInsight by viewModel.weeklyInsight.collectAsState()
                val isLoadingInsight by viewModel.isLoadingInsight.collectAsState()
                SevenDayChartSection(
                    weeklyData = weeklyData,
                    monthAvg = monthAvg,
                    weeklyInsight = weeklyInsight,
                    isLoadingInsight = isLoadingInsight
                )
            }
            item {
                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }
}

@Composable
fun HeroHeartRateCard(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, ZivaaTheme.colors.line, CircleShape)
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ZivaaTheme.colors.textStrong,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "HEART RATE  ·  TUE 16 JUN",
                style = ZivaaTheme.typography.meta,
                color = ZivaaTheme.colors.textMeta
            )
        }


    }
}

@Composable
fun BeatByBeatChartSection(viewModel: HeartRateViewModel) {
    val chartEntryModelProducer by viewModel.chartEntryModelProducer.collectAsState()
    val colors = ZivaaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Column {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()
                val density = LocalDensity.current
                val axisHeightPx = with(density) { 30.dp.toPx() } 
                val dataHeightPx = heightPx - axisHeightPx
                val xMin = 0f
                val xMax = 48f
                val yMin = 61f
                val yMax = 106f
                
                fun getX(x: Float) = (x - xMin) / (xMax - xMin) * widthPx
                fun getY(y: Float) = dataHeightPx - (y - yMin) / (yMax - yMin) * dataHeightPx

                // Background Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val restingTop = getY(80f)
                    val restingBottom = getY(60f)
                    drawRect(
                        color = Color(0xFFF4F5EE), 
                        topLeft = Offset(0f, restingTop),
                        size = Size(widthPx, restingBottom - restingTop)
                    )
                }

                // Vico Chart
                Chart(
                    chart = columnChart(
                        columns = listOf(
                            lineComponent(
                                color = Color.Transparent,
                                thickness = 6.dp,
                                shape = Shapes.rectShape
                            ),
                            lineComponent(
                                color = colors.leaf,
                                thickness = 6.dp,
                                shape = Shapes.roundedCornerShape(topLeftPercent = 50, topRightPercent = 50, bottomLeftPercent = 50, bottomRightPercent = 50)
                            )
                        ),
                        mergeMode = com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode.Stack,
                        axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                            minX = 0f,
                            maxX = 48f,
                            minY = 50f,
                            maxY = 200f
                        )
                    ),
                    chartModelProducer = chartEntryModelProducer!!,
                    chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                    startAxis = rememberStartAxis(
                        label = textComponent(
                            color = colors.inkMute,
                            textSize = 10.sp
                        ),
                        axis = null,
                        tick = null,
                        guideline = lineComponent(
                            color = colors.line,
                            thickness = 1.dp
                        ),
                        itemPlacer = com.patrykandpatrick.vico.core.axis.AxisItemPlacer.Vertical.default(maxItemCount = 4)
                    ),
                    bottomAxis = null,
                    marker = rememberMarker(),
                    modifier = Modifier.fillMaxWidth().height(with(density){ dataHeightPx.toDp() })
                )

                // Draw X axis labels
                val labels = listOf(0 to "12A", 12 to "6A", 24 to "12P", 36 to "6P", 48 to "12A")
                labels.forEach { (xVal, text) ->
                    Box(modifier = Modifier.offset(x = with(density) { getX(xVal.toFloat()).toDp() } - 10.dp, y = with(density) { dataHeightPx.toDp() } + 8.dp)) {
                        Text(text, style = ZivaaTheme.typography.meta, color = colors.inkMute.copy(alpha=0.6f))
                    }
                }
                
                // USUAL RESTING
                Box(modifier = Modifier.offset(x = 0.dp, y = with(density) { getY(60f).toDp() } - 6.dp)) {
                    Text("USUAL RESTING 60-80", style = ZivaaTheme.typography.meta, color = colors.sageInk.copy(alpha=0.6f))
                }
            }


        }
    }
}

@Composable
fun ChartMarker(
    xPx: Float, yPx: Float, 
    label: String, 
    labelOffsetY: Dp, 
    labelOffsetX: Dp,
    dotType: Int = 0 
) {
    val density = LocalDensity.current
    val xDp = with(density) { xPx.toDp() }
    val yDp = with(density) { yPx.toDp() }
    val colors = ZivaaTheme.colors

    Box(modifier = Modifier.offset(x = xDp + labelOffsetX, y = yDp + labelOffsetY)) {
        Text(label, style = ZivaaTheme.typography.meta.copy(fontWeight = FontWeight.Bold), color = colors.inkMute)
    }

    if (dotType == 0) {
        Box(modifier = Modifier.offset(x = xDp - 6.dp, y = yDp - 6.dp).size(12.dp)) {
            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(colors.sageInk).border(2.dp, Color.White, CircleShape))
        }
    } else {
        Box(modifier = Modifier.offset(x = xDp - 10.dp, y = yDp - 10.dp).size(20.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFE2EACF))) // Light green
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors.sageInk).border(2.dp, Color.White, CircleShape))
        }
    }
}


@Composable
fun DistributionSection(zones: List<ZoneDistribution>?) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        if (zones != null && zones.size == 3) {
            DistributionBar(label = "RESTING", range = zones[0].rangeText, percentage = zones[0].percentageText, fraction = zones[0].fraction, color = ZivaaTheme.colors.sage)
            Spacer(modifier = Modifier.height(16.dp))
            DistributionBar(label = "EASY", range = zones[1].rangeText, percentage = zones[1].percentageText, fraction = zones[1].fraction, color = ZivaaTheme.colors.leaf)
            Spacer(modifier = Modifier.height(16.dp))
            DistributionBar(label = "ACTIVE", range = zones[2].rangeText, percentage = zones[2].percentageText, fraction = zones[2].fraction, color = ZivaaTheme.colors.clay)
        } else {
            // Loading or no data fallback
            DistributionBar(label = "RESTING", range = "Calculating...", percentage = "0%", fraction = 0f, color = ZivaaTheme.colors.sage)
            Spacer(modifier = Modifier.height(16.dp))
            DistributionBar(label = "EASY", range = "Calculating...", percentage = "0%", fraction = 0f, color = ZivaaTheme.colors.leaf)
            Spacer(modifier = Modifier.height(16.dp))
            DistributionBar(label = "ACTIVE", range = "Calculating...", percentage = "0%", fraction = 0f, color = ZivaaTheme.colors.clay)
        }
    }
}

@Composable
fun DistributionBar(label: String, range: String, percentage: String, fraction: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, style = ZivaaTheme.typography.eyebrow, color = ZivaaTheme.colors.textStrong)
                Text(text = " • $range", style = ZivaaTheme.typography.meta, color = ZivaaTheme.colors.textMeta, modifier = Modifier.padding(start = 4.dp))
            }
            Text(text = percentage, style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = ZivaaTheme.colors.textStrong)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(ZivaaTheme.colors.line)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
fun SevenDayChartSection(
    weeklyData: List<com.zivaa.app.presentation.heartrate.DayData>,
    monthAvg: Float,
    weeklyInsight: String?,
    isLoadingInsight: Boolean
) {
    val colors = ZivaaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "YOUR LAST SEVEN DAYS",
                style = ZivaaTheme.typography.eyebrow,
                color = colors.textStrong
            )
            Spacer(modifier = Modifier.height(32.dp))

            val minSeries = mutableListOf<FloatEntry>()
            val rangeSeries = mutableListOf<FloatEntry>()
            val avgSeries = mutableListOf<FloatEntry>()

            weeklyData.forEachIndexed { index, data ->
                if (data.minHr > 0f && data.maxHr > 0f) {
                    minSeries.add(FloatEntry(index.toFloat(), data.minHr))
                    rangeSeries.add(FloatEntry(index.toFloat(), data.maxHr - data.minHr))
                } else {
                    minSeries.add(FloatEntry(index.toFloat(), 0f))
                    rangeSeries.add(FloatEntry(index.toFloat(), 0f))
                }
                
                if (data.restingHr > 0f) {
                    avgSeries.add(FloatEntry(index.toFloat(), data.restingHr))
                }
            }

            // Fallback for empty avgSeries to avoid crash
            if (avgSeries.isEmpty()) {
                avgSeries.add(FloatEntry(0f, 0f))
            }

            val columnModel = entryModelOf(minSeries, rangeSeries)
            val lineModel = entryModelOf(avgSeries)
            val composedModel = columnModel + lineModel

            val myColumnChart = columnChart(
                columns = listOf(
                    com.patrykandpatrick.vico.compose.component.lineComponent(color = Color.Transparent, thickness = 12.dp),
                    com.patrykandpatrick.vico.compose.component.lineComponent(
                        color = colors.muted.copy(alpha = 0.5f),
                        thickness = 12.dp,
                        shape = Shapes.roundedCornerShape(50)
                    )
                ),
                mergeMode = ColumnChart.MergeMode.Stack
            )

            val myLineChart = lineChart(
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = Color.Transparent.toArgb(),
                        point = com.patrykandpatrick.vico.compose.component.shapeComponent(shape = Shapes.pillShape, color = colors.sage),
                        pointSizeDp = 12f
                    )
                )
            )

            val composedChart = androidx.compose.runtime.remember(myColumnChart, myLineChart) { 
                myColumnChart + myLineChart
            }
            
            val transparentLabelComponent = com.patrykandpatrick.vico.compose.component.textComponent(color = Color.Transparent, textSize = 0.sp)
            val thresholdLine = androidx.compose.runtime.remember(monthAvg, colors, transparentLabelComponent) {
                com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine(
                    thresholdValue = monthAvg,
                    thresholdLabel = "",
                    labelComponent = transparentLabelComponent,
                    lineComponent = com.patrykandpatrick.vico.core.component.shape.ShapeComponent(
                        color = Color.Black.toArgb(),
                        shape = DashedShape(
                            shape = Shapes.rectShape,
                            dashLengthDp = 6f,
                            gapLengthDp = 4f
                        )
                    )
                )
            }
            
            myColumnChart.addDecoration(thresholdLine)

            Chart(
                chart = composedChart,
                model = composedModel,
                startAxis = rememberStartAxis(
                    label = com.patrykandpatrick.vico.compose.component.textComponent(color = colors.textMeta, textSize = 10.sp),
                    valueFormatter = { value, _ -> value.toInt().toString() },
                    itemPlacer = com.patrykandpatrick.vico.core.axis.AxisItemPlacer.Vertical.default(maxItemCount = 5),
                    axis = null,
                    tick = null,
                    guideline = com.patrykandpatrick.vico.compose.component.lineComponent(color = colors.line, thickness = 1.dp)
                ),
                bottomAxis = rememberBottomAxis(
                    label = com.patrykandpatrick.vico.compose.component.textComponent(color = colors.textMeta, textSize = 12.sp),
                    valueFormatter = { value, _ ->
                        val index = value.toInt()
                        if (index in weeklyData.indices) weeklyData[index].dayName else ""
                    },
                    axis = null,
                    tick = null,
                    guideline = null
                ),
                chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.sage))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESTING", style = ZivaaTheme.typography.meta, color = colors.textMeta, maxLines = 1)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(1.dp).height(10.dp).background(colors.lineStrong))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LOW-HIGH", style = ZivaaTheme.typography.meta, color = colors.textMeta, maxLines = 1)
                }
                
                Text("-- MO AVG ${monthAvg.toInt()}", style = ZivaaTheme.typography.meta, color = colors.textMeta, maxLines = 1)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.lineStrong))
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoadingInsight) {
                Text(
                    text = "Generating insight...",
                    style = ZivaaTheme.typography.bodyLarge,
                    color = colors.textMeta
                )
            } else if (weeklyInsight != null) {
                Text(
                    text = weeklyInsight,
                    style = ZivaaTheme.typography.bodyLarge,
                    color = colors.textStrong
                )
            } else {
                Text(
                    text = "Hovering within a beat or two of ${monthAvg.toInt()} all week — the steadiness matters more than the number.",
                    style = ZivaaTheme.typography.bodyLarge,
                    color = colors.textStrong
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
        padding = com.patrykandpatrick.vico.compose.dimensions.dimensionsOf(8.dp, 4.dp),
        color = ZivaaTheme.colors.sageInk,
        textSize = 12.sp,
    )
    val indicator = shapeComponent(Shapes.pillShape, ZivaaTheme.colors.sage)
    val guideline = lineComponent(
        color = ZivaaTheme.colors.sage.copy(alpha = 0.5f),
        thickness = 2.dp,
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
                val min = markedEntries.first().entry.y.toInt()
                val diff = markedEntries.getOrNull(1)?.entry?.y?.toInt() ?: 0
                val max = min + diff
                if (min == max) return "$min BPM"
                return "$min-$max BPM"
            }
        }
    }
}
