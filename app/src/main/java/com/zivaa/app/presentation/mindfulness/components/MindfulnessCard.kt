package com.zivaa.app.presentation.mindfulness.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessDivider

@Composable
fun MindfulnessCard(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    contentPadding: Dp = 24.dp,
    backgroundBrush: Brush = SolidColor(Color.White.copy(alpha = 0.05f)),
    borderAlpha: Float = 0.1f,
    content: @Composable BoxScope.() -> Unit
) {
    val borderStroke = BorderStroke(1.dp, Color.White.copy(alpha = borderAlpha))
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceModifier = modifier
        .clip(shape)
        .background(backgroundBrush)
        .border(borderStroke, shape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(
        modifier = surfaceModifier
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
