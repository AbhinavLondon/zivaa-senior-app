package com.zivaa.app.presentation.mindfulness

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.mindfulness.components.GlowingOrb
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme

@Composable
fun ActiveBreathingScreen(
    onBack: () -> Unit = {},
    rhythmName: String = "Box Breath",
    instructionTitle: String = "Hold",
    instructionSubtitle: String = "4 Seconds",
    hintText: String = "Shoulders soft. Let the belly do the breathing, not\nthe chest.",
    roundText: String = "Round 1"
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.2f), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.8f)
                    )
                }
                
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = rhythmName,
                        style = MindfulnessTheme.typography.eyebrow,
                        color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlowingOrb(
                modifier = Modifier.fillMaxWidth(0.7f),
                isPulsing = true
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = instructionTitle,
                        style = MindfulnessTheme.typography.titleItalicSerif,
                        color = MindfulnessTheme.typography.bodyLarge.color // Normally dark text on bright orb, wait, the design shows dark text on orb!
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = instructionSubtitle,
                        style = MindfulnessTheme.typography.eyebrow,
                        color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.7f) // Need dark text
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = hintText,
                style = MindfulnessTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = roundText,
                style = MindfulnessTheme.typography.eyebrow,
                color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // I'm done button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.3f), CircleShape)
                    .clickable { onBack() }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "I'm done",
                    style = MindfulnessTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
