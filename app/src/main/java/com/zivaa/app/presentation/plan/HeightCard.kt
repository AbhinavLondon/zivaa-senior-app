package com.zivaa.app.presentation.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UnfoldMore
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
fun HeightCard(
    modifier: Modifier = Modifier,
    tone: PlanSetupTone,
    icon: ImageVector,
    eyebrow: String,
    heightInches: Int?,
    onHeightChanged: (Int) -> Unit
) {
    val isAnswered = heightInches != null
    val borderColor = if (isAnswered) tone.bg else PlanSetupTheme.Line
    val borderWidth = if (isAnswered) 1.dp else 0.5.dp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    Text(
                        text = eyebrow.toEyebrowTitleCase(),
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        letterSpacing = 0.2.sp,
                        color = Color(0xFF111111),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    
                    Text(
                        text = "Roughly how tall are you?",
                        fontFamily = Manrope,
                        fontSize = 21.sp,
                        lineHeight = 23.sp,
                        letterSpacing = (-0.1).sp,
                        color = PlanSetupTheme.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(5.dp))
                    
                    Text(
                        text = "Drag the marker up the wall \u2014 like the pencil marks at home.",
                        fontFamily = Manrope,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = PlanSetupTheme.InkSoft,
                        fontWeight = FontWeight.Normal
                    )
                }
                
                if (isAnswered) {
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
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeightSlider(
                    heightInches = heightInches,
                    onHeightChanged = onHeightChanged,
                    modifier = Modifier
                        .width(96.dp)
                        .height(220.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "YOUR HEIGHT",
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        letterSpacing = 0.7.sp,
                        color = PlanSetupTheme.InkMute,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    val heightLabel = if (heightInches == null) "\u2014" else {
                        val ft = heightInches / 12
                        val inc = heightInches % 12
                        "${ft}' ${inc}\""
                    }
                    Text(
                        text = heightLabel,
                        fontFamily = Manrope,
                        fontSize = 42.sp,
                        lineHeight = 42.sp,
                        letterSpacing = (-0.1).sp,
                        color = PlanSetupTheme.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val heightSub = if (heightInches == null) {
                        "Drag the marker to where you stand."
                    } else {
                        val cm = (heightInches * 2.54).roundToInt()
                        "about $cm cm \u2014 noted."
                    }
                    Text(
                        text = heightSub,
                        fontFamily = Manrope,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = PlanSetupTheme.InkSoft
                    )
                }
            }
        }
    }
}

@Composable
fun HeightSlider(
    modifier: Modifier = Modifier,
    heightInches: Int?,
    onHeightChanged: (Int) -> Unit
) {
    val minHeight = 56f
    val maxHeight = 76f
    
    val currentHeight = heightInches?.toFloat() ?: 64f
    val percentage = (currentHeight - minHeight) / (maxHeight - minHeight)
    
    var containerHeight by remember { mutableStateOf(0f) }
    
    fun updateHeight(yPos: Float) {
        if (containerHeight == 0f) return
        val clampedY = yPos.coerceIn(0f, containerHeight)
        val p = 1f - (clampedY / containerHeight)
        val newValue = minHeight + p * (maxHeight - minHeight)
        onHeightChanged(newValue.roundToInt().coerceIn(minHeight.toInt(), maxHeight.toInt()))
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, PlanSetupTheme.LineStrong, RoundedCornerShape(16.dp))
            .background(PlanSetupTheme.Bg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        containerHeight = size.height.toFloat()
                        updateHeight(offset.y)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    updateHeight(change.position.y)
                }
            }
    ) {
        val smallLineColor = PlanSetupTheme.LineStrong
        val bigLineColor = PlanSetupTheme.InkMute.copy(alpha = 0.34f)
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            containerHeight = size.height
            
            val totalInches = (maxHeight - minHeight).toInt()
            val spacing = size.height / totalInches
            
            for (i in 0..totalInches) {
                val y = size.height - (i * spacing)
                val isBig = i % 6 == 0
                val color = if (isBig) bigLineColor else smallLineColor
                val lineWidth = if (isBig) size.width * 0.34f else size.width * 0.20f
                val strokeW = if (isBig) 1.5.dp.toPx() else 1.dp.toPx()
                
                drawLine(
                    color = color,
                    start = Offset(size.width - lineWidth, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeW
                )
            }
        }
        
        Text(
            text = "6'4\"",
            modifier = Modifier.padding(start = 10.dp, top = 8.dp),
            fontFamily = Manrope,
            fontSize = 9.5.sp,
            letterSpacing = 0.4.sp,
            color = PlanSetupTheme.InkMute
        )
        Text(
            text = "4'8\"",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp),
            fontFamily = Manrope,
            fontSize = 9.5.sp,
            letterSpacing = 0.4.sp,
            color = PlanSetupTheme.InkMute
        )
        
        // Fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(percentage)
                .align(Alignment.BottomCenter)
                .background(PlanSetupTheme.SurfaceHero.copy(alpha = 0.10f)) // rgba(35,75,63,0.10)
        )
        
        // Handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = -(220.dp * percentage))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(PlanSetupTheme.Sage)
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 36.dp, height = 20.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(PlanSetupTheme.Sage),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null,
                    tint = PlanSetupTheme.SageInk,
                    modifier = Modifier.size(16.dp) // Adjusted slightly from 11 since we use standard icon
                )
            }
        }
    }
}
