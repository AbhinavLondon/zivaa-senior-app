package com.zivaa.app.presentation.setup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.HealthConnectSupport
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun SetupHealthConnectScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissionsLauncher = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.permissions)) {
            viewModel.selectWearable("Google Health Connect")
            onNext() // Permissions granted, proceed
        } else {
            Toast.makeText(context, "Health Connect permissions are required to sync your data fully.", Toast.LENGTH_SHORT).show()
            // Even if denied, they can proceed, or they try again. Let's let them proceed without it.
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.zivaa.app.ui.theme.LocalZivaaColors.current.bg)
    ) {
        ZivaaTopBar(stepNo = 3, totalSteps = 6, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Big Icon
            Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = com.zivaa.app.ui.theme.LocalZivaaColors.current.sage.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = com.zivaa.app.ui.theme.LocalZivaaColors.current.sage,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Sync with Health Connect",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                color = com.zivaa.app.ui.theme.LocalZivaaColors.current.ink,
                style = ZivaaTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Securely sync your daily activity and vitals from your favorite wearable directly to Zivaa. We use this data to tailor your daily plan.",
                style = ZivaaTheme.typography.bodyLarge,
                color = com.zivaa.app.ui.theme.LocalZivaaColors.current.inkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Feature List
            HealthFeatureRow(icon = Icons.AutoMirrored.Rounded.DirectionsWalk, title = "Steps & Movement", color = com.zivaa.app.ui.theme.LocalZivaaColors.current.sage)
            HealthFeatureRow(icon = Icons.Rounded.NightsStay, title = "Sleep Tracking", color = com.zivaa.app.ui.theme.LocalZivaaColors.current.clay)
            HealthFeatureRow(icon = Icons.Rounded.Favorite, title = "Heart Rate & Vitals", color = com.zivaa.app.ui.theme.LocalZivaaColors.current.rose)
            HealthFeatureRow(icon = Icons.Rounded.Security, title = "Private & Secure", color = com.zivaa.app.ui.theme.LocalZivaaColors.current.amber)
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding()
        ) {
            ZivaaButton(
                text = "Connect Health Connect",
                onClick = {
                    val support = healthConnectManager.checkHealthConnectSupportAndRedirect()
                    if (support == HealthConnectSupport.AVAILABLE) {
                        requestPermissionsLauncher.launch(healthConnectManager.permissions)
                    } else if (support == HealthConnectSupport.INSTALL_REQUIRED) {
                        Toast.makeText(context, "Please install Health Connect from the Play Store.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = { 
                    viewModel.selectWearable("None")
                    onNext() 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for now",
                    color = com.zivaa.app.ui.theme.LocalZivaaColors.current.inkMute,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
fun HealthFeatureRow(icon: ImageVector, title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title,
            color = com.zivaa.app.ui.theme.LocalZivaaColors.current.ink,
            style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
