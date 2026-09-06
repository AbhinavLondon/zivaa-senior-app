package com.zivaa.app.ui.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@Composable
fun LabBookingConfirmationScreen(
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
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Big Checkmark
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceHero),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Title
                Column {
                    Text(
                        text = "All booked, Ranjit.",
                        style = typography.displayMedium.copy(fontSize = 38.sp),
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "The phlebotomist will call you when they're on their way.",
                        style = typography.bodyMedium,
                        color = colors.inkSoft
                    )
                }

                // Summary Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zivaaShadow(cornerRadius = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.bg)
                        .border(0.5.dp, colors.line, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryItem("Tests", "Diabetes panel")
                    SummaryItem("Pickup", "Tomorrow, 6:30 - 7:30 AM")
                    SummaryItem("At", "Home (Pune)")
                    SummaryItem("Reports", "By evening on your phone")
                }

                // Fasting Reminder Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEBE3D5)) // A specific warm highlight color
                        .border(0.5.dp, Color(0xFFD3C8B3), RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.clay,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Reminder: please stop eating by 9 PM tonight for the fasting tests. Water is fine.",
                        style = typography.bodyMedium,
                        color = colors.ink
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
            Button(
                onClick = { onDone() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .zivaaShadow(cornerRadius = 999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.ink),
                shape = CircleShape
            ) {
                Text(
                    text = "Done",
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.bg
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = typography.meta.copy(fontSize = 11.sp, letterSpacing = 0.08.em),
            color = colors.inkMute,
            modifier = Modifier.width(80.dp).padding(top = 2.dp)
        )
        Text(
            text = value,
            style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.ink,
            modifier = Modifier.weight(1f)
        )
    }
}
