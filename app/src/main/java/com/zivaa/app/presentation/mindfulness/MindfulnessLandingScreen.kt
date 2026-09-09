package com.zivaa.app.presentation.mindfulness

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.zivaa.app.presentation.mindfulness.components.GlowingOrb
import com.zivaa.app.presentation.mindfulness.components.MindfulnessCard
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessAccent
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

private enum class MindfulnessTimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT
}

private data class MindfulnessGreeting(
    val prefix: String,
    val accentWord: String,
    val subtitle: String
)

@Composable
fun MindfulnessLandingScreen(
    userName: String = "",
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToMeditation: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentHour = remember { LocalTime.now().hour }
    val timeOfDay = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> MindfulnessTimeOfDay.MORNING
            in 12..16 -> MindfulnessTimeOfDay.AFTERNOON
            in 17..21 -> MindfulnessTimeOfDay.EVENING
            else -> MindfulnessTimeOfDay.NIGHT
        }
    }

    val dayOfWeek = remember {
        LocalDate.now().dayOfWeek.getDisplayName(
            TextStyle.FULL,
            Locale.getDefault()
        )
    }

    val periodLabel = when (timeOfDay) {
        MindfulnessTimeOfDay.MORNING -> "Morning"
        MindfulnessTimeOfDay.AFTERNOON -> "Afternoon"
        MindfulnessTimeOfDay.EVENING -> "Evening"
        MindfulnessTimeOfDay.NIGHT -> "Night"
    }

    val eyebrowText = "Mindfulness • $dayOfWeek $periodLabel"

    val resolvedName = remember(userName) {
        if (userName.isNotBlank()) {
            userName.trim()
        } else {
            try {
                val profile = com.zivaa.app.data.remote.AuthManager.getInstance(context).getPatientProfile()
                profile["full_name"]?.split(" ")?.firstOrNull()?.trim().orEmpty()
            } catch (e: Exception) {
                ""
            }
        }
    }

    val nameSuffix = if (resolvedName.isNotEmpty()) ", $resolvedName." else "."

    val greeting = remember(timeOfDay) {
        val dayOfYear = LocalDate.now().dayOfYear
        val variations = when (timeOfDay) {
            MindfulnessTimeOfDay.MORNING -> listOf(
                MindfulnessGreeting(
                    prefix = "The morning is\n",
                    accentWord = "yours",
                    subtitle = "A few unhurried minutes to start the day. Nothing to\nachieve — just arrive."
                ),
                MindfulnessGreeting(
                    prefix = "Begin your day in\n",
                    accentWord = "stillness",
                    subtitle = "Set a calm foundation before the hours unfold.\nNothing to achieve — just arrive."
                ),
                MindfulnessGreeting(
                    prefix = "A peaceful morning\n",
                    accentWord = "awaits",
                    subtitle = "Take a quiet moment to breathe and center yourself\nfor the day ahead."
                )
            )
            MindfulnessTimeOfDay.AFTERNOON -> listOf(
                MindfulnessGreeting(
                    prefix = "The afternoon is\n",
                    accentWord = "yours",
                    subtitle = "A gentle pause in the middle of your day. Nothing to\nachieve — just arrive."
                ),
                MindfulnessGreeting(
                    prefix = "Pause and take a\n",
                    accentWord = "breath",
                    subtitle = "Step away from the rush for a few unhurried minutes\nof quiet."
                ),
                MindfulnessGreeting(
                    prefix = "Find your midday\n",
                    accentWord = "calm",
                    subtitle = "Clear the mind and recharge your spirit for the\nrest of today."
                )
            )
            MindfulnessTimeOfDay.EVENING -> listOf(
                MindfulnessGreeting(
                    prefix = "The evening is\n",
                    accentWord = "yours",
                    subtitle = "A few unhurried minutes for the mind. Nothing to\nachieve — just arrive."
                ),
                MindfulnessGreeting(
                    prefix = "Unwind into the\n",
                    accentWord = "evening",
                    subtitle = "Let the day go softly. Settle into the quiet of\nthe coming night."
                ),
                MindfulnessGreeting(
                    prefix = "Time to gently\n",
                    accentWord = "slow down",
                    subtitle = "Release whatever was carried today. Nothing to\nachieve — just arrive."
                )
            )
            MindfulnessTimeOfDay.NIGHT -> listOf(
                MindfulnessGreeting(
                    prefix = "The night is\n",
                    accentWord = "yours",
                    subtitle = "A quiet space to rest the mind and ease into\npeaceful sleep."
                ),
                MindfulnessGreeting(
                    prefix = "Rest easy tonight,\n",
                    accentWord = "peaceful",
                    subtitle = "Let thoughts drift away like clouds. Nothing left\nto do tonight."
                ),
                MindfulnessGreeting(
                    prefix = "Settle into the\n",
                    accentWord = "quiet",
                    subtitle = "Soft, gentle moments to welcome stillness and\ndeep rest."
                )
            )
        }
        variations[(dayOfYear + currentHour) % variations.size]
    }

    val breathingSubtitle = when (timeOfDay) {
        MindfulnessTimeOfDay.MORNING -> "Four ways to slow the breath — pick what suits this morning."
        MindfulnessTimeOfDay.AFTERNOON -> "Four ways to slow the breath — pick what suits this afternoon."
        else -> "Four ways to slow the breath — pick what suits tonight."
    }

    Scaffold(
        containerColor = Color(0xFF0D0F1C),
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF4C3B7D).copy(alpha = 0.55f), Color.Transparent),
                            center = Offset(size.width * 0.3f, size.height * 0.2f),
                            radius = size.width * 0.8f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF7C4A63).copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.7f),
                            radius = size.width * 0.8f
                        )
                    )
                    // Stars
                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 2.dp.toPx(), center = Offset(size.width * 0.15f, size.height * 0.15f))
                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 1.5.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.2f))
                    drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 1.5.dp.toPx(), center = Offset(size.width * 0.35f, size.height * 0.3f))
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = eyebrowText,
                    style = MindfulnessTheme.typography.eyebrow.copy(letterSpacing = 0.2.em),
                    color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append(greeting.prefix)
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = MindfulnessAccent)) {
                            append(greeting.accentWord)
                        }
                        append(nameSuffix)
                    },
                    style = MindfulnessTheme.typography.titleLargeSerif,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = greeting.subtitle,
                    style = MindfulnessTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MindfulnessTheme.typography.bodyMedium.color.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                GlowingOrb(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterHorizontally),
                    isPulsing = false
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Breathing Card
                MindfulnessCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToBreathing,
                    backgroundBrush = Brush.horizontalGradient(
                        listOf(Color(0xFF211D2B).copy(alpha = 0.8f), Color(0xFF3B2E28).copy(alpha = 0.8f))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BreathingCardIcon()

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Breathing",
                                style = MindfulnessTheme.typography.cardTitleSerif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = breathingSubtitle,
                                style = MindfulnessTheme.typography.bodySmall,
                                color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meditation Card
                MindfulnessCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToMeditation,
                    backgroundBrush = Brush.horizontalGradient(
                        listOf(Color(0xFF211D2B).copy(alpha = 0.8f), Color(0xFF332040).copy(alpha = 0.8f))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MeditationCardIcon()

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Meditation",
                                style = MindfulnessTheme.typography.cardTitleSerif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sit with sound — tanpura, flute, rain — with or without a voice.",
                                style = MindfulnessTheme.typography.bodySmall,
                                color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Even two minutes counts. The mind, like the garden, likes to be visited daily.",
                    style = MindfulnessTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }
}

