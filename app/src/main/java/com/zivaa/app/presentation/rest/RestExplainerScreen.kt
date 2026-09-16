package com.zivaa.app.presentation.rest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
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
fun RestExplainerScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Understanding Rest Score", 
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
                text = "Your daily Rest Score is out of 100 points. It is made of four simple parts:",
                style = ZivaaTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = Color(0xFFD1D5DB)),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Factor 1: Sleep Time
            ExplainerCard(
                icon = Icons.Default.NightsStay,
                iconBgColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                iconColor = Color(0xFF60A5FA),
                title = "Sleep Time (Max 40 points)",
                description = "You earn up to 40 points by getting closer to 8 hours of sleep. Sleeping 6 hours gives you 30 points!"
            )

            // Factor 2: Sleep Quality
            ExplainerCard(
                icon = Icons.Default.CheckCircle,
                iconBgColor = Color(0xFF4EAE7B).copy(alpha = 0.2f),
                iconColor = Color(0xFF4EAE7B),
                title = "Sleep Quality (Max 30 points)",
                description = "This checks your sleep efficiency and if you reached your 1.5-hour target for Deep Sleep and REM Sleep."
            )

            // Factor 3: Resting Heart Rate
            ExplainerCard(
                icon = Icons.Rounded.Favorite,
                iconBgColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                iconColor = Color(0xFFF87171),
                title = "Resting HR (Max 30 points)",
                description = "During sleep, your heart should rest! You get full points if your heart rate is at or below your normal average."
            )

            // Factor 4: Stress Penalties
            ExplainerCard(
                icon = Icons.Default.Warning,
                iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                iconColor = Color(0xFFFBBF24),
                title = "Stress Penalties (Up to -30 pts)",
                description = "If you are fighting an illness or physically strained, your Skin Temperature or Breathing Rate might be high. This will deduct points from your final score."
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
                    text = "If your ring fails to track your sleep completely, your Rest Score will safely be skipped for the day so it doesn't hurt your average!",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, color = Color(0xFFA0A6B2)),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ExplainerCard(
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
        
        Column {
            Text(
                text = title,
                style = ZivaaTheme.typography.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = Color(0xFFA0A6B2)
            )
        }
    }
}
