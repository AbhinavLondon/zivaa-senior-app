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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@Composable
fun DoctorProfileScreen(
    onBack: () -> Unit,
    onNavigateToBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bgElev, // Looks like a continuous elevated background
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
                    text = "GET TO KNOW DR. DESAI",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Top Card (Identity)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.leaf),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SD",
                            style = typography.cardTitle.copy(fontSize = 26.sp, fontStyle = FontStyle.Italic),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Dr. Sunita Desai",
                            style = typography.titleLarge.copy(fontSize = 26.sp),
                            color = colors.ink
                        )
                        Text(
                            text = "Endocrinologist · diabetes & hormones",
                            style = typography.bodyMedium,
                            color = colors.inkSoft,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Jehangir Hospital, Pune",
                            style = typography.bodySmall,
                            color = colors.inkMute,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Recommendation Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zivaaShadow(cornerRadius = 22.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.surfaceHero)
                        .padding(22.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x29F6F3EE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AM",
                                    style = typography.meta.copy(fontSize = 10.sp, fontStyle = FontStyle.Italic),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "RECOMMENDED BY YOUR FAMILY DOCTOR",
                                style = typography.meta.copy(fontSize = 9.sp, letterSpacing = 0.08.em),
                                color = Color(0xB8F6F3EE)
                            )
                        }

                        Text(
                            text = "“If it's about Ranjit's sugar, Dr. Desai is who I'd send my own family to.”",
                            style = typography.cardTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Text(
                            text = "— Dr. Anjali Mehta, your family doctor",
                            style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xE0F6F3EE),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                // Verified Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zivaaShadow(cornerRadius = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bg)
                        .border(0.5.dp, colors.line, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.sage.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified",
                            tint = colors.sage,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Identity & licence verified by Sahayak",
                            style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.ink
                        )
                        Text(
                            text = "NMC reg 2006-11432",
                            style = typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.05.em),
                            color = colors.inkMute,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("18 yrs", "EXPERIENCE", modifier = Modifier.weight(1f))
                    StatCard("4.9", "210 REVIEWS", modifier = Modifier.weight(1f))
                    StatCard("40+", "SENIORS NEARBY", modifier = Modifier.weight(1f))
                }

                // Patient Review
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zivaaShadow(cornerRadius = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bg)
                        .border(0.5.dp, colors.line, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "“She explained everything slowly, in Marathi. My father finally felt at ease.”",
                            style = typography.titleLarge.copy(fontSize = 19.sp, fontStyle = FontStyle.Italic, lineHeight = 26.sp),
                            color = colors.ink
                        )
                        Text(
                            text = "Meena K. · daughter · Koregaon Park",
                            style = typography.bodySmall,
                            color = colors.inkMute,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                // Reassurances
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "SO YOU'RE NEVER ALONE IN IT",
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
                            .padding(horizontal = 18.dp)
                    ) {
                        Column {
                            ReassuranceItem(
                                icon = Icons.Default.Call,
                                text = "Priya can join the video call, right from Mumbai.",
                                showDivider = true
                            )
                            ReassuranceItem(
                                icon = Icons.Outlined.IosShare,
                                text = "Dr. Mehta receives the visit notes, so your care stays joined-up.",
                                showDivider = true
                            )
                            ReassuranceItem(
                                icon = Icons.Default.Shield,
                                text = "A Sahayak helper checks in after, to make sure all went well.",
                                showDivider = false
                            )
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onNavigateToBooking() },
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
                        Text(
                            text = "Book with ",
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Dr. Desai",
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.amber // Highlighted part
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
                Spacer(modifier = Modifier.height(14.dp))
                Row {
                    Text("Still unsure? ", style = typography.bodyMedium, color = colors.inkSoft)
                    Text(
                        "Ask Dr. Mehta about her",
                        style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.sage,
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    Box(
        modifier = modifier
            .zivaaShadow(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bg)
            .border(0.5.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = typography.cardTitle.copy(fontSize = 24.sp),
                color = colors.ink
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = typography.meta.copy(fontSize = 8.5.sp, letterSpacing = 0.08.em),
                color = colors.inkSoft
            )
        }
    }
}

@Composable
private fun ReassuranceItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, showDivider: Boolean) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    Column {
        Row(
            modifier = Modifier.padding(vertical = 18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.sage,
                modifier = Modifier.size(18.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = typography.bodyMedium.copy(fontSize = 14.5.sp, lineHeight = 21.sp),
                color = colors.ink
            )
        }
        if (showDivider) {
            HorizontalDivider(color = colors.line, thickness = 0.5.dp)
        }
    }
}
