package com.zivaa.app.presentation.mindfulness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.mindfulness.components.MindfulnessCard
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessAccent
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme

data class BreathingRhythm(
    val id: String,
    val title: String,
    val iconText: String,
    val instructions: String,
    val description: String,
    val tag: String,
    val pathPoints: List<Float> // Simple normalized points 0..1 for the line graph
)

val breathingOptions = listOf(
    BreathingRhythm("box", "Box", "44", "In 4 • hold 4 • out 4 • hold 4.", "Steadying, like a square drawn slowly.", "STEADY", listOf(0f, 1f, 1f, 0f, 0f)),
    BreathingRhythm("exhale", "Long Exhale", "48", "In for 4, out for 8.", "The quickest way to calm a racing evening.", "CALMING", listOf(0f, 1f, 0f)),
    BreathingRhythm("equal", "Equal", "55", "In 5, out 5 — a gentle, even tide.", "Good for beginners.", "GENTLE", listOf(0f, 1f, 0f)),
    BreathingRhythm("custom", "Custom", "?", "Set your own in and out.", "Your breath, your rules.", "YOURS", listOf(0f, 0.5f, 1f, 0f))
)

@Composable
fun BreathingSelectionScreen(
    onBack: () -> Unit = {},
    onRhythmSelected: (BreathingRhythm) -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(
                top = 40.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.2f), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "BREATHING",
                        style = MindfulnessTheme.typography.eyebrow,
                        color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Choose tonight's ")
                        withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MindfulnessAccent)) {
                            append("rhythm")
                        }
                        append(".")
                    },
                    style = MindfulnessTheme.typography.titleLargeSerif
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "All of them slow the heart. The best one is the one that\nfeels easy.",
                    style = MindfulnessTheme.typography.bodyMedium,
                    color = MindfulnessTheme.typography.bodyMedium.color.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            items(breathingOptions) { rhythm ->
                MindfulnessCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = { onRhythmSelected(rhythm) }
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MindfulnessAccent.copy(alpha = 0.5f), CircleShape)
                                    .background(MindfulnessAccent.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rhythm.iconText,
                                    style = MindfulnessTheme.typography.cardTitleSerif,
                                    color = MindfulnessAccent,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rhythm.title,
                                    style = MindfulnessTheme.typography.cardTitleSerif
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rhythm.instructions,
                                    style = MindfulnessTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = rhythm.description,
                                    style = MindfulnessTheme.typography.bodySmall,
                                    color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.7f)
                                )
                            }
                            
                            Text(
                                text = rhythm.tag,
                                style = MindfulnessTheme.typography.eyebrow,
                                color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Path visualization
                        Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                            val width = size.width
                            val height = size.height
                            val segmentWidth = width / (rhythm.pathPoints.size - 1)
                            
                            for (i in 0 until rhythm.pathPoints.size - 1) {
                                val startX = i * segmentWidth
                                val startY = height - (rhythm.pathPoints[i] * height)
                                val endX = (i + 1) * segmentWidth
                                val endY = height - (rhythm.pathPoints[i + 1] * height)
                                
                                drawLine(
                                    color = MindfulnessAccent.copy(alpha = 0.6f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
