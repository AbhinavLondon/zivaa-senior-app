package com.zivaa.app.ui.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@Composable
fun LabSchedulingScreen(
    onBack: () -> Unit,
    onNavigateToConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val scrollState = rememberScrollState()

    var selectedDay by remember { mutableStateOf("Tomorrow") }
    var selectedTime by remember { mutableStateOf("6:30 - 7:30 AM") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bgElev,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.bg)
                        .border(0.5.dp, colors.line, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.ink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "WHEN SHALL WE COME?",
                    style = typography.meta.copy(fontSize = 11.sp, letterSpacing = 0.08.em),
                    color = colors.inkMute
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Title
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Pick a ")
                            withStyle(style = SpanStyle(fontFamily = typography.displayMedium.fontFamily, fontStyle = FontStyle.Italic, color = colors.sage)) {
                                append("morning")
                            }
                            append(".")
                        },
                        style = typography.displayMedium.copy(fontSize = 38.sp),
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fasting tests are best done early. We have plenty of early slots.",
                        style = typography.bodyMedium,
                        color = colors.inkSoft
                    )
                }

                // WHICH DAY
                Column {
                    Text(
                        text = "WHICH DAY",
                        style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                        color = colors.inkMute,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zivaaShadow(cornerRadius = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.bg)
                            .border(0.5.dp, colors.line, RoundedCornerShape(16.dp))
                            .padding(6.dp)
                    ) {
                        listOf("Tomorrow", "Day after", "Another day").forEach { day ->
                            val isSelected = selectedDay == day
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) colors.surfaceHero else Color.Transparent)
                                    .clickable { selectedDay = day }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    style = typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                                    color = if (isSelected) Color.White else colors.inkSoft
                                )
                            }
                        }
                    }
                }

                // WHICH TIME
                Column {
                    Text(
                        text = "WHICH TIME",
                        style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                        color = colors.inkMute,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val times = listOf("6:00 - 6:30 AM", "6:30 - 7:30 AM", "7:30 - 8:30 AM", "8:30 - 9:30 AM")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zivaaShadow(cornerRadius = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.bg)
                            .border(0.5.dp, colors.line, RoundedCornerShape(20.dp))
                    ) {
                        times.forEachIndexed { index, time ->
                            val isSelected = selectedTime == time
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTime = time }
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = time,
                                    style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.ink,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.surfaceHero else colors.line),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            if (index < times.lastIndex) {
                                HorizontalDivider(color = colors.line, thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // COLLECT FROM
                Column {
                    Text(
                        text = "COLLECT FROM",
                        style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                        color = colors.inkMute,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zivaaShadow(cornerRadius = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.bg)
                            .border(0.5.dp, colors.line, RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.clay),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Home",
                                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.ink
                            )
                            Text(
                                text = "Ranjit Iyer • Pune",
                                style = typography.bodySmall,
                                color = colors.inkSoft,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Text(
                            text = "Change",
                            style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.surfaceHero,
                            modifier = Modifier.clickable { }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 200.dp)) // padding for bottom CTA
            }
        }

        // Bottom CTA Sticky
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgElev)
                    .padding(horizontal = 22.dp)
                    .padding(top = 22.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "₹700",
                            style = typography.cardTitle.copy(fontSize = 28.sp),
                            color = colors.ink
                        )
                        Text(
                            text = "Pay after the visit, as usual.",
                            style = typography.meta.copy(fontSize = 9.sp, letterSpacing = 0.08.em),
                            color = colors.inkMute,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onNavigateToConfirmation() },
                        modifier = Modifier
                            .height(60.dp)
                            .zivaaShadow(cornerRadius = 999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceHero),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Confirm & book",
                                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
