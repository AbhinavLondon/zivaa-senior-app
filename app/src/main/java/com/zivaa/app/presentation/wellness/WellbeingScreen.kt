package com.zivaa.app.presentation.wellness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.getValue
import com.zivaa.app.presentation.wellness.theme.*

@Composable
fun WellbeingScreen(
    isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToCare: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMindfulness: () -> Unit = {},
    onNavigateToChooseAreas: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {}
) {
    WellbeingTheme(darkTheme = isDarkTheme) {
        Scaffold(
            containerColor = WellnessTheme.colors.background,
            contentWindowInsets = WindowInsets.systemBars
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 130.dp)
            ) {
                item {
                    HeaderSection()
                    Spacer(modifier = Modifier.height(16.dp))
                    StreakStatus()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    ActionCard(
                        overline = "BODY - MOVE",
                        titleNormal = "Wake the ",
                        titleHighlight = "legs",
                        description = "Gentle exercises and chair yoga — knees, back, balance. Ten minutes, your pace.",
                        buttonText = "Start moving",
                        backgroundColor = WellnessTheme.colors.cardBody,
                        highlightColor = WellnessTheme.colors.highlightBody,
                        icon = Icons.Default.AccessibilityNew,
                        onButtonClick = onNavigateToChooseAreas
                    )
                }

                item {
                    ActionCard(
                        overline = "MIND - SETTLE",
                        titleNormal = "Quiet the ",
                        titleHighlight = "mind",
                        description = "A mood check-in, slow breathing, or sitting with the tanpura. Two minutes counts.",
                        buttonText = "Find some quiet",
                        backgroundColor = WellnessTheme.colors.cardMind,
                        highlightColor = WellnessTheme.colors.highlightMind,
                        icon = Icons.Default.NightlightRound,
                        onButtonClick = onNavigateToMindfulness
                    )
                }

                item {
                    ActionCard(
                        overline = "FOOD - NOURISH",
                        titleNormal = "Eat on ",
                        titleHighlight = "time",
                        description = "Tick off today's plate, count the water glasses — and keep dinner before 8.",
                        buttonText = "See today's plate",
                        backgroundColor = WellnessTheme.colors.cardFood,
                        highlightColor = WellnessTheme.colors.highlightFood,
                        icon = Icons.Default.Coffee,
                        onButtonClick = onNavigateToNutrition
                    )
                }

                item {
                    DailyTipCard()
                }

                item {
                    FooterText()
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column {
        Text(
            text = "YOUR WELLBEING · DUE 21 JUL",
            style = WellnessLabel,
            color = WellnessTheme.colors.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                append("Three little gardens,\n")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("Ranjit")
                }
                append(". Tend one.")
            },
            style = WellnessHeadline,
            color = WellnessTheme.colors.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Body, mind, and the kitchen. A few minutes in any of them makes the whole day stand taller.",
            style = WellnessBody,
            color = WellnessTheme.colors.onBackground.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun StreakStatus() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WellnessTheme.colors.surface, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(WellnessTheme.colors.statusDot, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = buildAnnotatedString {
                append("You've tended ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("something")
                }
                append(" 6 days in a row.")
            },
            style = WellnessBody.copy(fontSize = 14.sp),
            color = WellnessTheme.colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "6 DAYS",
            style = WellnessLabel,
            color = WellnessTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ActionCard(
    overline: String,
    titleNormal: String,
    titleHighlight: String,
    description: String,
    buttonText: String,
    backgroundColor: Color,
    highlightColor: Color,
    icon: ImageVector,
    onButtonClick: () -> Unit
) {
    val darkerBg = Color(
        red = (backgroundColor.red * 0.8f).coerceIn(0f, 1f),
        green = (backgroundColor.green * 0.8f).coerceIn(0f, 1f),
        blue = (backgroundColor.blue * 0.8f).coerceIn(0f, 1f),
        alpha = backgroundColor.alpha
    )
    
    val gradient = Brush.linearGradient(
        colors = listOf(backgroundColor, darkerBg),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = overline,
                style = WellnessLabel,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append(titleNormal)
                    withStyle(style = SpanStyle(color = highlightColor, fontStyle = FontStyle.Italic)) {
                        append(titleHighlight)
                    }
                    append(".")
                },
                style = WellnessHeadline.copy(
                    color = Color.White,
                    fontSize = 28.sp,
                    lineHeight = (28 * 1.1).sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = description,
                style = WellnessBody.copy(color = Color.White.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(buttonText, style = WellnessBody.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        val infiniteTransition = rememberInfiniteTransition(label = "iconFloat")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        // Background decorative icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = highlightColor.copy(alpha = alpha),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(72.dp)
                .offset(x = 8.dp, y = offsetY.dp)
        )
    }
}

@Composable
fun DailyTipCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WellnessTheme.colors.surface, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(WellnessTheme.colors.background, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EnergySavingsLeaf,
                contentDescription = null,
                tint = WellnessTheme.colors.statusDot
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "IF YOU DO ONE THING TODAY",
                style = WellnessLabel,
                color = WellnessTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Walk the garden round after chai — your knee likes the rhythm.",
                style = WellnessBody,
                color = WellnessTheme.colors.onSurface
            )
        }
    }
}

@Composable
fun FooterText() {
    Text(
        text = "No scores, no targets. Gardens don't grow faster for being measured — just visited.",
        style = WellnessBody.copy(fontSize = 14.sp),
        color = WellnessTheme.colors.onBackground.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}



@Preview(showBackground = true)
@Composable
fun WellbeingScreenPreviewLight() {
    WellbeingScreen()
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WellbeingScreenPreviewDark() {
    WellbeingScreen()
}
