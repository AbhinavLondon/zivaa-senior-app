package com.zivaa.app.ui.care

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Schedule
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
fun DoctorBookingScreen(
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val scrollState = rememberScrollState()

    var selectedMode by remember { mutableStateOf("video") } // "video" or "home"
    var selectedTimeSlot by remember { mutableStateOf(0) }

    val videoSlots = listOf("Today · 4:30 PM", "Today · 6:00 PM", "Tomorrow · 11:00 AM")
    val homeSlots = listOf("Tomorrow · 10–11 AM", "Tomorrow · 5–6 PM", "Thu · 10–11 AM")
    val currentSlots = if (selectedMode == "video") videoSlots else homeSlots

    // Reset slot when mode changes to prevent out of bounds
    LaunchedEffect(selectedMode) {
        selectedTimeSlot = 0
    }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.leaf),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SD", style = typography.meta.copy(fontSize = 11.sp, fontStyle = FontStyle.Italic), color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Dr. Sunita Desai", style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.ink)
                        Text("Endocrinologist", style = typography.meta.copy(fontSize = 9.sp), color = colors.inkSoft)
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Title
                Text(
                    text = buildAnnotatedString {
                        append("How shall we ")
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.sage)) {
                            append("meet")
                        }
                        append("?")
                    },
                    style = typography.displayMedium.copy(fontSize = 38.sp),
                    color = colors.ink
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mode Selectors
                ModeCard(
                    title = "Video consultation",
                    subtitle = "Talk from your chair, face to face",
                    meta = "Next slot in ~30 minutes",
                    price = "₹700",
                    icon = Icons.Default.Message,
                    isSelected = selectedMode == "video",
                    onClick = { selectedMode = "video" }
                )

                ModeCard(
                    title = "Home visit",
                    subtitle = "The doctor comes to your door",
                    meta = "Tomorrow onwards",
                    price = "₹1500",
                    icon = Icons.Default.Home,
                    isSelected = selectedMode == "home",
                    onClick = { selectedMode = "home" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PICK A TIME",
                    style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                    color = colors.inkMute,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Time Slots
                currentSlots.forEachIndexed { index, slot ->
                    TimeSlotCard(
                        label = slot,
                        isSelected = selectedTimeSlot == index,
                        onClick = { selectedTimeSlot = index }
                    )
                }

                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 190.dp)) // padding for bottom CTA
            }
        }

        // Bottom CTA Sticky
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onConfirm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .zivaaShadow(cornerRadius = 999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceHero),
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm with ",
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Dr. Desai",
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.amber
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No payment now · cancel any time before the visit",
                    style = typography.bodySmall,
                    color = colors.inkSoft
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    meta: String,
    price: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val bgColor = if (isSelected) colors.sage.copy(alpha = 0.08f) else colors.bg
    val borderColor = if (isSelected) colors.sage else colors.line
    val iconBgColor = if (isSelected) colors.sage else colors.muted
    val iconColor = if (isSelected) colors.sageInk else colors.ink

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zivaaShadow(cornerRadius = 22.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = typography.cardTitle.copy(fontSize = 19.sp), color = colors.ink)
            Text(subtitle, style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.padding(top = 2.dp))
            Text(meta, style = typography.meta.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = colors.sage, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(price, style = typography.titleLarge.copy(fontSize = 20.sp), color = colors.ink)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.sage else Color.Transparent)
                    .border(if (isSelected) 0.5.dp else 1.5.dp, if (isSelected) colors.sage else colors.lineStrong, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.sageInk, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TimeSlotCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val bgColor = if (isSelected) colors.sage.copy(alpha = 0.08f) else colors.bg
    val borderColor = if (isSelected) colors.sage else colors.line

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zivaaShadow(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = colors.inkSoft,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
            color = colors.ink,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.sage else Color.Transparent)
                .border(if (isSelected) 0.5.dp else 1.5.dp, if (isSelected) colors.sage else colors.lineStrong, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colors.sageInk, modifier = Modifier.size(16.dp))
            }
        }
    }
}
