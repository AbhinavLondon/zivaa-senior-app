package com.zivaa.app.presentation.setup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.plan.PlanSetupTheme
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import kotlinx.coroutines.delay

@Composable
fun PlanRevealScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNavigateToDashboard: () -> Unit
) {
    var isSynthesizing by remember { mutableStateOf(true) }
    var synthesisStep by remember { mutableStateOf(0) }
    var pendingNavigation by remember { mutableStateOf(false) }

    val synthesisTexts = listOf(
        "Analyzing your movement and daily rhythms...",
        "Customizing gentle step milestones...",
        "Setting up your AI Health Coach...",
        "Your personalized daily plan is ready!"
    )

    LaunchedEffect(Unit) {
        // Automatically initiate save in background while animating synthesis
        viewModel.finishSetup()
        for (i in 0 until synthesisTexts.size - 1) {
            delay(700)
            synthesisStep = i + 1
        }
        delay(600)
        isSynthesizing = false
    }

    LaunchedEffect(state.isSetupComplete, pendingNavigation) {
        if (pendingNavigation && state.isSetupComplete) {
            onNavigateToDashboard()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LocalZivaaColors.current.bg
    ) {
        AnimatedContent(
            targetState = isSynthesizing,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "SynthesisTransition"
        ) { synthesizing ->
            if (synthesizing) {
                // Live Synthesis Animation State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(900, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Surface(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = LocalZivaaColors.current.sage.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier.size(76.dp),
                                shape = CircleShape,
                                color = LocalZivaaColors.current.sage
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = LocalZivaaColors.current.sageInk,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "Synthesizing Your Plan",
                        fontFamily = Manrope,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalZivaaColors.current.ink,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = synthesisTexts.getOrElse(synthesisStep) { synthesisTexts.last() },
                        fontFamily = Manrope,
                        fontSize = 15.sp,
                        color = LocalZivaaColors.current.inkSoft,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = LocalZivaaColors.current.sage,
                        strokeWidth = 3.dp
                    )
                }
            } else {
                // Gold Standard Personalized Plan Reveal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Success Badge
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = LocalZivaaColors.current.leaf.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = LocalZivaaColors.current.leaf
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val userName = if (state.name.isNotBlank()) state.name.trim().split(" ").first() else ""
                        val greeting = if (userName.isNotBlank()) "All set, $userName" else "All set"

                        Text(
                            text = greeting.toEyebrowTitleCase(),
                            style = ZivaaTheme.typography.eyebrow,
                            color = LocalZivaaColors.current.eyebrow
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Your daily rhythm is\n")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("ready.")
                                }
                            },
                            style = ZivaaTheme.typography.displayLarge,
                            color = LocalZivaaColors.current.ink,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Calm, proactive, and shaped around your natural pace starting tomorrow morning.",
                            style = ZivaaTheme.typography.bodyMedium,
                            color = LocalZivaaColors.current.inkSoft,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Plan Summary Cards
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Focus & Goal
                            val focusTitle = when (state.primaryFocus) {
                                "mobility" -> "Mobility & Joint Health"
                                "energy" -> "Steady Daily Energy"
                                "longevity" -> "Healthy Longevity"
                                "sleep" -> "Restful Sleep"
                                "condition" -> "Condition Management"
                                "recovery" -> "Gentle Recovery"
                                else -> "Healthy Aging & Vitality"
                            }
                            PlanSummaryCard(
                                icon = Icons.Default.Favorite,
                                toneColor = LocalZivaaColors.current.rose,
                                title = "Primary Focus",
                                subtitle = focusTitle
                            )

                            // Movement & Steps
                            val movementPace = state.movementLevel ?: "Gentle"
                            val stepsText = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(state.stepsGoal)
                            PlanSummaryCard(
                                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                                toneColor = LocalZivaaColors.current.leaf,
                                title = "Daily Movement",
                                subtitle = "$movementPace pace · $stepsText steps goal"
                            )

                            // Rhythm Timings
                            val wakeText = state.wakeTime?.let { "Wake $it" } ?: "Natural wake"
                            val dietText = state.dietType ?: "Balanced diet"
                            PlanSummaryCard(
                                icon = Icons.Default.WbSunny,
                                toneColor = LocalZivaaColors.current.amber,
                                title = "Natural Rhythm",
                                subtitle = "$wakeText · $dietText"
                            )

                            // AI Health Coach & Care Circle
                            val circleCount = state.familyMembers.size
                            val circleText = if (circleCount > 0) "$circleCount in Family Care Circle" else "Family updates optional"
                            val langText = state.preferredLanguage.ifBlank { "English" }
                            PlanSummaryCard(
                                icon = Icons.Default.Psychology,
                                toneColor = LocalZivaaColors.current.sage,
                                title = "AI Health Coach",
                                subtitle = "Ready in $langText · $circleText"
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Nothing here is rigid — change any answer anytime, and Zivaa adapts quietly.",
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = LocalZivaaColors.current.inkMute,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Bottom Primary CTA
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 16.dp)
                    ) {
                        Surface(
                            onClick = {
                                if (state.isSetupComplete) {
                                    onNavigateToDashboard()
                                } else {
                                    pendingNavigation = true
                                    if (!state.isSubmitting) {
                                        viewModel.finishSetup()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
                            color = LocalZivaaColors.current.sage
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (state.isSubmitting || (pendingNavigation && !state.isSetupComplete)) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = LocalZivaaColors.current.sageInk,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Saving your plan...",
                                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = LocalZivaaColors.current.sageInk
                                    )
                                } else {
                                    Text(
                                        text = "Step into Today",
                                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = LocalZivaaColors.current.sageInk
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = LocalZivaaColors.current.sageInk,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (state.error != null) {
                            Text(
                                text = state.error,
                                color = LocalZivaaColors.current.rose,
                                style = ZivaaTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanSummaryCard(
    icon: ImageVector,
    toneColor: Color,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x0F000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = PlanSetupTheme.BgElev,
        border = BorderStroke(0.5.dp, PlanSetupTheme.Line)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = toneColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = toneColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.toEyebrowTitleCase(),
                    style = ZivaaTheme.typography.eyebrow,
                    color = PlanSetupTheme.Eyebrow
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PlanSetupTheme.Ink
                )
            }
        }
    }
}
