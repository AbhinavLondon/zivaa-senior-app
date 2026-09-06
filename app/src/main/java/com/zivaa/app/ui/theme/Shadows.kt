package com.zivaa.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.zivaaShadow(
    color: Color = Color(0x0F234B3F), // Sage at ~6% opacity
    blurRadius: Dp = 24.dp,
    offsetY: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    cornerRadius: Dp = 22.dp
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = Color.Transparent.toArgb()
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            color.toArgb()
        )
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = paint
        )
    }
}

fun Modifier.zivaaHeroShadow(): Modifier = this.zivaaShadow(
    color = Color(0x29234B3F), // Sage at ~16% opacity
    blurRadius = 30.dp,
    offsetY = 12.dp
)
