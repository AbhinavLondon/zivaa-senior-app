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

import androidx.compose.foundation.border

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
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(22.dp))
            .padding(22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Left: Progress Ring
            CircularProgressRing(
                progress = progress,
                trackColor = colors.lineStrong,
                progressColor = colors.sage,
                strokeWidth = 10.dp,
                modifier = Modifier.size(110.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentCalories.toString(),
                        fontFamily = InstrumentSerif,
                        fontSize = 34.sp,
                        color = colors.textStrong
                    )
                    Text(
                        text = "OF $totalCalories KCAL",
                        style = typography.meta.copy(
                            fontSize = 9.5.sp,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(0.05f, androidx.compose.ui.unit.TextUnitType.Em)
                        ),
                        color = colors.textMeta
                    )
                }
            }
            
            // Right: Text content
            Column {
                Text(
                    text = title,
                    fontFamily = InstrumentSerif,
                    fontSize = 26.sp,
                    color = colors.textStrong
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = typography.bodySmall,
                    color = colors.textBody
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                // Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.sage.copy(alpha = 0.12f))
                        .border(0.5.dp, colors.sage.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.sage)) {
                                append("$remaining ")
                            }
                            withStyle(SpanStyle(color = colors.sage, fontWeight = FontWeight.SemiBold)) {
                                append("Kcal Left")
                            }
                        },
                        style = typography.meta.copy(
                            fontSize = 11.sp,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(0.04f, androidx.compose.ui.unit.TextUnitType.Em)
                        )
                    )
                }
            }
        }
    }
}
