package com.zivaa.app.presentation.mindfulness

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zivaa.app.R
import com.zivaa.app.presentation.mindfulness.components.GlowingOrb
import com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActiveMeditationScreen(
    onBack: () -> Unit = {},
    soundId: String = "tanpura",
    voice: String = "No voice",
    durationMinutes: Int = 10
) {
    val context = LocalContext.current
    var timeLeftSeconds by remember { mutableIntStateOf(durationMinutes * 60) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val volumeAnimatable = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var isFadingOut by remember { mutableStateOf(false) }

    val formattedTime = remember(timeLeftSeconds) {
        val minutes = timeLeftSeconds / 60
        val seconds = timeLeftSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    val displaySoundName = when (soundId) {
        "tanpura" -> "Tanpura Drone"
        "bansuri" -> "Bansuri At Dawn"
        "rain" -> "Rain On Neem Leaves"
        "om" -> "Om Chanting"
        "silence" -> "Silence"
        else -> soundId.replaceFirstChar { it.uppercase() }
    }

    DisposableEffect(soundId) {
        // Initialize MediaPlayer
        val resId = when (soundId) {
            "tanpura" -> R.raw.tanpura
            "bansuri" -> R.raw.bansuri
            "rain" -> R.raw.rain
            "om" -> R.raw.om
            "silence" -> R.raw.silence
            else -> null // Fallback if no audio found
        }
        
        if (resId != null) {
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                isLooping = true
                setVolume(1f, 1f)
                start()
            }
        }
        
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Handle smooth fade out
    LaunchedEffect(volumeAnimatable.value) {
        mediaPlayer?.setVolume(volumeAnimatable.value, volumeAnimatable.value)
    }

    // Timer effect
    LaunchedEffect(timeLeftSeconds) {
        while (timeLeftSeconds > 0) {
            delay(1000L)
            timeLeftSeconds--
            
            // Start fading out when 3 seconds are left
            if (timeLeftSeconds <= 3 && !isFadingOut) {
                isFadingOut = true
                scope.launch {
                    volumeAnimatable.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                    )
                }
            }
        }
        
        // Timer finished
        if (timeLeftSeconds == 0) {
            onBack()
        }
    }

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
                        .clickable { 
                            // Quick fade out before back
                            scope.launch {
                                volumeAnimatable.animateTo(0f, tween(500))
                                onBack()
                            }
                        },
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
                        text = displaySoundName,
                        style = MindfulnessTheme.typography.eyebrow,
                        color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlowingOrb(
                modifier = Modifier.fillMaxWidth(0.85f),
                isPulsing = true
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        style = MindfulnessTheme.typography.timerLarge,
                        color = MindfulnessTheme.typography.bodyLarge.color // Dark text on orb
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remaining",
                        style = MindfulnessTheme.typography.eyebrow,
                        color = MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Nowhere to be. Nothing to fix.",
                style = MindfulnessTheme.typography.titleItalicSerif,
                color = MindfulnessTheme.typography.bodyLarge.color
            )

            Spacer(modifier = Modifier.height(40.dp))

            // End early button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, MindfulnessTheme.typography.bodyLarge.color.copy(alpha = 0.3f), CircleShape)
                    .clickable { 
                        scope.launch {
                            volumeAnimatable.animateTo(0f, tween(500))
                            onBack()
                        }
                    }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "End early — that's fine",
                    style = MindfulnessTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
