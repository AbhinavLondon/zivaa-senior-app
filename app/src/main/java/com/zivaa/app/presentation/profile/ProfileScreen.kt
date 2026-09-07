package com.zivaa.app.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.profile.components.*
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.ZivaaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigateToCustomisePlan: () -> Unit = {},
    onNavigateToHealthWallet: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.loadProfile()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadProfilePicture(context, uri)
        }
    }

    var showAddCondition by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    val frequencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val conditionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var conditionInput by remember { mutableStateOf("") }
    val selectedConditions = remember { mutableStateListOf<String>() }
    val commonConditions = listOf(
        "Arthritis", "High cholesterol", "Cataract", "Acid reflux", "Asthma", 
        "Thyroid (hypo)", "Osteoporosis", "Hearing loss", "Insomnia", 
        "Chronic back pain", "Glaucoma", "Anaemia"
    )

    if (showAddCondition) {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ProfileTheme.colors.accentGreen,
            focusedLabelColor = ProfileTheme.colors.accentGreen,
            cursorColor = ProfileTheme.colors.accentGreen
        )
        ModalBottomSheet(
            onDismissRequest = { showAddCondition = false },
            sheetState = conditionSheetState,
            containerColor = ProfileTheme.colors.cardBackground,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Conditions", style = ProfileTheme.typography.sectionHeader)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Add a condition", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "We'll keep it in mind across reports, visits and reminders.",
                    style = ProfileTheme.typography.cardTitle,
                    color = ProfileTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = conditionInput,
                    onValueChange = { conditionInput = it },
                    placeholder = { Text("e.g. Arthritis in the left knee") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonConditions.forEach { condition ->
                        val isSelected = selectedConditions.contains(condition)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) ProfileTheme.colors.accentGreen else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) ProfileTheme.colors.accentGreen else Color.LightGray,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { 
                                    if (isSelected) selectedConditions.remove(condition) 
                                    else selectedConditions.add(condition) 
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = condition,
                                color = if (isSelected) Color.White else ProfileTheme.colors.textPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddCondition = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Cancel", color = ProfileTheme.colors.textPrimary)
                    }
                    Button(
                        onClick = {
                            val toAdd = selectedConditions.toMutableList()
                            if (conditionInput.isNotBlank()) {
                                toAdd.addAll(conditionInput.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                            }
                            if (toAdd.isNotEmpty()) {
                                viewModel.addConditions(toAdd)
                            }
                            showAddCondition = false
                            conditionInput = ""
                            selectedConditions.clear()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfileTheme.colors.accentGreen),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Add condition")
                    }
                }
            }
        }
    }

    var showAddCaregiver by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var cgName by remember { mutableStateOf("") }
    var cgRelation by remember { mutableStateOf("") }
    var cgPhone by remember { mutableStateOf("") }
    var cgCity by remember { mutableStateOf("") }
    var cgCountryCode by remember { mutableStateOf("+91") }
    var showCountryDropdown by remember { mutableStateOf(false) }
    
    val countryCodes = listOf("🇮🇳 +91" to "+91", "🇺🇸 +1" to "+1", "🇬🇧 +44" to "+44", "🇦🇺 +61" to "+61")
    val relations = listOf("Son", "Daughter", "Spouse", "Neighbour", "Doctor", "Friend")

    if (showAddCaregiver) {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ProfileTheme.colors.accentGreen,
            focusedLabelColor = ProfileTheme.colors.accentGreen,
            cursorColor = ProfileTheme.colors.accentGreen
        )
        ModalBottomSheet(
            onDismissRequest = { showAddCaregiver = false },
            sheetState = sheetState,
            containerColor = ProfileTheme.colors.cardBackground,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Care Circle", style = ProfileTheme.typography.sectionHeader)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Add someone", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "They'll get the morning report over WhatsApp, and a call if anything urgent comes up.",
                    style = ProfileTheme.typography.cardTitle,
                    color = ProfileTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = cgName,
                    onValueChange = { cgName = it },
                    label = { Text("Their name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .height(56.dp)
                            .background(Color.Transparent, RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .clickable { showCountryDropdown = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val displayCode = countryCodes.find { it.second == cgCountryCode }?.first ?: "🇮🇳 +91"
                        Text(displayCode, style = ProfileTheme.typography.cardSubtitle)
                        DropdownMenu(
                            expanded = showCountryDropdown,
                            onDismissRequest = { showCountryDropdown = false },
                            modifier = Modifier.background(ProfileTheme.colors.cardBackground)
                        ) {
                            countryCodes.forEach { (label, code) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = ProfileTheme.colors.textPrimary) },
                                    onClick = { 
                                        cgCountryCode = code
                                        showCountryDropdown = false 
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = cgPhone,
                        onValueChange = { cgPhone = it },
                        label = { Text("WhatsApp number") },
                        modifier = Modifier.weight(0.65f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Who They Are To ${state.fullName.substringBefore(" ").lowercase().replaceFirstChar { it.uppercase() }}",
                    style = ProfileTheme.typography.sectionHeader
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    relations.forEach { relation ->
                        val isSelected = cgRelation == relation
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) ProfileTheme.colors.accentGreen else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) ProfileTheme.colors.accentGreen else Color.LightGray,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { cgRelation = relation }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = relation,
                                color = if (isSelected) Color.White else ProfileTheme.colors.textPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = cgCity,
                    onValueChange = { cgCity = it },
                    label = { Text("City (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddCaregiver = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Cancel", color = ProfileTheme.colors.textPrimary)
                    }
                    Button(
                        onClick = {
                            if (cgName.isNotBlank() && cgRelation.isNotBlank()) {
                                viewModel.addCaregiver(cgName, cgRelation, "$cgCountryCode$cgPhone", cgCity)
                            }
                            showAddCaregiver = false
                            cgName = ""
                            cgRelation = ""
                            cgPhone = ""
                            cgCity = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfileTheme.colors.accentGreen),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Add to circle")
                    }
                }
            }
        }
    }

    val colors = ZivaaTheme.colors
    Box {
        Scaffold(
            containerColor = ProfileTheme.colors.background,
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            style = ProfileTheme.typography.sectionHeader
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More options",
                                tint = ProfileTheme.colors.textPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            var fallDetection by remember { mutableStateOf(true) }
            var quietHours by remember { mutableStateOf(false) }
            var morningReport by remember { mutableStateOf(true) }
            val darkTheme by viewModel.darkThemeEnabled.collectAsState()
            val showLongevityPlan by viewModel.showLongevityPlanEnabled.collectAsState()
            var showDeleteDialog by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(bottom = 130.dp)
            ) {
                item {
                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = Color.Red,
                            style = ProfileTheme.typography.cardSubtitle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val initial = state.fullName.firstOrNull()?.uppercase() ?: "P"
                    val locationStr = state.locationCity.ifBlank { "" }
                    val withZivaaStr = if (state.withZivaa.isNotBlank()) "with Zivaa ${state.withZivaa}" else ""
                    val subtitleParts = listOf(state.age, locationStr, withZivaaStr).filter { it.isNotBlank() }
                    val subtitle = subtitleParts.joinToString(" · ")
                    
                    ProfileHero(
                        name = state.fullName,
                        initial = initial,
                        subtitle = subtitle,
                        profilePicUrl = state.profilePicUrl,
                        onAddPhotoClick = {
                            photoPickerLauncher.launch("image/*")
                        }
                    )
                }

                item {
                    StatsCard(
                        morningReports = state.morningReportsCount,
                        homeVisits = state.homeVisitsCount,
                        doctorCalls = state.doctorCallsCount,
                        sosResolved = state.sosResolvedCount
                    )
                }

                item {
                    HealthWalletCard(
                        documentsCount = state.documentsCount,
                        onClick = onNavigateToHealthWallet
                    )
                }

                item {
                    ConditionsCard(
                        conditions = state.conditions,
                        onAddClick = { showAddCondition = true },
                        onDeleteClick = { viewModel.deleteCondition(it) }
                    )
                }

                item {
                    val contacts = state.caregivers.mapIndexed { index, caregiver ->
                        val isPrimary = caregiver.role == "primary"
                        val hasPattern = !isPrimary && (index % 2 != 0)
                        CareContact(
                            id = caregiver.id,
                            initial = caregiver.name.firstOrNull()?.uppercase() ?: "?",
                            name = caregiver.name,
                            relation = caregiver.relation + (if (caregiver.city != null) " · ${caregiver.city}" else ""),
                            isPrimary = isPrimary,
                            receivesAlerts = caregiver.receivesAlerts,
                            hasPattern = hasPattern
                        )
                    }
                    CareCircleCard(
                        contacts = contacts,
                        onAddClick = { showAddCaregiver = true }
                    )
                }

                item {
                    val devices = mutableListOf<com.zivaa.app.presentation.profile.components.ConnectedDevice>()
                    
                    if (state.wearables.isNotEmpty()) {
                        state.wearables.forEachIndexed { index, wearableName ->
                            // For now, apply battery level to the first wearable
                            val isFirst = index == 0
                            val batteryLvl = if (isFirst) state.batteryLevel else null
                            
                            devices.add(
                                com.zivaa.app.presentation.profile.components.ConnectedDevice(
                                    id = "w_$index",
                                    initial = wearableName.take(2).replaceFirstChar { it.uppercase() },
                                    name = wearableName,
                                    statusText = if (batteryLvl != null) "Synced recently" else "Unknown status",
                                    indicatorColor = if (batteryLvl != null && batteryLvl > 20) ProfileTheme.colors.accentGreen else com.zivaa.app.ui.theme.ZivaaTheme.colors.amber,
                                    indicatorText = batteryLvl?.let { "$it%" } ?: "--",
                                    iconBackgroundColor = if (batteryLvl != null && batteryLvl > 20) ProfileTheme.colors.accentGreen else com.zivaa.app.ui.theme.ZivaaTheme.colors.amber
                                )
                            )
                        }
                    } else {
                        // Fallback default devices if nothing is set in the DB
                        devices.addAll(listOf(
                            com.zivaa.app.presentation.profile.components.ConnectedDevice(
                                id = "1",
                                initial = "Zw",
                                name = "Zivaa Watch",
                                statusText = "On his wrist · synced 5 min ago",
                                indicatorColor = ProfileTheme.colors.accentGreen,
                                indicatorText = "82%",
                                iconBackgroundColor = ProfileTheme.colors.accentGreen
                            ),
                            com.zivaa.app.presentation.profile.components.ConnectedDevice(
                                id = "2",
                                initial = "BP",
                                name = "BP monitor",
                                statusText = "Omron HEM-7156 · last reading 7:15 AM",
                                indicatorColor = ProfileTheme.colors.accentGreen,
                                indicatorText = "OK",
                                iconBackgroundColor = com.zivaa.app.ui.theme.ZivaaTheme.colors.amber,
                                initialColor = ProfileTheme.colors.textSecondary
                            ),
                            com.zivaa.app.presentation.profile.components.ConnectedDevice(
                                id = "3",
                                initial = "Gl",
                                name = "Glucometer",
                                statusText = "Last synced yesterday, 8:40 PM",
                                indicatorColor = com.zivaa.app.ui.theme.ZivaaTheme.colors.amber,
                                indicatorText = "IDLE",
                                iconBackgroundColor = com.zivaa.app.ui.theme.ZivaaTheme.colors.amber,
                                hasPattern = true,
                                initialColor = ProfileTheme.colors.textSecondary
                            )
                        ))
                    }

                    com.zivaa.app.presentation.profile.components.ConnectedDevicesCard(
                        devices = devices,
                        onAddClick = { /* No-op for now */ }
                    )
                }

                item {
                    SettingsCard(
                        header = "Alerts & Reminders",
                        toggles = listOf(
                            SettingToggle("Fall detection", "Auto-SOS if a fall is detected", fallDetection) { fallDetection = it },
                            SettingToggle("Quiet hours", "Hold non-urgent alerts 10 PM–7 AM", quietHours) { quietHours = it },
                            SettingToggle("Morning report", "A gentle summary, every day by 9:30 AM", morningReport) { morningReport = it }
                        ),
                        bottomContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showFrequencyDialog = true }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text(
                                        text = "Alert sensitivity",
                                        style = ProfileTheme.typography.cardTitle
                                    )
                                    Text(
                                        text = "Choose how easily we should nudge you",
                                        style = ProfileTheme.typography.cardSubtitle,
                                        color = ProfileTheme.colors.textSecondary
                                    )
                                }
                                
                                val displayValue = when (state.caregiverNudgePreference) {
                                    "LOW" -> "High"
                                    "HIGH" -> "Low"
                                    else -> "Medium"
                                }
                                
                                Text(
                                    text = displayValue,
                                    color = ProfileTheme.colors.accentGreen,
                                    style = ProfileTheme.typography.cardTitle
                                )
                            }
                        }
                    )
                }

                item {
                    SettingsCard(
                        header = "CUSTOMISE YOUR EXPERIENCE",
                        toggles = listOf(
                            SettingToggle("Dark theme", "Easier on the eyes in the evening", darkTheme) { viewModel.setDarkTheme(it) },
                            SettingToggle("Show my longevity plan", "Show longevity plan on Today's screen", showLongevityPlan) { viewModel.setShowLongevityPlan(it) }
                        ),
                        bottomContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .border(1.dp, ProfileTheme.colors.accentGreen, RoundedCornerShape(999.dp))
                                    .background(Color.Transparent)
                                    .clickable { onNavigateToCustomisePlan() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = ProfileTheme.colors.accentGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Customise my plan",
                                        style = ProfileTheme.typography.cardTitle.copy(color = ProfileTheme.colors.accentGreen)
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Account Settings",
                            style = ProfileTheme.typography.sectionHeader
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ProfileTheme.colors.cardBackground, RoundedCornerShape(24.dp))
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDeleteDialog = true }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Delete Account",
                                    style = ProfileTheme.typography.cardTitle.copy(color = Color(0xFFE53935))
                                )
                                Text(
                                    text = "Permanently erase all your data and settings",
                                    style = ProfileTheme.typography.cardSubtitle,
                                    color = ProfileTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(999.dp))
                            .clickable { viewModel.signOut() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Out",
                            style = ProfileTheme.typography.cardTitle.copy(color = Color(0xFFE53935))
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account") },
                    text = { Text("Are you sure you want to delete your account? This action cannot be undone and all your health data will be permanently erased.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteAccount()
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel", color = ProfileTheme.colors.textPrimary)
                        }
                    },
                    containerColor = ProfileTheme.colors.cardBackground,
                    titleContentColor = ProfileTheme.colors.textPrimary,
                    textContentColor = ProfileTheme.colors.textSecondary
                )
            }

            if (showFrequencyDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showFrequencyDialog = false },
                    sheetState = frequencySheetState,
                    containerColor = ProfileTheme.colors.cardBackground,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text("Alert Sensitivity", style = ProfileTheme.typography.sectionHeader)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val options = listOf(
                            "High" to ("Tell Me Everything" to "LOW"),
                            "Medium" to ("Balanced" to "MEDIUM"),
                            "Low" to ("Emergency Only" to "HIGH")
                        )
                        options.forEach { (displayTitle, pair) ->
                            val (desc, backendValue) = pair
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateNudgePreference(backendValue)
                                        showFrequencyDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (state.caregiverNudgePreference == backendValue),
                                    onClick = {
                                        viewModel.updateNudgePreference(backendValue)
                                        showFrequencyDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = ProfileTheme.colors.accentGreen)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = displayTitle, style = ProfileTheme.typography.cardTitle)
                                    Text(text = desc, style = ProfileTheme.typography.cardSubtitle, color = ProfileTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
