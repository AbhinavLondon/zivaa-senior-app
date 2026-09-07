package com.zivaa.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.zivaa.app.ui.theme.ZivaaTheme
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec

@Composable
fun NudgeDeepDiveScreen(
    viewModel: NudgeDeepDiveViewModel,
    onNavigateBack: () -> Unit,
    onAction: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val graphs by viewModel.graphs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val alert = viewModel.alert

    var isAcknowledged by remember { mutableStateOf(alert?.acknowledged == true) }
    var hasReachedBottom by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.maxValue, scrollState.value) {
        if (scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue - 50) {
            hasReachedBottom = true
        }
    }

    Scaffold(
        containerColor = ZivaaTheme.colors.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                    text = "Nudge · Deep Dive",
                    style = ZivaaTheme.typography.eyebrow,
                    color = Color(0xFF111111)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (alert != null) {
                // Original Alert Text Card
                val isHighRisk = alert.risk_level.equals("HIGH", ignoreCase = true)
                val isDarkTheme = ZivaaTheme.colors.bg == Color(0xFF14171A)
                
                val bgColor = if (isHighRisk) {
                    if (isDarkTheme) Color(0xFF2E282A) else Color(0xFFF2E7E3)
                } else {
                    if (isDarkTheme) Color(0xFF2D2C29) else Color(0xFFF7EFE4)
                }
                
                val borderColor = if (isHighRisk) {
                    if (isDarkTheme) Color(0xFFD27A6B).copy(alpha = 0.493f) else Color(0xFF98463A).copy(alpha = 0.493f)
                } else {
                    if (isDarkTheme) Color(0xFF574A36) else Color(0xFFEADCC6)
                }
                
                val inkColor = if (isHighRisk) {
                    if (isDarkTheme) Color.White else Color(0xFF2F302D)
                } else {
                    if (isDarkTheme) Color(0xFFF0F0F0) else Color(0xFF2F302D) // Ensure texts are not amber
                }
                
                val titleAccentColor = if (isHighRisk) {
                    if (isDarkTheme) Color(0xFFD27A6B) else Color(0xFFA1493A)
                } else {
                    inkColor
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(bgColor)
                        .padding(24.dp)
                ) {
                    Column {
                        val formattedDate = try {
                            if (alert.created_at.isNotEmpty()) {
                                val instant = java.time.Instant.parse(alert.created_at)
                                val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, h:mm a").withZone(java.time.ZoneId.systemDefault())
                                formatter.format(instant)
                            } else ""
                        } catch (e: Exception) {
                            ""
                        }
                        
                        if (formattedDate.isNotEmpty()) {
                            val eyebrowBase = if (isHighRisk) "Needs Attention Now" else "A Pattern Worth A Look"
                            Text(
                                text = "$eyebrowBase • $formattedDate",
                                style = ZivaaTheme.typography.meta.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.em
                                ),
                                color = titleAccentColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Text(
                            text = alert.nudge_title ?: "Recent Anomaly Detected",
                            style = ZivaaTheme.typography.cardTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
                            color = titleAccentColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = alert.nudge_text ?: "",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp),
                            color = inkColor.copy(alpha = 0.8f)
                        )
                        

                    }
                }

                // Add WHY WE'RE NUDGING YOU Section
                val whyFlaggedData = parseWhyFlagged(alert.why_flagged)
                if (whyFlaggedData != null && whyFlaggedData.vitals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Why We're Nudging You",
                        style = ZivaaTheme.typography.eyebrow,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WhyFlaggedSection(whyFlaggedData, alert.risk_level ?: "LOW")
                }

                val actionStepsData = parseActionSteps(alert.action_steps)
                if (actionStepsData != null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "What We Recommend",
                        style = ZivaaTheme.typography.eyebrow,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WhatWeRecommendSection(actionStepsData, alert.risk_level ?: "LOW", onAction = onAction)
                }

                var isFullPictureExpanded by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFullPictureExpanded = !isFullPictureExpanded }
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "The Full Picture",
                        style = ZivaaTheme.typography.eyebrow,
                        color = Color(0xFF111111),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (isFullPictureExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Toggle The Full Picture",
                        tint = ZivaaTheme.colors.inkMute
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = isFullPictureExpanded) {
                    Column {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = ZivaaTheme.colors.sage)
                            }
                        } else if (graphs.isEmpty()) {
                            Text(
                                text = "No historical data available for these vitals.",
                                style = ZivaaTheme.typography.bodyMedium,
                                color = ZivaaTheme.colors.inkSoft,
                                modifier = Modifier.padding(horizontal = 22.dp)
                            )
                        } else {
                            graphs.forEach { graph ->
                                VitalGraphCard(graph)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(130.dp))
        }

        if (alert != null) {
            val isHighRisk = alert.risk_level.equals("HIGH", ignoreCase = true)
            val isDarkTheme = ZivaaTheme.colors.bg == Color(0xFF14171A)
            
            val accentColor = if (isHighRisk) {
                if (isDarkTheme) Color(0xFFD27A6B) else Color(0xFFA1493A)
            } else {
                if (isDarkTheme) Color(0xFFD6A35A) else Color(0xFFC98A3A)
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 120.dp)
            ) {
                if (isAcknowledged) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ZivaaTheme.colors.surfaceCard)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You have acknowledged this. You can access it in the History of Health tab.",
                            style = ZivaaTheme.typography.bodyMedium,
                            color = ZivaaTheme.colors.inkSoft,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (hasReachedBottom) accentColor else accentColor.copy(alpha = 0.5f))
                            .clickable(enabled = hasReachedBottom) {
                                isAcknowledged = true
                                viewModel.acknowledgeAlert()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I Acknowledge",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = if (hasReachedBottom) 1f else 0.5f)
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
fun VitalGraphCard(graph: VitalGraphData) {
    val themeColor = if (graph.colorTone == "rose") ZivaaTheme.colors.toneAct else ZivaaTheme.colors.toneWatch
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = themeColor.copy(alpha = 0.08f),
                spotColor = themeColor.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "${graph.title} (${graph.unit})",
                style = ZivaaTheme.typography.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                color = ZivaaTheme.colors.ink
            )
            Spacer(modifier = Modifier.height(16.dp))

            val mainEntries = graph.points.mapIndexedNotNull { index, pair ->
                pair.second?.let { FloatEntry(x = index.toFloat(), y = it.toFloat()) }
            }
            
            // Only add a highlight entry if concernIndex is valid
            val highlightEntries = if (graph.concernIndex != null && graph.concernIndex in graph.points.indices) {
                graph.points[graph.concernIndex].second?.let {
                    listOf(FloatEntry(x = graph.concernIndex.toFloat(), y = it.toFloat()))
                } ?: emptyList()
            } else {
                emptyList()
            }

            val chartEntryModel = if (highlightEntries.isNotEmpty()) {
                entryModelOf(mainEntries, highlightEntries)
            } else {
                entryModelOf(mainEntries)
            }

            val bottomAxisValueFormatter = AxisValueFormatter<com.patrykandpatrick.vico.core.axis.AxisPosition.Horizontal.Bottom> { value, _ ->
                graph.points.getOrNull(value.toInt())?.first ?: ""
            }
            
            val startAxisValueFormatter = AxisValueFormatter<com.patrykandpatrick.vico.core.axis.AxisPosition.Vertical.Start> { value, _ ->
                value.toInt().toString()
            }

            val lines = mutableListOf(
                com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec(
                    lineColor = themeColor.toArgb(),
                    lineThicknessDp = 3f,
                    point = com.patrykandpatrick.vico.compose.component.shapeComponent(
                        shape = com.patrykandpatrick.vico.core.component.shape.Shapes.pillShape,
                        color = themeColor
                    ),
                    pointSizeDp = 6f
                )
            )

            if (highlightEntries.isNotEmpty()) {
                lines.add(
                    com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec(
                        lineColor = ZivaaTheme.colors.rose.toArgb(), // Highlight color
                        lineThicknessDp = 0f, // No line, just the point
                        point = com.patrykandpatrick.vico.compose.component.shapeComponent(
                            shape = com.patrykandpatrick.vico.core.component.shape.Shapes.pillShape,
                            color = ZivaaTheme.colors.rose
                        ),
                        pointSizeDp = 16f // Distinctively large
                    )
                )
            }

            Chart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                chart = lineChart(
                    lines = lines,
                    axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(
                        minX = 0f,
                        maxX = 6f
                    )
                ),
                model = chartEntryModel,
                bottomAxis = rememberBottomAxis(
                    valueFormatter = bottomAxisValueFormatter,
                    tickLength = 0.dp,
                    guideline = null,
                    label = textComponent(
                        color = ZivaaTheme.colors.inkMute.copy(alpha = 0.6f),
                        textSize = 10.sp
                    )
                ),
                startAxis = rememberStartAxis(
                    valueFormatter = startAxisValueFormatter,
                    tickLength = 0.dp,
                    label = textComponent(
                        color = ZivaaTheme.colors.inkMute.copy(alpha = 0.6f),
                        textSize = 10.sp
                    ),
                    guideline = com.patrykandpatrick.vico.compose.component.lineComponent(
                        color = ZivaaTheme.colors.line,
                        thickness = 1.dp
                    )
                )
            )
        }
    }
}

