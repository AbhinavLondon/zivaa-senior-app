package com.zivaa.app.presentation.setup.wearable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun WearableBrandIcon(
    brand: WearableBrand,
    tint: Color,
    modifier: Modifier = Modifier.size(26.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (brand) {
            WearableBrand.SAMSUNG -> {
                // Samsung Galaxy Watch: circular stainless case, two right-side pushers, straps & hands
                val strapW = w * 0.28f
                val strapH = h * 0.16f
                val strapX = (w - strapW) / 2f
                drawRoundRect(
                    color = tint.copy(alpha = 0.45f),
                    topLeft = Offset(strapX, 0f),
                    size = Size(strapW, strapH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = tint.copy(alpha = 0.45f),
                    topLeft = Offset(strapX, h - strapH),
                    size = Size(strapW, strapH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                val center = Offset(w * 0.47f, h * 0.5f)
                val radius = w * 0.36f
                drawCircle(
                    color = tint,
                    center = center,
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 2 right buttons (Home & Back key)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.84f, h * 0.30f),
                    size = Size(w * 0.08f, h * 0.13f),
                    cornerRadius = CornerRadius(1.5.dp.toPx())
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.84f, h * 0.57f),
                    size = Size(w * 0.08f, h * 0.13f),
                    cornerRadius = CornerRadius(1.5.dp.toPx())
                )

                // Watch hands
                drawCircle(color = tint, center = center, radius = 1.8.dp.toPx())
                drawLine(
                    color = tint,
                    start = center,
                    end = Offset(center.x, center.y - radius * 0.55f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = center,
                    end = Offset(center.x + radius * 0.48f, center.y + radius * 0.12f),
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            WearableBrand.OURA -> {
                // Oura Ring: Smart Ring with flat horizon top and 3 inner PPG sensor nodules
                val center = Offset(w * 0.5f, h * 0.5f)
                val rOuter = w * 0.38f
                drawCircle(
                    color = tint,
                    center = center,
                    radius = rOuter,
                    style = Stroke(width = 3.6.dp.toPx())
                )

                // Flat top plateau (signature Oura design)
                drawLine(
                    color = tint,
                    start = Offset(center.x - 5.5.dp.toPx(), center.y - rOuter),
                    end = Offset(center.x + 5.5.dp.toPx(), center.y - rOuter),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 3 inner sensor nodes on bottom inner curve
                val sensorR = 1.2.dp.toPx()
                val innerY = center.y + rOuter - 4.dp.toPx()
                drawCircle(color = tint, center = Offset(center.x, innerY), radius = sensorR * 1.2f)
                drawCircle(color = tint, center = Offset(center.x - 4.5.dp.toPx(), innerY - 0.8.dp.toPx()), radius = sensorR)
                drawCircle(color = tint, center = Offset(center.x + 4.5.dp.toPx(), innerY - 0.8.dp.toPx()), radius = sensorR)
            }

            WearableBrand.ULTRAHUMAN -> {
                // Ultrahuman Ring AIR: Sculpted ring with continuous biometric ECG/PPG pulse line
                val center = Offset(w * 0.5f, h * 0.5f)
                val rOuter = w * 0.38f
                drawCircle(
                    color = tint,
                    center = center,
                    radius = rOuter,
                    style = Stroke(width = 2.8.dp.toPx())
                )
                drawCircle(
                    color = tint.copy(alpha = 0.25f),
                    center = center,
                    radius = rOuter - 3.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Biometric pulse wave through the ring center
                val pulsePath = Path().apply {
                    moveTo(center.x - rOuter * 0.65f, center.y)
                    lineTo(center.x - rOuter * 0.30f, center.y)
                    lineTo(center.x - rOuter * 0.15f, center.y + 3.dp.toPx())
                    lineTo(center.x + rOuter * 0.05f, center.y - 6.dp.toPx())
                    lineTo(center.x + rOuter * 0.25f, center.y + 6.dp.toPx())
                    lineTo(center.x + rOuter * 0.40f, center.y)
                    lineTo(center.x + rOuter * 0.65f, center.y)
                }
                drawPath(
                    path = pulsePath,
                    color = tint,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 2 sensor dots at bottom inner rim
                drawCircle(color = tint, center = Offset(center.x - 2.5.dp.toPx(), center.y + rOuter - 3.2.dp.toPx()), radius = 1.1.dp.toPx())
                drawCircle(color = tint, center = Offset(center.x + 2.5.dp.toPx(), center.y + rOuter - 3.2.dp.toPx()), radius = 1.1.dp.toPx())
            }

            WearableBrand.FITBIT -> {
                // Fitbit: Iconic 13-dot diamond matrix constellation
                val center = Offset(w * 0.5f, h * 0.5f)
                val spacing = 4.2.dp.toPx()
                val dotCols = listOf(
                    -2 to listOf(0),
                    -1 to listOf(-1, 0, 1),
                    0 to listOf(-2, -1, 0, 1, 2),
                    1 to listOf(-1, 0, 1),
                    2 to listOf(0)
                )
                for ((col, rows) in dotCols) {
                    val x = center.x + col * spacing
                    val baseR = when (Math.abs(col)) {
                        0 -> 1.7.dp.toPx()
                        1 -> 1.4.dp.toPx()
                        else -> 1.1.dp.toPx()
                    }
                    for (row in rows) {
                        val y = center.y + row * spacing
                        val r = if (row == 0 && col == 0) baseR * 1.15f else baseR
                        drawCircle(color = tint, center = Offset(x, y), radius = r)
                    }
                }
            }

            WearableBrand.GABIT -> {
                // Gabit Smart Ring: Smart Ring with stylized "G" health arc inside
                val center = Offset(w * 0.5f, h * 0.5f)
                val rOuter = w * 0.38f
                drawCircle(
                    color = tint,
                    center = center,
                    radius = rOuter,
                    style = Stroke(width = 3.2.dp.toPx())
                )

                val gPath = Path().apply {
                    arcTo(
                        rect = Rect(
                            left = center.x - rOuter * 0.52f,
                            top = center.y - rOuter * 0.52f,
                            right = center.x + rOuter * 0.52f,
                            bottom = center.y + rOuter * 0.52f
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -270f,
                        forceMoveTo = false
                    )
                    lineTo(center.x, center.y)
                }
                drawPath(
                    path = gPath,
                    color = tint,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )

                drawCircle(color = tint, center = Offset(center.x - 3.dp.toPx(), center.y + rOuter - 3.5.dp.toPx()), radius = 1.dp.toPx())
                drawCircle(color = tint, center = Offset(center.x + 3.dp.toPx(), center.y + rOuter - 3.5.dp.toPx()), radius = 1.dp.toPx())
            }

            WearableBrand.GARMIN -> {
                // Garmin: Rugged outdoor watch bezel with 5 pushers & the iconic Garmin Delta
                val center = Offset(w * 0.5f, h * 0.5f)
                val rOuter = w * 0.38f
                drawCircle(
                    color = tint,
                    center = center,
                    radius = rOuter,
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // 3 pushers left (Light, Up, Down)
                drawRoundRect(color = tint, topLeft = Offset(w * 0.07f, h * 0.28f), size = Size(w * 0.06f, h * 0.09f), cornerRadius = CornerRadius(1.dp.toPx()))
                drawRoundRect(color = tint, topLeft = Offset(w * 0.05f, h * 0.46f), size = Size(w * 0.06f, h * 0.09f), cornerRadius = CornerRadius(1.dp.toPx()))
                drawRoundRect(color = tint, topLeft = Offset(w * 0.07f, h * 0.64f), size = Size(w * 0.06f, h * 0.09f), cornerRadius = CornerRadius(1.dp.toPx()))
                // 2 pushers right (Start/Stop, Back)
                drawRoundRect(color = tint, topLeft = Offset(w * 0.87f, h * 0.32f), size = Size(w * 0.06f, h * 0.11f), cornerRadius = CornerRadius(1.dp.toPx()))
                drawRoundRect(color = tint, topLeft = Offset(w * 0.87f, h * 0.60f), size = Size(w * 0.06f, h * 0.09f), cornerRadius = CornerRadius(1.dp.toPx()))

                // Garmin Delta Arrowhead in center
                val delta = Path().apply {
                    moveTo(center.x + rOuter * 0.40f, center.y - rOuter * 0.38f)
                    lineTo(center.x - rOuter * 0.38f, center.y + rOuter * 0.36f)
                    lineTo(center.x - rOuter * 0.04f, center.y + rOuter * 0.10f)
                    close()
                }
                drawPath(path = delta, color = tint)
            }

            WearableBrand.INDIAN_BRANDS -> {
                // Noise / boAt / Amazfit: Modern squircle smartwatch with crown and activity pulse
                val strapW = w * 0.34f
                val strapH = h * 0.16f
                val strapX = (w - strapW) / 2f
                drawRoundRect(
                    color = tint.copy(alpha = 0.4f),
                    topLeft = Offset(strapX, 0f),
                    size = Size(strapW, strapH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = tint.copy(alpha = 0.4f),
                    topLeft = Offset(strapX, h - strapH),
                    size = Size(strapW, strapH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                val caseRect = Rect(w * 0.20f, h * 0.14f, w * 0.78f, h * 0.86f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(caseRect.left, caseRect.top),
                    size = Size(caseRect.width, caseRect.height),
                    cornerRadius = CornerRadius(5.dp.toPx()),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Rotating Crown
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.78f, h * 0.28f),
                    size = Size(w * 0.07f, h * 0.16f),
                    cornerRadius = CornerRadius(1.5.dp.toPx())
                )

                // Activity line on screen
                val screenCenterY = caseRect.top + caseRect.height * 0.5f
                val screenCenterX = caseRect.left + caseRect.width * 0.5f
                val pulse = Path().apply {
                    moveTo(caseRect.left + 3.dp.toPx(), screenCenterY)
                    lineTo(screenCenterX - 3.dp.toPx(), screenCenterY)
                    lineTo(screenCenterX, screenCenterY - 4.dp.toPx())
                    lineTo(screenCenterX + 3.dp.toPx(), screenCenterY + 4.dp.toPx())
                    lineTo(screenCenterX + 6.dp.toPx(), screenCenterY)
                    lineTo(caseRect.right - 3.dp.toPx(), screenCenterY)
                }
                drawPath(
                    path = pulse,
                    color = tint,
                    style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            WearableBrand.NONE -> {
                // Smartphone with walking footprints (step counter)
                val phoneRect = Rect(w * 0.26f, h * 0.06f, w * 0.74f, h * 0.94f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(phoneRect.left, phoneRect.top),
                    size = Size(phoneRect.width, phoneRect.height),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Punch-hole camera
                drawCircle(
                    color = tint,
                    center = Offset(phoneRect.left + phoneRect.width * 0.5f, phoneRect.top + 3.dp.toPx()),
                    radius = 1.dp.toPx()
                )

                // Footstep prints on screen
                val footL = Offset(phoneRect.left + phoneRect.width * 0.38f, phoneRect.top + phoneRect.height * 0.58f)
                val footR = Offset(phoneRect.left + phoneRect.width * 0.62f, phoneRect.top + phoneRect.height * 0.44f)
                drawOval(
                    color = tint,
                    topLeft = Offset(footL.x - 1.8.dp.toPx(), footL.y - 3.dp.toPx()),
                    size = Size(3.6.dp.toPx(), 6.dp.toPx())
                )
                drawOval(
                    color = tint,
                    topLeft = Offset(footR.x - 1.8.dp.toPx(), footR.y - 3.dp.toPx()),
                    size = Size(3.6.dp.toPx(), 6.dp.toPx())
                )
            }
        }
    }
}
