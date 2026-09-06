package com.zivaa.app.presentation.mindfulness.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme
import com.zivaa.app.presentation.mindfulness.theme.MoodGood
import com.zivaa.app.presentation.mindfulness.theme.MoodLow
import com.zivaa.app.presentation.mindfulness.theme.MoodOkay
import com.zivaa.app.presentation.mindfulness.theme.MoodVeryLow
import com.zivaa.app.presentation.mindfulness.theme.MoodWonderful

enum class Mood(val label: String, val color: Color, val smileLevel: Float) {
    WONDERFUL("Wonderful", MoodWonderful, 1f),
    GOOD("Good", MoodGood, 0.5f),
    OKAY("Okay", MoodOkay, 0f),
    LOW("Low", MoodLow, -0.5f),
    VERY_LOW("Very low", MoodVeryLow, -1f)
}

@Composable
fun MoodCheckInRow(
    modifier: Modifier = Modifier,
    selectedMood: Mood? = null,
    onMoodSelected: (Mood) -> Unit = {}
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Mood.values().forEach { mood ->
            val isSelected = selectedMood == mood
            MoodFace(
                mood = mood,
                isSelected = isSelected,
                onClick = {
                    onMoodSelected(mood)
                }
            )
        }
    }
}

@Composable
private fun MoodFace(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        val borderColor = if (isSelected) Color.Transparent else mood.color.copy(alpha = 0.5f)
        val backgroundColor = if (isSelected) mood.color else Color.White.copy(alpha = 0.05f)
        
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val strokeWidth = 1.5.dp.toPx()
                    val color = if (isSelected) Color(0xFF0D0F1C) else mood.color
                
                // Eyes
                drawCircle(
                    color = color,
                    radius = strokeWidth,
                    center = Offset(size.width * 0.35f, size.height * 0.35f)
                )
                drawCircle(
                    color = color,
                    radius = strokeWidth,
                    center = Offset(size.width * 0.65f, size.height * 0.35f)
                )
                
                // Mouth
                if (mood.smileLevel == 0f) {
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.35f, size.height * 0.65f),
                        end = Offset(size.width * 0.65f, size.height * 0.65f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                } else {
                    val absSmile = Math.abs(mood.smileLevel)
                    val sweepAngle = 100f * absSmile
                    if (mood.smileLevel > 0) {
                        // Smile (bottom half of circle)
                        drawArc(
                            color = color,
                            startAngle = 90f - (sweepAngle / 2f),
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.2f, size.height * 0.15f),
                            size = Size(size.width * 0.6f, size.height * 0.6f),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    } else {
                        // Frown (top half of circle)
                        drawArc(
                            color = color,
                            startAngle = 270f - (sweepAngle / 2f),
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.2f, size.height * 0.5f),
                            size = Size(size.width * 0.6f, size.height * 0.6f),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = mood.label,
            style = MindfulnessTheme.typography.bodySmall,
            color = if (isSelected) Color.White else MindfulnessTheme.typography.bodySmall.color
        )
    }
}
