package com.zivaa.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.sensors.PhoneSensorService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val syncPrefsManager = remember { SyncPrefsManager(context) }
    var isPhoneSensorEnabled by remember { mutableStateOf(syncPrefsManager.isPhoneSensorEnabled()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
            if (audioGranted) {
                isPhoneSensorEnabled = true
                syncPrefsManager.setPhoneSensorEnabled(true)
                // Start the service
                val intent = Intent(context, PhoneSensorService::class.java)
                context.startForegroundService(intent)
            } else {
                isPhoneSensorEnabled = false
                syncPrefsManager.setPhoneSensorEnabled(false)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    Button(onClick = onNavigateBack, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Data Collection",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Phone Sensors (Edge Computing)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Allow Zivaa to use phone microphone (for coughing/snoring) and gyroscope (for mobility) to strengthen health insights securely.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = isPhoneSensorEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            // Request permissions
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.RECORD_AUDIO
                                )
                            )
                        } else {
                            isPhoneSensorEnabled = false
                            syncPrefsManager.setPhoneSensorEnabled(false)
                            // Stop the service
                            val intent = Intent(context, PhoneSensorService::class.java)
                            context.stopService(intent)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp))
        }
    }
}
