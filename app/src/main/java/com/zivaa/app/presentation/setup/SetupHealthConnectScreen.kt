package com.zivaa.app.presentation.setup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.HealthConnectSupport
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlinx.coroutines.launch

@Composable
fun SetupHealthConnectScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val healthConnectManager = remember { HealthConnectManager(context) }

    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissionsLauncher = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.permissions)) {
            viewModel.selectWearable("Google Health Connect")
            Toast.makeText(context, "Health Connect connected successfully!", Toast.LENGTH_SHORT).show()
            onNext()
        } else {
            viewModel.selectWearable("Google Health Connect")
            Toast.makeText(context, "Some permissions granted. You can adjust them anytime.", Toast.LENGTH_SHORT).show()
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZivaaTheme.colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            ZivaaTopBar(stepNo = 4, totalSteps = 6, onBack = onBack)

            ZivaaHeader(
                label = "Step 4 of 6 · Device Sync · Optional",
                title = buildAnnotatedString {
                    append("Connect ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ZivaaTheme.colors.sage)) {
                        append("Health Connect")
                    }
                },
                subtitle = "Securely sync your daily activity and vitals from your favorite wearable directly to Zivaa. We use this data to tailor your daily plan."
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Big Icon / Illustration
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = ZivaaTheme.colors.sage.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            tint = ZivaaTheme.colors.sage,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Feature List in clean elevated cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HealthFeatureCard(
                    icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    title = "Steps & Daily Movement",
                    description = "Sync step counts from your phone or watch automatically.",
                    color = ZivaaTheme.colors.sage
                )
                HealthFeatureCard(
                    icon = Icons.Rounded.NightsStay,
                    title = "Sleep & Rest Tracking",
                    description = "Analyze restful sleep stages and recovery baselines.",
                    color = ZivaaTheme.colors.clay
                )
                HealthFeatureCard(
                    icon = Icons.Rounded.Favorite,
                    title = "Heart Rate & Vitals",
                    description = "Keep track of resting heart rate and cardiovascular trends.",
                    color = ZivaaTheme.colors.rose
                )
                HealthFeatureCard(
                    icon = Icons.Rounded.Security,
                    title = "Safe and Secure",
                    description = "Your health data is processed safely and securely.",
                    color = ZivaaTheme.colors.amber
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom CTAs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ZivaaButton(
                text = "Connect Health Connect",
                onClick = {
                    coroutineScope.launch {
                        val support = healthConnectManager.checkHealthConnectSupportAndRedirect()
                        if (support == HealthConnectSupport.AVAILABLE) {
                            if (healthConnectManager.hasAllPermissions()) {
                                viewModel.selectWearable("Google Health Connect")
                                Toast.makeText(context, "Health Connect is already connected!", Toast.LENGTH_SHORT).show()
                                onNext()
                            } else {
                                requestPermissionsLauncher.launch(healthConnectManager.permissions)
                            }
                        } else if (support == HealthConnectSupport.INSTALL_REQUIRED) {
                            Toast.makeText(context, "Please install Health Connect from the Play Store.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    viewModel.selectWearable("None")
                    onNext()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for now",
                    color = ZivaaTheme.colors.inkMute,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
fun HealthFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ZivaaTheme.colors.bgElev,
        border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = ZivaaTheme.colors.ink,
                    style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = ZivaaTheme.colors.inkSoft,
                    style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp)
                )
            }
        }
    }
}
