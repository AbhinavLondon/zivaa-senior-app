package com.zivaa.app.presentation.nutrition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

@Composable
fun NutritionSummaryCard(
    currentCalories: Int,
    totalCalories: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    val remaining = (totalCalories - currentCalories).coerceAtLeast(0)
    val progress = if (totalCalories > 0) currentCalories.toFloat() / totalCalories else 0f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgElev)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left: Progress Ring
            CircularProgressRing(
                progress = progress,
                trackColor = colors.muted,
                progressColor = colors.sage,
                strokeWidth = 10.dp,
                modifier = Modifier.size(110.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentCalories.toString(),
                        fontFamily = InstrumentSerif,
                        fontSize = 32.sp,
                        color = colors.textStrong
                    )
                    Text(
                        text = "OF $totalCalories KCAL",
                        style = typography.meta,
                        color = colors.textMeta
                    )
                }
            }
            
            // Right: Text content
            Column {
                Text(
                    text = title,
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    color = colors.textStrong
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = typography.bodyMedium,
                    color = colors.textBody
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.sage.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF111111))) {
                                append("$remaining ")
                            }
                            withStyle(SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF111111), fontWeight = FontWeight.SemiBold)) {
                                append("Kcal Left")
                            }
                        },
                        style = typography.eyebrow
                    )
                }
            }
        }
    }
}
