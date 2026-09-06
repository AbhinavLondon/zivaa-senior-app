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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LineStyle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun LabTestsScreen(
    onBack: () -> Unit,
    onNavigateToConcernPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val scrollState = rememberScrollState()

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
                    text = "HOME LAB TESTS",
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
                            append("A blood test, ")
                            withStyle(style = SpanStyle(fontFamily = typography.displayMedium.fontFamily, fontStyle = FontStyle.Italic, color = colors.sage)) {
                                append("from home")
                            }
                            append(".")
                        },
                        style = typography.displayMedium.copy(fontSize = 38.sp),
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Someone comes to you — no clinic, no queue. Don't worry which tests; we'll sort that. Where shall we start?",
                        style = typography.bodyMedium,
                        color = colors.inkSoft
                    )
                }

                // Hero Card (Doctor's slip)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zivaaShadow(cornerRadius = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surfaceHero)
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x33F6F3EE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "THE EASIEST WAY",
                                style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                                color = Color(0xB8F6F3EE)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Have a doctor's slip?",
                            style = typography.cardTitle.copy(fontSize = 24.sp),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Hold it up to the camera. We'll read every test on it and book them — no typing, no guessing.",
                            style = typography.bodyMedium,
                            color = Color(0xE0F6F3EE)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.bg),
                            shape = CircleShape
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = colors.ink,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scan a prescription",
                                    style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.ink
                                )
                            }
                        }
                    }
                }

                // No slip list
                Column {
                    Text(
                        text = "NO SLIP? NO PROBLEM",
                        style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                        color = colors.inkMute,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LabOptionCard(
                        icon = Icons.Outlined.HelpOutline,
                        iconBg = colors.clay,
                        title = "Not sure what to test?",
                        subtitle = "Tell us the worry — we'll pick the right tests.",
                        onClick = { onNavigateToConcernPicker() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LabOptionCard(
                        icon = Icons.Outlined.LineStyle,
                        iconBg = colors.leaf,
                        title = "Diabetes & heart panel",
                        subtitle = "Follows your sugar and the cholesterol your last report flagged.",
                        tag = "RECOMMENDED FOR YOU",
                        onClick = { }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Small rebook card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                            .clickable { }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = colors.inkSoft,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = buildAnnotatedString {
                                    append("Book the same as last time — ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.ink)) {
                                        append("Full body check")
                                    }
                                    append(", March")
                                },
                                style = typography.bodySmall,
                                color = colors.inkSoft
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.inkMute,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }
}

@Composable
private fun LabOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    tag: String? = null,
    onClick: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zivaaShadow(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bg)
            .border(0.5.dp, colors.line, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (tag != null) {
                Text(
                    text = tag,
                    style = typography.meta.copy(fontSize = 9.sp, letterSpacing = 0.08.em),
                    color = colors.sage,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = title,
                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.ink
            )
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.inkSoft,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.inkMute,
            modifier = Modifier.size(20.dp)
        )
    }
}