@Composable
private fun BreathingCardIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathingPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(52.dp)
            .border(1.dp, Color(0xFFF2EDE4).copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val currentRadius = (size.minDimension / 2f) * scale
            // Ambient soft golden glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE0B273).copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.5f
                ),
                radius = currentRadius * 1.5f,
                center = center
            )
            // Luminous golden core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDF7EA),
                        Color(0xFFE0B273),
                        Color(0xFFC8934A)
                    ),
                    center = Offset(center.x - currentRadius * 0.25f, center.y - currentRadius * 0.3f),
                    radius = currentRadius
                ),
                radius = currentRadius,
                center = center
            )
        }
    }
}

@Composable
private fun MeditationCardIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFA99FD6)
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .border(1.dp, Color(0xFFF2EDE4).copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val scale = w / 24f

            val outerMoon = Path().apply {
                addOval(Rect(2f * scale, 2f * scale, 22f * scale, 22f * scale))
            }
            val innerCut = Path().apply {
                addOval(Rect(6.5f * scale, -1.5f * scale, 22.5f * scale, 14.5f * scale))
            }
            val crescent = Path().apply {
                op(outerMoon, innerCut, PathOperation.Difference)
            }
            // Gentle ambient lavender glow
            drawPath(
                path = crescent,
                color = tint.copy(alpha = 0.25f),
                style = Stroke(
                    width = 3.5f * scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            // Crisp crescent moon stroke
            drawPath(
                path = crescent,
                color = tint,
                style = Stroke(
                    width = 1.65f * scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
