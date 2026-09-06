package com.zivaa.app.presentation.mindfulness.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.mindfulness.theme.OrbCenter
import com.zivaa.app.presentation.mindfulness.theme.OrbEdge

@Composable
fun GlowingOrb(
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "orbPulse")

    val pulseRadius by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    val ringRadius by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRadius"
    )

    val ringAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width / 3f // Base radius of the inner orb

            // If pulsing, draw animated expanding rings
            if (isPulsing) {
                drawCircle(
                    color = Color.White.copy(alpha = ringAlpha),
                    radius = baseRadius * ringRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                
                // Secondary ring
                drawCircle(
                    color = Color.White.copy(alpha = ringAlpha * 0.5f),
                    radius = baseRadius * ringRadius * 0.8f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            val currentRadius = if (isPulsing) baseRadius * pulseRadius else baseRadius

            // Draw glowing shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrbEdge.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.5f
                ),
                radius = currentRadius * 1.5f,
                center = center
            )

            // Draw solid orb with gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrbCenter, OrbEdge),
                    center = center,
                    radius = currentRadius
                ),
                radius = currentRadius,
                center = center
            )
        }
        
        content()
    }
}
