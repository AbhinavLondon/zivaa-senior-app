package com.zivaa.app.presentation.setup

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.InstrumentSerif
import kotlinx.coroutines.isActive
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════
// AMBIENT FLOATING PARTICLES ENGINE
// ═══════════════════════════════════════════════════════════════════

private class FloatingParticle(
    var x: Float,
    var y: Float,
    val baseRadiusDp: Float,
    val speedY: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float,
    val phase: Float,
    val color: Color,
    val isGlowingOrb: Boolean
)

@Composable
fun AmbientFloatingParticlesBackground(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(
            Color(0xFFE2C07D), // Champagne Gold
            Color(0xFFF3DFA2), // Warm Gold
            Color(0xFF6EE7B7), // Bioluminescent Emerald
            Color(0xFFA7F3D0), // Soft Sage Mint
            Color(0xFFFFFDF8)  // Starlight Ivory
        )
        List(42) { i ->
            val isOrb = (i % 7 == 0) // ~6 soft ambient blurred bokeh orbs
            val radius = if (isOrb) {
                Random.nextFloat() * 10f + 12f // 12..22dp
            } else {
                Random.nextFloat() * 3.2f + 1.4f // 1.4..4.6dp
            }
            FloatingParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                baseRadiusDp = radius,
                speedY = Random.nextFloat() * 0.00035f + 0.00018f,
                swayAmplitude = Random.nextFloat() * 0.022f + 0.008f,
                swayFrequency = Random.nextFloat() * 1.8f + 0.9f,
                phase = Random.nextFloat() * (Math.PI.toFloat() * 2f),
                color = colors[i % colors.size],
                isGlowingOrb = isOrb
            )
        }
    }

    var animationTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { nowNanos ->
                if (lastNanos != 0L) {
                    val dt = (nowNanos - lastNanos) / 1_000_000_000f
                    animationTime += dt
                    for (p in particles) {
                        p.y -= p.speedY * (dt * 60f)
                        if (p.y < -0.06f) {
                            p.y = 1.06f
                            p.x = Random.nextFloat()
                        }
                    }
                }
                lastNanos = nowNanos
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Deep Luxurious Obsidian-Emerald Forest Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF06120E), // Deep midnight forest at top
                    Color(0xFF0C2018), // Rich botanical sage in middle
                    Color(0xFF143025)  // Warm moss-emerald at base
                )
            )
        )

        // 2. Upper Ambient Sage Halo behind the Logo Medallion
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x4D2D5E4E), // Luminous sage core
                    Color(0x24244D3F),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.28f),
                radius = w * 0.85f
            ),
            center = Offset(w * 0.5f, h * 0.28f),
            radius = w * 0.85f
        )

        // 3. Lower Warm Golden-Hour Aura behind Action Buttons
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x28C98A3A), // Warm amber gold
                    Color(0x12C98A3A),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.88f),
                radius = w * 0.95f
            ),
            center = Offset(w * 0.5f, h * 0.88f),
            radius = w * 0.95f
        )

        // 4. Render All Floating Light Particles
        for (p in particles) {
            val posX = (p.x + sin(p.phase + animationTime * p.swayFrequency) * p.swayAmplitude) * w
            val posY = p.y * h
            val pulse = (sin(p.phase * 2f + animationTime * 2.4f) + 1f) * 0.5f // 0..1
            val radiusPx = p.baseRadiusDp.dp.toPx()

            if (p.isGlowingOrb) {
                // Out-of-focus bokeh orb with radial gradient fade
                val orbAlpha = 0.12f + pulse * 0.18f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            p.color.copy(alpha = orbAlpha),
                            p.color.copy(alpha = orbAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(posX, posY),
                        radius = radiusPx
                    ),
                    radius = radiusPx,
                    center = Offset(posX, posY)
                )
            } else {
                // Crystalline floating star mote
                val moteAlpha = 0.35f + pulse * 0.55f

                // Outer soft luminous halo
                drawCircle(
                    color = p.color.copy(alpha = moteAlpha * 0.30f),
                    radius = radiusPx * 2.2f,
                    center = Offset(posX, posY)
                )
                // Core sparkling point
                drawCircle(
                    color = p.color.copy(alpha = moteAlpha),
                    radius = radiusPx,
                    center = Offset(posX, posY)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAIN WELCOME SCREEN
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    state: SetupState,
    onContinue: () -> Unit,
    onManualEntry: () -> Unit,
    onSignIn: () -> Unit,
    onBypassSetup: () -> Unit,
    onLanguageSelected: (String) -> Unit = {}
) {
    LaunchedEffect(state.isSetupComplete) {
        if (state.isSetupComplete) {
            onBypassSetup()
        }
    }

    LaunchedEffect(state.isEmailVerified) {
        if (state.isEmailVerified && !state.isSetupComplete) {
            onContinue()
        }
    }

    var showLanguageSheet by remember { mutableStateOf(false) }

    val languages = remember {
        listOf(
            "English" to "English (Default)",
            "Hindi" to "हिंदी (Hindi)",
            "Bengali" to "বাংলা (Bengali)",
            "Marathi" to "मराठी (Marathi)",
            "Telugu" to "తెలుగు (Telugu)",
            "Tamil" to "தமிழ் (Tamil)",
            "Gujarati" to "ગુજરાતી (Gujarati)",
            "Kannada" to "ಕನ್ನಡ (Kannada)",
            "Malayalam" to "മലയാളം (Malayalam)",
            "Punjabi" to "ਪੰਜਾਬੀ (Punjabi)",
            "Odia" to "ଓଡ଼ିଆ (Odia)"
        )
    }

    // Concentric breathing halo animation for the central logo emblem
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloScale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAlpha"
    )

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = Color(0xFF0C1F18),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0x666EE7B7)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Choose Preferred Language",
                    style = ZivaaTheme.typography.titleLarge.copy(fontFamily = InstrumentSerif),
                    fontSize = 24.sp,
                    color = Color(0xFFFBF9F5),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Zivaa's AI coach and daily plan will adapt to your language.",
                    style = ZivaaTheme.typography.bodyMedium,
                    color = Color(0xFFB4C8BF),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(languages) { (langCode, displayLabel) ->
                        val isSelected = state.preferredLanguage.equals(langCode, ignoreCase = true)
                        Surface(
                            onClick = {
                                onLanguageSelected(langCode)
                                showLanguageSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0x286EE7B7) else Color(0x0EFFFFFF),
                            border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF6EE7B7)) else BorderStroke(1.dp, Color(0x14FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = displayLabel,
                                        style = ZivaaTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color(0xFF6EE7B7) else Color(0xFFFBF9F5)
                                    )
                                }
                                if (isSelected) {
                                    Text("✓", color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Living Ambient Floating Particles Background
        AmbientFloatingParticlesBackground(modifier = Modifier.fillMaxSize())

        // Layer 2: Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row with Frosted Language Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = { showLanguageSheet = true },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0x2BFFFFFF),
                    border = BorderStroke(1.dp, Color(0x40FFFFFF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌐", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.preferredLanguage,
                            style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("▾", fontSize = 13.sp, color = Color(0xFFE2C07D))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.9f))

            // Central Emblem: Concentric Breathing Ring + Jewel "Z" Medallion
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Expanding breathing wave ring
                Box(
                    modifier = Modifier
                        .size((84 * haloScale).dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(1.5.dp, Color(0xFF6EE7B7).copy(alpha = haloAlpha), CircleShape)
                )

                // Secondary subtle gold ripple
                Box(
                    modifier = Modifier
                        .size((96 * (1f + (haloScale - 1f) * 0.7f)).dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE2C07D).copy(alpha = haloAlpha * 0.7f), CircleShape)
                )

                // Core Medallion
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(
                        2.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFE2C07D), // Champagne Gold
                                Color(0x44E2C07D),
                                Color(0xFF6EE7B7)  // Soft Emerald
                            )
                        )
                    ),
                    shadowElevation = 16.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF2E6353), // Jewel emerald
                                        Color(0xFF132F26)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Z",
                            color = Color(0xFFFFFDF8),
                            fontFamily = InstrumentSerif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Elegant Frosted Pill: "✨ AI ELDERCARE & LONGEVITY"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x26E2C07D),
                border = BorderStroke(1.dp, Color(0x4DE2C07D))
            ) {
                Text(
                    text = "✨  AI ELDERCARE & LONGEVITY",
                    color = Color(0xFFE2C07D),
                    style = ZivaaTheme.typography.eyebrow.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Display Headline
            Text(
                text = buildAnnotatedString {
                    append("Care tailored around\n")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color(0xFFE2C07D))) {
                        append("your life.")
                    }
                },
                style = ZivaaTheme.typography.displayLarge.copy(
                    fontSize = 35.sp,
                    lineHeight = 43.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Body Subtitle (High contrast for older adults)
            Text(
                text = "A gentle companion for your daily vitals, joint comfort, and peace of mind.",
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 23.sp
                ),
                color = Color(0xFFD4E2DC),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 310.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Reassurance Capsule
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0x1FFFFFFF),
                border = BorderStroke(1.dp, Color(0x28FFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Takes 2 Minutes · 5 Gentle Steps",
                        color = Color(0xFFE5EDE9),
                        style = ZivaaTheme.typography.eyebrow.copy(
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1.1f))

            // Primary CTA: 1-Tap Google Sign-In (Pristine Luxury White Button)
            Surface(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF234B3F),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Connecting with Google...",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1F1F1F)
                        )
                    } else {
                        Text(
                            text = "G",
                            style = ZivaaTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF4285F4),
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = ZivaaTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF1F1F1F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary CTA: Frosted Glass Button for Manual Entry
            Surface(
                onClick = onManualEntry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
                color = Color(0x1AFFFFFF),
                border = BorderStroke(1.dp, Color(0x38FFFFFF))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Or enter details manually",
                        style = ZivaaTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFFFBF9F5)
                    )
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.error,
                    color = Color(0xFFFF8B7B),
                    style = ZivaaTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
