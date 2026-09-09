package com.zivaa.app.presentation.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import kotlin.math.roundToInt

@Composable
fun WeightCard(
    modifier: Modifier = Modifier,
    tone: PlanSetupTone,
    icon: ImageVector,
    eyebrow: String,
    weightKg: Int?, // null means not answered, -1 can mean "Rather not say"
    onWeightChanged: (Int?) -> Unit,
    goalWeightKg: Int? = null,
    onGoalWeightChanged: (Int?) -> Unit = {},
    stepNumber: Int? = null,
    totalSteps: Int? = null
) {
    val isAnswered = weightKg != null
    val isPrivate = weightKg == -1
    
    val borderColor = if (isAnswered) tone.bg.copy(alpha = 0.35f) else PlanSetupTheme.Line
    val borderWidth = if (isAnswered) 1.dp else 0.5.dp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(PlanSetupTheme.BgElev)
            .border(borderWidth, borderColor, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (stepNumber != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isAnswered) tone.bg.copy(alpha = 0.15f) else PlanSetupTheme.LineStrong.copy(alpha = 0.35f))
                                .border(
                                    0.5.dp,
                                    if (isAnswered) tone.bg.copy(alpha = 0.45f) else PlanSetupTheme.Line,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = "STEP $stepNumber" + (totalSteps?.let { " OF $it" } ?: ""),
                                fontFamily = Manrope,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = if (isAnswered) tone.bg else PlanSetupTheme.InkMute,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Text(
                            text = eyebrow.toEyebrowTitleCase(),
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            letterSpacing = 0.2.sp,
                            color = PlanSetupTheme.Eyebrow,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    if (isAnswered) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PlanSetupTheme.Leaf.copy(alpha = 0.12f))
                                .border(0.5.dp, PlanSetupTheme.Leaf.copy(alpha = 0.35f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PlanSetupTheme.Leaf,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (isPrivate) "Private" else "Answered",
                                fontFamily = Manrope,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlanSetupTheme.Leaf
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icon Bubble
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tone.bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tone.fg,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Text Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 1.dp)
                ) {
                    if (stepNumber == null) {
                        Text(
                            text = eyebrow.toEyebrowTitleCase(),
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            letterSpacing = 0.2.sp,
                            color = PlanSetupTheme.Eyebrow,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    
                    Text(
                        text = "And roughly, your weight?",
                        fontFamily = Manrope,
                        fontSize = 21.sp,
                        lineHeight = 23.sp,
                        letterSpacing = (-0.1).sp,
                        color = PlanSetupTheme.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(5.dp))
                    
                    Text(
                        text = "Slide the tape \u2014 roughly is perfect. It only shapes portions and pace.",
                        fontFamily = Manrope,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = PlanSetupTheme.InkSoft,
                        fontWeight = FontWeight.Normal
                    )
                }
                
                if (isAnswered && stepNumber == null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(tone.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = tone.fg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "YOUR WEIGHT",
                    fontFamily = Manrope,
                    fontSize = 10.sp,
                    letterSpacing = 0.7.sp,
                    color = PlanSetupTheme.InkMute,
                    fontWeight = FontWeight.SemiBold
                )
                
                val weightLabel = when {
                    weightKg == null -> "\u2014"
                    weightKg == -1 -> "Private"
                    else -> "$weightKg kg"
                }
                
                Text(
                    text = weightLabel,
                    fontFamily = Manrope,
                    fontSize = 34.sp,
                    lineHeight = 34.sp,
                    letterSpacing = (-0.1).sp,
                    color = PlanSetupTheme.Ink,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            WeightSlider(
                weightKg = if (isPrivate) null else weightKg,
                isPrivate = isPrivate,
                onWeightChanged = { w -> onWeightChanged(w) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "40 KG",
                    fontFamily = Manrope,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.5.sp,
                    color = PlanSetupTheme.InkMute
                )
                
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(CircleShape)
                        .background(if (isPrivate) PlanSetupTheme.Clay else Color.Transparent)
                        .border(
                            1.dp,
                            if (isPrivate) PlanSetupTheme.Clay else PlanSetupTheme.LineStrong,
                            CircleShape
                        )
                        .clickable {
                            onWeightChanged(if (isPrivate) null else -1)
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Rather not say",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPrivate) Color.White else PlanSetupTheme.InkSoft
                    )
                }
                
                Text(
                    text = "180 KG",
                    fontFamily = Manrope,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.5.sp,
                    color = PlanSetupTheme.InkMute
                )
            }
            
            if (isAnswered && !isPrivate) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(PlanSetupTheme.Line)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "A GOAL IN MIND? - OPTIONAL",
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        letterSpacing = 0.7.sp,
                        color = PlanSetupTheme.InkMute,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color.Transparent)
                            .border(1.dp, PlanSetupTheme.LineStrong, RoundedCornerShape(13.dp))
                            .clickable { onGoalWeightChanged(null) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Clear",
                            fontFamily = Manrope,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = PlanSetupTheme.InkSoft
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(18.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minus button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PlanSetupTheme.Bg)
                            .border(1.dp, PlanSetupTheme.LineStrong, CircleShape)
                            .clickable {
                                val currentGoal = goalWeightKg ?: weightKg!!
                                onGoalWeightChanged(currentGoal - 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "−", // minus sign
                            fontFamily = Manrope,
                            fontSize = 20.sp,
                            color = PlanSetupTheme.Ink,
                            modifier = Modifier.padding(bottom = 2.dp) // Optical center adjustment
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val currentGoal = goalWeightKg ?: weightKg!!
                        Text(
                            text = "${currentGoal} kg",
                            fontFamily = Manrope,
                            fontSize = 28.sp,
                            lineHeight = 28.sp,
                            letterSpacing = (-0.1).sp,
                            color = PlanSetupTheme.Ink
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val diff = weightKg!! - currentGoal
                        val diffText = when {
                            diff > 0 -> "$diff KG LIGHTER THAN TODAY"
                            diff < 0 -> "${-diff} KG HEAVIER THAN TODAY"
                            else -> "SAME AS TODAY"
                        }
                        
                        Text(
                            text = diffText,
                            fontFamily = Manrope,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
                            color = PlanSetupTheme.InkMute,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Plus button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PlanSetupTheme.Bg)
                            .border(1.dp, PlanSetupTheme.LineStrong, CircleShape)
                            .clickable {
                                val currentGoal = goalWeightKg ?: weightKg!!
                                onGoalWeightChanged(currentGoal + 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontFamily = Manrope,
                            fontSize = 20.sp,
                            color = PlanSetupTheme.Ink,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeightSlider(
    modifier: Modifier = Modifier,
    weightKg: Int?,
    isPrivate: Boolean,
    onWeightChanged: (Int) -> Unit
) {
    val minWeight = 40f
    val maxWeight = 180f
    
    // Internal state for smooth scrolling
    var currentWeight by remember { mutableStateOf(weightKg?.toFloat() ?: 65f) }
    
    // Sync external state if changed outside (except private state where we keep the visual tape as is)
    LaunchedEffect(weightKg) {
        if (weightKg != null && weightKg != -1) {
            currentWeight = weightKg.toFloat()
        }
    }
    
    val alpha = if (isPrivate) 0.35f else 1f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, PlanSetupTheme.LineStrong, RoundedCornerShape(14.dp))
            .background(PlanSetupTheme.Bg)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // dragAmount.x is negative when dragging left, positive when dragging right
                    // scrolling left means higher weight, right means lower weight
                    // 10 pixels = 1 kg
                    val newWeight = currentWeight - dragAmount.x / 10f
                    currentWeight = newWeight.coerceIn(minWeight, maxWeight)
                    onWeightChanged(currentWeight.roundToInt())
                }
            }
    ) {
        val smallLineColor = PlanSetupTheme.LineStrong
        val bigLineColor = PlanSetupTheme.InkMute.copy(alpha = 0.38f)
        
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
            val spacing = 10.dp.toPx() // 10 pixels per kg
            
            // Draw lines from minWeight to maxWeight
            for (i in 0..((maxWeight - minWeight).toInt())) {
                val weight = minWeight + i
                val xOffset = (weight - currentWeight) * spacing
                val xPos = size.width / 2f + xOffset
                
                // Only draw if within bounds
                if (xPos >= 0 && xPos <= size.width) {
                    val isBig = i % 5 == 0
                    val color = if (isBig) bigLineColor else smallLineColor
                    val lineH = if (isBig) size.height * 0.68f else size.height * 0.40f
                    val strokeW = if (isBig) 1.5.dp.toPx() else 1.dp.toPx()
                    
                    drawLine(
                        color = color.copy(alpha = color.alpha * alpha),
                        start = Offset(xPos, size.height),
                        end = Offset(xPos, size.height - lineH),
                        strokeWidth = strokeW
                    )
                }
            }
        }
        
        // Needle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .fillMaxHeight()
                .padding(vertical = 6.dp)
                .shadow(elevation = 0.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PlanSetupTheme.Clay)
        )
    }
}
