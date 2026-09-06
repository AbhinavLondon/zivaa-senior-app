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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@Composable
fun DoctorBookingConfirmationScreen(
    onDone: () -> Unit,
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
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Big Checkmark
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(colors.sage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your call is booked.",
                    style = typography.displayMedium,
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Dr. Desai will video-call you at the time below. We'll send a simple one-tap link — no fiddling.",
                    style = typography.bodyMedium,
                    color = colors.inkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zivaaShadow(cornerRadius = 22.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.bg)
                        .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SummaryRow("Doctor", "Dr. Sunita Desai")
                        HorizontalDivider(color = colors.line, thickness = 0.5.dp)
                        SummaryRow("How", "Video consultation")
                        HorizontalDivider(color = colors.line, thickness = 0.5.dp)
                        SummaryRow("When", "Today · 4:30 PM")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priya Info Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.bg)
                        .border(1.dp, colors.lineStrong, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = colors.sage,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Priya will be told it's booked, and Dr. Mehta gets a copy of the visit notes.",
                        style = typography.bodySmall.copy(lineHeight = 20.sp),
                        color = colors.ink
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // After your visit section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "AFTER YOUR VISIT",
                        style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                        color = colors.inkMute,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zivaaShadow(cornerRadius = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.bg)
                            .border(0.5.dp, colors.line, RoundedCornerShape(16.dp))
                            .clickable { }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.clay),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Rx", style = typography.titleLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dr. Desai's prescription", style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.ink)
                                Text("The doctor uploads it here — we explain it in plain words.", style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.padding(top = 2.dp))
                            }
                            Text("›", style = typography.titleLarge, color = colors.inkSoft)
                        }
                    }
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
            Button(
                onClick = { onDone() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .zivaaShadow(cornerRadius = 999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.sage),
                shape = CircleShape
            ) {
                Text(
                    text = "Done",
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = typography.meta.copy(fontSize = 11.sp),
            color = colors.inkSoft,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = value,
            style = typography.bodyMedium,
            color = colors.ink,
            modifier = Modifier.weight(0.7f)
        )
    }
}
