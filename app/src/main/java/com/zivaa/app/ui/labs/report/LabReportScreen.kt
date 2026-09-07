package com.zivaa.app.ui.labs.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.zivaa.app.presentation.components.MarkdownText
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun LabReportScreen(
    viewModel: LabReportViewModel,
    onNavigateBack: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ZivaaTheme.colors.bg,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { 
                TopAppBarArea(
                    title = state.reportTitle,
                    onNavigateBack = onNavigateBack
                ) 
            }
            
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ZivaaTheme.colors.sage)
                    }
                }
            } else if (state.error != null) {
                item {
                    Text(text = state.error ?: "Unknown error", color = ZivaaTheme.colors.toneAct)
                }
            } else {
                item { HeroSection(state) }
                item { SummaryStatsSection(state) }
                item { ProgressBarSection(state) }
                item { CategoryHeader(state) }
                item { CategoryGridSection(state, onCategoryClick) }
                item { BottomActionsSection() }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun TopAppBarArea(title: String, onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
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
            Column {
                Text(
                    text = title.toEyebrowTitleCase(),
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.eyebrow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Read Just Now",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZivaaTheme.colors.textMeta
                )
            }
        }
        
        Row(
            modifier = Modifier
                .background(ZivaaTheme.colors.borderStrong.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "READ",
                style = MaterialTheme.typography.labelSmall,
                color = ZivaaTheme.colors.textStrong
            )
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ZivaaTheme.colors.textStrong,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun HeroSection(state: LabReportState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ZivaaTheme.colors.sage)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Report Summary",
                style = MaterialTheme.typography.headlineMedium,
                color = ZivaaTheme.colors.sageInk
            )
            Spacer(modifier = Modifier.height(16.dp))
            MarkdownText(
                text = state.heroText,
                color = ZivaaTheme.colors.sageInk.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun SummaryStatsSection(state: LabReportState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // In Range Card
        Card(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 150.dp)
                .border(1.dp, ZivaaTheme.colors.borderStrong, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ZivaaTheme.colors.bgElev)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${state.inRangeCount}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                        color = ZivaaTheme.colors.sage
                    )
                    Text(
                        text = " of ${state.totalCount}",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                        color = ZivaaTheme.colors.textBody,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "In range",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = ZivaaTheme.colors.textStrong
                )
                Text(
                    text = "where they should be",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textBody
                )
            }
        }

        // Out of Range Card
        Card(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 150.dp)
                .border(1.dp, ZivaaTheme.colors.toneWatch.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ZivaaTheme.colors.bgElev)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${state.outOfRangeCount}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                    color = ZivaaTheme.colors.toneWatch
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Out of range",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = ZivaaTheme.colors.textStrong
                )
                Text(
                    text = if (state.outOfRangeCount > 0) "Needs attention" else "none serious",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textBody
                )
            }
        }
    }
}

@Composable
fun ProgressBarSection(state: LabReportState) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        ) {
            if (state.totalCount == 0 || state.inRangeCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(if (state.totalCount > 0) state.inRangeCount.toFloat() else 1f)
                        .fillMaxHeight()
                        .background(ZivaaTheme.colors.sage)
                )
            }
            if (state.outOfRangeCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(state.outOfRangeCount.toFloat())
                        .fillMaxHeight()
                        .background(ZivaaTheme.colors.toneWatch)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.inRangeCount} IN RANGE",
                style = MaterialTheme.typography.labelSmall,
                color = ZivaaTheme.colors.sage
            )
            Text(
                text = "${state.outOfRangeCount} OUT",
                style = MaterialTheme.typography.labelSmall,
                color = ZivaaTheme.colors.toneWatch
            )
        }
    }
}

@Composable
fun CategoryHeader(state: LabReportState) {
    val outCount = state.categories.count { it.isOut }
    val totalCount = state.categories.size
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "By Category",
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.eyebrow
        )
        Text(
            text = "$outCount Of $totalCount Need A Look",
            style = MaterialTheme.typography.labelSmall,
            color = ZivaaTheme.colors.toneWatch
        )
    }
}

@Composable
fun CategoryGridSection(state: LabReportState, onCategoryClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val pairs = state.categories.chunked(2)
        pairs.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    title = rowItems[0].title,
                    desc = rowItems[0].description,
                    status = rowItems[0].statusText,
                    isOut = rowItems[0].isOut,
                    onClick = { onCategoryClick(rowItems[0].title) }
                )
                if (rowItems.size > 1) {
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = rowItems[1].title,
                        desc = rowItems[1].description,
                        status = rowItems[1].statusText,
                        isOut = rowItems[1].isOut,
                        onClick = { onCategoryClick(rowItems[1].title) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    status: String,
    isOut: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val normalizedName = title.lowercase().replace(" ", "_")
    val resName = "${normalizedName}_icon"
    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZivaaTheme.colors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(ZivaaTheme.colors.line)
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check, // Placeholder
                            contentDescription = null,
                            tint = ZivaaTheme.colors.textBody,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Photo",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ZivaaTheme.colors.textStrong
                        )
                        Text(
                            text = "or browse files",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = ZivaaTheme.colors.textBody
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = ZivaaTheme.colors.textStrong
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textBody
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOut) ZivaaTheme.colors.toneWatch else ZivaaTheme.colors.sage
                )
            }
        }
    }
}

@Composable
fun BottomActionsSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZivaaTheme.colors.sage)
            ) {
                Text("Ask about this report", style = MaterialTheme.typography.bodyLarge, color = ZivaaTheme.colors.sageInk)
            }
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ZivaaTheme.colors.borderStrong),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZivaaTheme.colors.textStrong)
            ) {
                Text("Share with Meera", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This report is generated by AI and may contain errors. It should not replace medical advice. If you are concerned, please consult a medical professional.",
            style = MaterialTheme.typography.bodyMedium,
            color = ZivaaTheme.colors.textBody,
            textAlign = TextAlign.Center
        )
    }
}
