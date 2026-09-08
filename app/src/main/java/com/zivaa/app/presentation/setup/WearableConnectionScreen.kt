package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.PermissionController
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.HealthConnectSupport

data class WearableOption(
    val title: String,
    val subtitle: String?,
    val icon: ImageVector?,
    val colorProvider: @Composable () -> Color
)

@Composable
fun WearableConnectionScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val healthConnectManager = androidx.compose.runtime.remember { HealthConnectManager(context) }
    
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissionsLauncher = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.permissions)) {
            onNext() // Permissions granted, proceed
        } else {
            Toast.makeText(context, "Health Connect permissions are required to sync your data.", Toast.LENGTH_SHORT).show()
        }
    }
    val WEARABLES = listOf(
        WearableOption("Google Health Connect", "Syncs Fitbit, Samsung Health, Oura & more", Icons.Rounded.Speed) { ZivaaTheme.colors.sage },
        WearableOption("I'll connect later", null, null) { ZivaaTheme.colors.clay }
    )

    ZivaaSetupBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ZivaaTopBar(stepNo = 4, totalSteps = 6, onBack = onBack)

            ZivaaHeader(
                label = null, // No eyebrow in this specific design
                title = buildAnnotatedString {
                    append("Connect a ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("wearable.")
                    }
                },
                subtitle = "It quietly tracks your heart, sleep and steps — so your morning update writes itself."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Zivaa Ring Prominent Card
            val isRingSelected = state.selectedWearable == "Zivaa Ring"
            Surface(
                onClick = { viewModel.selectWearable("Zivaa Ring") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = ZivaaTheme.colors.sage
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Top Row: Pill and Checkmark
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(ZivaaTheme.colors.bgElev, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recommended For You",
                                    color = ZivaaTheme.colors.bgElev,
                                    style = ZivaaTheme.typography.eyebrow.copy(fontSize = 10.sp)
                                )
                            }
                        }

                        // Checkmark
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = if (isRingSelected) ZivaaTheme.colors.sage else Color.Transparent,
                            border = if (!isRingSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isRingSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = ZivaaTheme.colors.bgElev,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White.copy(alpha = 0f), // invisible
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Content Row: Ring Icon and Texts
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Ring Canvas Graphic
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(64.dp)) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.15f),
                                    radius = size.minDimension / 2
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 12.dp.toPx(),
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Zivaa Ring",
                                color = ZivaaTheme.colors.bgElev,
                                style = ZivaaTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Light as a feather. 5-day battery. Made for resting hands.",
                                color = Color.White.copy(alpha = 0.8f),
                                style = ZivaaTheme.typography.bodyMedium,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Pair my ring >",
                        color = ZivaaTheme.colors.bgElev,
                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable { viewModel.selectWearable("Zivaa Ring") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Or Connect Something You Have",
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.eyebrow,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            WEARABLES.forEach { option ->
                ZivaaWearableCard(
                    title = option.title,
                    subtitle = option.subtitle,
                    icon = option.icon,
                    iconColor = option.colorProvider(),
                    isSelected = state.selectedWearable == option.title,
                    onClick = { viewModel.selectWearable(option.title) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        ZivaaButton(
            text = "Continue",
            onClick = {
                if (state.selectedWearable == "Google Health Connect") {
                    val support = healthConnectManager.checkHealthConnectSupportAndRedirect()
                    if (support == HealthConnectSupport.AVAILABLE) {
                        // We must launch a coroutine to check permissions since it's a suspend function
                        // But since we can't easily launch coroutine here without a scope, 
                        // we'll just try to request permissions. If they already have it, the launcher handles it.
                        requestPermissionsLauncher.launch(healthConnectManager.permissions)
                    } else if (support == HealthConnectSupport.INSTALL_REQUIRED) {
                        Toast.makeText(context, "Please install Health Connect from the Play Store.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    onNext()
                }
            }
        )
    }
}
