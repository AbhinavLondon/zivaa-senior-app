package com.zivaa.app.presentation.setup.wearable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.setup.ZivaaButton
import com.zivaa.app.presentation.setup.ZivaaHeader
import com.zivaa.app.presentation.setup.ZivaaTopBar
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun WearableBridgeGuideScreen(
    brand: WearableBrand,
    onCheckAgain: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sageColor = ZivaaTheme.colors.sage

    fun highlightHealthConnect(text: String): AnnotatedString {
        val target = "Health Connect"
        val idx = text.indexOf(target, ignoreCase = true)
        return buildAnnotatedString {
            if (idx >= 0) {
                append(text.substring(0, idx))
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = sageColor)) {
                    append(text.substring(idx, idx + target.length))
                }
                append(text.substring(idx + target.length))
            } else {
                append(text)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        ZivaaTopBar(stepNo = 4, totalSteps = 6, onBack = onBack)

        ZivaaHeader(
            label = "Step 4 of 6 · One Quick Switch",
            title = highlightHealthConnect(brand.bridgeTitle),
            subtitle = buildAnnotatedString {
                append("We see ${brand.displayName} is installed on your phone. To let Zivaa read your heart and sleep numbers, turn on the ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = sageColor, fontWeight = FontWeight.SemiBold)) {
                    append("Health Connect")
                }
                append(" switch.")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Instructional Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = ZivaaTheme.colors.bgElev,
            border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "How to turn it on:",
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = ZivaaTheme.colors.ink
                )

                Spacer(modifier = Modifier.height(16.dp))

                brand.bridgeSteps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = ZivaaTheme.colors.sage.copy(alpha = 0.16f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    style = ZivaaTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ZivaaTheme.colors.sageInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = step,
                            style = ZivaaTheme.typography.bodyMedium,
                            color = ZivaaTheme.colors.ink,
                            modifier = Modifier.weight(1f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Open Companion App Button
        ButtonWithIcon(
            text = "Open ${brand.displayName}",
            onClick = {
                WearableCompanionDetector.openAppOrPlayStore(context, brand.packageName)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Check Again Primary Button
        ZivaaButton(
            text = "I've Turned It On — Check Again",
            onClick = onCheckAgain
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ask Family Member Button
        OutlinedButton(
            onClick = {
                WearableCompanionDetector.shareCaregiverHelp(
                    context,
                    brand,
                    "I need help turning on the Health Connect switch in ${brand.displayName}."
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
            border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
        ) {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = null,
                tint = ZivaaTheme.colors.inkSoft,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ask a Family Member for Help",
                style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ZivaaTheme.colors.inkSoft
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skip button
        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Set up later (Use Phone Step Tracker)",
                style = ZivaaTheme.typography.bodyMedium,
                color = ZivaaTheme.colors.inkMute
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ButtonWithIcon(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
        color = ZivaaTheme.colors.bgElev,
        border = BorderStroke(1.5.dp, ZivaaTheme.colors.sage)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint = ZivaaTheme.colors.sage,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = ZivaaTheme.colors.sage
            )
        }
    }
}
