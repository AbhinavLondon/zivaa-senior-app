package com.zivaa.app.presentation.mindfulness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.zivaa.app.R
import com.zivaa.app.presentation.mindfulness.components.GlowingOrb
import com.zivaa.app.presentation.mindfulness.components.MindfulnessCard
import com.zivaa.app.presentation.mindfulness.components.MoodCheckInRow
import com.zivaa.app.presentation.mindfulness.components.Mood
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessAccent
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme

@Composable
fun MindfulnessLandingScreen(
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToMeditation: () -> Unit = {}
) {
    var selectedMood by remember { mutableStateOf<Mood?>(null) }

    Scaffold(
        containerColor = Color(0xFF0D0F1C),
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF4C3B7D).copy(alpha = 0.55f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.2f),
                            radius = size.width * 0.8f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF7C4A63).copy(alpha = 0.5f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.7f),
                            radius = size.width * 0.8f
                        )
                    )
                    // Stars
                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.15f))
                    drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 1.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.2f))
                    drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 1.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.3f))
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mindfulness • Tuesday Evening",
                    style = MindfulnessTheme.typography.eyebrow.copy(letterSpacing = 0.2.em),
                    color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.5f)
                )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    append("The evening is\n")
                    withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MindfulnessAccent)) {
                        append("yours")
                    }
                    append(", Ranjit.")
                },
                style = MindfulnessTheme.typography.titleLargeSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "A few unhurried minutes for the mind. Nothing to\nachieve — just arrive.",
                style = MindfulnessTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MindfulnessTheme.typography.bodyMedium.color.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlowingOrb(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterHorizontally),
                isPulsing = false
            )

            Spacer(modifier = Modifier.height(32.dp))

            MindfulnessCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF283A4A).copy(alpha = 0.6f), Color(0xFF191D2F).copy(alpha = 0.6f)))
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mood Check-In",
                            style = MindfulnessTheme.typography.eyebrow.copy(letterSpacing = 0.2.em),
                            color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.5f)
                        )
                        if (selectedMood != null) {
                            Text(
                                text = "Change",
                                style = MindfulnessTheme.typography.eyebrow.copy(letterSpacing = 0.2.em),
                                color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { selectedMood = null }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (selectedMood == null) {
                        Text(
                            text = "How are you feeling tonight?",
                            style = MindfulnessTheme.typography.cardTitleSerif
                        )
                    } else {
                        Text(
                            text = "${selectedMood!!.label}.",
                            style = MindfulnessTheme.typography.titleLargeSerif.copy(fontSize = 32.sp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when(selectedMood!!) {
                                Mood.WONDERFUL -> "Then simply enjoy it — maybe a short sit to seal the day."
                                Mood.GOOD -> "Glad to hear it. A quick breath to anchor the feeling?"
                                Mood.OKAY -> "Right down the middle. A good place to start."
                                Mood.LOW -> "Sorry to hear that. A gentle sit might help."
                                Mood.VERY_LOW -> "Take it easy tonight. A soothing sound might be best."
                            },
                            style = MindfulnessTheme.typography.bodyMedium,
                            color = MindfulnessTheme.typography.bodyMedium.color.copy(alpha = 0.8f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    MoodCheckInRow(
                        modifier = Modifier.fillMaxWidth(),
                        selectedMood = selectedMood,
                        onMoodSelected = { mood ->
                            selectedMood = mood
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MindfulnessCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToBreathing,
                backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFF211D2B).copy(alpha = 0.8f), Color(0xFF3B2E28).copy(alpha = 0.8f)))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Orb icon placeholder
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(48.dp).border(1.dp, Color.White.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape))
                        GlowingOrb(modifier = Modifier.size(24.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Breathing",
                            style = MindfulnessTheme.typography.cardTitleSerif
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Four ways to slow the breath — pick what suits tonight.",
                            style = MindfulnessTheme.typography.bodySmall,
                            color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MindfulnessCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToMeditation,
                backgroundBrush = Brush.horizontalGradient(listOf(Color(0xFF211D2B).copy(alpha = 0.8f), Color(0xFF332040).copy(alpha = 0.8f)))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Moon icon placeholder
                    Box(modifier = Modifier.size(48.dp).border(1.dp, Color.White.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_myplaces), // Using a placeholder that looks vaguely moon-ish if no moon icon exists, wait I'll use text for now
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Meditation",
                            style = MindfulnessTheme.typography.cardTitleSerif
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sit with sound — tanpura, flute, rain — with or without a voice.",
                            style = MindfulnessTheme.typography.bodySmall,
                            color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Even two minutes counts. The mind, like the garden, likes",
                style = MindfulnessTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}
}
