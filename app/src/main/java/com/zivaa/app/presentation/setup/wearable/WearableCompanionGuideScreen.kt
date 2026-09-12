package com.zivaa.app.presentation.setup.wearable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.setup.ZivaaButton
import com.zivaa.app.presentation.setup.ZivaaHeader
import com.zivaa.app.presentation.setup.ZivaaTopBar
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun WearableCompanionGuideScreen(
    brand: WearableBrand,
    isAppInstalled: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        ZivaaTopBar(stepNo = 4, totalSteps = 6, onBack = onBack)

        ZivaaHeader(
            label = "Step 4 of 6 · Pair Your Device",
            title = "Pair your ${brand.displayName}",
            subtitle = if (isAppInstalled) {
                "Open ${brand.displayName} to ensure your watch or ring is paired via Bluetooth."
            } else {
                "Your watch connects to your phone through its free companion app. Let's install it first."
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: Install from Play Store (if not installed)
        if (!isAppInstalled) {
            Surface(
                onClick = { WearableCompanionDetector.openPlayStore(context, brand.playStorePackage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
                color = ZivaaTheme.colors.sage
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = ZivaaTheme.colors.sageInk,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Install ${brand.displayName} (Free)",
                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = ZivaaTheme.colors.sageInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Instructional Card: 3 simple pairing steps
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = ZivaaTheme.colors.bgElev,
            border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Simple Pairing Steps:",
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = ZivaaTheme.colors.ink
                )

                Spacer(modifier = Modifier.height(16.dp))

                brand.pairingSteps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = ZivaaTheme.colors.clay.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    style = ZivaaTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ZivaaTheme.colors.ink
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

        // Open app button if already installed
        if (isAppInstalled) {
            ZivaaButton(
                text = "Open ${brand.displayName} to Pair",
                onClick = { WearableCompanionDetector.openAppOrPlayStore(context, brand.packageName) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Primary Next Button
        ZivaaButton(
            text = "My Watch is Paired — Next Step",
            onClick = onNext
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ask Family Member Button
        OutlinedButton(
            onClick = {
                WearableCompanionDetector.shareCaregiverHelp(
                    context,
                    brand,
                    "I am pairing my ${brand.displayName} with my phone and need a hand."
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

        // Skip Button
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
