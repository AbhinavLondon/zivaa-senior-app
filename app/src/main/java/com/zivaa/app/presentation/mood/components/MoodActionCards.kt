package com.zivaa.app.presentation.mood.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.mood.theme.SahayakTheme
import com.zivaa.app.presentation.mood.theme.coloredShadow

@Composable
fun MoodInsightCard(
    modifier: Modifier = Modifier
) {
    val bgTint = lerp(SahayakTheme.colors.bgElev, SahayakTheme.colors.sage, 0.07f)
    val borderTint = lerp(SahayakTheme.colors.line, SahayakTheme.colors.sage, 0.24f)

    Box(modifier = modifier.padding(horizontal = 22.dp).padding(top = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(bgTint)
                .border(0.5.dp, borderTint, RoundedCornerShape(22.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SahayakTheme.colors.sage, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = SahayakTheme.colors.sageInk,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "What lifts your days",
                    style = SahayakTheme.typography.title.copy(fontSize = 18.sp, lineHeight = 21.6.sp),
                    color = SahayakTheme.colors.ink
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Garden mornings and calls with the family show up again and again on your brightest days. Worth protecting both.",
                    style = SahayakTheme.typography.bodySm.copy(fontSize = 13.5.sp, lineHeight = 20.25.sp),
                    color = SahayakTheme.colors.inkSoft
                )
            }
        }
    }
}

@Composable
fun MoodActionCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(modifier = modifier.padding(horizontal = 22.dp).padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .coloredShadow(
                    color = Color(0xFF234B3F),
                    alpha = 0.06f,
                    borderRadius = 22.dp,
                    shadowRadius = 24.dp,
                    offsetY = 8.dp
                )
                .clip(RoundedCornerShape(22.dp))
                .background(SahayakTheme.colors.bgElev)
                .border(0.5.dp, SahayakTheme.colors.line, RoundedCornerShape(22.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SahayakTheme.colors.muted, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                FaceIcon(moodIndex = 0, size = 20.dp, color = SahayakTheme.colors.ink)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "THIS EVENING",
                    style = SahayakTheme.typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.07.em),
                    color = SahayakTheme.colors.inkMute
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Check in again, anytime",
                    style = SahayakTheme.typography.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = SahayakTheme.colors.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "A quiet minute to say how the day felt.",
                    style = SahayakTheme.typography.bodySm.copy(lineHeight = 18.85.sp),
                    color = SahayakTheme.colors.inkSoft
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SahayakTheme.colors.inkMute,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
