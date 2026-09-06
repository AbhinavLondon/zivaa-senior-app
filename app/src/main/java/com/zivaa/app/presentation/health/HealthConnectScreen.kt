package com.zivaa.app.presentation.health

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.HealthConnectSupport
import com.zivaa.app.data.health.worker.HealthDataSyncWorker
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Composable
fun HealthConnectScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val healthConnectManager = remember { HealthConnectManager(context) }

    var isSupported by remember { mutableStateOf(HealthConnectSupport.UNSUPPORTED) }
    var hasPermissions by remember { mutableStateOf(false) }
    var aggregatedData by remember { mutableStateOf<com.zivaa.app.data.health.AggregatedHealthData?>(null) }
    var rawRecordCount by remember { mutableStateOf<Int?>(null) }
    var canReadBackground by remember { mutableStateOf(false) }
    var canReadHistory by remember { mutableStateOf(false) }

    // Request permissions launcher
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissionsLauncher = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.permissions)) {
            hasPermissions = true
        } else {
            Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Check availability and permissions on start
    LaunchedEffect(Unit) {
        isSupported = healthConnectManager.checkHealthConnectSupportAndRedirect()
        if (isSupported == HealthConnectSupport.AVAILABLE) {
            hasPermissions = healthConnectManager.hasAllPermissions()
            canReadBackground = healthConnectManager.isBackgroundReadAvailable()
            canReadHistory = healthConnectManager.isHistoryReadAvailable()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Health Connect Integration", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Status: $isSupported")
        Text(text = "Permissions Granted: $hasPermissions")
        Text(text = "Background Read Available: $canReadBackground")
        Text(text = "History Read Available: $canReadHistory")
        
        Spacer(modifier = Modifier.height(32.dp))

        if (isSupported == HealthConnectSupport.AVAILABLE) {
            if (!hasPermissions) {
                Button(onClick = {
                    requestPermissionsLauncher.launch(healthConnectManager.permissions)
                }) {
                    Text("Grant Permissions")
                }
            } else {
                Button(onClick = {
                    coroutineScope.launch {
                        val end = Instant.now()
                        val start = end.minus(90, ChronoUnit.DAYS)
                        aggregatedData = healthConnectManager.aggregateAllMetrics(start, end)
                    }
                }) {
                    Text("Read Last 90 Days Data")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val workRequest = OneTimeWorkRequestBuilder<HealthDataSyncWorker>().build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                    Toast.makeText(context, "Sync started! Check Supabase in a few seconds.", Toast.LENGTH_LONG).show()
                }) {
                    Text("Sync Data to Supabase")
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = {
                    val intent = Intent(androidx.health.connect.client.HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Text("Enable Background Sync Settings")
                }

                Spacer(modifier = Modifier.height(16.dp))

                aggregatedData?.let { data ->
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "Steps: ${data.steps}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Distance: ${"%.2f".format(data.distanceMeters / 1000)} km", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Active Calories: ${"%.0f".format(data.activeCalories)} kcal", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Active Time: ${data.activeTimeMinutes} mins", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Total Calories: ${"%.0f".format(data.totalCalories)} kcal", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Avg Heart Rate: ${data.heartRateAvg} bpm", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Sleep: ${data.sleepDurationMinutes / 60}h ${data.sleepDurationMinutes % 60}m", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } else {
            Text(
                text = "Please install or update Health Connect to continue.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp))
    }
}
