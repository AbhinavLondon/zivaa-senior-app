package com.zivaa.app.presentation.sleep

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
// removed NavController
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase

import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
@Composable
fun SleepScreen(
    onNavigateBack: () -> Unit,
    viewModel: SleepViewModel = viewModel(factory = SleepViewModelFactory())
) {
    val colors = ZivaaTheme.colors
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .systemBarsPadding()
        ) {
            // Scrolling content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                TopRow(onBackClick = onNavigateBack)

                HeroCard(
                    latestSleep = viewModel.latestSleepHoursText,
                    latestSleepStartEndTime = viewModel.latestSleepStartEndTime,
                    summaryText = viewModel.nightSummaryText,
                    actionNudgeText = viewModel.actionNudgeText,
                    heroInsightText = viewModel.heroInsightText,
                    isHeroInsightLoading = viewModel.isHeroInsightLoading,
                    sleepConsistencyPct = viewModel.sleepConsistencyPct,
                    bedtimeVarianceMins = viewModel.bedtimeVarianceMins
                )

                SectionHeader(title = "How The Night Went")
                NightEventsList(events = viewModel.nightEvents)

                SectionHeader(title = "Your Last Seven Nights")
                WeeklySleepChart(
                    history = viewModel.sleepHistory,
                    averageSleep = viewModel.averageSleepHoursText,
                    insightText = viewModel.weeklyInsightText,
                    isInsightLoading = viewModel.isWeeklyInsightLoading
                )

                ClosingLine()

                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }
}

@Composable
private fun TopRow(onBackClick: () -> Unit) {
    val colors = ZivaaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.bgElev)
                .border(0.5.dp, colors.line, CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = colors.ink,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Sleep · Tue 16 Jun",
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.eyebrow
        )
    }
}

@Composable
private fun HeroCard(
    latestSleep: String,
    latestSleepStartEndTime: String,
    summaryText: String,
    actionNudgeText: String = "",
    heroInsightText: String = "",
    isHeroInsightLoading: Boolean = false,
    sleepConsistencyPct: Double? = null,
    bedtimeVarianceMins: Double? = null
) {
    val colors = ZivaaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = colors.line,
                spotColor = colors.line
            )
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceHero)
    ) {
        // Subtle radial gradient background effect
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1EF6F3EE), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = 200.dp.toPx()
                ),
                center = Offset(size.width, 0f),
                radius = 200.dp.toPx()
            )
        }

        val isSteady = heroInsightText.contains("[STEADY]") || heroInsightText.contains("[GOOD]")
        val isWatch = heroInsightText.contains("[WORTH ATTENTION]") || heroInsightText.contains("[AVG]")
        val isAct = heroInsightText.contains("[NEEDS ATTENTION]") || heroInsightText.contains("[ATTN]")

        val badgeText = when {
            isWatch -> "Worth Attention"
            isAct -> "Needs Attention"
            isSteady -> "Steady"
            else -> "Rested Well"
        }
        
        val brightAmber = Color(0xFFFFC278) // Brightened for dark background
        val brightRose = Color(0xFFFF9E94)  // Brightened for dark background
        
        val pillDotColor = when {
            isWatch -> brightAmber
            isAct -> brightRose
            isSteady -> Color(0xFFD9E8D2) // Bright green for contrast on Sage
            else -> Color(0xFFD9E8D2)
        }
        
        val pillBgColor = Color(0x24F6F3EE) // Keep white semi-transparent to prevent muddying against dark green
        
        val pillTextColor = when {
            isWatch -> brightAmber
            isAct -> brightRose
            else -> Color(0xC7F6F3EE)
        }
        
        val cleanHeroText = heroInsightText
            .replace("[STEADY]", "")
            .replace("[WORTH ATTENTION]", "")
            .replace("[NEEDS ATTENTION]", "")
            .trim()
            .ifEmpty { "A calm, restful night" }

        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Pills row (Status + Sleep Consistency)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Main Status Pill
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(pillBgColor)
                        .padding(start = 9.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(pillDotColor)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = pillTextColor
                    )
                }

                // Sleep Consistency Pill
                if (sleepConsistencyPct != null) {
                    val score = sleepConsistencyPct.toInt()
                    val (consistencyLabel, consistencyDot) = when {
                        score >= 85 -> "Steady Rhythm · ${score}%" to Color(0xFFD9E8D2)
                        score >= 70 -> "Moderate · ${score}%" to brightAmber
                        else -> "Variable · ${score}%" to brightRose
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(pillBgColor)
                            .padding(start = 9.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(consistencyDot)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = consistencyLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = consistencyDot
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.animation.Crossfade(targetState = isHeroInsightLoading, label = "sleepHeroCrossfade") { loading ->
                if (loading) {
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.01).em),
                        color = colors.sageInk.copy(alpha = 0.5f)
                    )
                } else {
                    Text(
                        text = cleanHeroText,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.01).em),
                        color = colors.sageInk
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = latestSleep,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp, lineHeight = 46.sp),
                    color = colors.sageInk
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = latestSleepStartEndTime,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.7.sp), // 0.07em
                    color = Color(0xB8F6F3EE) // 0.72 approx
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Actionable Sleep Nudge Banner (Replaces redundant stage summary)
            val displayText = actionNudgeText.ifEmpty { summaryText }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x1EF6F3EE))
                    .border(0.5.dp, Color(0x33F6F3EE), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = brightAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TODAY'S ACTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = brightAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 19.5.sp
                        ),
                        color = Color(0xF2F6F3EE)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.toEyebrowTitleCase(),
        style = ZivaaTheme.typography.eyebrow,
        color = ZivaaTheme.colors.eyebrow,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 10.dp)
    )
}

