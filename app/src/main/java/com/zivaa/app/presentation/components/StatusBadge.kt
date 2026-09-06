package com.zivaa.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class StatusBadgeVariant {
    OK,       // Leaf (Stable)
    WATCH,    // Amber (Observation) - pulsates
    ACT,      // Rose (Immediate alert)
    WARM,     // Clay (Care active)
    ON_ACCENT // Semi-transparent white on dark backgrounds
}

@Composable
fun StatusBadge(
    text: String,
    variant: StatusBadgeVariant,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default
) {
    val leaf = Color(0xFF5D7A4E)
    val amber = Color(0xFFC98A3A)
    val rose = Color(0xFFA4493D)
    val clay = Color(0xFFB6643D)
    val cream = Color(0xFFF6F3EE)
    
    val (bgColor, contentColor) = when (variant) {
        StatusBadgeVariant.OK -> leaf.copy(alpha = 0.15f) to leaf
        StatusBadgeVariant.WATCH -> amber.copy(alpha = 0.15f) to amber
        StatusBadgeVariant.ACT -> rose.copy(alpha = 0.15f) to rose
        StatusBadgeVariant.WARM -> clay.copy(alpha = 0.15f) to clay
        StatusBadgeVariant.ON_ACCENT -> Color.White.copy(alpha = 0.2f) to cream
    }

    val pulsateAlpha by if (variant == StatusBadgeVariant.WATCH) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else {
        androidx.compose.runtime.mutableStateOf(1f)
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(start = 9.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .alpha(pulsateAlpha)
                .clip(CircleShape)
                .background(contentColor)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            style = textStyle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            letterSpacing = 0.6.sp
        )
    }
}
