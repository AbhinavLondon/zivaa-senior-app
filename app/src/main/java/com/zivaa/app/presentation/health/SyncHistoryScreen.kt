package com.zivaa.app.presentation.health

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.InstrumentSerif

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncHistoryScreen(
    viewModel: HealthConnectSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)
    val syncLogs by viewModel.syncLogs.collectAsState()
    val isLoading by viewModel.isLoadingLogs.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSyncLogs()
    }

    val filteredLogs = remember(syncLogs, selectedFilter) {
        when (selectedFilter) {
            SyncTypeFilter.ALL -> syncLogs
            SyncTypeFilter.FOREGROUND -> syncLogs.filter { it.syncType.contains("FOREGROUND") }
            SyncTypeFilter.BACKGROUND -> syncLogs.filter { it.syncType.contains("BACKGROUND") }
        }
    }

    Scaffold(
        containerColor = ProfileTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sync History",
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
                actions = {
                    IconButton(
                        onClick = { viewModel.loadSyncLogs() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ProfileTheme.colors.accentGreen
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = ProfileTheme.colors.textPrimary
                            )
                        }
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
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SyncFilterChip(
                    text = "All Syncs",
                    isSelected = selectedFilter == SyncTypeFilter.ALL,
                    onClick = { viewModel.setFilter(SyncTypeFilter.ALL) }
                )
                SyncFilterChip(
                    text = "Foreground",
                    isSelected = selectedFilter == SyncTypeFilter.FOREGROUND,
                    onClick = { viewModel.setFilter(SyncTypeFilter.FOREGROUND) }
                )
                SyncFilterChip(
                    text = "Background",
                    isSelected = selectedFilter == SyncTypeFilter.BACKGROUND,
                    onClick = { viewModel.setFilter(SyncTypeFilter.BACKGROUND) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isLoading && syncLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ProfileTheme.colors.accentGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading sync history...",
                            style = ProfileTheme.typography.cardSubtitle,
                            color = ProfileTheme.colors.textSecondary
                        )
                    }
                }
            } else if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(ProfileTheme.colors.cardBackground)
                            .padding(28.dp)
                    ) {
                        Text(
                            text = "No Sync Activity Found",
                            style = ProfileTheme.typography.cardTitle
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Background and manual sync logs from Health Connect will appear here with local timestamps.",
                            style = ProfileTheme.typography.cardSubtitle,
                            color = ProfileTheme.colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { logItem ->
                        SyncLogCard(logItem = logItem)
                    }
                    item {
                        Spacer(modifier = Modifier.height(130.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) ProfileTheme.colors.accentGreen else ProfileTheme.colors.cardBackground
    val textColor = if (isSelected) ProfileTheme.colors.accentBeige else ProfileTheme.colors.textPrimary
    val borderColor = if (isSelected) Color.Transparent else ProfileTheme.colors.divider

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun SyncLogCard(logItem: SyncLogUiItem) {
    val isSuccess = logItem.status == "SUCCESS"
    val isForeground = logItem.syncType.contains("FOREGROUND")
    val isBackfill = logItem.syncType.contains("BACKFILL") || logItem.syncType.contains("HISTORICAL")

    val typeBadgeColor = when {
        isForeground -> Color(0xFF2563EB)
        isBackfill -> Color(0xFF7C3AED)
        else -> Color(0xFF059669) // Background
    }

    val typeBadgeBg = when {
        isForeground -> Color(0xFF2563EB).copy(alpha = 0.12f)
        isBackfill -> Color(0xFF7C3AED).copy(alpha = 0.12f)
        else -> Color(0xFF059669).copy(alpha = 0.12f)
    }

    val typeDisplayName = when {
        isForeground -> "Foreground Sync"
        isBackfill -> "Historical Backfill"
        else -> "Background Sync"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ProfileTheme.colors.cardBackground)
            .padding(18.dp)
    ) {
        // Header Row: Type Badge + Local Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(typeBadgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = typeDisplayName,
                    color = typeBadgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = logItem.localTimeString,
                style = ProfileTheme.typography.meta,
                color = ProfileTheme.colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metrics Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isSuccess) "✓ Succeeded" else "✕ ${logItem.status}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                )

                Text(
                    text = "·",
                    color = ProfileTheme.colors.textSecondary
                )

                Text(
                    text = "${logItem.recordsSynced} records",
                    style = ProfileTheme.typography.cardTitle.copy(fontSize = 14.sp)
                )
            }

            if (!logItem.durationString.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ProfileTheme.colors.divider.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = logItem.durationString,
                        style = ProfileTheme.typography.meta,
                        fontSize = 11.sp,
                        color = ProfileTheme.colors.textSecondary
                    )
                }
            }
        }

        // Details breakdown
        if (!logItem.metricsSummary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = logItem.metricsSummary,
                style = ProfileTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                color = ProfileTheme.colors.textSecondary
            )
        }

        // Error message if present
        if (!logItem.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = logItem.errorMessage,
                fontSize = 12.sp,
                color = Color(0xFFEF4444)
            )
        }
    }
}