@Composable
private fun NightEventsList(events: List<NightEvent>) {
    val colors = ZivaaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 24.dp, // Approximation of shadow-soft
                shape = RoundedCornerShape(22.dp),
                ambientColor = colors.line,
                spotColor = colors.line
            )
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgElev)
            .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = if (events.isEmpty()) 20.dp else 6.dp)
    ) {
        if (events.isEmpty()) {
            Text(
                text = "We will show the breakdown of your sleep here.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                color = colors.inkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            events.forEachIndexed { index, event ->
                val dotColor = when (event.type) {
                    NightEventType.ASLEEP, NightEventType.DEEP, NightEventType.REM -> colors.sage
                    NightEventType.AWAKE -> colors.muted
                }
                NightEventRow(
                    time = event.time,
                    text = event.text,
                    dotColor = dotColor,
                    showDivider = index < events.size - 1
                )
            }
        }
    }
}

@Composable
private fun NightEventRow(time: String, text: String, dotColor: Color, showDivider: Boolean) {
    val colors = ZivaaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (showDivider) {
                    drawLine(
                        color = colors.line,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(dotColor)
                .border(0.5.dp, colors.lineStrong, CircleShape)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.4.sp), // 0.04em
            color = colors.inkMute,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
            color = colors.ink
        )
    }
}

@Composable
fun rememberSleepMarker(): Marker {
    val colors = ZivaaTheme.colors
    val labelBackground = shapeComponent(Shapes.pillShape, colors.sage)
    val label = textComponent(
        background = labelBackground,
        padding = dimensionsOf(8.dp, 4.dp),
        color = Color.White,
        textSize = 12.sp,
    )
    val indicator = shapeComponent(Shapes.pillShape, colors.sage)
    val guideline = lineComponent(
        color = colors.sage.copy(alpha = 0.5f),
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
                val total = markedEntries.sumOf { it.entry.y.toDouble() }
                val hours = total.toInt()
                val minutes = ((total - hours) * 60).toInt()
                return "${hours}h ${minutes}m"
            }
        }
    }
}