data class WhyFlaggedData(
    val summary: String,
    val vitals: List<WhyFlaggedVital>,
    val supportingVitals: List<WhyFlaggedVital>,
    val conclusion: String
)

data class WhyFlaggedVital(
    val name: String,
    val description: String,
    val value: String,
    val usual: String
)

fun parseWhyFlagged(data: Any?): WhyFlaggedData? {
    if (data is Map<*, *>) {
        val summary = data["summary"] as? String ?: return null
        val conclusion = data["conclusion"] as? String ?: return null
        
        // Backward compatibility: use primary_vitals if exists, otherwise fallback to vitals
        val vitalsRaw = data["primary_vitals"] ?: data["vitals"]
        val vitalsList = vitalsRaw as? List<*> ?: emptyList<Any>()
        
        val vitals = vitalsList.mapNotNull { v ->
            if (v is Map<*, *>) {
                WhyFlaggedVital(
                    name = v["name"] as? String ?: "",
                    description = v["description"] as? String ?: "",
                    value = v["value"] as? String ?: "",
                    usual = v["usual"] as? String ?: ""
                )
            } else null
        }
        
        val supportingVitalsList = data["supporting_vitals"] as? List<*> ?: emptyList<Any>()
        val supportingVitals = supportingVitalsList.mapNotNull { v ->
            if (v is Map<*, *>) {
                WhyFlaggedVital(
                    name = v["name"] as? String ?: "",
                    description = v["description"] as? String ?: "",
                    value = v["value"] as? String ?: "",
                    usual = v["usual"] as? String ?: ""
                )
            } else null
        }
        
        return WhyFlaggedData(summary, vitals, supportingVitals, conclusion)
    }
    return null
}
@Composable
fun WhyFlaggedSection(data: WhyFlaggedData, riskLevel: String) {
    val isHighRisk = riskLevel.equals("HIGH", ignoreCase = true)
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    val inkColor = ZivaaTheme.colors.ink
    val inkSoftColor = ZivaaTheme.colors.inkSoft
    
    val valueColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFFD27A6B) else Color(0xFF98463A)
    } else {
        if (isDarkTheme) Color(0xFFD6A35A) else Color(0xFFC98A3A)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Clinical Headline (Summary)
        val annotatedSummary = androidx.compose.ui.text.buildAnnotatedString {
            val boldTarget = "three different vitals"
            val startIndex = data.summary.indexOf(boldTarget)
            if (startIndex >= 0) {
                append(data.summary.substring(0, startIndex))
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = inkColor)) {
                    append(boldTarget)
                }
                append(data.summary.substring(startIndex + boldTarget.length))
            } else {
                append(data.summary)
            }
        }

        Text(
            text = annotatedSummary,
            style = ZivaaTheme.typography.bodyLarge.copy(
                fontSize = 18.sp, 
                lineHeight = 26.sp,
                fontFamily = com.zivaa.app.ui.theme.Manrope,
                fontWeight = FontWeight.Medium
            ),
            color = inkColor.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 22.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Anomaly Cards
        data.vitals.forEach { vital ->
            VitalCard(vital, valueColor, inkColor, inkSoftColor, isDarkTheme)
        }
        
        if (data.supportingVitals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Supporting Factors",
                style = ZivaaTheme.typography.eyebrow,
                color = Color(0xFF111111),
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            data.supportingVitals.forEach { vital ->
                VitalCard(vital, valueColor.copy(alpha = 0.7f), inkColor, inkSoftColor, isDarkTheme)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Key Takeaway / Conclusion Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ZivaaTheme.colors.surfaceCard)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Accent Line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(ZivaaTheme.colors.sage)
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "THE BOTTOM LINE",
                        style = ZivaaTheme.typography.meta.copy(
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 0.05.em
                        ),
                        color = ZivaaTheme.colors.sage
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = data.conclusion,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp, 
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = inkColor.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun VitalCard(
    vital: WhyFlaggedVital,
    valueColor: Color,
    inkColor: Color,
    inkSoftColor: Color,
    isDarkTheme: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(0.5.dp, ZivaaTheme.colors.lineStrong.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column {
            // Top Row: Icon + Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconRes = when {
                    vital.name.contains("Oxygen", ignoreCase = true) -> Icons.Outlined.AllInclusive
                    vital.name.contains("Sleep", ignoreCase = true) || vital.name.contains("Awake", ignoreCase = true) -> Icons.Outlined.Nightlight
                    vital.name.contains("Heart", ignoreCase = true) || vital.name.contains("Breathing", ignoreCase = true) -> Icons.Outlined.Speed
                    else -> Icons.Filled.Info
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(valueColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconRes,
                        contentDescription = null,
                        tint = valueColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = vital.name,
                    style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                    color = inkColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Middle Row: Value + Usual Pill
            Column {
                Text(
                    text = vital.value,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = com.zivaa.app.ui.theme.Manrope,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = valueColor,
                        lineHeight = 24.sp
                    )
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ZivaaTheme.colors.muted.copy(alpha = if(isDarkTheme) 0.5f else 1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "USUAL ",
                            style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.05.em, fontWeight = FontWeight.Bold),
                            color = inkSoftColor
                        )
                        Text(
                            text = vital.usual,
                            style = ZivaaTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = inkColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom Row: Insight Context
            Text(
                text = vital.description,
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = inkSoftColor
            )
        }
    }
}

data class CTAButtonData(
    val label: String,
    val action: String,
    val icon: String? = null
)

data class ActionStepsData(
    val title: String,
    val titleEmphasis: String?,
    val description: String,
    val primaryCta: CTAButtonData?,
    val secondaryCta: CTAButtonData?
)

fun parseActionSteps(raw: Any?): ActionStepsData? {
    if (raw !is Map<*, *>) return null
    try {
        val title = raw["title"] as? String ?: return null
        val titleEmphasis = raw["title_emphasis"] as? String
        val description = raw["description"] as? String ?: ""
        
        val primaryMap = raw["primary_cta"] as? Map<*, *>
        val primaryCta = primaryMap?.let {
            CTAButtonData(
                label = it["label"] as? String ?: "",
                action = it["action"] as? String ?: "",
                icon = it["icon"] as? String
            )
        }
        
        val secondaryMap = raw["secondary_cta"] as? Map<*, *>
        val secondaryCta = secondaryMap?.let {
            CTAButtonData(
                label = it["label"] as? String ?: "",
                action = it["action"] as? String ?: "",
                icon = it["icon"] as? String
            )
        }
        
        return ActionStepsData(title, titleEmphasis, description, primaryCta, secondaryCta)
    } catch (e: Exception) {
        return null
    }
}

@Composable
fun WhatWeRecommendSection(data: ActionStepsData, riskLevel: String, onAction: (String) -> Unit = {}) {
    val isHighRisk = riskLevel.equals("HIGH", ignoreCase = true)
    val isDarkTheme = ZivaaTheme.colors.bg == Color(0xFF14171A)
    
    val bgColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFF2E282A) else Color(0xFFF2E7E3)
    } else {
        if (isDarkTheme) Color(0xFF2D2C29) else Color(0xFFF7EFE4)
    }
    
    val borderColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFFD27A6B).copy(alpha = 0.493f) else Color(0xFF98463A).copy(alpha = 0.493f)
    } else {
        if (isDarkTheme) Color(0xFF574A36) else Color(0xFFEADCC6)
    }
    
    val inkColor = if (isHighRisk) {
        if (isDarkTheme) Color.White else Color(0xFF2F302D)
    } else {
        if (isDarkTheme) Color(0xFFF0F0F0) else Color(0xFF2F302D)
    }
    
    val buttonColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFFD27A6B) else Color(0xFF98463A)
    } else {
        if (isDarkTheme) Color(0xFF574A36) else Color(0xFFEADCC6)
    }
    
    val buttonTextColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    } else {
        if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFF2F302D)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .padding(24.dp)
    ) {
        Column {
            val annotatedTitle = buildAnnotatedString {
                withStyle(style = androidx.compose.ui.text.SpanStyle(
                    fontFamily = com.zivaa.app.ui.theme.InstrumentSerif,
                    fontSize = 26.sp,
                    color = inkColor
                )) {
                    append(data.title)
                    if (!data.titleEmphasis.isNullOrEmpty()) {
                        append(" – ")
                        withStyle(style = androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = if(isHighRisk) { if(isDarkTheme) Color(0xFFD27A6B) else Color(0xFF98463A) } else inkColor)) {
                            append(data.titleEmphasis)
                        }
                    }
                }
            }
            
            Text(text = annotatedTitle, lineHeight = 32.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = data.description,
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp),
                color = inkColor.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val context = androidx.compose.ui.platform.LocalContext.current
            
            if (data.primaryCta != null) {
                Button(
                    onClick = { 
                        onAction(data.primaryCta.action)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (data.primaryCta.icon == "calendar") {
                            Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, tint = buttonTextColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        } else if (data.primaryCta.icon == "clipboard") {
                            Icon(imageVector = Icons.Outlined.Assignment, contentDescription = null, tint = buttonTextColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = data.primaryCta.label, style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = buttonTextColor)
                    }
                }
            }
            
            if (data.secondaryCta != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { 
                        onAction(data.secondaryCta.action)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, buttonColor.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = inkColor)
                ) {
                    Text(text = data.secondaryCta.label, style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp))
                }
            }
        }
    }
}
