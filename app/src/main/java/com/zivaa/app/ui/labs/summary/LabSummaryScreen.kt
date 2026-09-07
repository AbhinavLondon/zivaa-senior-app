package com.zivaa.app.ui.labs.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.components.MarkdownText
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
            item { Spacer(modifier = Modifier.height(32.dp)) }
            // HeroTitleSection() is removed per requirements
            item { SegmentedControlSection() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ZivaaSummaryCard(state.categorySummary, state.isSummaryLoading) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
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
    badgeColor: Color,
    comparisonText: String,
    insight: String,
    progress: Float?,
    rangeStartProgress: Float?,
    rangeEndProgress: Float?,
    rangeText: String,
    hasReferenceRange: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ZivaaTheme.colors.bgElev, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = ZivaaTheme.colors.ink
                )
                Text(
                    text = subtitle,
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textMeta
                )
            }
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    style = ZivaaTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = badgeColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                val isTextValue = value.any { it.isLetter() } || value.length > 6
                Text(
                    text = value,
                    style = if (isTextValue) ZivaaTheme.typography.leadParagraph else ZivaaTheme.typography.displayMedium,
                    color = ZivaaTheme.colors.ink,
                    modifier = Modifier.padding(bottom = if (isTextValue) 4.dp else 0.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = ZivaaTheme.typography.meta,
                    color = ZivaaTheme.colors.textMeta,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                text = comparisonText,
                style = ZivaaTheme.typography.bodySmall.copy(fontFamily = com.zivaa.app.ui.theme.IBMPlexMono),
                color = badgeColor,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        
        if (hasReferenceRange && progress != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
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
                        .height(8.dp)
                        .background(ZivaaTheme.colors.muted.copy(alpha = 0.5f), CircleShape)
                ) {
                    // Green range section
                    Box(
                        modifier = Modifier
                            .width(greenWidth)
                            .offset(x = greenStart)
                            .fillMaxHeight()
                            .background(ZivaaTheme.colors.leaf.copy(alpha = 0.4f))
                    )
                }
                
                // Thumb
                val thumbOffset = totalWidth * (progress ?: 0.5f) - 10.dp
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(20.dp)
                        .background(badgeColor, CircleShape)
                        .border(3.dp, ZivaaTheme.colors.bgElev, CircleShape)
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
                        text = "GREEN = HEALTHY",
                        style = ZivaaTheme.typography.meta.copy(fontFamily = com.zivaa.app.ui.theme.IBMPlexMono),
                        color = ZivaaTheme.colors.leaf
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(3.dp).background(ZivaaTheme.colors.leaf, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rangeText,
                        style = ZivaaTheme.typography.meta.copy(fontFamily = com.zivaa.app.ui.theme.IBMPlexMono),
                        color = ZivaaTheme.colors.leaf
                    )
                }
                Text(
                    text = "YOU ARE HERE",
                    style = ZivaaTheme.typography.meta.copy(fontFamily = com.zivaa.app.ui.theme.IBMPlexMono),
                    color = badgeColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = insight,
            style = ZivaaTheme.typography.bodyMedium,
            color = ZivaaTheme.colors.textBody
        )
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
                Divider(
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
