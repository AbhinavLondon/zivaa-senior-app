package com.zivaa.app.ui.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Search
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.wallet.theme.*

data class DocumentItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val docType: String,
    val dateStr: String,
    val iconLetters: String,
    val iconColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HealthWalletScreen(
    viewModel: HealthWalletViewModel,
    onNavigateToLabReport: (String) -> Unit = {}
) {
    val groupedDocuments by viewModel.groupedDocuments.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.sync()
    }
    
    androidx.compose.runtime.LaunchedEffect(uploadState) {
        if (uploadState is com.zivaa.app.data.remote.GlobalUploadState.Success) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "zivaa_early_warnings"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId, "Early Warning Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Report Analysis Complete")
                .setContentText("Your lab report has been uploaded and analyzed. Check the dashboard for insights.")
                .setColor(0x234B3F)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Your lab report has been uploaded and analyzed. Check the dashboard for insights."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TopHeaderArea()
                    Spacer(modifier = Modifier.height(24.dp))
                    SearchBarArea()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZivaaTheme.colors.bg)
                        .padding(bottom = 16.dp)
                ) {
                    FilterChipsRow()
                }
            }

            if (uploadState is com.zivaa.app.data.remote.GlobalUploadState.Uploading || 
                uploadState is com.zivaa.app.data.remote.GlobalUploadState.Processing ||
                uploadState is com.zivaa.app.data.remote.GlobalUploadState.Error) {
                
                val fileName = when(val state = uploadState) {
                    is com.zivaa.app.data.remote.GlobalUploadState.Uploading -> state.fileName
                    is com.zivaa.app.data.remote.GlobalUploadState.Processing -> state.fileName
                    is com.zivaa.app.data.remote.GlobalUploadState.Error -> state.fileName
                    else -> ""
                }
                
                val progress = when(val state = uploadState) {
                    is com.zivaa.app.data.remote.GlobalUploadState.Uploading -> state.progress
                    is com.zivaa.app.data.remote.GlobalUploadState.Processing -> state.progress
                    is com.zivaa.app.data.remote.GlobalUploadState.Error -> 100 // Full bar but red
                    else -> 0
                }
                
                val message = when(val state = uploadState) {
                    is com.zivaa.app.data.remote.GlobalUploadState.Uploading -> state.message
                    is com.zivaa.app.data.remote.GlobalUploadState.Processing -> state.message
                    is com.zivaa.app.data.remote.GlobalUploadState.Error -> state.message
                    else -> ""
                }
                
                item {
                    val isError = uploadState is com.zivaa.app.data.remote.GlobalUploadState.Error
                    val accentColor = if (isError) androidx.compose.ui.graphics.Color.Red else ZivaaTheme.colors.accent

                    Box(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ZivaaTheme.colors.surfaceCard),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, if (isError) androidx.compose.ui.graphics.Color.Red else ZivaaTheme.colors.borderStrong.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ZivaaTheme.colors.textStrong,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isError) androidx.compose.ui.graphics.Color.Red else ZivaaTheme.colors.textBody
                                        )
                                    }
                                    Text(
                                        text = if (isError) "Failed" else "${progress}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { progress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = accentColor,
                                    trackColor = ZivaaTheme.colors.borderStrong
                                )
                            }
                        }
                    }
                }
            }

            groupedDocuments.forEach { (groupName, docs) ->
                stickyHeader {
                    SectionHeader(groupName, modifier = Modifier.padding(horizontal = 20.dp))
                }
                items(docs) { doc ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        DocumentCard(doc, onClick = { onNavigateToLabReport(doc.id) })
                    }
                }
            }

            item {
                Text(
                    text = "Everything here is private to the family — and ready in Dr.\nKulkarni's hand before each visit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textBody,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TopHeaderArea(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, ZivaaTheme.colors.borderStrong, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = ZivaaTheme.colors.textStrong
                )
            }
            Column {
                Text(
                    text = "Health Wallet",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ZivaaTheme.colors.textStrong
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "20 DOCUMENTS — ALL OF PAPA'S PAPERS, ONE PLACE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZivaaTheme.colors.textBody
                )
            }
        }
        
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(40.dp)
                .background(ZivaaTheme.colors.accent, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = ZivaaTheme.colors.textOnAccent
            )
        }
    }
}

@Composable
fun SearchBarArea(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, ZivaaTheme.colors.borderStrong, RoundedCornerShape(24.dp))
            .background(ZivaaTheme.colors.surfaceCard, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = ZivaaTheme.colors.textBody,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Search... try 'knee' or 'Dr. Kulkarni'",
            style = MaterialTheme.typography.bodyLarge,
            color = ZivaaTheme.colors.textBody
        )
    }
}

@Composable
fun FilterChipsRow() {
    val filters = listOf("All · 12", "Lab reports · 4", "Prescriptions · 2", "X-rays · 2")
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters.size) { index ->
            val isSelected = index == 0
            val text = filters[index]
            
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else ZivaaTheme.colors.borderStrong,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(
                        color = if (isSelected) ZivaaTheme.colors.accent else Color.Transparent,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) ZivaaTheme.colors.textOnAccent else ZivaaTheme.colors.textStrong
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = ZivaaTheme.colors.textBody,
        modifier = modifier
            .fillMaxWidth()
            .background(ZivaaTheme.colors.bg)
            .padding(vertical = 8.dp)
    )
}


@Composable
fun DocumentCard(item: DocumentItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZivaaTheme.colors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, ZivaaTheme.colors.borderStrong.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(item.iconColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.iconLetters,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    color = ZivaaTheme.colors.textOnAccent
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ZivaaTheme.colors.textStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textBody,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = item.docType,
                    style = MaterialTheme.typography.labelSmall,
                    color = ZivaaTheme.colors.textBody
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = ZivaaTheme.colors.textStrong,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Right
                )
            }
        }
    }
}
