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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════
// AMBIENT PARTICLES: RISING FROM BOTTOM, RANDOMLY GATHERING & SETTLING
// ═══════════════════════════════════════════════════════════════════

private class RisingAndGatheringParticle(
    val startXRatio: Float,
    val startYRatio: Float,
    val baseRadiusDp: Float,
    val riseSpeed: Float,
    val swayAmpDp: Float,
    val swayFreq: Float,
    val gatherDelay: Float,
    val gatherDuration: Float,
    val targetOrbitRadiusDp: Float,
    val curveDirection: Float,
    val orbitSpeed: Float,
    val color: Color,
    val isGlowingOrb: Boolean,
    val phase: Float
)

@Composable
fun AmbientFloatingParticlesBackground(
    emblemCenter: Offset = Offset.Unspecified,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val colors = listOf(
            Color(0xFF7ED8AF), // Luminous Zivaa Green
            Color(0xFF52B788), // Rich Zivaa Emerald
            Color(0xFFE2C07D), // Champagne Gold
            Color(0xFFA7F3D0), // Soft Mint Sage
            Color(0xFFF3DFA2), // Warm Amber Gold
            Color(0xFFFFFDF8)  // Starlight Ivory
        )
        List(52) { i ->
            val isOrb = (i % 7 == 0)
            val radius = if (isOrb) {
                Random.nextFloat() * 6f + 11f // 11..17dp
            } else {
                Random.nextFloat() * 2.5f + 1.4f // 1.4..3.9dp
            }

            // Distribute settled orbit radius across 3 concentric tiers aligned with the 3 big rings:
            // Tier 1 ~76dp, Tier 2 ~116dp, Tier 3 ~156dp
            val targetRadius = when (i % 3) {
                0 -> Random.nextFloat() * 16f + 68f // 68..84dp (around Ring 1)
                1 -> Random.nextFloat() * 20f + 106f // 106..126dp (around Ring 2)
                else -> Random.nextFloat() * 24f + 144f // 144..168dp (around Ring 3)
            }

            RisingAndGatheringParticle(
                startXRatio = Random.nextFloat() * 0.90f + 0.05f,
                startYRatio = Random.nextFloat() * 0.70f + 0.45f, // Rising from lower screen / below bottom
                baseRadiusDp = radius,
                riseSpeed = Random.nextFloat() * 0.045f + 0.028f, // Slow, peaceful, stately rise
                swayAmpDp = Random.nextFloat() * 10f + 4f,
                swayFreq = Random.nextFloat() * 1.0f + 0.7f,
                gatherDelay = Random.nextFloat() * 2.2f + 1.2f, // Randomly begins gathering between 1.2s and 3.4s
                gatherDuration = Random.nextFloat() * 0.7f + 1.9f, // 1.9s..2.6s gentle transition
                targetOrbitRadiusDp = targetRadius,
                curveDirection = if (Random.nextBoolean()) 1f else -1f,
                orbitSpeed = (Random.nextFloat() * 0.15f + 0.18f) * if (Random.nextBoolean()) 1f else -1f, // Serene slow orbit
                color = colors[i % colors.size],
                isGlowingOrb = isOrb,
                phase = Random.nextFloat() * (Math.PI.toFloat() * 2f)
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
                }
                lastNanos = nowNanos
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cx = if (emblemCenter != Offset.Unspecified && emblemCenter.x > 0f) emblemCenter.x else w * 0.5f
        val cy = if (emblemCenter != Offset.Unspecified && emblemCenter.y > 0f) emblemCenter.y else h * 0.27f
        val centerOffset = Offset(cx, cy)

        // 1. Pure OLED Black Background with subtle depth
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF000000), // Pure Black
                    Color(0xFF050806), // Ultra subtle deep midnight hue
                    Color(0xFF000000)  // Pure Black
                )
            )
        )

        // 2. Upper Ambient Luminous Zivaa Green Halo behind Emblem
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x38234B3F), // Soft Zivaa Green halo
                    Color(0x14102D24),
                    Color.Transparent
                ),
                center = centerOffset,
                radius = w * 0.80f
            ),
            center = centerOffset,
            radius = w * 0.80f
        )

        // 3. Lower Ambient Aura framing action buttons
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x181F4D3F),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.90f),
                radius = w * 0.90f
            ),
            center = Offset(w * 0.5f, h * 0.90f),
            radius = w * 0.90f
        )

        // 4. Three Big Thin Concentric Circles around the Logo
        // Emerging smoothly as particles settle down (between ~2.0s and 3.8s)
        val circlesProgress = ((animationTime - 2.0f) / 1.8f).coerceIn(0f, 1f)
        val circlesEase = circlesProgress * circlesProgress * (3f - 2f * circlesProgress)

        if (circlesEase > 0.005f) {
            // Ring 1 (Inner big circle): radius ~76dp - Light White
            val r1 = 76.dp.toPx() + sin(animationTime * 1.0f) * 1.2.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = circlesEase * 0.42f),
                radius = r1,
                center = centerOffset,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )

            // Ring 2 (Middle big circle): radius ~116dp - Light White
            val r2 = 116.dp.toPx() + sin(animationTime * 0.8f + 1.2f) * 1.6.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = circlesEase * 0.28f),
                radius = r2,
                center = centerOffset,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.95f.dp.toPx())
            )

            // Ring 3 (Outer big circle): radius ~156dp - Light White
            val r3 = 156.dp.toPx() + sin(animationTime * 0.6f + 2.4f) * 2.0.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = circlesEase * 0.18f),
                radius = r3,
                center = centerOffset,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.85f.dp.toPx())
            )
        }

        // 5. Render All Rising, Gathering, and Settling Particles
        for (p in particles) {
            val posX: Float
            val posY: Float
            val settleFactor: Float

            if (animationTime < p.gatherDelay) {
                // Phase 1: Rising slowly from the bottom to the top
                val freeY = (p.startYRatio * h) - (p.riseSpeed * animationTime * h)
                val freeX = (p.startXRatio * w) + sin(p.phase + animationTime * p.swayFreq) * p.swayAmpDp.dp.toPx()
                posX = freeX
                posY = freeY
                settleFactor = 0f
            } else {
                // Phase 2 & 3: Randomly starts gathering together, then settles around the logo
                val startGatherY = (p.startYRatio * h) - (p.riseSpeed * p.gatherDelay * h)
                val startGatherX = (p.startXRatio * w) + sin(p.phase + p.gatherDelay * p.swayFreq) * p.swayAmpDp.dp.toPx()

                val dx = startGatherX - cx
                val dy = startGatherY - cy
                val distStart = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val phiStart = atan2(dy, dx)

                val tg = animationTime - p.gatherDelay
                val u = (tg / p.gatherDuration).coerceIn(0f, 1f)
                val s = u * u * (3f - 2f * u) // Smoothstep
                settleFactor = s

                val targetOrbitPx = p.targetOrbitRadiusDp.dp.toPx()
                val currentRadius = if (u < 1f) {
                    distStart * (1f - s) + targetOrbitPx * s
                } else {
                    targetOrbitPx + sin(p.phase + animationTime * 1.2f) * 3.5.dp.toPx()
                }

                // Gentle, graceful curve towards orbit (NO rapid spinning!)
                val curveAngle = (p.curveDirection * 0.45f) * s // ~25 degree subtle arc
                val orbitAngle = p.orbitSpeed * tg // very gentle slow orbit
                val currentAngle = phiStart + curveAngle + orbitAngle

                posX = cx + currentRadius * cos(currentAngle)
                posY = cy + currentRadius * sin(currentAngle)
            }

            val pulse = (sin(p.phase * 2f + animationTime * 2.0f) + 1f) * 0.5f // 0..1
            val settleBoost = 1f + settleFactor * 0.35f
            val radiusPx = p.baseRadiusDp.dp.toPx()

            if (p.isGlowingOrb) {
                // Out-of-focus bokeh orb with radial gradient fade
                val orbAlpha = (0.09f + pulse * 0.15f) * settleBoost
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            p.color.copy(alpha = orbAlpha.coerceAtMost(0.40f)),
                            p.color.copy(alpha = (orbAlpha * 0.35f).coerceAtMost(0.20f)),
                            Color.Transparent
                        ),
                        center = Offset(posX, posY),
                        radius = radiusPx
                    ),
                    radius = radiusPx,
                    center = Offset(posX, posY)
                )
            } else {
                // Crystalline floating starlight mote
                val moteAlpha = (0.35f + pulse * 0.48f) * settleBoost

                // Outer soft luminous halo
                drawCircle(
                    color = p.color.copy(alpha = (moteAlpha * 0.30f).coerceAtMost(0.42f)),
                    radius = radiusPx * 2.2f,
                    center = Offset(posX, posY)
                )
                // Core sparkling point
                drawCircle(
                    color = p.color.copy(alpha = moteAlpha.coerceAtMost(1.0f)),
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
    var userInitiatedSignIn by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSetupComplete) {
        if (userInitiatedSignIn && state.isSetupComplete) {
            onBypassSetup()
        }
    }

    LaunchedEffect(state.isEmailVerified) {
        if (userInitiatedSignIn && state.isEmailVerified && !state.isSetupComplete) {
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

    var emblemCenter by remember { mutableStateOf(Offset.Unspecified) }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = Color(0xFF0D1310),
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
        // Layer 1: Living Ambient Particles: Gathering, Swirling & Orbiting around Zivaa
        AmbientFloatingParticlesBackground(
            emblemCenter = emblemCenter,
            modifier = Modifier.fillMaxSize()
        )

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
                        Text("▾", fontSize = 13.sp, color = Color(0xFF7ED8AF))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.75f))

            // Central Emblem: Concentric Breathing Ring + Signature Zivaa Green Medallion
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .onGloballyPositioned { coordinates ->
                        val pos = coordinates.positionInRoot()
                        val size = coordinates.size
                        emblemCenter = Offset(pos.x + size.width / 2f, pos.y + size.height / 2f)
                    }
            ) {
                // Ambient Zivaa Green glow behind medallion
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x4D52B788),
                                    Color(0x22234B3F),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Core Medallion in Authentic Zivaa Green
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(
                        2.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF7ED8AF), // Luminous Zivaa Green
                                Color(0xFF234B3F), // Signature Zivaa Green
                                Color(0xFF52B788)  // Vibrant Zivaa Emerald
                            )
                        )
                    ),
                    shadowElevation = 18.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF2E6353), // Radiant Zivaa Green
                                        Color(0xFF234B3F), // Classic Zivaa Green
                                        Color(0xFF16352C)  // Deep Zivaa Green shadow
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

            Spacer(modifier = Modifier.height(14.dp))

            // Brand Name right under Z icon
            Text(
                text = "Zivaa",
                style = ZivaaTheme.typography.displayMedium.copy(
                    fontFamily = InstrumentSerif,
                    fontSize = 32.sp,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFFFBF9F5)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Display Headline: "Welcome to your Longevity companion."
            Text(
                text = buildAnnotatedString {
                    append("Welcome to your\n")
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            fontFamily = InstrumentSerif,
                            color = Color(0xFF7ED8AF), // Luminous Zivaa Green
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append("Longevity")
                    }
                    append(" companion.")
                },
                style = ZivaaTheme.typography.displayLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    fontFamily = InstrumentSerif,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFFFBF9F5),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-headline: "Care tailored around your life."
            Text(
                text = "Care tailored around your life.",
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFFCCE0D6),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1.0f))

            // Primary CTA: 1-Tap Google Sign-In (Pristine Luxury White Button)
            Surface(
                onClick = {
                    userInitiatedSignIn = true
                    onSignIn()
                },
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
