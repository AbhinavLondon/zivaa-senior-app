package com.zivaa.app.presentation.heartrate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
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
import com.zivaa.app.ui.theme.ZivaaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartScoreExplainerScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Understanding Heart Score", 
                        style = ZivaaTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141519),
                    scrolledContainerColor = Color(0xFF141519)
                )
            )
        },
        containerColor = Color(0xFF141519)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your daily Heart Health Score is out of 100 points, tracking how calm, resilient, and adaptable your heart is across day and night:",
                style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 17.sp, color = Color(0xFFD1D5DB)),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Status Tiers Section Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "WHAT YOUR SCORE MEANS",
                    style = ZivaaTheme.typography.eyebrow.copy(
                        fontSize = 11.5.sp,
                        letterSpacing = 0.08.sp
                    ),
                    color = ZivaaTheme.colors.sage
                )
                Text(
                    text = "Heart Health Status Tiers",
                    style = ZivaaTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            // Tier 1: Optimal Rhythm (85-100)
            ScoreTierCard(
                tierTitle = "Optimal Rhythm",
                scoreRange = "85 – 100",
                tierColor = Color(0xFF4EAE7B),
                description = "Outstanding autonomic tone and cardiac recovery. Calm resting rate at or below baseline, healthy circadian dipping (>10%) during sleep, and flexible daytime responsiveness."
            )

            // Tier 2: Resilient & Stable (70-84)
            ScoreTierCard(
                tierTitle = "Resilient & Stable",
                scoreRange = "70 – 84",
                tierColor = Color(0xFF3B82F6),
                description = "Solid, healthy cardiovascular rhythm with good recovery. Your heart adapts smoothly to daily activity and rests effectively at night, reflecting reliable cardiovascular resilience."
            )

            // Tier 3: Mild Cardiac Strain (50-69)
            ScoreTierCard(
                tierTitle = "Mild Cardiac Strain",
                scoreRange = "50 – 69",
                tierColor = Color(0xFFE5A643),
                description = "Your heart is working slightly harder than usual. This may be caused by fatigue, physical exertion, dehydration, or incomplete sleep dipping. Extra rest and hydration are recommended."
            )

            // Tier 4: Attention Advised (<50)
            ScoreTierCard(
                tierTitle = "Attention Advised",
                scoreRange = "< 50",
                tierColor = Color(0xFFEF4444),
                description = "Noticeable cardiovascular strain, such as unexplained resting heart rate spikes, absence of nocturnal dipping, or rigid span. Take restorative rest and consider consulting your physician."
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Contributing Factors Section Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SCORING BREAKDOWN",
                    style = ZivaaTheme.typography.eyebrow.copy(
                        fontSize = 11.5.sp,
                        letterSpacing = 0.08.sp
                    ),
                    color = ZivaaTheme.colors.sage
                )
                Text(
                    text = "The 4 Contributing Factors",
                    style = ZivaaTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            // Factor 1: Resting Heart Rate
            HeartExplainerCard(
                icon = Icons.Default.Favorite,
                iconBgColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                iconColor = Color(0xFFF87171),
                title = "Resting Heart Rate (Max 30 points)",
                description = "Evaluates your resting heart rate against your personal 14-day rolling baseline. A calm, efficient resting heart rate (at or below your usual baseline) earns up to 30 points."
            )

            // Factor 2: Autonomic Tone / HRV
            HeartExplainerCard(
                icon = Icons.Rounded.Favorite,
                iconBgColor = Color(0xFF06B6D4).copy(alpha = 0.2f),
                iconColor = Color(0xFF22D3EE),
                title = "Heart Rate Variability (Max 25 points)",
                description = "Measures millisecond variations between heartbeats (HRV RMSSD). Higher variability indicates good autonomic tone and stress resilience. If your wearable lacks an HRV sensor, these points are automatically redistributed so your score is never penalized."
            )

            // Factor 3: Circadian Dipping
            HeartExplainerCard(
                icon = Icons.Default.NightsStay,
                iconBgColor = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                iconColor = Color(0xFFA78BFA),
                title = "Day-to-Night Dipping (Max 25 points)",
                description = "A healthy heart rests deeply while you sleep, naturally dropping 10% to 20% compared to daytime. Maintaining this natural diurnal rhythm earns up to 25 points and protects against nocturnal hypertension."
            )

            // Factor 4: Dynamic Range & Flexibility
            HeartExplainerCard(
                icon = Icons.Default.Timeline,
                iconBgColor = Color(0xFF10B981).copy(alpha = 0.2f),
                iconColor = Color(0xFF34D399),
                title = "Heart Rate Range (Max 20 points)",
                description = "Checks your dynamic daily span between your lowest resting rate and peak movement (healthy senior range: 35–65 bpm). This ensures your heart can safely accelerate when walking and rest deeply without stiffness or erratic spikes."
            )

            // Factor 5: Safety Guardrails
            HeartExplainerCard(
                icon = Icons.Default.Warning,
                iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                iconColor = Color(0xFFFBBF24),
                title = "Safety Guardrails (Deductions)",
                description = "Unexplained nocturnal heart rate surges (>100 bpm) or low oxygen drops during sleep apply gentle safety deductions to help alert you and your caregiver."
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF23252C))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Tip: Consistent sleep timing, staying well hydrated, and gentle morning walks help maintain a strong, steady Heart Health Score!",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, color = Color(0xFFA0A6B2)),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HeartExplainerCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B1D23))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun ScoreTierCard(
    tierTitle: String,
    scoreRange: String,
    tierColor: Color,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B1D23))
            .border(1.dp, tierColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(74.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tierColor.copy(alpha = 0.14f))
                .border(1.dp, tierColor.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = scoreRange,
                    style = ZivaaTheme.typography.bodyLarge.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                )
                Text(
                    text = "pts",
                    style = ZivaaTheme.typography.meta.copy(
                        fontSize = 10.sp,
                        color = tierColor.copy(alpha = 0.85f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tierTitle,
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = tierColor
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = ZivaaTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFF9CA3AF)
                )
            )
        }
    }
}
