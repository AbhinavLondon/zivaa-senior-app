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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import com.zivaa.app.ui.wallet.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HealthWalletScreen(
    viewModel: HealthWalletViewModel,
    onNavigateToLabReport: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToUpload: () -> Unit = {}
) {
    val groupedDocuments by viewModel.groupedDocuments.collectAsState()
    val allDocuments by viewModel.allDocuments.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
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
                    TopHeaderArea(
                        totalCount = allDocuments.size,
                        onNavigateBack = onNavigateBack,
                        onNavigateToUpload = onNavigateToUpload
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SearchBarArea(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) }
                    )
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
                    FilterChipsRow(
                        selectedCategory = selectedCategory,
                        categoryCounts = categoryCounts,
                        onSelectCategory = { viewModel.setCategory(it) }
                    )
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

            if (groupedDocuments.isEmpty() && uploadState !is com.zivaa.app.data.remote.GlobalUploadState.Uploading && uploadState !is com.zivaa.app.data.remote.GlobalUploadState.Processing) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching documents" else "No documents yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = ZivaaTheme.colors.textStrong
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) 
                                "Try searching for a different test or doctor name."
                            else 
                                "Upload your lab reports, prescriptions, or scans to keep them organized in one place.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZivaaTheme.colors.textBody,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                groupedDocuments.forEach { (groupName, docs) ->
                    stickyHeader {
                        SectionHeader(groupName, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                    items(docs, key = { it.id }) { doc ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            DocumentCard(doc, onClick = { onNavigateToLabReport(doc.id) })
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Everything is Private",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.textBody,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TopHeaderArea(
    totalCount: Int,
    onNavigateBack: () -> Unit,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                onClick = onNavigateBack,
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
                val docLabel = if (totalCount == 1) "1 DOCUMENT" else "$totalCount DOCUMENTS"
                Text(
                    text = "$docLabel — ALL YOUR PAPERS, ONE PLACE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZivaaTheme.colors.textBody
                )
            }
        }
        
        IconButton(
            onClick = onNavigateToUpload,
            modifier = Modifier
                .size(40.dp)
                .background(ZivaaTheme.colors.accent, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Document",
                tint = ZivaaTheme.colors.textOnAccent
            )
        }
    }
}

@Composable
fun SearchBarArea(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search... try 'knee' or 'Dr. Kulkarni'",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ZivaaTheme.colors.textBody
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ZivaaTheme.colors.textStrong),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = ZivaaTheme.colors.textBody,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedCategory: DocumentCategory,
    categoryCounts: Map<DocumentCategory, Int>,
    onSelectCategory: (DocumentCategory) -> Unit
) {
    val categories = DocumentCategory.values()
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val count = categoryCounts[category] ?: 0
            val text = "${category.label} · $count"
            
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectCategory(category) }
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
        text = title.toEyebrowTitleCase(),
        style = com.zivaa.app.ui.theme.ZivaaTheme.typography.eyebrow,
        color = com.zivaa.app.ui.theme.ZivaaTheme.colors.eyebrow,
        modifier = modifier
            .fillMaxWidth()
            .background(com.zivaa.app.ui.theme.ZivaaTheme.colors.bg)
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
