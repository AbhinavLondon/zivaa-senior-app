package com.zivaa.app.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.Manrope

@Composable
fun PlanSetupHeroCard(
    modifier: Modifier = Modifier,
    totalQuestions: Int,
    answeredQuestions: Int
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(PlanSetupTheme.SurfaceHero)
            .padding(top = 22.dp, start = 22.dp, end = 22.dp, bottom = 20.dp)
    ) {
        // Top right decorative gradient
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = (-50).dp)
                .size(200.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PlanSetupTheme.SageInk.copy(alpha = 0.12f), Color.Transparent),
                        radius = 200f // Approx 70% of 200px
                    )
                )
        )
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "EIGHT LITTLE QUESTIONS",
                fontFamily = Manrope,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = PlanSetupTheme.SageInk.copy(alpha = 0.62f),
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = buildAnnotatedString {
                    append("Tell us how your day ")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("should feel.")
                    }
                },
                fontFamily = Manrope,
                fontSize = 31.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.1).sp,
                color = PlanSetupTheme.SageInk,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "The plan bends to how you live \u2014 not the other way around.",
                fontFamily = Manrope,
                fontSize = 13.sp,
                lineHeight = 19.5.sp, // ~1.5
                color = PlanSetupTheme.SageInk.copy(alpha = 0.82f),
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Progress dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until totalQuestions) {
                    val isAnswered = i < answeredQuestions
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(CircleShape)
                            .background(if (isAnswered) PlanSetupTheme.Leaf.copy(alpha = 0.85f) else PlanSetupTheme.SageInk.copy(alpha = 0.16f))
                            .then(
                                if (isAnswered) Modifier.border(0.5.dp, PlanSetupTheme.Sage, CircleShape)
                                else Modifier
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val progressText = when {
                answeredQuestions == 0 -> "Tap through at your own pace"
                answeredQuestions == totalQuestions -> "All eight \u2014 lovely."
                else -> "$answeredQuestions of $totalQuestions answered"
            }
            
            Text(
                text = progressText,
                fontFamily = Manrope,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp,
                color = PlanSetupTheme.SageInk.copy(alpha = 0.72f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
