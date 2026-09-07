package com.zivaa.app.presentation.mood.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.mood.theme.SahayakTheme
import com.zivaa.app.presentation.mood.theme.coloredShadow
import com.zivaa.app.ui.theme.toEyebrowTitleCase

@Composable
fun getHeroBackgroundColor(moodLabel: String?): Color {
    val index = if (moodLabel == null) null else listOf("Wonderful", "Good", "Okay", "Low", "Very low").indexOf(moodLabel).takeIf { it != -1 }
    return when (index) {
        0 -> SahayakTheme.colors.sage
        1 -> SahayakTheme.colors.leaf
        2 -> SahayakTheme.colors.amber
        3 -> SahayakTheme.colors.clay
        4 -> SahayakTheme.colors.rose
        else -> SahayakTheme.colors.surfaceHero
    }
}

@Composable
fun getHeroIconIndex(moodLabel: String?): Int {
    val index = if (moodLabel == null) null else listOf("Wonderful", "Good", "Okay", "Low", "Very low").indexOf(moodLabel).takeIf { it != -1 }
    return index ?: 0 // Default to 0 (Sage face) if none
}

@Composable
fun MoodHeroCard(
    modifier: Modifier = Modifier,
    recentMoodLabel: String? = null,
    todayMoodLabels: List<String> = emptyList(),
    onCheckInClick: () -> Unit = {}
) {
    val todayCheckinsCount = todayMoodLabels.size
    val backgroundColor = getHeroBackgroundColor(recentMoodLabel)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .coloredShadow(
                color = backgroundColor, // Use the same color for shadow
                alpha = 0.16f,
                borderRadius = 22.dp,
                shadowRadius = 30.dp,
                offsetY = 12.dp
            )
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .drawBehind {
                val radius = 100.dp.toPx()
                val center = Offset(size.width + 40.dp.toPx() - radius, -50.dp.toPx() + radius)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF6F3EE).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 0.7f
                    ),
                    radius = radius,
                    center = center
                )
            }
            .padding(24.dp)
    ) {
        Column {
            // Top Row: Eyebrow + Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Eyebrow
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF6F3EE).copy(alpha = 0.14f), CircleShape)
                        .padding(start = 9.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFFD9E8D2), CircleShape)
                            .coloredShadow(
                                color = Color(0xFFD9E8D2),
                                alpha = 0.18f,
                                borderRadius = 3.5.dp,
                                shadowRadius = 4.dp
                            )
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    val eyebrowText = if (todayCheckinsCount > 0) "Feeling ${recentMoodLabel?.toEyebrowTitleCase() ?: "Unknown"}" else "No Check-Ins"
                    Text(
                        text = eyebrowText,
                        style = SahayakTheme.typography.eyebrow,
                        color = Color(0xFFF6F3EE).copy(alpha = 0.78f)
                    )
                }

                // Action
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onCheckInClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Log +",
                        style = SahayakTheme.typography.meta.copy(fontSize = 11.sp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headline (No headline text, just icon, per requirements)
            if (todayCheckinsCount == 0) {
                Text(
                    text = "Ready for your first check-in today?",
                    style = SahayakTheme.typography.display,
                    color = SahayakTheme.colors.sageInk
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Icon + Check in count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(Color(0xFFF6F3EE).copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    FaceIcon(
                        moodIndex = getHeroIconIndex(recentMoodLabel),
                        size = 38.dp,
                        color = Color(0xFFF6F3EE) // The face icon in hero card uses the bright tint
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    val countText = if (todayCheckinsCount == 1) "1 checkin today" else "$todayCheckinsCount checkins today"
                    Text(
                        text = if (todayCheckinsCount > 0) countText else "Log your mood",
                        style = SahayakTheme.typography.body.copy(fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                        color = Color(0xFFF6F3EE)
                    )
                    
                    if (todayCheckinsCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            todayMoodLabels.forEach { label ->
                                FaceIcon(
                                    moodIndex = getHeroIconIndex(label),
                                    size = 18.dp,
                                    color = Color(0xFFF6F3EE).copy(alpha = 0.85f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "We’ll check in again tonight.",
                            style = SahayakTheme.typography.meta.copy(fontSize = 12.sp),
                            color = Color(0xFFF6F3EE).copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}
