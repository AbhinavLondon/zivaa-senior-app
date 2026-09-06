package com.zivaa.app.presentation.healthassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun PrescriptionResultScreen(
    onNavigateBack: () -> Unit,
    onSetReminders: () -> Unit,
    onAskQuestion: () -> Unit
) {
    val bgColors = ZivaaTheme.colors
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, bgColors.ink.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColors.bgElev)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Back",
                    tint = bgColors.textStrong
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "PRESCRIPTION",
                style = ZivaaTheme.typography.meta.copy(letterSpacing = 0.1.sp),
                color = bgColors.textMeta
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 130.dp)
        ) {
            item {
                Text(
                    text = "Let's read the doctor's hand.",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        color = bgColors.textStrong
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Read indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DR. KULKARNI · TODAY",
                        style = ZivaaTheme.typography.meta,
                        color = bgColors.textMeta
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(bgColors.leaf.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = bgColors.leaf,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "READ",
                                style = ZivaaTheme.typography.meta.copy(fontSize = 9.sp),
                                color = bgColors.leaf
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Two medicines — here's the routine.",
                    style = ZivaaTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        color = bgColors.textStrong
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Medicines
            item {
                MedicineRoutineCard(
                    name = "Metformin",
                    strength = "500mg",
                    schedule = "Morning & Night",
                    notes = "Take it after meals to prevent tummy upset."
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                MedicineRoutineCard(
                    name = "Calcium + D3",
                    schedule = "Lunchtime",
                    notes = "Just once a day. Good for those joints."
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Note
            item {
                RoutineInfoNote(
                    text = "One thing to know:\nBoth these are standard. The Metformin continues as before, the Calcium is a new addition from today."
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Actions
            item {
                val primaryBg = if (isDarkTheme) Color(0xFF1E3A32) else bgColors.sage
                val primaryText = if (isDarkTheme) Color(0xFFE9E5DD) else bgColors.sageInk
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryBg)
                        .clickable { onSetReminders() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = primaryText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set the reminders",
                            style = ZivaaTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = primaryText
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColors.bgElev)
                        .clickable { onAskQuestion() }
                        .border(1.dp, bgColors.ink.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = bgColors.textStrong,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask a question",
                            style = ZivaaTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = bgColors.textStrong
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MedicineRoutineCard(
    name: String,
    strength: String? = null,
    schedule: String,
    notes: String
) {
    val isDarkTheme = isSystemInDarkTheme()
    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.1f)
    } else {
        ZivaaTheme.colors.ink.copy(alpha = 0.1f)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ZivaaTheme.colors.surfaceCard)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = name,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp,
                        color = ZivaaTheme.colors.textStrong
                    )
                )
                if (strength != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strength,
                        style = ZivaaTheme.typography.meta,
                        color = ZivaaTheme.colors.textMeta,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(ZivaaTheme.colors.bg, RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = schedule,
                        style = ZivaaTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = ZivaaTheme.colors.textStrong
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = notes,
                style = ZivaaTheme.typography.bodyMedium.copy(
                    color = ZivaaTheme.colors.textBody,
                    lineHeight = 22.sp
                )
            )
        }
    }
}

@Composable
fun RoutineInfoNote(text: String) {
    val isDarkTheme = isSystemInDarkTheme()
    val noteBg = if (isDarkTheme) Color(0xFF2A1215) else ZivaaTheme.colors.eveningTint // Warm note background
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(noteBg)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = ZivaaTheme.typography.bodyMedium.copy(
                color = if (isDarkTheme) Color(0xFFE9E5DD) else ZivaaTheme.colors.textStrong,
                lineHeight = 22.sp
            )
        )
    }
}
