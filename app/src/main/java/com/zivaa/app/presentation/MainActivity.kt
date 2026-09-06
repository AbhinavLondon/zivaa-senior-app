package com.zivaa.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.worker.HealthDataSyncWorker
import com.zivaa.app.presentation.dashboard.DashboardScreen
import com.zivaa.app.presentation.dashboard.DashboardViewModel
import com.zivaa.app.presentation.dashboard.DashboardViewModelFactory
import com.zivaa.app.ui.medicine.OrderMedicineViewModel

import com.zivaa.app.presentation.sleep.SleepScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.zivaa.app.ui.theme.ZivaaTheme
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Context
import com.zivaa.app.data.remote.DeviceRegistrationRequest
import com.zivaa.app.data.remote.ZivaaBackendClient

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy { HealthConnectManager(applicationContext) }

    private val permissions = setOf(
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ElevationGainedRecord::class),
        HealthPermission.getReadPermission(StepsCadenceRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SkinTemperatureRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(Vo2MaxRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(FloorsClimbedRecord::class)
    )

    private var currentScreen by mutableStateOf("dashboard")
    private var forceDashboardRefresh by mutableStateOf(false)
    private var selectedMeditationSound by mutableStateOf("tanpura")
    private var selectedMeditationVoice by mutableStateOf("No voice")
    private var selectedMeditationDuration by mutableStateOf(10)
    private var selectedLabPanelId by mutableStateOf("diabetes")
    private var selectedMedicineCategory by mutableStateOf("everyday")
    private var currentReportId by mutableStateOf<String?>(null)
    private var currentCategoryName by mutableStateOf<String?>(null)
    private var selectedBodyPart by mutableStateOf("neck")
    private var selectedExercise by mutableStateOf<com.zivaa.app.data.remote.SupabaseExerciseRecord?>(null)
    private lateinit var authManager: com.zivaa.app.data.remote.AuthManager
    private var isFabExpanded by mutableStateOf(true)
    private var showHealthAssistSheet by mutableStateOf(false)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private var hasCheckedForUpdate = false

    override fun onResume() {
        super.onResume()
        if (!hasCheckedForUpdate) {
            hasCheckedForUpdate = true
            try {
                com.google.firebase.appdistribution.FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()
            } catch (e: Exception) {
                // App Distribution in-app alerts are active in release builds for registered testers
            }
        }
    }

    private fun handleIntent(intent: android.content.Intent?) {
        intent?.extras?.let { extras ->
            val type = extras.getString("type")
            if (type == "coach_message") {
                currentScreen = "coach_chat"
            } else if (type == "midday_checkin" || type == "evening_checkin" || type == "morning_briefing") {
                currentScreen = "dashboard"
                forceDashboardRefresh = true
            }
        }
        intent?.data?.let { uri ->
            if (uri.scheme == "zivaa" && uri.host == "auth") {
                val fragment = uri.fragment ?: return
                // fragment is access_token=...&refresh_token=...
                val params = fragment.split("&").associate { 
                    val parts = it.split("=")
                    parts[0] to (if (parts.size > 1) parts[1] else "")
                }
                val accessToken = params["access_token"]
                val refreshToken = params["refresh_token"]
                
                if (accessToken != null && refreshToken != null) {
                    val userId = decodeJwtUserId(accessToken)
                    if (userId != null) {
                        authManager.saveSession(accessToken, refreshToken, userId)
                    }
                }
            }
        }
    }

    private fun decodeJwtUserId(token: String): String? {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                val json = org.json.JSONObject(payload)
                return json.optString("sub", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @androidx.compose.material3.ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "zivaa_nudge_channel"
            val channelName = "Zivaa Nudge Alerts"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channel = android.app.NotificationChannel(
                channelId,
                channelName,
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
            // Permissions request completed
        }

        val requestNotificationPermission = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Automatically schedule background health data sync on app start
        val workRequest = PeriodicWorkRequestBuilder<HealthDataSyncWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "HealthDataSync",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        // Trigger an immediate one-time sync so user doesn't wait an hour
        val oneTimeWork = androidx.work.OneTimeWorkRequestBuilder<HealthDataSyncWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWork)

        val prefsManager = com.zivaa.app.data.local.SyncPrefsManager(applicationContext)
        val appSettingsManager = com.zivaa.app.data.local.AppSettingsManager(applicationContext)
        authManager = com.zivaa.app.data.remote.AuthManager(applicationContext)
        com.zivaa.app.data.remote.RetrofitClient.authManager = authManager

        handleIntent(intent)

        setContent {
            var showSetup by androidx.compose.runtime.remember { 
                androidx.compose.runtime.mutableStateOf(!prefsManager.isSetupComplete() || !authManager.hasValidSession()) 
            }
            val darkThemeEnabled by appSettingsManager.darkThemeFlow.collectAsState(initial = false)

            androidx.compose.runtime.LaunchedEffect(Unit) {
                if (!showSetup) {
                    // User is already logged in, register FCM token on startup
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            val patientId = authManager.getUserId()
                            if (patientId != null) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    try {
                                        ZivaaBackendClient.apiService.registerDevice(
                                            DeviceRegistrationRequest(patient_id = patientId, fcm_token = token, timezone = java.util.TimeZone.getDefault().id)
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }

                authManager.authEvents.collect {
                    showSetup = !prefsManager.isSetupComplete() || !authManager.hasValidSession()
                    
                    if (!showSetup) {
                        // User is logged in, register FCM token
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                val patientId = authManager.getUserId()
                                if (patientId != null) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        try {
                                            ZivaaBackendClient.apiService.registerDevice(
                                                DeviceRegistrationRequest(patient_id = patientId, fcm_token = token, timezone = java.util.TimeZone.getDefault().id)
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ZivaaTheme(darkTheme = darkThemeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSetup) {
                        com.zivaa.app.presentation.setup.SetupNavHost(
                            onSetupComplete = {
                                prefsManager.setSetupComplete(true)
                                showSetup = false
                                currentScreen = "dashboard"
                            }
                        )
                    } else {
                        val viewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModelFactory(application, healthConnectManager, prefsManager, appSettingsManager)
                        )
                        val moodViewModel: com.zivaa.app.presentation.mood.MoodViewModel = viewModel(
                            factory = com.zivaa.app.presentation.mood.MoodViewModelFactory(authManager)
                        )

                        androidx.compose.runtime.LaunchedEffect(forceDashboardRefresh) {
                            if (forceDashboardRefresh) {
                                viewModel.fetchVitalsAndSync()
                                forceDashboardRefresh = false
                            }
                        }

                        if (healthConnectManager.isSdkAvailable()) {
                            val client = androidx.health.connect.client.HealthConnectClient.getOrCreate(applicationContext)
                            lifecycleScope.launch {
                                try {
                                    val grantedPermissions = client.permissionController.getGrantedPermissions()
                                    if (!grantedPermissions.containsAll(permissions)) {
                                        requestPermissions.launch(permissions)
                                    } else {
                                        viewModel.fetchVitalsAndSync()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        
                        androidx.compose.runtime.LaunchedEffect(currentScreen) {
                            isFabExpanded = true
                            if (currentScreen == "dashboard" || currentScreen == "mood") {
                                moodViewModel.fetchCheckins()
                            }
                        }

                        val activeTab = when (currentScreen) {
                            "dashboard" -> "home"
                            "care" -> "care"
                            "health_connect", "health_wallet", "lab_report", "lab_summary" -> "health"
                            "wellness", "mood", "movement", "sleep", "heart_rate" -> "wellness"
                            "profile", "settings", "primary_focus", "plan_setup", "plan" -> "ranjit"
                            else -> "home"
                        }

                        val nestedScrollConnection = androidx.compose.runtime.remember(currentScreen) {
                            object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                                private var scrollY = 0f

                                override fun onPreScroll(
                                    available: androidx.compose.ui.geometry.Offset,
                                    source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                                ): androidx.compose.ui.geometry.Offset {
                                    if (available.y < -15f) {
                                        // Scrolling down into the page
                                        isFabExpanded = false
                                    }
                                    return androidx.compose.ui.geometry.Offset.Zero
                                }

                                override fun onPostScroll(
                                    consumed: androidx.compose.ui.geometry.Offset,
                                    available: androidx.compose.ui.geometry.Offset,
                                    source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                                ): androidx.compose.ui.geometry.Offset {
                                    scrollY = (scrollY - consumed.y).coerceAtLeast(0f)

                                    if (available.y > 0.5f || scrollY <= 15f) {
                                        // Reached the very top of the page
                                        scrollY = 0f
                                        isFabExpanded = true
                                    } else if (scrollY > 30f) {
                                        isFabExpanded = false
                                    }
                                    return androidx.compose.ui.geometry.Offset.Zero
                                }

                                override suspend fun onPostFling(
                                    consumed: androidx.compose.ui.unit.Velocity,
                                    available: androidx.compose.ui.unit.Velocity
                                ): androidx.compose.ui.unit.Velocity {
                                    if (available.y > 0f) {
                                        // Fling hit the top of the page
                                        scrollY = 0f
                                        isFabExpanded = true
                                    }
                                    return androidx.compose.ui.unit.Velocity.Zero
                                }
                            }
                        }

                            androidx.compose.material3.Scaffold(
                                modifier = Modifier.nestedScroll(nestedScrollConnection),
                                containerColor = com.zivaa.app.ui.theme.ZivaaTheme.colors.bg,
                                bottomBar = {
                                    if (currentScreen != "coach_chat") {
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .padding(horizontal = 24.dp, vertical = 16.dp),
                                        contentAlignment = androidx.compose.ui.Alignment.BottomEnd
                                    ) {
                                        androidx.compose.foundation.layout.Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                                        ) {
                                            com.zivaa.app.presentation.components.BottomNavigationBar(
                                                selectedTab = activeTab,
                                                onTabSelected = { tab ->
                                                    currentScreen = when (tab) {
                                                        "home" -> "dashboard"
                                                        "care" -> "care"
                                                        "health" -> "health_wallet" // Usually health goes to wallet or connect
                                                        "wellness" -> "wellness"
                                                        "ranjit" -> "profile"
                                                        else -> "dashboard"
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = if (currentScreen == "dashboard") 16.dp else 0.dp)
                                            )
                                            if (currentScreen == "dashboard") {
                                                androidx.compose.material3.FloatingActionButton(
                                                    onClick = { showHealthAssistSheet = true },
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                                                    containerColor = com.zivaa.app.ui.theme.ZivaaTheme.colors.sage,
                                                    contentColor = androidx.compose.ui.graphics.Color.White,
                                                    modifier = Modifier.size(56.dp)
                                                ) {
                                                    androidx.compose.material3.Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                                                        contentDescription = "Add"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    }
                                },
                                floatingActionButton = {
                                    // Show the floating coach button on all screens except the chat itself or setup screens
                                    if (currentScreen != "coach_chat") {
                                        androidx.compose.foundation.layout.Column(
                                            horizontalAlignment = androidx.compose.ui.Alignment.End,
                                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                                        ) {
                                            com.zivaa.app.presentation.components.FloatingCoachButton(
                                                onClick = { currentScreen = "coach_chat" },
                                                expanded = isFabExpanded
                                            )
                                        }
                                    }
                                },
                                contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                            ) { paddingValues ->
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().consumeWindowInsets(paddingValues)) {
                                when (currentScreen) {
                            "coach_chat" -> {
                                val coachChatViewModel: com.zivaa.app.presentation.coach.CoachChatViewModel = viewModel(
                                    factory = com.zivaa.app.presentation.coach.CoachChatViewModelFactory(authManager)
                                )
                                com.zivaa.app.presentation.coach.CoachChatScreen(
                                    viewModel = coachChatViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" },
                                    onNavigateToCare = { currentScreen = "care" }
                                )
                            }
                            "plan" -> com.zivaa.app.presentation.plan.PlanScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "dashboard" }
                            )
                            "longevity" -> {
                                val longevityViewModel: com.zivaa.app.presentation.longevity.LongevityViewModel = viewModel(
                                    factory = com.zivaa.app.presentation.longevity.LongevityViewModelFactory(application)
                                )
                                com.zivaa.app.presentation.longevity.LongevityPlanScreen(
                                    viewModel = longevityViewModel,
                                    patientId = authManager.getUserId() ?: "",
                                    onBack = { currentScreen = "dashboard" }
                                )
                            }
                            "movement" -> {
                                val movementViewModel: com.zivaa.app.presentation.movement.MovementViewModel = viewModel()
                                com.zivaa.app.presentation.movement.MovementScreen(
                                    viewModel = movementViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" }
                                )
                            }
                            "sleep" -> SleepScreen(
                                onNavigateBack = { currentScreen = "dashboard" }
                            )
                            "health_connect" -> com.zivaa.app.presentation.health.HealthConnectScreen()
                            "settings" -> com.zivaa.app.presentation.settings.SettingsScreen(
                                onNavigateBack = { currentScreen = "dashboard" }
                            )
                            "profile" -> {
                                val profileViewModel: com.zivaa.app.presentation.profile.ProfileViewModel = viewModel(
                                    factory = com.zivaa.app.presentation.profile.ProfileViewModelFactory(authManager, appSettingsManager, prefsManager)
                                )
                                com.zivaa.app.presentation.profile.ProfileScreen(
                                    viewModel = profileViewModel,
                                    onNavigateToCustomisePlan = { currentScreen = "primary_focus" },
                                    onNavigateToHealthWallet = { currentScreen = "health_wallet" }
                                )
                            }
                            "mood" -> {
                                com.zivaa.app.presentation.mood.MoodScreen(
                                    viewModel = moodViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" },
                                    onNavigateToCheckIn = { currentScreen = "mood_check_in" },
                                    isDarkTheme = darkThemeEnabled
                                )
                            }
                            "health_wallet" -> {
                                val healthWalletViewModel: com.zivaa.app.ui.wallet.HealthWalletViewModel = viewModel(
                                    factory = com.zivaa.app.ui.wallet.HealthWalletViewModelFactory(
                                        repository = com.zivaa.app.data.DiagnosticReportRepository(
                                            apiService = com.zivaa.app.data.remote.RetrofitClient.apiService,
                                            dao = com.zivaa.app.data.local.AppDatabase.getDatabase(applicationContext).diagnosticReportDao()
                                        ),
                                        authManager = authManager
                                    )
                                )
                                com.zivaa.app.ui.wallet.HealthWalletScreen(
                                    viewModel = healthWalletViewModel,
                                    onNavigateToLabReport = { reportId -> 
                                        currentReportId = reportId
                                        currentScreen = "lab_report" 
                                    }
                                )
                            }
                            "lab_report" -> {
                                currentReportId?.let { rId ->
                                    val labReportViewModel: com.zivaa.app.ui.labs.report.LabReportViewModel = viewModel(
                                        key = "lab_report_$rId",
                                        factory = com.zivaa.app.ui.labs.report.LabReportViewModelFactory(
                                            reportId = rId,
                                            apiService = com.zivaa.app.data.remote.RetrofitClient.apiService
                                        )
                                    )
                                    com.zivaa.app.ui.labs.report.LabReportScreen(
                                        viewModel = labReportViewModel,
                                        onNavigateBack = { currentScreen = "health_wallet" },
                                        onCategoryClick = { categoryName ->
                                            currentCategoryName = categoryName
                                            currentScreen = "lab_summary"
                                        }
                                    )
                                } ?: run {
                                    currentScreen = "health_wallet"
                                }
                            }
                            "lab_summary" -> {
                                val rId = currentReportId
                                val cName = currentCategoryName
                                if (rId != null && cName != null) {
                                    val labSummaryViewModel: com.zivaa.app.ui.labs.summary.LabSummaryViewModel = viewModel(
                                        key = "lab_summary_${rId}_${cName}",
                                        factory = com.zivaa.app.ui.labs.summary.LabSummaryViewModelFactory(
                                            reportId = rId,
                                            categoryName = cName,
                                            supabaseApiService = com.zivaa.app.data.remote.RetrofitClient.apiService,
                                            zivaaApiService = com.zivaa.app.data.remote.ZivaaBackendClient.apiService
                                        )
                                    )
                                    com.zivaa.app.ui.labs.summary.LabSummaryScreen(
                                        viewModel = labSummaryViewModel,
                                        onNavigateBack = { currentScreen = "lab_report" }
                                    )
                                } else {
                                    currentScreen = "health_wallet"
                                }
                            }
                            "mood_check_in" -> {
                                com.zivaa.app.presentation.mood.MoodCheckInScreen(
                                    viewModel = moodViewModel,
                                    onNavigateBack = { currentScreen = "mood" },
                                    onFinish = { currentScreen = "dashboard" },
                                    isDarkTheme = darkThemeEnabled
                                )
                            }
                            "primary_focus" -> {
                                val planSetupViewModel: com.zivaa.app.presentation.plan.PlanSetupViewModel = viewModel(this@MainActivity)
                                com.zivaa.app.presentation.plan.PrimaryFocusScreen(
                                    viewModel = planSetupViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" },
                                    onNavigateNext = { currentScreen = "plan_setup" }
                                )
                            }
                            "plan_setup" -> {
                                // Important: in this manual navigation setup (without NavController), 
                                // we need to maintain the same viewModel instance if we want shared state, 
                                // but since they are composed separately, `viewModel()` might return different instances 
                                // unless scoped to the Activity. Let's scope it to MainActivity.
                                val planSetupViewModel: com.zivaa.app.presentation.plan.PlanSetupViewModel = viewModel(this@MainActivity)
                                com.zivaa.app.presentation.plan.PlanSetupScreen(
                                    viewModel = planSetupViewModel,
                                    onBack = { currentScreen = "primary_focus" },
                                    onNavigateNext = { currentScreen = "plan" }
                                )
                            }
                            "heart_rate" -> {
                                val heartRateViewModel: com.zivaa.app.presentation.heartrate.HeartRateViewModel = viewModel(
                                    factory = com.zivaa.app.presentation.heartrate.HeartRateViewModelFactory()
                                )
                                com.zivaa.app.presentation.heartrate.HeartRateScreen(
                                    viewModel = heartRateViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" }
                                )
                            }
                            "nudge_deep_dive" -> {
                                val nudgeViewModel: com.zivaa.app.presentation.dashboard.NudgeDeepDiveViewModel = viewModel(
                                    key = viewModel.selectedNudgeAlert?.id ?: "deep_dive",
                                    factory = com.zivaa.app.presentation.dashboard.NudgeDeepDiveViewModelFactory(viewModel.selectedNudgeAlert)
                                )
                                com.zivaa.app.presentation.dashboard.NudgeDeepDiveScreen(
                                    viewModel = nudgeViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" },
                                    onAction = { action ->
                                        if (action == "START_COACH_CHAT") {
                                            currentScreen = "coach_chat"
                                        }
                                    }
                                )
                            }
                            "doctor_booking_confirmation" -> {
                                com.zivaa.app.ui.care.DoctorBookingConfirmationScreen(
                                    onDone = { currentScreen = "dashboard" }
                                )
                            }
                            "doctor_booking" -> {
                                com.zivaa.app.ui.care.DoctorBookingScreen(
                                    onBack = { currentScreen = "doctor_profile" },
                                    onConfirm = { currentScreen = "doctor_booking_confirmation" }
                                )
                            }
                            "doctor_profile" -> {
                                com.zivaa.app.ui.care.DoctorProfileScreen(
                                    onBack = { currentScreen = "doctor_visit" },
                                    onNavigateToBooking = { currentScreen = "doctor_booking" }
                                )
                            }
                            "doctor_visit" -> {
                                com.zivaa.app.ui.care.DoctorVisitScreen(
                                    onBack = { currentScreen = "care" },
                                    onNavigateToDoctorProfile = { currentScreen = "doctor_profile" }
                                )
                            }
                            "care" -> {
                                com.zivaa.app.ui.care.CareScreen(
                                    onNavigateToDoctorVisit = { currentScreen = "doctor_visit" },
                                    onNavigateToLabTests = { currentScreen = "lab_tests" },
                                    onNavigateToOrderMedicine = { currentScreen = "order_medicine" }
                                )
                            }
                            "lab_tests" -> {
                                com.zivaa.app.ui.labs.LabTestsScreen(
                                    onBack = { currentScreen = "care" },
                                    onNavigateToConcernPicker = { currentScreen = "lab_concern_picker" }
                                )
                            }
                            "lab_concern_picker" -> {
                                com.zivaa.app.ui.labs.LabConcernPickerScreen(
                                    onBack = { currentScreen = "lab_tests" },
                                    onNavigateToPanelDetails = { panelId -> 
                                        selectedLabPanelId = panelId
                                        currentScreen = "lab_panel_details" 
                                    }
                                )
                            }
                            "lab_panel_details" -> {
                                com.zivaa.app.ui.labs.LabPanelDetailsScreen(
                                    panelId = selectedLabPanelId,
                                    onBack = { currentScreen = "lab_concern_picker" },
                                    onNavigateToScheduling = { currentScreen = "lab_scheduling" }
                                )
                            }
                            "lab_scheduling" -> {
                                com.zivaa.app.ui.labs.LabSchedulingScreen(
                                    onBack = { currentScreen = "lab_panel_details" },
                                    onNavigateToConfirmation = { currentScreen = "lab_booking_confirmation" }
                                )
                            }
                            "lab_booking_confirmation" -> {
                                com.zivaa.app.ui.labs.LabBookingConfirmationScreen(
                                    onDone = { currentScreen = "dashboard" }
                                )
                            }
                            "order_medicine" -> {
                                com.zivaa.app.ui.medicine.OrderMedicineScreen(
                                    onBack = { currentScreen = "care" },
                                    onNavigateToCategory = { category ->
                                        selectedMedicineCategory = category
                                        currentScreen = "everyday_medicines"
                                    }
                                )
                            }
                            "everyday_medicines" -> {
                                val medicineViewModel: OrderMedicineViewModel = viewModel(this@MainActivity)
                                com.zivaa.app.ui.medicine.EverydayMedicinesScreen(
                                    categoryId = selectedMedicineCategory,
                                    viewModel = medicineViewModel,
                                    onBack = { currentScreen = "order_medicine" },
                                    onNavigateToBasket = { currentScreen = "medicine_basket" }
                                )
                            }
                            "medicine_basket" -> {
                                val medicineViewModel: OrderMedicineViewModel = viewModel(this@MainActivity)
                                com.zivaa.app.ui.medicine.BasketScreen(
                                    viewModel = medicineViewModel,
                                    onBack = { currentScreen = "everyday_medicines" },
                                    onNavigateToDelivery = { currentScreen = "medicine_delivery" }
                                )
                            }
                            "medicine_delivery" -> {
                                val medicineViewModel: OrderMedicineViewModel = viewModel(this@MainActivity)
                                com.zivaa.app.ui.medicine.DeliveryOptionsScreen(
                                    viewModel = medicineViewModel,
                                    onBack = { currentScreen = "medicine_basket" },
                                    onNavigateToConfirmation = { currentScreen = "medicine_confirmation" }
                                )
                            }
                            "medicine_confirmation" -> {
                                val medicineViewModel: OrderMedicineViewModel = viewModel(this@MainActivity)
                                com.zivaa.app.ui.medicine.OrderPlacedScreen(
                                    viewModel = medicineViewModel,
                                    onDone = { currentScreen = "dashboard" }
                                )
                            }
                            "health_assistant_upload_lab_report" -> {
                                val uploadDocViewModel: com.zivaa.app.presentation.healthassistant.UploadDocumentViewModel = viewModel(
                                    factory = com.zivaa.app.presentation.healthassistant.UploadDocumentViewModelFactory(authManager)
                                )
                                com.zivaa.app.presentation.healthassistant.UploadDocumentScreen(
                                    title = "Let's read that report together.",
                                    subtitle = "A photo or the PDF, I'll break it down for you.",
                                    label = "LAB REPORT",
                                    viewModel = uploadDocViewModel,
                                    onNavigateBack = { currentScreen = "health_assistant_dashboard" },
                                    onUploadOptionSelected = { currentScreen = "health_wallet" }
                                )
                            }
                            "health_assistant_upload_prescription" -> {
                                val uploadDocViewModel: com.zivaa.app.presentation.healthassistant.UploadDocumentViewModel = viewModel(
                                    factory = com.zivaa.app.presentation.healthassistant.UploadDocumentViewModelFactory(authManager)
                                )
                                com.zivaa.app.presentation.healthassistant.UploadDocumentScreen(
                                    title = "Let's read the doctor's hand.",
                                    subtitle = "A photo of the prescription or the WhatsApp forward, I'll break it down for you.",
                                    label = "PRESCRIPTION",
                                    viewModel = uploadDocViewModel,
                                    onNavigateBack = { currentScreen = "health_assistant_dashboard" },
                                    onUploadOptionSelected = { currentScreen = "health_assistant_prescription_result" }
                                )
                            }
                            "health_assistant_prescription_result" -> {
                                com.zivaa.app.presentation.healthassistant.PrescriptionResultScreen(
                                    onNavigateBack = { currentScreen = "health_assistant_upload_prescription" },
                                    onSetReminders = { currentScreen = "dashboard" },
                                    onAskQuestion = { /* Mock */ }
                                )
                            }
                            "health_assistant_lab_results_summary" -> {
                                com.zivaa.app.presentation.healthassistant.LabResultsSummaryScreen(
                                    onNavigateBack = { currentScreen = "health_assistant_upload_lab_report" },
                                    onAskAboutResults = { currentScreen = "dashboard" },
                                    onShare = { /* Mock */ }
                                )
                            }
                            "wellness" -> {
                                com.zivaa.app.presentation.wellness.WellbeingScreen(
                                    isDarkTheme = darkThemeEnabled,
                                    onNavigateToHome = { currentScreen = "dashboard" },
                                    onNavigateToCare = { currentScreen = "care" },
                                    onNavigateToHealth = { currentScreen = "health_connect" },
                                    onNavigateToProfile = { currentScreen = "profile" },
                                    onNavigateToMindfulness = { currentScreen = "mindfulness_landing" },
                                    onNavigateToChooseAreas = { currentScreen = "choose_areas" },
                                    onNavigateToNutrition = { currentScreen = "nutrition" }
                                )
                            }
                            "nutrition" -> {
                                com.zivaa.app.presentation.nutrition.NutritionScreen(
                                    onNavigateBack = { currentScreen = "wellness" }
                                )
                            }
                            "nutrition_log_food" -> {
                                com.zivaa.app.presentation.nutrition.NutritionScreen(
                                    onNavigateBack = { currentScreen = "health_assistant_dashboard" },
                                    initialShowLogSheet = true
                                )
                            }
                            "choose_areas" -> {
                                com.zivaa.app.presentation.wellness.chooseareas.ChooseAreasScreen(
                                    isDarkTheme = darkThemeEnabled,
                                    onNavigateBack = { currentScreen = "wellness" },
                                    onSessionBuilt = { areas ->
                                        if (areas.isNotEmpty()) {
                                            selectedBodyPart = areas.first()
                                        }
                                        currentScreen = "exercise_list"
                                    }
                                )
                            }
                            "exercise_list" -> {
                                val exerciseListViewModel: com.zivaa.app.presentation.wellness.chooseareas.ExerciseListViewModel = viewModel(
                                    key = "exercise_list_$selectedBodyPart",
                                    factory = com.zivaa.app.presentation.wellness.chooseareas.ExerciseListViewModelFactory(
                                        apiService = com.zivaa.app.data.remote.RetrofitClient.apiService,
                                        bodyPart = selectedBodyPart
                                    )
                                )
                                com.zivaa.app.presentation.wellness.chooseareas.ExerciseListScreen(
                                    viewModel = exerciseListViewModel,
                                    bodyPartName = selectedBodyPart.replaceFirstChar { it.uppercase() },
                                    isDarkTheme = darkThemeEnabled,
                                    onNavigateBack = { currentScreen = "choose_areas" },
                                    onExerciseClick = { exercise ->
                                        selectedExercise = exercise
                                        currentScreen = "exercise_detail"
                                    }
                                )
                            }
                            "exercise_detail" -> {
                                selectedExercise?.let { exercise ->
                                    com.zivaa.app.presentation.wellness.chooseareas.ExerciseDetailScreen(
                                        exercise = exercise,
                                        isDarkTheme = darkThemeEnabled,
                                        onNavigateBack = { currentScreen = "exercise_list" }
                                    )
                                } ?: run {
                                    currentScreen = "exercise_list" // Fallback if null
                                }
                            }
                            "mindfulness_landing" -> {
                                com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme {
                                    com.zivaa.app.presentation.mindfulness.MindfulnessLandingScreen(
                                        onNavigateToBreathing = { currentScreen = "mindfulness_breathing_selection" },
                                        onNavigateToMeditation = { currentScreen = "mindfulness_meditation_selection" }
                                    )
                                }
                            }
                            "mindfulness_breathing_selection" -> {
                                com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme {
                                    com.zivaa.app.presentation.mindfulness.BreathingSelectionScreen(
                                        onBack = { currentScreen = "mindfulness_landing" },
                                        onRhythmSelected = { currentScreen = "mindfulness_active_breathing" }
                                    )
                                }
                            }
                            "mindfulness_active_breathing" -> {
                                com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme {
                                    com.zivaa.app.presentation.mindfulness.ActiveBreathingScreen(
                                        onBack = { currentScreen = "mindfulness_breathing_selection" }
                                    )
                                }
                            }
                            "mindfulness_meditation_selection" -> {
                                com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme {
                                    com.zivaa.app.presentation.mindfulness.MeditationSelectionScreen(
                                        onBack = { currentScreen = "mindfulness_landing" },
                                        onBegin = { soundId, voice, duration ->
                                            selectedMeditationSound = soundId
                                            selectedMeditationVoice = voice
                                            selectedMeditationDuration = duration
                                            currentScreen = "mindfulness_active_meditation"
                                        }
                                    )
                                }
                            }
                            "mindfulness_active_meditation" -> {
                                com.zivaa.app.presentation.mindfulness.theme.MindfulnessTheme {
                                    com.zivaa.app.presentation.mindfulness.ActiveMeditationScreen(
                                        onBack = { currentScreen = "mindfulness_landing" },
                                        soundId = selectedMeditationSound,
                                        voice = selectedMeditationVoice,
                                        durationMinutes = selectedMeditationDuration
                                    )
                                }
                            }
                            else -> DashboardScreen(
                                viewModel = viewModel,
                                moodViewModel = moodViewModel,
                                onNavigateToPlan = { currentScreen = "plan" },
                                onNavigateToHealthConnect = { currentScreen = "health_connect" },
                                onNavigateToSettings = { currentScreen = "settings" },
                                onNavigateToMovement = { currentScreen = "movement" },
                                onNavigateToSleep = { currentScreen = "sleep" },
                                onNavigateToProfile = { currentScreen = "profile" },
                                onNavigateToMood = { currentScreen = "mood" },
                                onNavigateToCheckIn = { currentScreen = "mood_check_in" },
                                onNavigateToHeartRate = { currentScreen = "heart_rate" },
                                onNavigateToDeepDive = { currentScreen = "nudge_deep_dive" },
                                onNavigateToCare = { currentScreen = "care" },
                                onNavigateToHealthAssistant = { showHealthAssistSheet = true },
                                onNavigateToWellness = { currentScreen = "wellness" },
                                onNavigateToLongevity = { currentScreen = "longevity" }
                            )
                        } // end when
                        
                                if (showHealthAssistSheet) {
                                    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
                                    androidx.compose.material3.ModalBottomSheet(
                                        onDismissRequest = { showHealthAssistSheet = false },
                                        sheetState = sheetState,
                                        containerColor = com.zivaa.app.ui.theme.ZivaaTheme.colors.bg,
                                        windowInsets = androidx.compose.foundation.layout.WindowInsets(0)
                                    ) {
                                        androidx.compose.foundation.layout.Column(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                                        ) {
                                            androidx.compose.material3.Text(
                                                text = "Quick Actions",
                                                style = com.zivaa.app.ui.theme.ZivaaTheme.typography.titleLarge,
                                                color = com.zivaa.app.ui.theme.ZivaaTheme.colors.textStrong,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            androidx.compose.foundation.layout.Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                                            ) {
                                                com.zivaa.app.presentation.healthassistant.HealthAssistantGridCard(
                                                    modifier = Modifier.weight(1f),
                                                    icon = androidx.compose.material.icons.Icons.Outlined.Description,
                                                    iconBg = com.zivaa.app.ui.theme.ZivaaTheme.colors.leaf,
                                                    title = "Upload lab\nreport",
                                                    onClick = { showHealthAssistSheet = false; currentScreen = "health_assistant_upload_lab_report" }
                                                )
                                                com.zivaa.app.presentation.healthassistant.HealthAssistantGridCard(
                                                    modifier = Modifier.weight(1f),
                                                    icon = androidx.compose.material.icons.Icons.Outlined.Link,
                                                    iconBg = com.zivaa.app.ui.theme.ZivaaTheme.colors.clay,
                                                    title = "Upload\nprescription",
                                                    onClick = { showHealthAssistSheet = false; currentScreen = "health_assistant_upload_prescription" }
                                                )
                                            }
                                            androidx.compose.foundation.layout.Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                                            ) {
                                                com.zivaa.app.presentation.healthassistant.HealthAssistantGridCard(
                                                    modifier = Modifier.weight(1f),
                                                    icon = androidx.compose.material.icons.Icons.Outlined.Restaurant,
                                                    iconBg = com.zivaa.app.ui.theme.ZivaaTheme.colors.sage,
                                                    title = "Log Food",
                                                    onClick = { showHealthAssistSheet = false; currentScreen = "nutrition_log_food" }
                                                )
                                                com.zivaa.app.presentation.healthassistant.HealthAssistantGridCard(
                                                    modifier = Modifier.weight(1f),
                                                    icon = androidx.compose.material.icons.Icons.Outlined.Mood,
                                                    iconBg = com.zivaa.app.ui.theme.ZivaaTheme.colors.amber,
                                                    title = "Log Mood",
                                                    onClick = { showHealthAssistSheet = false; currentScreen = "mood" }
                                                )
                                            }
                                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
                                        }
                                    }
                                }
                            } // end Box
                        } // end Scaffold
                    } // end else
                } // end Surface
            } // end ZivaaTheme
        } // end setContent
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        if (currentScreen != "dashboard") {
            currentScreen = "dashboard"
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
