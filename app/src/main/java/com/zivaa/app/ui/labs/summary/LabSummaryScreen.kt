package com.zivaa.app.ui.labs.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.components.MarkdownText
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase

@Composable
fun LabSummaryScreen(
    viewModel: LabSummaryViewModel,
    initialFilter: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedFilter by remember(initialFilter) { mutableStateOf(initialFilter) }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ZivaaTheme.colors.bg
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(), bottom = 130.dp)
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { SummaryTopBar(state.title, onNavigateBack) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            // Summary card and segmented control - only shown when not filtered
            if (selectedFilter == null) {
                item { SegmentedControlSection() }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { ZivaaSummaryCard(state.categorySummary, state.isSummaryLoading) }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            item { 
                StatBoxesSection(
                    state = state, 
                    selectedFilter = selectedFilter,
                    onFilterClick = { filter ->
                        selectedFilter = if (selectedFilter == filter) null else filter
                    }
                ) 
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { 
                Text(
                    text = "Tap a box to see just those results - tap again for everything.",
                    style = ZivaaTheme.typography.bodySmall,
                    color = ZivaaTheme.colors.textMeta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            // Render Dynamic Biomarkers
            if (state.biomarkers.isNotEmpty()) {
                val outOfRange = state.biomarkers.filter { it.badgeTone == "watch" || it.badgeTone == "bad" }
                if (outOfRange.isNotEmpty() && (selectedFilter == null || selectedFilter == "watch" || selectedFilter == "bad")) {
                    item { CategoryEyebrow(text = "Out of Range - Needs Attention", color = ZivaaTheme.colors.amber) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    outOfRange.forEach { biomarker ->
                        item {
                            MetricCard(
                                title = biomarker.headline,
                                subtitle = "",
                                value = biomarker.value,
                                unit = biomarker.unit,
                                badgeText = biomarker.badgeText,
                                badgeTone = biomarker.badgeTone,
                                badgeColor = ZivaaTheme.colors.amber,
                                comparisonText = "",
                                insight = biomarker.insight,
                                progress = biomarker.progress,
                                rangeStartProgress = biomarker.rangeStartProgress,
                                rangeEndProgress = biomarker.rangeEndProgress,
                                rangeText = biomarker.rangeText,
                                hasReferenceRange = biomarker.hasReferenceRange
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                val good = state.biomarkers.filter { it.badgeTone == "good" }
                if (good.isNotEmpty() && (selectedFilter == null || selectedFilter == "good")) {
                    item { CategoryEyebrow(text = "In Range - Looking Good", color = ZivaaTheme.colors.leaf) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    good.forEach { biomarker ->
                        item {
                            MetricCard(
                                title = biomarker.headline,
                                subtitle = "",
                                value = biomarker.value,
                                unit = biomarker.unit,
                                badgeText = biomarker.badgeText,
                                badgeTone = biomarker.badgeTone,
                                badgeColor = ZivaaTheme.colors.leaf,
                                comparisonText = "",
                                insight = biomarker.insight,
                                progress = biomarker.progress,
                                rangeStartProgress = biomarker.rangeStartProgress,
                                rangeEndProgress = biomarker.rangeEndProgress,
                                rangeText = biomarker.rangeText,
                                hasReferenceRange = biomarker.hasReferenceRange
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
            
            // What it means
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { CategoryEyebrow(text = "What It Means, Day To Day", color = ZivaaTheme.colors.textMeta) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { AdviceCard() }
            
            // Actions
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { ActionButtons() }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { 
                Text(
                    text = "This summary helps you understand the report - it doesn't\nreplace your doctor's advice.",
                    style = ZivaaTheme.typography.meta,
                    color = ZivaaTheme.colors.textMeta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SummaryTopBar(title: String, onNavigateBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, ZivaaTheme.colors.borderStrong, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Back",
                tint = ZivaaTheme.colors.textStrong
            )
        }
        Text(
            text = title.uppercase(),
            style = ZivaaTheme.typography.meta,
            color = ZivaaTheme.colors.textMeta,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SegmentedControlSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(ZivaaTheme.colors.muted.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Simple",
                style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ZivaaTheme.colors.ink
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Clinical",
                style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ZivaaTheme.colors.inkMute
            )
        }
    }
}

@Composable
fun ZivaaSummaryCard(summaryText: String?, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ZivaaTheme.colors.sage, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Z",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = ZivaaTheme.colors.sageInk
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ZIVAA · PLAIN SUMMARY",
                style = ZivaaTheme.typography.meta,
                color = ZivaaTheme.colors.sageInk.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ZivaaTheme.colors.sageInk,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            MarkdownText(
                text = summaryText ?: "No summary available for this category.",
                color = ZivaaTheme.colors.sageInk
            )
        }
    }
}

@Composable
fun StatBoxesSection(
    state: LabSummaryState,
    selectedFilter: String?,
    onFilterClick: (String) -> Unit
) {
    val outCount = state.notSoGoodCount + state.badCount
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            count = state.goodCount.toString(),
            label = "IN RANGE",
            subLabel = "LOOKING GOOD",
            color = ZivaaTheme.colors.leaf,
            isActive = selectedFilter == null || selectedFilter == "good",
            onClick = { onFilterClick("good") }
        )
        StatBox(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            count = outCount.toString(),
            label = "OUT OF RANGE",
            subLabel = "NEEDS ATTENTION",
            color = ZivaaTheme.colors.amber,
            isActive = selectedFilter == null || selectedFilter == "watch" || selectedFilter == "bad",
            onClick = { onFilterClick("watch") }
        )
    }
}

@Composable
fun StatBox(
    modifier: Modifier = Modifier,
    count: String,
    label: String,
    subLabel: String,
    color: Color,
    isActive: Boolean,
    isMuted: Boolean = false,
    onClick: () -> Unit = {}
) {
    val bgColor = if (isMuted) ZivaaTheme.colors.muted else ZivaaTheme.colors.bgElev
    val borderColor = if (isMuted) ZivaaTheme.colors.lineStrong else color.copy(alpha = if (isActive) 1f else 0.2f)
    val borderStroke = if (isMuted) 1.dp else if (isActive) 2.dp else 1.5.dp
    val contentAlpha = if (isActive) 1f else 0.5f
    
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(borderStroke, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = ZivaaTheme.typography.displayMedium,
            color = color.copy(alpha = contentAlpha)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = ZivaaTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = color.copy(alpha = contentAlpha),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subLabel,
            style = ZivaaTheme.typography.meta,
            color = color.copy(alpha = contentAlpha * 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CategoryEyebrow(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text.toEyebrowTitleCase(),
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.eyebrow
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    subtitle: String,
    value: String,
    unit: String,
    badgeText: String,
    badgeTone: String,
    badgeColor: Color,
    comparisonText: String,
    insight: String,
    progress: Float?,
    rangeStartProgress: Float?,
    rangeEndProgress: Float?,
    rangeText: String,
    hasReferenceRange: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    val tagText = if (badgeTone == "good") "Within Range" else "Out of Range"
    val isTextValue = value.any { it.isLetter() } || value.length > 6

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ZivaaTheme.colors.bgElev)
            .border(1.dp, ZivaaTheme.colors.lineStrong.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
            )
            .padding(horizontal = 18.dp, vertical = 13.dp)
    ) {
        // Non-expanded header row (always visible)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Biomarker Name & Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                        fontSize = 14.5.sp,
                        letterSpacing = (-0.01).em,
                        lineHeight = 20.sp
                    ),
                    color = ZivaaTheme.colors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontSize = 11.5.sp
                        ),
                        color = ZivaaTheme.colors.textMeta
                    )
                }
            }

            // Right side: Value, Unit, Status Tag, and Animated Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Value and half-size unit (both strictly non-italic)
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = value,
                            style = TextStyle(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal,
                                fontSize = if (isTextValue) 13.5.sp else 18.sp,
                                letterSpacing = (-0.02).em
                            ),
                            color = ZivaaTheme.colors.ink
                        )
                        if (unit.isNotBlank()) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = unit,
                                style = TextStyle(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 9.sp, // Half size of the numerical value
                                    letterSpacing = 0.02.em
                                ),
                                color = ZivaaTheme.colors.textMeta,
                                modifier = Modifier.padding(bottom = 1.5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Polished status micro-tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .border(0.75.dp, badgeColor.copy(alpha = 0.22f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.5.dp)
                                .background(badgeColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.5.dp))
                        Text(
                            text = tagText.uppercase(),
                            style = TextStyle(
                                fontFamily = Manrope,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal,
                                letterSpacing = 0.05.em
                            ),
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Smoothly rotating chevron indicator inside soft circular target
                val rotationState by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                    label = "chevron_rotation"
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(ZivaaTheme.colors.muted.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = ZivaaTheme.colors.textMeta,
                        modifier = Modifier
                            .size(15.dp)
                            .rotate(rotationState)
                    )
                }
            }
        }

        // Expanded details (graphical representation & clinical explanation)
        if (isExpanded) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = ZivaaTheme.colors.line.copy(alpha = 0.35f),
                thickness = 0.75.dp
            )

            if (comparisonText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = comparisonText,
                    style = TextStyle(
                        fontFamily = com.zivaa.app.ui.theme.IBMPlexMono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = badgeColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            if (hasReferenceRange && progress != null) {
                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar Dynamically Calculated
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val totalWidth = maxWidth
                    val greenStart = totalWidth * (rangeStartProgress ?: 0f)
                    val greenWidth = totalWidth * ((rangeEndProgress ?: 1f) - (rangeStartProgress ?: 0f))

                    // Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(ZivaaTheme.colors.muted.copy(alpha = 0.4f), CircleShape)
                    ) {
                        // Green range section
                        Box(
                            modifier = Modifier
                                .width(greenWidth)
                                .offset(x = greenStart)
                                .fillMaxHeight()
                                .background(ZivaaTheme.colors.leaf.copy(alpha = 0.45f))
                        )
                    }

                    // Thumb
                    val thumbOffset = (totalWidth * progress - 8.dp).coerceIn(0.dp, totalWidth - 16.dp)
                    Box(
                        modifier = Modifier
                            .offset(x = thumbOffset)
                            .size(16.dp)
                            .background(badgeColor, CircleShape)
                            .border(2.5.dp, ZivaaTheme.colors.bgElev, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HEALTHY RANGE",
                            style = TextStyle(
                                fontFamily = com.zivaa.app.ui.theme.IBMPlexMono,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.05.em
                            ),
                            color = ZivaaTheme.colors.leaf
                        )
                        if (rangeText.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(3.dp).background(ZivaaTheme.colors.leaf, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rangeText,
                                style = TextStyle(
                                    fontFamily = com.zivaa.app.ui.theme.IBMPlexMono,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.04.em
                                ),
                                color = ZivaaTheme.colors.leaf
                            )
                        }
                    }
                    Text(
                        text = if (badgeText.isNotBlank() && badgeText != tagText) "YOU: $value ($badgeText)" else "YOU ARE HERE",
                        style = TextStyle(
                            fontFamily = com.zivaa.app.ui.theme.IBMPlexMono,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.em
                        ),
                        color = badgeColor
                    )
                }
            }

            if (insight.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZivaaTheme.colors.muted.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = insight,
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            lineHeight = 18.sp
                        ),
                        color = ZivaaTheme.colors.textBody
                    )
                }
            }
        }
    }
}

@Composable
fun AdviceCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ZivaaTheme.colors.bgElev, RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp)
    ) {
        val advices = listOf(
            "A little less ghee and fried food - it nudges the cholesterol down.",
            "Ten quiet minutes of morning sun for the vitamin D.",
            "Keep the daily walks and the lighter meals - the sugar numbers show they work."
        )
        
        advices.forEachIndexed { index, text ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ZivaaTheme.colors.leaf.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textStrong
                )
            }
            if (index < advices.size - 1) {
                HorizontalDivider(
                    color = ZivaaTheme.colors.line,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButtons() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ZivaaTheme.colors.sage
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Ask about these results",
                style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        
        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ZivaaTheme.colors.ink
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, ZivaaTheme.colors.borderStrong),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Outlined.IosShare, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Share with Dr. Mehta",
                style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
