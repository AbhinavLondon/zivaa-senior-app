package com.zivaa.app.presentation.longevity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import androidx.compose.runtime.*
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.data.model.LongevityProtocol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongevityPlanScreen(
    viewModel: LongevityViewModel,
    patientId: String,
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Daily Goals", "Journey")

    LaunchedEffect(Unit) {
        viewModel.fetchTodayProtocols(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Check-in") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent // Dark mode background handled by Box
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF23153C), // Deep vibrant purple
                            Color(0xFF0F1E3D), // Deep vibrant blue
                            Color(0xFF122E26)  // Deep vibrant teal
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00FF88))
            } else if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color(0xFF00FF88)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, color = if (selectedTabIndex == index) Color(0xFF00FF88) else Color.White) }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (selectedTabIndex == 0) {
                            ProgressHeader(viewModel.completedTasks, viewModel.totalTasks)

                            viewModel.groupedProtocols.forEach { (category, protocols) ->
                                CategoryCard(
                                    category = category,
                                    protocols = protocols,
                                    onToggleComplete = { protocolId ->
                                        val currentStatus = protocols.find { it.safeId == protocolId }?.isCompleted ?: false
                                        viewModel.toggleProtocolCompletion(patientId, protocolId, currentStatus)
                                    }
                                )
                            }
                        } else if (selectedTabIndex == 1) {
                            JourneySection(viewModel = viewModel, patientId = patientId)
                        }
                        Spacer(modifier = Modifier.height(130.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressHeader(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val percentage = (progress * 100).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Streak: 18 Days", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("$completed/$total Habits", color = Color.Gray, fontSize = 14.sp)
        }
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            CircularProgressIndicator(
                progress = { 1f },
                color = Color.DarkGray,
                strokeWidth = 6.dp,
                modifier = Modifier.fillMaxSize()
            )
            CircularProgressIndicator(
                progress = { progress },
                color = Color(0xFF00FF88),
                strokeWidth = 6.dp,
                modifier = Modifier.fillMaxSize()
            )
            Text("$percentage%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryCard(
    category: String,
    protocols: List<LongevityProtocol>,
    onToggleComplete: (String) -> Unit
) {
    val (icon, tintColor) = when (category.lowercase()) {
        "sleep" -> Pair(Icons.Default.NightsStay, Color(0xFF64B5F6)) // Soft Blue
        "diagnostics" -> Pair(Icons.Default.MedicalServices, Color(0xFFE57373)) // Soft Red
        "mobility" -> Pair(Icons.Default.DirectionsRun, Color(0xFFFFB74D)) // Soft Orange
        "lifestyle" -> Pair(Icons.Default.SelfImprovement, Color(0xFFBA68C8)) // Soft Purple
        "nutrition" -> Pair(Icons.Default.Restaurant, Color(0xFF81C784)) // Soft Green
        "mind" -> Pair(Icons.Default.Psychology, Color(0xFF4DD0E1)) // Soft Cyan
        "app setup" -> Pair(Icons.Default.Star, Color(0xFFFFD700)) // Soft Gold
        else -> Pair(Icons.Default.Star, Color(0xFFE0E0E0))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.toEyebrowTitleCase(),
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )
        }

        Surface(
            color = Color(0x1AFFFFFF), // 10% white for frosted glass effect
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0x40FFFFFF), // 25% white border
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                protocols.forEachIndexed { index, protocol ->
                    ProtocolItem(
                        protocol = protocol,
                        onToggleComplete = { onToggleComplete(protocol.safeId) }
                    )
                    if (index < protocols.size - 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProtocolItem(
    protocol: LongevityProtocol,
    onToggleComplete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            IconButton(onClick = onToggleComplete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (protocol.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Complete",
                    tint = if (protocol.isCompleted) Color(0xFF00FF88) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = protocol.displayTitle,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (protocol.is_modified_today || protocol.is_new_from_coach) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (protocol.is_new_from_coach) {
                            Surface(
                                color = Color(0xFF81C784).copy(alpha = 0.2f), // Soft green
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF81C784).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "New",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (protocol.is_modified_today) {
                            Surface(
                                color = Color(0xFF64B5F6).copy(alpha = 0.2f), // Soft blue
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF64B5F6).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Modified",
                                    color = Color(0xFF90CAF9),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                if (protocol.is_modified_today && !protocol.modification_note.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFF64B5F6).copy(alpha = 0.1f), // Soft blue tint
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64B5F6).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("MODIFIED FOR TODAY", color = Color(0xFF64B5F6), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(protocol.modification_note ?: "", color = Color(0xFFBBDEFB), fontSize = 14.sp)
                        }
                    }
                }
                
                Surface(
                    color = Color.Black.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What to do",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = protocol.displayDescription,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 16.dp))
                        
                        Text(
                            text = "Why it matters",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = protocol.displayReasoning,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun JourneySection(viewModel: LongevityViewModel, patientId: String) {
    Text(
        text = "Symptom Progression",
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    val activeSymptoms = viewModel.activeSymptoms
    val resolvedSymptoms = viewModel.resolvedSymptoms

    if (activeSymptoms.isEmpty() && resolvedSymptoms.isEmpty()) {
        Text("No symptoms tracked.", color = Color.White.copy(alpha = 0.7f))
    } else {
        if (activeSymptoms.isNotEmpty()) {
            activeSymptoms.forEach { symptom ->
                SymptomTimelineCard(symptom, viewModel, patientId)
            }
        }
        
        if (resolvedSymptoms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Resolved",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            resolvedSymptoms.forEach { symptom ->
                SymptomTimelineCard(symptom, viewModel, patientId)
            }
        }
    }
}

@Composable
fun SymptomTimelineCard(symptom: com.zivaa.app.data.model.PatientSymptom, viewModel: LongevityViewModel, patientId: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        color = Color(0x1AFFFFFF),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = symptom.name.ifBlank { "Symptom" },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${symptom.status.ifBlank { "Active" }}",
                        color = when (symptom.status) {
                            "Resolved" -> Color(0xFF81C784)
                            "Worse" -> Color(0xFFFF5252)
                            "Resolving" -> Color(0xFF64B5F6)
                            else -> Color(0xFFFFD700)
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
            
            // GRAPH (Rectangle Boxes)
            val logs = symptom.logs?.sortedBy { it.created_at } ?: emptyList()
            if (symptom.status != "Resolved") {
                if (logs.size > 1) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Severity Trend", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        logs.forEach { log ->
                            val color = when (log.severity) {
                                "Severe" -> Color(0xFFFF5252)
                                "Moderate" -> Color(0xFFFFD700)
                                "Mild" -> Color(0xFF64B5F6)
                                else -> Color(0xFF81C784) // None/Resolved
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                }
    
                // ALWAYS VISIBLE FEEDBACK BUTTONS
                Spacer(modifier = Modifier.height(24.dp))
                Text("How is it now?", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val buttons = listOf("Better", "Same", "Worse", "Resolved")
                    buttons.forEach { label ->
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f).clickable {
                                val status = when (label) {
                                    "Better" -> "Resolving"
                                    "Same" -> "Active"
                                    "Worse" -> "Worse"
                                    "Resolved" -> "Resolved"
                                    else -> "Active"
                                }
                                viewModel.updateSymptom(patientId, symptom.id, status, "User marked as $label")
                            }
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            // EXPANDED VIEW
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 16.dp))
                    
                    if (logs.isNotEmpty()) {
                        Text("Log History", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 12.dp))
                        
                        logs.asReversed().forEachIndexed { index, log ->
                            val logColor = when (log.severity) {
                                "Severe" -> Color(0xFFFF5252)
                                "Moderate" -> Color(0xFFFFD700)
                                "Mild" -> Color(0xFF64B5F6)
                                else -> Color(0xFF81C784)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = if (index == logs.size - 1) 0.dp else 16.dp)) {
                                // Vertical timeline line & dot
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(logColor))
                                    if (index != logs.size - 1) {
                                        Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.White.copy(alpha = 0.2f)))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = log.created_at.take(10), color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            color = logColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, logColor.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = log.severity,
                                                color = logColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (!log.note.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = log.note ?: "", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No logs available.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
