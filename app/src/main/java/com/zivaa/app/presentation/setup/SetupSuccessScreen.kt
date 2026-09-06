package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun SetupSuccessScreen(
    state: SetupState,
    onNavigateToDashboard: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ZivaaTheme.colors.sage // Dark Green background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = ZivaaTheme.spacing.padScreen)
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Top Graphic (Sun, Moon, Z Circle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dotted Arc
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val arcWidth = size.width * 0.75f
                        val arcHeight = size.height * 1.5f
                        val left = (size.width - arcWidth) / 2f
                        val top = size.height * 0.2f
                        drawArc(
                            color = Color.White.copy(alpha = 0.3f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(left, top),
                            size = Size(arcWidth, arcHeight),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f))
                            )
                        )
                        
                        // Sun Icon Placeholder (Left)
                        drawCircle(
                            color = Color.Transparent,
                            radius = 12.dp.toPx(),
                            center = Offset(left, top + arcHeight / 2)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = 8.dp.toPx(),
                            center = Offset(left, top + arcHeight / 2),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Sun Rays
                        for (i in 0..7) {
                            val angle = Math.toRadians((i * 45).toDouble())
                            val r1 = 12.dp.toPx()
                            val r2 = 16.dp.toPx()
                            val cx = left
                            val cy = top + arcHeight / 2
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(cx + (r1 * Math.cos(angle)).toFloat(), cy + (r1 * Math.sin(angle)).toFloat()),
                                end = Offset(cx + (r2 * Math.cos(angle)).toFloat(), cy + (r2 * Math.sin(angle)).toFloat()),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                        
                        // Moon Icon Placeholder (Right)
                        val moonCenter = Offset(left + arcWidth, top + arcHeight / 2)
                        drawCircle(
                            color = Color.Transparent,
                            radius = 12.dp.toPx(),
                            center = moonCenter
                        )
                        drawArc(
                            color = Color.White.copy(alpha = 0.5f),
                            startAngle = 90f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(moonCenter.x - 10.dp.toPx(), moonCenter.y - 10.dp.toPx()),
                            size = Size(20.dp.toPx(), 20.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Center Z Circle
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = ZivaaTheme.colors.bgElev
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Z",
                                style = ZivaaTheme.typography.displayLarge.copy(fontStyle = FontStyle.Italic),
                                color = ZivaaTheme.colors.ink,
                                fontSize = 32.sp
                            )
                        }
                    }
                    
                    // Outer faint dashed circles around Z
                    Canvas(modifier = Modifier.size(100.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = 42.dp.toPx(),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                            )
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = 50.dp.toPx(),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Header Section
                val name = if (state.name.isNotBlank()) state.name else "Ranjit"
                Text(
                    text = "ALL SET, ${name.uppercase()}",
                    style = ZivaaTheme.typography.eyebrow,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Your Zivaa is\n")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append("ready.")
                        }
                    },
                    style = ZivaaTheme.typography.displayLarge,
                    color = ZivaaTheme.colors.bgElev,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "From tomorrow morning, a calm, careful\nupdate — shaped around exactly what\nyou told us.",
                    style = ZivaaTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Checklist Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    // Item 1
                    ChecklistItem("Google Account connected")

                    // Item 2
                    val conditionsText = if (state.selectedConditions.isEmpty()) {
                        "No conditions to watch — lovely"
                    } else {
                        "${state.selectedConditions.size} condition${if(state.selectedConditions.size > 1) "s" else ""} to watch"
                    }
                    ChecklistItem(conditionsText)

                    // Item 3
                    val wearableText = if (state.selectedWearable != "I'll connect later" && state.selectedWearable.isNotBlank()) {
                        "${state.selectedWearable} connected"
                    } else {
                        "No wearable connected"
                    }
                    ChecklistItem(wearableText)

                    // Item 4
                    val circleCount = state.familyMembers.size
                    if (circleCount > 0) {
                        val namesList = state.familyMembers.map { it.name.split(" ").first() }.toMutableList()
                        val namesStr = namesList.joinToString(", ")
                        ChecklistItem("$circleCount in your family circle • $namesStr")
                    } else {
                        ChecklistItem("0 in your family circle")
                    }
                }
            }

            // Bottom Primary Button (Light background, Dark text)
            Surface(
                onClick = {
                    if (!state.isSubmitting) {
                        onNavigateToDashboard()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                color = if (state.isSubmitting) ZivaaTheme.colors.bgElev.copy(alpha = 0.6f) else ZivaaTheme.colors.bgElev
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isSubmitting) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ZivaaTheme.colors.sage,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Saving your plan...",
                            style = ZivaaTheme.typography.bodyLarge,
                            color = ZivaaTheme.colors.sage
                        )
                    } else {
                        Text(
                            text = "Step into your first morning",
                            style = ZivaaTheme.typography.bodyLarge,
                            color = ZivaaTheme.colors.sage
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = "Forward",
                            tint = ZivaaTheme.colors.sage,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = Color.White.copy(alpha = 0.9f),
                    style = ZivaaTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChecklistItem(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = ZivaaTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}
