package com.zivaa.app.presentation.health

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.ZivaaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectSettingsScreen(
    viewModel: HealthConnectSettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSyncHistory: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsState()
    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsState()
    val grantedCount by viewModel.grantedPermissionsCount.collectAsState()
    val totalCount by viewModel.totalPermissionsCount.collectAsState()
    val isBackgroundReadGranted by viewModel.isBackgroundReadGranted.collectAsState()
    val isHistoryReadGranted by viewModel.isHistoryReadGranted.collectAsState()
    val isBatteryOptimizationIgnored by viewModel.isBatteryOptimizationIgnored.collectAsState()
    val reSyncStatus by viewModel.reSyncStatus.collectAsState()

    var showReSyncDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.refreshState()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshState()
    }

    Scaffold(
        containerColor = ProfileTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health Connect",
                        fontFamily = InstrumentSerif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = ProfileTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ProfileTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfileTheme.colors.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // --- 1. Connection Status Card ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileTheme.colors.cardBackground)
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (grantedCount > 0) Color(0xFF10B981) else Color(0xFFE5A643))
                        )
                        Text(
                            text = if (grantedCount > 0) "Connected & Active" else "Permissions Required",
                            style = ProfileTheme.typography.cardSubtitle.copy(
                                color = if (grantedCount > 0) Color(0xFF10B981) else Color(0xFFE5A643),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = "Google Health Connect",
                        style = ProfileTheme.typography.meta,
                        color = ProfileTheme.colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Wearable Health Hub",
                    fontFamily = InstrumentSerif,
                    fontSize = 22.sp,
                    color = ProfileTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Health Connect acts as the secure bridge between your watch companion app and Zivaa's restorative health engine.",
                    style = ProfileTheme.typography.cardSubtitle,
                    color = ProfileTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Last Synced",
                            style = ProfileTheme.typography.meta,
                            color = ProfileTheme.colors.textSecondary
                        )
                        Text(
                            text = lastSyncTime ?: "Never",
                            style = ProfileTheme.typography.cardTitle,
                            color = ProfileTheme.colors.textPrimary
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerSyncNow() },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileTheme.colors.accentGreen,
                            contentColor = ProfileTheme.colors.accentBeige,
                            disabledContainerColor = ProfileTheme.colors.accentGreen.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ProfileTheme.colors.accentBeige
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing...", fontSize = 14.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (!syncStatusMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = syncStatusMessage!!,
                        style = ProfileTheme.typography.meta,
                        color = ProfileTheme.colors.accentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 2. Battery & Background Sync Optimization Card ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileTheme.colors.cardBackground)
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isBatteryOptimizationIgnored) Color(0xFF10B981) else Color(0xFFE5A643))
                        )
                        Text(
                            text = if (isBatteryOptimizationIgnored) "Unrestricted Background Sync" else "Background Sync Restricted",
                            style = ProfileTheme.typography.cardSubtitle.copy(
                                color = if (isBatteryOptimizationIgnored) Color(0xFF10B981) else Color(0xFFE5A643),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = "Android Power",
                        style = ProfileTheme.typography.meta,
                        color = ProfileTheme.colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isBatteryOptimizationIgnored) "24/7 Real-Time Sync Active" else "Allow Background Battery Usage",
                    fontFamily = InstrumentSerif,
                    fontSize = 20.sp,
                    color = ProfileTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBatteryOptimizationIgnored) {
                        "Android battery optimization is configured to keep Health Connect vitals syncing continuously, even when your phone is locked or idle."
                    } else {
                        "Android's Adaptive Battery is currently restricting Zivaa. Background sync may pause or be delayed until you open the app. Set battery usage to 'Unrestricted' for continuous health monitoring."
                    },
                    style = ProfileTheme.typography.cardSubtitle,
                    color = ProfileTheme.colors.textSecondary
                )

                if (!isBatteryOptimizationIgnored) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "1. Tap below to open Settings\n2. Tap 'App battery usage'\n3. Select 'Unrestricted'",
                        style = ProfileTheme.typography.meta.copy(
                            color = ProfileTheme.colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.openBatterySettings(context) },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileTheme.colors.accentGreen,
                            contentColor = ProfileTheme.colors.accentBeige
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Configure Battery Settings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. Quick Links Card ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileTheme.colors.cardBackground)
                    .padding(20.dp)
            ) {
                // Link: Sync History
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToSyncHistory() }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sync History",
                            style = ProfileTheme.typography.cardTitle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Audit background vs. foreground syncs with local timestamps",
                            style = ProfileTheme.typography.cardSubtitle,
                            color = ProfileTheme.colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Sync History",
                        tint = ProfileTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(
                    color = ProfileTheme.colors.divider,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Link: Open System Health Connect App
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open Health Connect settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Manage in Android Settings",
                            style = ProfileTheme.typography.cardTitle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Control system data permissions & connected wearable apps",
                            style = ProfileTheme.typography.cardSubtitle,
                            color = ProfileTheme.colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Android Settings",
                        tint = ProfileTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. Data Permissions Card ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileTheme.colors.cardBackground)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DATA PERMISSIONS",
                        style = ProfileTheme.typography.sectionHeader
                    )
                    Text(
                        text = "$grantedCount / $totalCount active",
                        style = ProfileTheme.typography.meta,
                        color = ProfileTheme.colors.accentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                PermissionCategoryItem(
                    title = "Clinical Vitals",
                    subtitle = "Resting HR, Heart Rate samples, HRV RMSSD, SpO2, Resp. Rate, Skin Temp",
                    isGranted = grantedCount >= 5
                )

                HorizontalDivider(color = ProfileTheme.colors.divider, modifier = Modifier.padding(vertical = 10.dp))

                PermissionCategoryItem(
                    title = "Sleep Architecture",
                    subtitle = "Sleep sessions, Deep sleep, REM sleep, WASO awakenings",
                    isGranted = true
                )

                HorizontalDivider(color = ProfileTheme.colors.divider, modifier = Modifier.padding(vertical = 10.dp))

                PermissionCategoryItem(
                    title = "Movement & Activity",
                    subtitle = "Daily steps, Distance, Speed, Active & Total calories, Cadence",
                    isGranted = true
                )

                HorizontalDivider(color = ProfileTheme.colors.divider, modifier = Modifier.padding(vertical = 10.dp))

                PermissionCategoryItem(
                    title = "Background Reading",
                    subtitle = "Allows Zivaa to sync telemetry silently while the app is closed",
                    isGranted = isBackgroundReadGranted
                )

                HorizontalDivider(color = ProfileTheme.colors.divider, modifier = Modifier.padding(vertical = 10.dp))

                PermissionCategoryItem(
                    title = "Historical Access (30+ Days)",
                    subtitle = "Enables retrospective baseline imports and long-term health trends",
                    isGranted = isHistoryReadGranted
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        permissionLauncher.launch(healthConnectManager.permissions)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ProfileTheme.colors.accentGreen)
                ) {
                    Text(
                        text = "Edit Permissions in Health Connect",
                        color = ProfileTheme.colors.accentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 4. Maintenance / Deep Re-sync Card ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileTheme.colors.cardBackground)
                    .padding(20.dp)
            ) {
                Text(
                    text = "MAINTENANCE & HISTORY",
                    style = ProfileTheme.typography.sectionHeader
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Re-sync Past 90 Days",
                    style = ProfileTheme.typography.cardTitle
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Re-evaluates and pulls up to 90 days of past health telemetry from Health Connect into Zivaa. Use this if you recently granted history permissions or linked a new wearable.",
                    style = ProfileTheme.typography.cardSubtitle,
                    color = ProfileTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showReSyncDialog = true },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileTheme.colors.divider.copy(alpha = 0.35f),
                        contentColor = ProfileTheme.colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Trigger 90-Day Backfill",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                if (!reSyncStatus.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = reSyncStatus!!,
                        style = ProfileTheme.typography.meta,
                        color = ProfileTheme.colors.accentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
    }

    if (showReSyncDialog) {
        AlertDialog(
            onDismissRequest = { showReSyncDialog = false },
            title = {
                Text(
                    text = "Re-sync Past 90 Days?",
                    style = ProfileTheme.typography.cardTitle
                )
            },
            text = {
                Text(
                    text = "This will schedule a background worker to query your full 90-day history in Health Connect. It will run silently in the background without affecting your battery.",
                    style = ProfileTheme.typography.cardSubtitle,
                    color = ProfileTheme.colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReSyncDialog = false
                        viewModel.triggerDeepReSync()
                    }
                ) {
                    Text("Start Re-sync", color = ProfileTheme.colors.accentGreen, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReSyncDialog = false }) {
                    Text("Cancel", color = ProfileTheme.colors.textSecondary)
                }
            },
            containerColor = ProfileTheme.colors.cardBackground,
            titleContentColor = ProfileTheme.colors.textPrimary,
            textContentColor = ProfileTheme.colors.textSecondary
        )
    }
}

@Composable
private fun PermissionCategoryItem(
    title: String,
    subtitle: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isGranted) Color(0xFF10B981).copy(alpha = 0.15f)
                    else Color(0xFFE5A643).copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Info,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF10B981) else Color(0xFFE5A643),
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ProfileTheme.typography.cardTitle.copy(fontSize = 15.sp)
            )
            Text(
                text = subtitle,
                style = ProfileTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                color = ProfileTheme.colors.textSecondary
            )
        }
    }
}
