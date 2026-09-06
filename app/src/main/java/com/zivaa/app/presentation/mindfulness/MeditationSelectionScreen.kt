package com.zivaa.app.presentation.mindfulness

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.mindfulness.components.MindfulnessCard
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessAccent
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessSurfaceActive
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessSurfaceDark
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme
import com.zivaa.app.presentation.mindfulness.theme.OrbCenter
import com.zivaa.app.presentation.mindfulness.theme.OrbEdge

import androidx.annotation.RawRes
import com.zivaa.app.R

data class MeditationSound(
    val id: String,
    val title: String,
    val description: String,
    val colorStart: Color,
    val colorEnd: Color,
    @RawRes val audioRes: Int
)

val meditationSounds = listOf(
    MeditationSound("tanpura", "Tanpura drone", "A warm, endless hum", Color(0xFFD7B074), Color(0xFFC3934B), R.raw.tanpura),
    MeditationSound("bansuri", "Bansuri at dawn", "Soft flute, far away", Color(0xFFEAD8C7), Color(0xFFD6C0B0), R.raw.bansuri),
    MeditationSound("rain", "Rain on neem leaves", "Steady monsoon patter", Color(0xFF7FA192), Color(0xFF678577), R.raw.rain),
    MeditationSound("om", "Om chanting", "Low voices, slow rhythm", Color(0xFFECA376), Color(0xFFA37095), R.raw.om),
    MeditationSound("silence", "Silence, with a bell", "A soft bell every minute", Color(0xFFC4C5CE), Color(0xFF8F91A2), R.raw.silence)
)

val supportOptions = listOf("Voice · Hindi", "Voice · English", "No voice")
val durationOptions = listOf(5, 10, 15, 20)

@Composable
fun MeditationSelectionScreen(
    onBack: () -> Unit = {},
    onBegin: (soundId: String, voiceSupport: String, durationMinutes: Int) -> Unit = { _, _, _ -> }
) {
    var selectedSound by remember { mutableStateOf(meditationSounds.first()) }
    var selectedSupport by remember { mutableStateOf(supportOptions.last()) }
    var selectedDuration by remember { mutableStateOf(durationOptions[1]) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(
                top = 40.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "MEDITATION",
                        style = MindfulnessTheme.typography.eyebrow,
                        color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = buildAnnotatedString {
                        append("What shall the ")
                        withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MindfulnessTheme.typography.bodyLarge.color)) {
                            append("silence ")
                        }
                        append("wear?")
                    },
                    style = MindfulnessTheme.typography.titleLargeSerif
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pick a sound, a voice if you'd like one, and how long to sit.",
                    style = MindfulnessTheme.typography.bodyMedium,
                    color = MindfulnessTheme.typography.bodyMedium.color.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "SOUND",
                    style = MindfulnessTheme.typography.eyebrow,
                    color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(meditationSounds) { sound ->
                val isSelected = selectedSound.id == sound.id
                MindfulnessCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    isActive = isSelected,
                    onClick = { selectedSound = sound },
                    cornerRadius = 20.dp,
                    contentPadding = 16.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(sound.colorStart, sound.colorEnd)))
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sound.title,
                                style = MindfulnessTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            )
                            Text(
                                text = sound.description,
                                style = MindfulnessTheme.typography.bodySmall,
                                color = MindfulnessTheme.typography.bodySmall.color.copy(alpha = 0.8f)
                            )
                        }
                        
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFA197B8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "SUPPORT",
                    style = MindfulnessTheme.typography.eyebrow,
                    color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    supportOptions.forEach { option ->
                        val isSelected = selectedSupport == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFA197B8) else MindfulnessSurfaceDark)
                                .border(1.dp, if (isSelected) Color.Transparent else MindfulnessSurfaceDark.copy(alpha = 0.5f), CircleShape)
                                .clickable { selectedSupport = option }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                style = MindfulnessTheme.typography.bodySmall,
                                color = if (isSelected) Color(0xFF110B29) else MindfulnessTheme.typography.bodyLarge.color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "HOW LONG",
                    style = MindfulnessTheme.typography.eyebrow,
                    color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durationOptions.forEach { duration ->
                        val isSelected = selectedDuration == duration
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFA197B8) else MindfulnessSurfaceDark)
                                .border(1.dp, if (isSelected) Color.Transparent else MindfulnessSurfaceDark.copy(alpha = 0.5f), CircleShape)
                                .clickable { selectedDuration = duration }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${duration} min",
                                style = MindfulnessTheme.typography.bodySmall,
                                color = if (isSelected) Color(0xFF110B29) else MindfulnessTheme.typography.bodyLarge.color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(OrbCenter, OrbEdge)))
                        .clickable { onBegin(selectedSound.id, selectedSupport, selectedDuration) }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Begin sitting",
                        style = MindfulnessTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                        color = Color(0xFF110B29) // Dark text on bright button
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
