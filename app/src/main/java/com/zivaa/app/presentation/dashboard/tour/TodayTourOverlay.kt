package com.zivaa.app.presentation.dashboard.tour

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlin.math.roundToInt

@Composable
fun TodayTourOverlay(
    tourState: TodayTourState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = tourState.isTourActive && tourState.currentStep != null,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        val currentStep = tourState.currentStep ?: return@AnimatedVisibility
        val density = LocalDensity.current

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Intercept all touches to protect underlying screen */ }
                )
        ) {
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()

            val rawBounds = tourState.boundsMap[currentStep]

            // Remember last valid bounds to prevent awkward jumps to center during fast scrolls
            var lastKnownBounds by remember { mutableStateOf<Rect?>(null) }
            if (rawBounds != null) {
                lastKnownBounds = rawBounds
            }

            val targetRect = rawBounds ?: lastKnownBounds ?: Rect(
                left = screenWidthPx * 0.05f,
                top = screenHeightPx * 0.15f,
                right = screenWidthPx * 0.95f,
                bottom = screenHeightPx * 0.40f
            )

            val paddingPx = with(density) { currentStep.padding.toPx() }
            val rawCornerRadiusPx = with(density) { currentStep.cornerRadius.toPx() }
            val targetCutoutWidth = (targetRect.width + paddingPx * 2f).coerceAtLeast(0f)
            val targetCutoutHeight = (targetRect.height + paddingPx * 2f).coerceAtLeast(0f)
            val maxPillRadius = minOf(targetCutoutWidth, targetCutoutHeight) / 2f
            val targetCornerRadiusPx = if (currentStep.cornerRadius > 100.dp) maxPillRadius else rawCornerRadiusPx

            // Premium Material 3 Emphasized Decelerate motion spec (440ms)
            val motionEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
            val animSpec = tween<Float>(440, easing = motionEasing)

            // Smooth fluid spotlight glide between targets
            val animLeft by animateFloatAsState(
                targetValue = targetRect.left - paddingPx,
                animationSpec = animSpec,
                label = "tour_left"
            )
            val animTop by animateFloatAsState(
                targetValue = targetRect.top - paddingPx,
                animationSpec = animSpec,
                label = "tour_top"
            )
            val animRight by animateFloatAsState(
                targetValue = targetRect.right + paddingPx,
                animationSpec = animSpec,
                label = "tour_right"
            )
            val animBottom by animateFloatAsState(
                targetValue = targetRect.bottom + paddingPx,
                animationSpec = animSpec,
                label = "tour_bottom"
            )
            val animCornerRadius by animateFloatAsState(
                targetValue = targetCornerRadiusPx,
                animationSpec = animSpec,
                label = "tour_corner"
            )

            // Animated pulsing beacon and breathing luminous glow
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_beacon")
            val glowBreath by infiniteTransition.animateFloat(
                initialValue = 0.78f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_breath"
            )
            val pulseSpread by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse_spread"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.55f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse_alpha"
            )

            // Dimmed Scrim with Glowing Spotlight Cutout
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            ) {
                // 1. Extra Deep Dark Scrim (High contrast dimming)
                drawRect(color = Color.Black.copy(alpha = 0.88f))

                val cutoutWidth = (animRight - animLeft).coerceAtLeast(0f)
                val cutoutHeight = (animBottom - animTop).coerceAtLeast(0f)
                val safeCornerRadius = animCornerRadius.coerceAtMost(minOf(cutoutWidth, cutoutHeight) / 2f)

                // 2. Luminous Multi-Layer Outer Glow Aura (radiates into the surrounding dark scrim)
                // Layer 1: Wide ambient diffusion
                val spread1 = 16.dp.toPx()
                val rad1 = safeCornerRadius + spread1
                drawRoundRect(
                    color = Color(0xFF52B788).copy(alpha = 0.10f * glowBreath),
                    topLeft = Offset(animLeft - spread1, animTop - spread1),
                    size = Size(cutoutWidth + spread1 * 2f, cutoutHeight + spread1 * 2f),
                    cornerRadius = CornerRadius(rad1, rad1),
                    style = Stroke(width = 14.dp.toPx())
                )

                // Layer 2: Mid-range bloom
                val spread2 = 10.dp.toPx()
                val rad2 = safeCornerRadius + spread2
                drawRoundRect(
                    color = Color(0xFF52B788).copy(alpha = 0.20f * glowBreath),
                    topLeft = Offset(animLeft - spread2, animTop - spread2),
                    size = Size(cutoutWidth + spread2 * 2f, cutoutHeight + spread2 * 2f),
                    cornerRadius = CornerRadius(rad2, rad2),
                    style = Stroke(width = 9.dp.toPx())
                )

                // Layer 3: Vibrant inner aura
                val spread3 = 5.dp.toPx()
                val rad3 = safeCornerRadius + spread3
                drawRoundRect(
                    color = Color(0xFF74C69D).copy(alpha = 0.35f * glowBreath),
                    topLeft = Offset(animLeft - spread3, animTop - spread3),
                    size = Size(cutoutWidth + spread3 * 2f, cutoutHeight + spread3 * 2f),
                    cornerRadius = CornerRadius(rad3, rad3),
                    style = Stroke(width = 5.dp.toPx())
                )

                // Layer 4: Close core glow
                val spread4 = 2.dp.toPx()
                val rad4 = safeCornerRadius + spread4
                drawRoundRect(
                    color = Color(0xFF95D5B2).copy(alpha = 0.52f * glowBreath),
                    topLeft = Offset(animLeft - spread4, animTop - spread4),
                    size = Size(cutoutWidth + spread4 * 2f, cutoutHeight + spread4 * 2f),
                    cornerRadius = CornerRadius(rad4, rad4),
                    style = Stroke(width = 3.dp.toPx())
                )

                // 3. Punch crystal-clear hole over the target element
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(animLeft, animTop),
                    size = Size(cutoutWidth, cutoutHeight),
                    cornerRadius = CornerRadius(safeCornerRadius, safeCornerRadius),
                    blendMode = BlendMode.Clear
                )

                // 4. Outer pulsing beacon expanding from cutout rim
                drawRoundRect(
                    color = Color(0xFF52B788).copy(alpha = pulseAlpha),
                    topLeft = Offset(animLeft - pulseSpread, animTop - pulseSpread),
                    size = Size(cutoutWidth + pulseSpread * 2f, cutoutHeight + pulseSpread * 2f),
                    cornerRadius = CornerRadius(safeCornerRadius + pulseSpread, safeCornerRadius + pulseSpread),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 5. Solid crisp accent border around cutout rim
                drawRoundRect(
                    color = Color(0xFF52B788),
                    topLeft = Offset(animLeft, animTop),
                    size = Size(cutoutWidth, cutoutHeight),
                    cornerRadius = CornerRadius(safeCornerRadius, safeCornerRadius),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Measure actual rendered height of card dynamically to prevent any overlap
            var actualCardHeightPx by remember { mutableStateOf(0f) }
            val effectiveCardHeightPx = if (actualCardHeightPx > 0f) actualCardHeightPx else with(density) { 170.dp.toPx() }
            val spacingPx = with(density) { 16.dp.toPx() }
            val topHeaderSafePx = with(density) { 55.dp.toPx() }

            // Precise step-specific vertical placement
            // Directly targeting clean, stable destination positions so transitions glide monotonically without bouncing
            val cardOffsetY = when (currentStep) {
                TodayTourStep.HERO_CARD -> {
                    // Hero briefing is at top: place explanation card comfortably below it
                    targetRect.bottom + spacingPx
                }
                TodayTourStep.GOALS_CARD -> {
                    // Goals card is in the middle: place card cleanly at top of screen
                    topHeaderSafePx
                }
                TodayTourStep.STATS_STRIP -> {
                    // Stats strip is at the bottom: place card cleanly at top of screen
                    topHeaderSafePx
                }
                TodayTourStep.COACH_BUTTON -> {
                    // The Coach button is in bottom-right:
                    // Place card in upper-middle area (y ~ 36%), well clear of the button
                    (screenHeightPx * 0.36f).coerceAtLeast(topHeaderSafePx)
                }
                TodayTourStep.QUICK_ACTIONS -> {
                    // The '+' button is in bottom navigation bar:
                    // Place card in upper-middle area (y ~ 36%), completely clear of bottom bar
                    (screenHeightPx * 0.36f).coerceAtLeast(topHeaderSafePx)
                }
            }

            // Clamping ensures the card is mathematically guaranteed to stay fully within the screen viewport
            val clampedCardOffsetY = cardOffsetY.coerceIn(
                with(density) { 45.dp.toPx() },
                (screenHeightPx - effectiveCardHeightPx - with(density) { 16.dp.toPx() }).coerceAtLeast(with(density) { 45.dp.toPx() })
            )

            // Silky smooth vertical translation for explanation card
            val animCardOffsetY by animateFloatAsState(
                targetValue = clampedCardOffsetY,
                animationSpec = animSpec,
                label = "tour_card_offset"
            )

            // Compact, Sleek Explanation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, animCardOffsetY.roundToInt()) }
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ZivaaTheme.colors.bgElev,
                    border = BorderStroke(1.dp, ZivaaTheme.colors.line),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            actualCardHeightPx = coords.size.height.toFloat()
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        // Top Header Row: Step Badge + Skip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = ZivaaTheme.colors.sage.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Step ${currentStep.stepIndex} of ${TodayTourStep.TOTAL_STEPS}",
                                    style = ZivaaTheme.typography.meta.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ZivaaTheme.colors.accent
                                )
                            }

                            TextButton(
                                onClick = { tourState.finishTour() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Skip",
                                    style = ZivaaTheme.typography.meta.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = ZivaaTheme.colors.inkMute
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Animated Step Title & Description
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = {
                                val isForward = targetState.stepIndex >= initialState.stepIndex
                                val slideDistance = 32
                                if (isForward) {
                                    (slideInHorizontally(animationSpec = tween(340, easing = motionEasing)) { slideDistance } +
                                     fadeIn(animationSpec = tween(260)))
                                        .togetherWith(
                                            slideOutHorizontally(animationSpec = tween(200, easing = FastOutLinearInEasing)) { -slideDistance } +
                                            fadeOut(animationSpec = tween(160))
                                        )
                                } else {
                                    (slideInHorizontally(animationSpec = tween(340, easing = motionEasing)) { -slideDistance } +
                                     fadeIn(animationSpec = tween(260)))
                                        .togetherWith(
                                            slideOutHorizontally(animationSpec = tween(200, easing = FastOutLinearInEasing)) { slideDistance } +
                                            fadeOut(animationSpec = tween(160))
                                        )
                                }
                            },
                            label = "tour_step_content"
                        ) { step ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = step.title,
                                    style = ZivaaTheme.typography.cardTitle.copy(
                                        fontSize = 16.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ZivaaTheme.colors.ink
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = step.description,
                                    style = ZivaaTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    color = ZivaaTheme.colors.inkMute
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions Row: Back + Step Dots + Next / Done
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentStep.stepIndex > 1) {
                                TextButton(
                                    onClick = { tourState.previousStep() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Back",
                                        style = ZivaaTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = ZivaaTheme.colors.inkMute
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }

                            // 5 Elegant Step Indicator Dots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(TodayTourStep.TOTAL_STEPS) { index ->
                                    val isSelected = index + 1 == currentStep.stepIndex
                                    val dotWidth by animateDpAsState(
                                        targetValue = if (isSelected) 18.dp else 5.dp,
                                        animationSpec = tween(300, easing = motionEasing),
                                        label = "dot_width_$index"
                                    )
                                    val dotColor by animateColorAsState(
                                        targetValue = if (isSelected) ZivaaTheme.colors.accent else ZivaaTheme.colors.inkMute.copy(alpha = 0.22f),
                                        animationSpec = tween(300),
                                        label = "dot_color_$index"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(dotWidth)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(dotColor)
                                    )
                                }
                            }

                            Button(
                                onClick = { tourState.nextStep() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZivaaTheme.colors.accent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(999.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = if (currentStep.stepIndex == TodayTourStep.TOTAL_STEPS) "Explore Today" else "Next",
                                    style = ZivaaTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
