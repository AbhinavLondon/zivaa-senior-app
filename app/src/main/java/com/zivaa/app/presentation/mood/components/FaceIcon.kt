package com.zivaa.app.presentation.mood.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FaceIcon(
    moodIndex: Int, // 0 = wonderful, 4 = very low
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.Black,
    strokeWidth: Dp = 1.8.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidthPx = strokeWidth.toPx()
        
        // Scale factor assuming 24x24 viewBox
        val scaleX: Float = this.size.width / 24f
        val scaleY: Float = this.size.height / 24f

        // Draw left eye (cx=9, cy=10, r=0.7) -> scaled up slightly to make it visible
        drawCircle(
            color = color,
            radius = 1.5f * scaleX, // slightly larger for visibility
            center = Offset(9f * scaleX, 10f * scaleY)
        )
        // Draw right eye (cx=15, cy=10, r=0.7)
        drawCircle(
            color = color,
            radius = 1.5f * scaleX,
            center = Offset(15f * scaleX, 10f * scaleY)
        )

        // Draw mouth path
        val path = Path()
        when (moodIndex.coerceIn(0, 4)) {
            0 -> {
                // M8 13.3 Q12 18.7 16 13.3
                path.moveTo(8f * scaleX, 13.3f * scaleY)
                path.quadraticBezierTo(12f * scaleX, 18.7f * scaleY, 16f * scaleX, 13.3f * scaleY)
            }
            1 -> {
                // M8 14 Q12 17 16 14
                path.moveTo(8f * scaleX, 14f * scaleY)
                path.quadraticBezierTo(12f * scaleX, 17f * scaleY, 16f * scaleX, 14f * scaleY)
            }
            2 -> {
                // M8 15 L16 15
                path.moveTo(8f * scaleX, 15f * scaleY)
                path.lineTo(16f * scaleX, 15f * scaleY)
            }
            3 -> {
                // M8 15.8 Q12 13.4 16 15.8
                path.moveTo(8f * scaleX, 15.8f * scaleY)
                path.quadraticBezierTo(12f * scaleX, 13.4f * scaleY, 16f * scaleX, 15.8f * scaleY)
            }
            4 -> {
                // M8 16.2 Q12 11.8 16 16.2
                path.moveTo(8f * scaleX, 16.2f * scaleY)
                path.quadraticBezierTo(12f * scaleX, 11.8f * scaleY, 16f * scaleX, 16.2f * scaleY)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