@Composable
private fun WeeklySleepChart(history: List<SleepNightSummary>, averageSleep: String, insightText: String, isInsightLoading: Boolean) {
    val colors = ZivaaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = colors.line,
                spotColor = colors.line
            )
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgElev)
            .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "SLEEP",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.05.em),
            color = colors.inkMute
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        val averageHours = if (history.isNotEmpty()) history.map { it.sleepHours }.average().toFloat() else 0f
        
        val displayData = if (history.size >= 7) history.takeLast(7) else List(7 - history.size) { SleepNightSummary("", 0.0) } + history

        val series = displayData.mapIndexed { index, summary ->
            List(7) { i -> FloatEntry(x = i.toFloat(), y = if (i == index) summary.sleepHours.toFloat() else 0f) }
        }
        val chartEntryModel = entryModelOf(*series.toTypedArray())

        val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.toInt()
            val summary = displayData.getOrNull(index)
            if (summary != null && summary.date.isNotEmpty()) {
                try {
                    val datePart = summary.date.substringBefore("T")
                    val date = LocalDate.parse(datePart)
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).first().toString().uppercase()
                } catch (e: Exception) {
                    "?"
                }
            } else "-"
        }

        val avgLine = ThresholdLine(
            thresholdValue = averageHours,
            lineComponent = lineComponent(
                color = colors.line.copy(alpha = 0.8f), 
                thickness = 1.dp,
                shape = DashedShape(shape = Shapes.rectShape, dashLengthDp = 4f, gapLengthDp = 4f)
            ),
            labelComponent = textComponent(
                color = colors.inkMute,
                textSize = 9.sp,
                margins = dimensionsOf(bottom = 4.dp)
            ),
            labelHorizontalPosition = ThresholdLine.LabelHorizontalPosition.Start,
            thresholdLabel = "Avg $averageSleep"
        )

        val columns = List(7) { index ->
            if (index == 6) {
                lineComponent(color = colors.sage, thickness = 24.dp, shape = Shapes.pillShape)
            } else {
                lineComponent(color = colors.muted, thickness = 24.dp, shape = Shapes.pillShape)
            }
        }

        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
            chart = columnChart(
                columns = columns,
                mergeMode = MergeMode.Stack,
                decorations = listOf(avgLine),
                spacing = 16.dp
            ),
            model = chartEntryModel,
            marker = rememberSleepMarker(),
            bottomAxis = rememberBottomAxis(
                valueFormatter = bottomAxisValueFormatter,
                tickLength = 0.dp,
                guideline = null,
                label = textComponent(
                    color = colors.inkMute.copy(alpha = 0.8f),
                    textSize = 10.sp
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.line)
        )
        if (isInsightLoading) {
            Text(
                text = "Generating sleep analysis...",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSoft,
                modifier = Modifier.padding(top = 14.dp)
            )
        } else if (insightText.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                val lines = insightText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                lines.forEachIndexed { index, line ->
                    val isSteady = line.startsWith("[STEADY]") || line.startsWith("-[STEADY]") || line.startsWith("*[STEADY]") || line.contains("[STEADY]")
                    val isWatch = line.startsWith("[WORTH ATTENTION]") || line.startsWith("-[WORTH ATTENTION]") || line.startsWith("*[WORTH ATTENTION]") || line.contains("[WORTH ATTENTION]")
                    val isAct = line.startsWith("[NEEDS ATTENTION]") || line.startsWith("-[NEEDS ATTENTION]") || line.startsWith("*[NEEDS ATTENTION]") || line.contains("[NEEDS ATTENTION]")
                    
                    val variant = when {
                        isWatch -> com.zivaa.app.presentation.components.StatusBadgeVariant.WATCH
                        isAct -> com.zivaa.app.presentation.components.StatusBadgeVariant.ACT
                        else -> com.zivaa.app.presentation.components.StatusBadgeVariant.OK
                    }
                    
                    val badgeText = when {
                        isWatch -> "Worth attention"
                        isAct -> "Needs attention"
                        else -> "Steady"
                    }
                    
                    val cleanText = line.replace("[STEADY]", "").replace("[WORTH ATTENTION]", "").replace("[NEEDS ATTENTION]", "").trim().removePrefix("-").removePrefix("*").trim()
                    
                    if (cleanText.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            com.zivaa.app.presentation.components.StatusBadge(
                                text = badgeText,
                                variant = variant,
                                textStyle = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cleanText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSoft
                            )
                        }
                    }
                    
                    if (index < lines.size - 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(colors.line)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        } else {
            Text(
                text = "Averaging $averageSleep this week — steady nights, most of them past seven hours.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSoft,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
private fun InsightCard() {
    val colors = ZivaaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFE5EBE8)) // color-mix(in srgb, var(--sage) 7%, var(--bg-elev)) approx calculation needed. colors.sage #234B3F (35, 75, 63), colors.bgElev #FBF9F5 (251, 249, 245). 7% Sage + 93% BgElev = ~ (236, 237, 232) -> #ECEDE8. Let's use an approximate visually pleasing color.
            .border(0.5.dp, Color(0xFFC7D0CD), RoundedCornerShape(22.dp)) // color-mix approx
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.sage),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star, // Using Star as placeholder for Spark
                    contentDescription = "Insight",
                    tint = colors.sageInk,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "What deepens your nights",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 21.6.sp), // 18 * 1.2
                    color = colors.ink
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "The nights after your 9:30 meditation run longer and calmer. Tonight's is already on your plan.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = colors.inkSoft
                )
            }
        }
    }
}

@Composable
private fun WindDownCard(onCardClick: () -> Unit) {
    val colors = ZivaaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 12.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = colors.line,
                spotColor = colors.line
            )
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgElev)
            .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
            .clickable(onClick = onCardClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.muted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NightlightRound, // Moon icon
                contentDescription = "Wind down",
                tint = colors.ink,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TONIGHT'S WIND-DOWN",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.7.sp),
                color = colors.inkMute
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Meditation · 9:30 PM",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Ten quiet minutes before bed — it's on your plan.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSoft
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go",
            tint = colors.inkMute
        )
    }
}

@Composable
private fun ClosingLine() {
    val colors = ZivaaTheme.colors
    Text(
        text = "We only listen for rest, never more. Sleep well tonight — we'll note the morning.",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 19.3.sp),
        color = colors.inkMute,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 22.dp, start = 30.dp, end = 30.dp)
    )
}
