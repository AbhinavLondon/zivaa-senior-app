package com.zivaa.app.presentation.healthassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun HealthAssistantDashboardScreen(
    onNavigateBack: () -> Unit,
    onUploadLabReport: () -> Unit,
    onUploadPrescription: () -> Unit,
    onLogFood: () -> Unit,
    onLogMood: () -> Unit,
    onViewResult: () -> Unit // Mock action for the recently explained items
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // In dark mode we can slightly adjust colors, but the general mapping applies
    val topCardBg = if (isDarkTheme) Color(0xFF1E3A32) else ZivaaTheme.colors.sage
    val topCardTextMain = if (isDarkTheme) Color(0xFFE9E5DD) else ZivaaTheme.colors.sageInk
    val topCardTextSecondary = if (isDarkTheme) Color(0xFFE9E5DD).copy(alpha = 0.8f) else ZivaaTheme.colors.sageInk.copy(alpha = 0.8f)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZivaaTheme.colors.bg)
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
            Text(
                text = "HEALTH ASSISTANT · SUN 19 JUL",
                style = ZivaaTheme.typography.meta,
                color = ZivaaTheme.colors.textMeta
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HealthAssistantGridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Description,
                        iconBg = ZivaaTheme.colors.leaf,
                        title = "Upload lab\nreport",
                        onClick = onUploadLabReport
                    )
                    HealthAssistantGridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Link,
                        iconBg = ZivaaTheme.colors.clay,
                        title = "Upload\nprescription",
                        onClick = onUploadPrescription
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HealthAssistantGridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Restaurant,
                        iconBg = ZivaaTheme.colors.sage,
                        title = "Log Food",
                        onClick = onLogFood
                    )
                    HealthAssistantGridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Mood,
                        iconBg = ZivaaTheme.colors.amber,
                        title = "Log Mood",
                        onClick = onLogMood
                    )
                }
            }
            

            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "I explain — I don't diagnose. For anything worrying, we'll bring\nin a real doctor.",
                    style = ZivaaTheme.typography.bodySmall.copy(
                        color = ZivaaTheme.colors.textMeta,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun HealthAssistantGridCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    title: String,
    onClick: () -> Unit,
    cardBg: Color = ZivaaTheme.colors.surfaceCard
) {
    val borderColor = if (isSystemInDarkTheme()) {
        Color.White.copy(alpha = 0.1f)
    } else {
        ZivaaTheme.colors.ink.copy(alpha = 0.1f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .clickable { onClick() }
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = ZivaaTheme.colors.textStrong
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentItemCard(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    type: String,
    onClick: () -> Unit
) {
    val borderColor = if (isSystemInDarkTheme()) {
        Color.White.copy(alpha = 0.1f)
    } else {
        ZivaaTheme.colors.ink.copy(alpha = 0.1f)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ZivaaTheme.colors.bg)
            .clickable { onClick() }
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = ZivaaTheme.colors.textStrong,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = ZivaaTheme.typography.bodySmall.copy(
                        color = ZivaaTheme.colors.textBody,
                        fontSize = 13.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = type,
                style = ZivaaTheme.typography.meta.copy(letterSpacing = 0.05.sp),
                color = ZivaaTheme.colors.textMeta
            )
        }
    }
}
