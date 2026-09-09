package com.zivaa.app.presentation.dashboard

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.data.remote.DailyPlanTask
import com.zivaa.app.data.remote.NudgeAlert
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import com.zivaa.app.data.remote.SupabasePatientCheckin
import com.zivaa.app.presentation.mood.components.FaceIcon
import com.zivaa.app.presentation.mood.components.getHeroBackgroundColor
import com.zivaa.app.presentation.mood.components.getHeroIconIndex
import com.zivaa.app.presentation.mood.MoodViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    moodViewModel: MoodViewModel,
    onNavigateToPlan: () -> Unit = {},
    onNavigateToHealthConnect: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMovement: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMood: () -> Unit = {},
    onNavigateToCheckIn: () -> Unit = {},
    onNavigateToHeartRate: () -> Unit = {},
    onNavigateToDeepDive: () -> Unit = {},
    onNavigateToCare: () -> Unit = {},
    onNavigateToHealthAssistant: () -> Unit = {},
    onNavigateToWellness: () -> Unit = {},
    onNavigateToLongevity: () -> Unit = {},
    onNavigateToMindfulness: () -> Unit = {},
    onNavigateToCoachChat: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val isDarkTheme = isSystemInDarkTheme()
    val showLongevityPlan by viewModel.showLongevityPlanEnabled.collectAsState(initial = true)
    val context = LocalContext.current
    var cityName by remember { mutableStateOf("") }
    
    val dateString = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEE dd MMM", Locale.ENGLISH)
        today.format(formatter)
    }

    val currentDate = remember(cityName) {
        val cityFormatted = if (cityName.isNotEmpty()) cityName.split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } } else ""
        if (cityFormatted.isNotEmpty()) "Today · $dateString · $cityFormatted" else "Today · $dateString"
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, let's fetch location
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val provider = locationManager.getBestProvider(android.location.Criteria(), true)
                    if (provider != null) {
                        val location = locationManager.getLastKnownLocation(provider)
                        if (location != null) {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                cityName = addresses[0].locality?.uppercase(Locale.ENGLISH) ?: ""
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore failure to fetch
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            withContext(Dispatchers.IO) {
                try {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val provider = locationManager.getBestProvider(android.location.Criteria(), true)
                    if (provider != null) {
                        val location = locationManager.getLastKnownLocation(provider)
                        if (location != null) {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val name = addresses[0].locality?.uppercase(Locale.ENGLISH) ?: ""
                                withContext(Dispatchers.Main) {
                                    cityName = name
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore failure to fetch
                }
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    var activeTab by remember { mutableStateOf("home") }
        val scope = rememberCoroutineScope()

        LaunchedEffect(viewModel.activeHeroPeriod) {
            if (viewModel.activeHeroPeriod == "afternoon") {
                viewModel.markAfternoonSummaryViewed()
            } else if (viewModel.activeHeroPeriod == "evening") {
                viewModel.markEveningSummaryViewed()
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshVitalsFromDB()
                    viewModel.fetchVitalsAndSync()
                    viewModel.refreshNudges()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Scaffold(


            containerColor = ZivaaTheme.colors.bg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ZivaaTheme.colors.bg)
                        .statusBarsPadding()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                        .verticalScroll(scrollState),
                ) {
                // Top Row Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentDate,
                        style = ZivaaTheme.typography.eyebrow,
                        color = ZivaaTheme.colors.eyebrow,
                    )



                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(ZivaaTheme.colors.bgElev)
                                .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(999.dp))
                        ) {
                            if (viewModel.patientProfilePicUrl != null) {
                                coil.compose.AsyncImage(
                                    model = viewModel.patientProfilePicUrl,
                                    contentDescription = "Profile",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = ZivaaTheme.colors.ink,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                if (viewModel.syncStatus.isNotEmpty()) {
                    Text(
                        text = viewModel.syncStatus,
                        style = ZivaaTheme.typography.meta,
                        color = ZivaaTheme.colors.inkMute,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                    )
                }

                // Hero State / Anomaly Warning Card
                Box(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 22.dp, end = 22.dp)) {
                    HeroCard(
                        viewModel = viewModel,
                        activePeriod = viewModel.activeHeroPeriod,
                        onPeriodChange = { viewModel.activeHeroPeriod = it },
                        onNavigateToMindfulness = onNavigateToMindfulness
                    )
                }

                if (viewModel.todayNudgeAlerts.isNotEmpty()) {
                    Box(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { viewModel.todayNudgeAlerts.size })
                        Column {
                            androidx.compose.foundation.pager.HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    start = 22.dp, 
                                    end = if (viewModel.todayNudgeAlerts.size > 1) 48.dp else 22.dp
                                ),
                                pageSpacing = 16.dp
                            ) { page ->
                                val alert = viewModel.todayNudgeAlerts[page]
                                NudgeAlertCard(
                                    alert = alert,
                                    onSeeFullPicture = {
                                        viewModel.selectedNudgeAlert = alert
                                        onNavigateToDeepDive()
                                    }
                                )
                            }
                            if (viewModel.todayNudgeAlerts.size > 1) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(viewModel.todayNudgeAlerts.size) { iteration ->
                                        val color = if (pagerState.currentPage == iteration) ZivaaTheme.colors.accent else ZivaaTheme.colors.muted
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .size(8.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showLongevityPlan) {
                    // Longevity Plan Button
                    Box(modifier = Modifier.padding(top = 18.dp, start = 22.dp, end = 22.dp)) {
                        LongevityCard(onNavigateToLongevity)
                    }
                }

                // Goals Checklist Panel
                Box(modifier = Modifier.padding(top = 18.dp, start = 22.dp, end = 22.dp)) {
                    GoalsCard(
                        viewModel = viewModel,
                        onNavigateToPlan = onNavigateToPlan,
                        onNavigateToCoachChat = onNavigateToCoachChat,
                        onNavigateToNutrition = onNavigateToNutrition,
                        onNavigateToHealthConnect = onNavigateToHealthConnect
                    )
                }

                // Section Header: How your day went
                Text(
                    text = "How Your Day Went",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.eyebrow,
                    modifier = Modifier.padding(top = 24.dp, start = 22.dp, end = 22.dp, bottom = 10.dp)
                )

                // Vitals Stats Strip
                Box(modifier = Modifier.padding(horizontal = 22.dp)) {
                    val moodState by moodViewModel.state.collectAsState()
                    val todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                    val todayCheckins = moodState.checkins.filter { it.date == todayStr }
                    val mostRecentCheckin = todayCheckins.firstOrNull()

                    StatsStrip(
                        steps = viewModel.steps,
                        stepsGoal = viewModel.stepsGoal,
                        mood = viewModel.mood,
                        sleepHours = viewModel.sleepHours,
                        avgHeartRate = viewModel.heartRate,
                        oxygenLevel = viewModel.oxygenLevel,
                        mostRecentCheckin = mostRecentCheckin,
                        sleepInsightText = viewModel.sleepInsightText,
                        onNavigateToMovement = onNavigateToMovement,
                        onNavigateToSleep = onNavigateToSleep,
                        onNavigateToMood = onNavigateToMood,
                        onNavigateToCheckIn = onNavigateToCheckIn,
                        onNavigateToHeartRate = onNavigateToHeartRate
                    )
                }

                // Closing line text
                Text(
                    text = "We kept a quiet eye on your day, so nothing slips by. We'll be here again tomorrow morning.",
                    style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = (12.5 * 1.55).sp),
                    color = ZivaaTheme.colors.inkMute,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 22.dp)
                )
                
                Spacer(modifier = Modifier.height(130.dp))
                // Gesture Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(124.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ZivaaTheme.colors.ink.copy(alpha = 0.26f))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.eveningSummaryText != null && !viewModel.hasViewedEveningSummary) {
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(999.dp),
                                ambientColor = ZivaaTheme.colors.ink.copy(alpha = 0.4f),
                                spotColor = ZivaaTheme.colors.ink.copy(alpha = 0.6f)
                            )
                            .height(42.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(ZivaaTheme.colors.sage)
                            .clickable {
                                viewModel.activeHeroPeriod = "evening"
                                scope.launch { scrollState.animateScrollTo(0) }
                            }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFFC0DAB6)))
                            Text(
                                text = "Evening summary",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                } else if (viewModel.middaySummaryText != null && !viewModel.hasViewedAfternoonSummary) {
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(999.dp),
                                ambientColor = ZivaaTheme.colors.ink.copy(alpha = 0.4f),
                                spotColor = ZivaaTheme.colors.ink.copy(alpha = 0.6f)
                            )
                            .height(42.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(ZivaaTheme.colors.sage)
                            .clickable {
                                viewModel.activeHeroPeriod = "afternoon"
                                scope.launch { scrollState.animateScrollTo(0) }
                            }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFFC0DAB6)))
                            Text(
                                text = "Afternoon summary",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            

        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeroCard(
    viewModel: DashboardViewModel,
    activePeriod: String = "morning",
    onPeriodChange: (String) -> Unit = {},
    onNavigateToMindfulness: () -> Unit = {}
) {
    val isLateNight = java.time.LocalTime.now().hour in 0..4
    val availablePeriods = buildList {
        if (isLateNight) {
            add("latenight")
        } else {
            add("morning")
        }
        if (viewModel.middaySummaryText != null) add("afternoon")
        if (viewModel.eveningSummaryText != null) add("evening")
    }
    val totalDots = availablePeriods.size
    
    val initialPage = maxOf(0, availablePeriods.indexOf(activePeriod))
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage,
        pageCount = { totalDots }
    )

    LaunchedEffect(activePeriod, availablePeriods) {
        val targetPage = availablePeriods.indexOf(activePeriod)
        if (targetPage != -1) {
            if (pagerState.currentPage != targetPage) {
                pagerState.animateScrollToPage(targetPage)
            } else if (pagerState.currentPageOffsetFraction != 0f && !pagerState.isScrollInProgress) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    LaunchedEffect(pagerState.settledPage, availablePeriods) {
        val currentPeriod = availablePeriods.getOrNull(pagerState.settledPage)
        if (currentPeriod != null && currentPeriod != activePeriod) {
            onPeriodChange(currentPeriod)
        }
    }

    val pageOffset = pagerState.currentPageOffsetFraction
    val page = pagerState.currentPage

    val surfaceHeroColor = ZivaaTheme.colors.surfaceHero
    val sageColor = ZivaaTheme.colors.sage

    fun colorForPage(p: Int) = when (availablePeriods.getOrNull(p)) {
        "latenight" -> Color(0xFF1A237E) // Twilight deep blue
        "afternoon" -> Color(0xFFA16B40) // Afternoon warm clay
        "evening" -> Color(0xFF2C3E50) // Evening dark blue/grey
        else -> surfaceHeroColor // Morning sage
    }

    fun shadowColorForPage(p: Int) = when (availablePeriods.getOrNull(p)) {
        "latenight" -> Color(0xFF1A237E).copy(alpha = 0.16f)
        "afternoon" -> Color(0xFFA16B40).copy(alpha = 0.16f)
        "evening" -> Color(0xFF2C3E50).copy(alpha = 0.16f)
        else -> sageColor.copy(alpha = 0.16f)
    }

    val targetPageOffset = if (pageOffset > 0) 1 else if (pageOffset < 0) -1 else 0
    val currentBg = colorForPage(page)
    val nextBg = colorForPage(page + targetPageOffset)
    val fraction = kotlin.math.abs(pageOffset)
    
    val backgroundColor = androidx.compose.ui.graphics.lerp(currentBg, nextBg, fraction)
    
    val currentShadow = shadowColorForPage(page)
    val nextShadow = shadowColorForPage(page + targetPageOffset)
    val ambientShadowColor = androidx.compose.ui.graphics.lerp(currentShadow, nextShadow, fraction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ambientShadowColor,
                spotColor = ambientShadowColor
            )
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .animateContentSize()
    ) {
        // Radial Gradient background effect
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (-50).dp)
                .size(200.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ZivaaTheme.colors.sageInk.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = 200f
                    )
                )
        )
        
        Column(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                val period = availablePeriods.getOrElse(pageIndex) { "morning" }
                HeroCardContent(
                    viewModel = viewModel,
                    period = period,
                    onNavigateToMindfulness = onNavigateToMindfulness
                )
            }
            
            // Pagination Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)
            ) {
                for (i in 0 until totalDots) {
                    val weight = when (i) {
                        page -> 1f - fraction
                        page + targetPageOffset -> fraction
                        else -> 0f
                    }
                    val dotColor = androidx.compose.ui.graphics.lerp(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.9f),
                        weight
                    )
                    val dotWidth = androidx.compose.ui.unit.lerp(
                        6.dp, 
                        18.dp, 
                        weight
                    )
                    
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(999.dp))
                            .background(dotColor)
                            .clickable {
                                val clickedPeriod = availablePeriods.getOrNull(i)
                                if (clickedPeriod != null) {
                                    onPeriodChange(clickedPeriod)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroCardContent(
    viewModel: DashboardViewModel,
    period: String,
    onNavigateToMindfulness: () -> Unit = {}
) {
    val colors = ZivaaTheme.colors
    
    val eyebrowText = when (period) {
        "afternoon" -> "Afternoon Check-In"
        "evening" -> "Evening Check-In"
        else -> "All Well Today"
    }
    
    val eyebrowDotColor = when (period) {
        "afternoon" -> Color(0xFFD6A35A) // Amber/Yellowish
        "evening" -> Color(0xFF8AA676) // Leaf
        else -> colors.leaf.copy(alpha = 0.3f)
    }

    val patientName = viewModel.patientFirstName.ifEmpty { "User" }
    val cleanMorningHeadline = viewModel.morningBriefingHeadline.removeSuffix(".").removeSuffix(", Ranjit").removeSuffix(" Ranjit").removeSuffix(", $patientName").removeSuffix(" $patientName").trim()
    val morningHeadline = if (cleanMorningHeadline.isNotEmpty()) cleanMorningHeadline else "Good morning"
    
    val headlineText = when (period) {
        "latenight" -> "It's late, $patientName"
        "afternoon" -> "Good afternoon"
        "evening" -> "Good evening"
        else -> morningHeadline
    }

    val bodyText = when (period) {
        "latenight" -> viewModel.lateNightInsightText ?: "Loading your late night summary..."
        "afternoon" -> viewModel.middaySummaryText ?: "Loading your afternoon check-in..."
        "evening" -> viewModel.eveningSummaryText ?: "Loading your evening wind down..."
        else -> if (viewModel.morningBriefingText.isNotEmpty()) viewModel.morningBriefingText else "Loading your morning briefing..."
    }
    
    val contentColor = if (period == "evening" || period == "latenight") Color.White else ZivaaTheme.colors.sageInk

    Box(modifier = Modifier.fillMaxWidth()) {
        if (period == "morning") {
            MorningSunAnimation()
        } else if (period == "afternoon") {
            SunCloudAnimation()
        } else if (period == "evening" || period == "latenight") {
            MoonStarAnimation()
        }
        
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)) {
            // Eyebrow
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(contentColor.copy(alpha = 0.14f))
                    .padding(start = 9.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(eyebrowDotColor) 
                            .border(4.dp, eyebrowDotColor.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                    )
                    Text(
                        text = eyebrowText,
                        style = ZivaaTheme.typography.meta,
                        color = contentColor.copy(alpha = 0.78f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "$headlineText.",
                style = ZivaaTheme.typography.displayMedium.copy(fontSize = 28.sp, lineHeight = (28 * 1.1).sp, letterSpacing = (-0.012).em),
                color = contentColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = bodyText,
                style = ZivaaTheme.typography.bodyMedium.copy(
                    fontSize = 16.5.sp, 
                    lineHeight = (16.5 * 1.45).sp, 
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor
            )
            
            if (period == "latenight") {
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.Button(
                    onClick = onNavigateToMindfulness,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.SelfImprovement,
                        contentDescription = "Meditation",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wind-down Meditation", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun PlanGenerationAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0)
        ),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(150)
        ),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(300)
        ),
        label = "dot3"
    )

    val dots = listOf(dot1, dot2, dot3)

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { value ->
            val scale = 0.5f + (value * 0.5f)
            val alpha = 0.3f + (value * 0.7f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(10.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(ZivaaTheme.colors.inkSoft)
            )
        }
    }
}
@Composable
fun SunCloudAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(top = 24.dp, end = 24.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        // Sun
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .size(48.dp)
                .offset(x = 0.dp, y = 0.dp)
                .rotate(rotation)
        ) {
            drawCircle(color = Color(0xFFFDE68A), radius = size.minDimension / 3.5f)
            for (i in 0 until 8) {
                withTransform({
                    rotate(i * 45f)
                }) {
                    drawLine(
                        color = Color(0xFFFDE68A),
                        start = center.copy(y = center.y - size.minDimension / 3f),
                        end = center.copy(y = center.y - size.minDimension / 2f),
                        strokeWidth = 4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
        
        // Cloud 1
        Box(
            modifier = Modifier
                .offset(x = (-40 + cloudOffset).dp, y = 20.dp)
                .width(48.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.25f))
        )
        // Cloud 2
        Box(
            modifier = Modifier
                .offset(x = (-10 - cloudOffset).dp, y = 38.dp)
                .width(36.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.15f))
        )
    }
}

@Composable
fun MoonStarAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "moon_infinite")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )
    val starAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha2"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(top = 24.dp, end = 24.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        // Moon (using Path difference to draw a crescent)
        Box(
            modifier = Modifier
                .size(44.dp)
                .offset(x = 0.dp, y = 0.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val moonPath = androidx.compose.ui.graphics.Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                }
                val shadowPath = androidx.compose.ui.graphics.Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(size.width * 0.35f, -size.height * 0.15f, size.width * 1.35f, size.height * 0.85f))
                }
                val crescentPath = androidx.compose.ui.graphics.Path()
                crescentPath.op(moonPath, shadowPath, androidx.compose.ui.graphics.PathOperation.Difference)
                drawPath(crescentPath, color = Color(0xFFE2E8F0))
            }
        }
        
        // Star 1
        Box(
            modifier = Modifier
                .offset(x = (-55).dp, y = 10.dp)
                .size(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = starAlpha))
        )
        // Star 2
        Box(
            modifier = Modifier
                .offset(x = (-25).dp, y = 35.dp)
                .size(2.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = starAlpha2))
        )
        // Star 3
        Box(
            modifier = Modifier
                .offset(x = (-8).dp, y = 12.dp)
                .size(1.5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = starAlpha))
        )
    }
}

@Composable
fun MorningSunAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "morning_sun_infinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(top = 24.dp, end = 24.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .size(48.dp)
                .offset(x = 0.dp, y = 0.dp)
                .rotate(rotation)
        ) {
            val sunColor = Color.White.copy(alpha = 0.8f)
            val rayColor = Color.White.copy(alpha = 0.5f)
            
            drawCircle(color = sunColor, radius = size.minDimension / 3.2f)
            for (i in 0 until 8) {
                withTransform({
                    rotate(i * 45f)
                }) {
                    drawLine(
                        color = rayColor,
                        start = center.copy(y = center.y - size.minDimension / 2.5f),
                        end = center.copy(y = center.y - size.minDimension / 1.7f),
                        strokeWidth = 3.5f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun GoalsCard(
    viewModel: DashboardViewModel,
    onNavigateToPlan: () -> Unit,
    onNavigateToCoachChat: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToHealthConnect: () -> Unit = {}
) {
    val colors = ZivaaTheme.colors
    val allGoals = remember(
        viewModel.morningTasks,
        viewModel.afternoonTasks,
        viewModel.eveningTasks,
        viewModel.nightTasks,
        viewModel.allWeeklyPlans
    ) {
        val list = mutableListOf<Triple<String, Int, DailyPlanTask>>()
        val todayStr = java.time.LocalDate.now().toString()
        val isTodaySelected = viewModel.selectedDate.isEmpty() || viewModel.selectedDate == todayStr
        if (isTodaySelected && (viewModel.morningTasks.isNotEmpty() || viewModel.afternoonTasks.isNotEmpty() || viewModel.eveningTasks.isNotEmpty() || viewModel.nightTasks.isNotEmpty())) {
            viewModel.morningTasks.forEachIndexed { idx, task -> list.add(Triple("morning", idx, task)) }
            viewModel.afternoonTasks.forEachIndexed { idx, task -> list.add(Triple("afternoon", idx, task)) }
            viewModel.eveningTasks.forEachIndexed { idx, task -> list.add(Triple("evening", idx, task)) }
            viewModel.nightTasks.forEachIndexed { idx, task -> list.add(Triple("night", idx, task)) }
        } else {
            val todayPlan = viewModel.allWeeklyPlans.find { it.date == todayStr } ?: viewModel.allWeeklyPlans.find { it.created_at?.startsWith(todayStr) == true }
            val schedule = todayPlan?.schedule
            schedule?.morning?.forEachIndexed { idx, task -> list.add(Triple("morning", idx, task)) }
            schedule?.afternoon?.forEachIndexed { idx, task -> list.add(Triple("afternoon", idx, task)) }
            schedule?.evening?.forEachIndexed { idx, task -> list.add(Triple("evening", idx, task)) }
            schedule?.night?.forEachIndexed { idx, task -> list.add(Triple("night", idx, task)) }
        }
        list
    }

    val totalGoals = allGoals.size
    val completedGoals = allGoals.count { it.third.completed }
    val progress = if (totalGoals > 0) completedGoals.toFloat() / totalGoals else 0f
    val displayName = viewModel.patientFirstName.ifEmpty { "there" }

    val progressCaption = when {
        totalGoals == 0 -> "Setting up your personalized routine..."
        completedGoals == totalGoals -> "All $totalGoals completed today. Wonderful work, $displayName!"
        completedGoals == 0 && viewModel.isNewUser -> "A few gentle steps to get familiar with Zivaa today."
        completedGoals == 0 -> "$totalGoals goals tailored for you today. Tap each one as you go."
        else -> "$completedGoals of $totalGoals done, ${totalGoals - completedGoals} to go."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.sage.copy(alpha = 0.1f)) // sage mix with bg
            .border(0.5.dp, colors.sage.copy(alpha = 0.3f), RoundedCornerShape(22.dp)) // sage mix with line
    ) {
        // Drop shadow for the inner effect soft
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.06f),
                    spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.06f)
                )
        )
        // Inner highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(22.dp)
                )
        )
        
        Column(modifier = Modifier.padding(20.dp)) {
            // Eyebrow and Streak / Welcome Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Spark",
                        tint = ZivaaTheme.colors.sage,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (viewModel.isNewUser) "Starter Plan" else "Your Plan",
                        style = ZivaaTheme.typography.meta,
                        color = ZivaaTheme.colors.eyebrow
                    )
                }
                
                if (viewModel.isNewUser || viewModel.currentStreak <= 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.sage.copy(alpha = 0.15f))
                            .padding(horizontal = 11.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Starter",
                                tint = ZivaaTheme.colors.sage,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (viewModel.isNewUser) "DAY 1 · WELCOME" else "TODAY'S FOCUS",
                                style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.04.em),
                                color = ZivaaTheme.colors.sage
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.amber.copy(alpha = 0.15f))
                            .padding(horizontal = 11.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Streak",
                                tint = ZivaaTheme.colors.amber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "DAY ${viewModel.currentStreak} STREAK",
                                style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.04.em),
                                color = ZivaaTheme.colors.amber
                            )
                        }
                    }
                }
            }
            
            // Progress Ring and Dynamic Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                CircularProgressRing(
                    progress = progress,
                    completedCount = completedGoals,
                    totalCount = totalGoals
                )
                Column(modifier = Modifier.weight(1f)) {
                    val titleText = remember(viewModel.isNewUser, completedGoals, totalGoals, displayName) {
                        buildAnnotatedString {
                            if (viewModel.isNewUser) {
                                append("Welcome, $displayName — your ")
                                withStyle(
                                    style = SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        color = colors.sage
                                    )
                                ) {
                                    append("starter focus")
                                }
                                append(".")
                            } else if (completedGoals == totalGoals && totalGoals > 0) {
                                append("All done for today, ")
                                withStyle(
                                    style = SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        color = colors.sage
                                    )
                                ) {
                                    append(displayName)
                                }
                                append("!")
                            } else {
                                val hour = java.time.LocalTime.now().hour
                                val periodWord = when {
                                    hour < 12 -> "morning focus"
                                    hour < 17 -> "afternoon focus"
                                    else -> "evening focus"
                                }
                                append("Your ")
                                withStyle(
                                    style = SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        color = colors.sage
                                    )
                                ) {
                                    append(periodWord)
                                }
                                append(" is set.")
                            }
                        }
                    }

                    Text(
                        text = titleText,
                        style = ZivaaTheme.typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 23.sp, lineHeight = (23 * 1.14).sp, letterSpacing = (-0.01).em),
                        color = ZivaaTheme.colors.ink
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = progressCaption,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = (13.5 * 1.45).sp),
                        color = ZivaaTheme.colors.inkSoft
                    )
                }
            }

            if (totalGoals == 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ZivaaTheme.colors.surface)
                        .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        PlanGenerationAnimation()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Personalizing your routine for today...",
                            style = ZivaaTheme.typography.bodyLarge,
                            color = ZivaaTheme.colors.inkSoft,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
                
                allGoals.forEach { (period, originalIndex, task) ->
                    val goalTaskText = task.task
                    val metaText = task.time ?: period.replaceFirstChar { it.uppercase() }
                    
                    val (iconVector, bgTone, textTone) = getGoalIconAndColors(goalTaskText)

                    val isCoachTask = task.category.equals("coach", ignoreCase = true) || goalTaskText.contains("Zivaa", ignoreCase = true) || goalTaskText.contains("coach", ignoreCase = true)
                    val isNutritionTask = task.category.equals("nutrition", ignoreCase = true) || goalTaskText.contains("meal", ignoreCase = true) || goalTaskText.contains("lunch", ignoreCase = true) || goalTaskText.contains("food", ignoreCase = true) || goalTaskText.contains("breakfast", ignoreCase = true)
                    val isVitalsTask = task.category.equals("vitals", ignoreCase = true) || goalTaskText.contains("vitals", ignoreCase = true) || goalTaskText.contains("blood pressure", ignoreCase = true)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!task.completed) {
                                    when {
                                        isCoachTask -> onNavigateToCoachChat()
                                        isNutritionTask -> onNavigateToNutrition()
                                        isVitalsTask -> onNavigateToHealthConnect()
                                        else -> {
                                            when (period) {
                                                "morning" -> viewModel.toggleMorningTask(originalIndex)
                                                "afternoon" -> viewModel.toggleAfternoonTask(originalIndex)
                                                "evening" -> viewModel.toggleEveningTask(originalIndex)
                                                "night" -> viewModel.toggleNightTask(originalIndex)
                                            }
                                        }
                                    }
                                } else {
                                    when (period) {
                                        "morning" -> viewModel.toggleMorningTask(originalIndex)
                                        "afternoon" -> viewModel.toggleAfternoonTask(originalIndex)
                                        "evening" -> viewModel.toggleEveningTask(originalIndex)
                                        "night" -> viewModel.toggleNightTask(originalIndex)
                                    }
                                }
                            }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bubble Container
                        ZivaaIconBubble(
                            icon = iconVector,
                            contentDescription = goalTaskText,
                            size = 38.dp,
                            iconSize = 20.dp,
                            backgroundColor = bgTone,
                            iconTint = textTone,
                            modifier = Modifier.alpha(if (task.completed) 0.5f else 1f)
                        )
                        
                        // Label Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = goalTaskText,
                                style = ZivaaTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (task.completed) ZivaaTheme.colors.inkSoft.copy(alpha = 0.5f) else ZivaaTheme.colors.ink,
                                modifier = Modifier.alpha(if (task.completed) 0.5f else 1f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = metaText.toEyebrowTitleCase(),
                                style = ZivaaTheme.typography.meta.copy(fontSize = 12.sp, letterSpacing = 0.05.em),
                                color = ZivaaTheme.colors.eyebrow
                            )
                        }
                        
                        // Custom Rounded Checkbox
                        RoundCheckbox(
                            checked = task.completed,
                            onCheckedChange = {
                                when (period) {
                                    "morning" -> viewModel.toggleMorningTask(originalIndex)
                                    "afternoon" -> viewModel.toggleAfternoonTask(originalIndex)
                                    "evening" -> viewModel.toggleEveningTask(originalIndex)
                                    "night" -> viewModel.toggleNightTask(originalIndex)
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider(color = ZivaaTheme.colors.line, thickness = 0.5.dp)
                }

                if (completedGoals == totalGoals && totalGoals > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ZivaaTheme.colors.surfaceHero)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Celebrate",
                            tint = colors.leaf.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )
                        val namePart = if (viewModel.patientFirstName.isNotBlank()) ", ${viewModel.patientFirstName}" else ""
                        Text(
                            text = if (totalGoals == 5) "All five, done. A wonderful day$namePart." else "All $totalGoals, done. A wonderful day$namePart.",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontFamily = com.zivaa.app.ui.theme.InstrumentSerif),
                            color = ZivaaTheme.colors.sageInk
                        )
                    }
                } else if (totalGoals > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(ZivaaTheme.colors.sage)
                            .clickable { onNavigateToPlan() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "See today's full plan",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ZivaaTheme.colors.sageInk
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Go",
                                tint = ZivaaTheme.colors.sageInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsStrip(
    steps: String,
    stepsGoal: Int? = 10000,
    mood: String,
    sleepHours: String,
    avgHeartRate: String = "0",
    oxygenLevel: String = "--",
    mostRecentCheckin: SupabasePatientCheckin? = null,
    sleepInsightText: String? = null,
    modifier: Modifier = Modifier,
    onNavigateToMovement: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onNavigateToMood: () -> Unit = {},
    onNavigateToCheckIn: () -> Unit = {},
    onNavigateToHeartRate: () -> Unit = {}
) {
    val colors = ZivaaTheme.colors
    val currentSteps = steps.replace(",", "").toIntOrNull() ?: 0
    val targetGoal = stepsGoal ?: 10000
    val progressRatio = if (targetGoal > 0) (currentSteps.toFloat() / targetGoal.toFloat()) else 0f
    val safeProgress = progressRatio.coerceIn(0f, 1f)
    val isGoalMet = currentSteps >= targetGoal && currentSteps > 0

    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "movementProgressBar"
    )

    val celebrationTransition = rememberInfiniteTransition(label = "celebrationShimmer")
    val shimmerOffset by celebrationTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "celebrationShimmerOffset"
    )

    val progressBarBrush = if (isGoalMet) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFD54F),
                Color(0xFFFFF9C4),
                Color.White,
                Color(0xFFA5D6A7),
                Color(0xFFFFD54F)
            ),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 350f, 0f)
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White
            )
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Full-Width: Movement
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.1f),
                    spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(ZivaaTheme.colors.sage)
                .clickable { onNavigateToMovement() }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Movement",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "MOVEMENT",
                            style = ZivaaTheme.typography.meta.copy(fontSize = 10.5.sp, letterSpacing = 0.08.em),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    if (isGoalMet) {
                        // Congratulatory Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = "Goal Met",
                                    tint = Color(0xFFFFF59D),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Goal Met! 🎉",
                                    style = ZivaaTheme.typography.eyebrow.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.02.em
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Goal ${java.text.NumberFormat.getNumberInstance().format(targetGoal)}",
                                style = ZivaaTheme.typography.eyebrow.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Movement",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Steps Count & Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = steps,
                        style = ZivaaTheme.typography.cardTitle.copy(
                            fontStyle = FontStyle.Normal,
                            fontSize = 28.sp,
                            lineHeight = 28.sp,
                            letterSpacing = (-0.02).em
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (targetGoal > 0) "${(progressRatio * 100).toInt()}%" else "--",
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isGoalMet) Color(0xFFFFF9C4) else Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progression Bar Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                ) {
                    if (animatedProgress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(progressBarBrush)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Congratulatory Microcopy or Steps Remaining Footnote
                if (isGoalMet) {
                    val extraSteps = currentSteps - targetGoal
                    val extraText = if (extraSteps > 0) " (+${java.text.NumberFormat.getNumberInstance().format(extraSteps)} extra)" else ""
                    Text(
                        text = "🎉 Fantastic effort! You've crushed your ${java.text.NumberFormat.getNumberInstance().format(targetGoal)} step goal today$extraText.",
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White
                    )
                } else {
                    val remaining = (targetGoal - currentSteps).coerceAtLeast(0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${steps} / ${java.text.NumberFormat.getNumberInstance().format(targetGoal)} steps",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (remaining > 0) "${java.text.NumberFormat.getNumberInstance().format(remaining)} steps left" else "Almost there!",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // Left Column: Movement and Heart Rate (Stacked)
        Column(
            modifier = Modifier
                .weight(1f)
                .height(210.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Left: Oxygen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.amber.copy(alpha = 0.1f))
                    .border(0.5.dp, Color.White, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OXYGEN",
                            style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                            color = ZivaaTheme.colors.inkMute
                        )
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Oxygen",
                            tint = ZivaaTheme.colors.amber.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = oxygenLevel,
                            style = ZivaaTheme.typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 24.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em),
                            color = ZivaaTheme.colors.ink
                        )
                        if (oxygenLevel != "--") {
                            Text(
                                text = "Avg today",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = ZivaaTheme.colors.inkSoft
                            )
                        }
                    }
                }
            }

            // Bottom Left: Heart Rate
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.rose.copy(alpha = 0.1f))
                    .border(0.5.dp, Color.White, RoundedCornerShape(22.dp))
                    .clickable { onNavigateToHeartRate() }
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HEART RATE",
                            style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                            color = ZivaaTheme.colors.inkMute
                        )
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart Rate",
                            tint = ZivaaTheme.colors.rose.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (avgHeartRate != "0" && avgHeartRate.isNotEmpty()) "$avgHeartRate bpm" else "--",
                            style = ZivaaTheme.typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 24.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em),
                            color = ZivaaTheme.colors.ink
                        )
                        if (avgHeartRate != "0" && avgHeartRate.isNotEmpty()) {
                            Text(
                                text = "Range today",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = ZivaaTheme.colors.inkSoft
                            )
                        }
                    }
                }
            }
        }

        // Right Column: Sleep and Mood (Stacked)
        Column(
            modifier = Modifier
                .weight(1f)
                .height(210.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Right: Sleep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.muted.copy(alpha = 0.2f)) // Soft misty blue/grey
                    .border(0.5.dp, Color.White, RoundedCornerShape(22.dp))
                    .clickable { onNavigateToSleep() }
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SLEEP",
                            style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                            color = ZivaaTheme.colors.inkMute
                        )
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = "Sleep",
                            tint = ZivaaTheme.colors.inkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = sleepHours,
                            style = ZivaaTheme.typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 24.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em),
                            color = ZivaaTheme.colors.ink
                        )
                        if (!sleepInsightText.isNullOrBlank()) {
                            Text(
                                text = sleepInsightText,
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = ZivaaTheme.colors.inkSoft,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Bottom Right: Mood
            val moodLabel = mostRecentCheckin?.mood_label
            val moodBgColor = if (moodLabel != null) getHeroBackgroundColor(moodLabel) else colors.toneWarm.copy(alpha = 0.2f)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (moodLabel != null) moodBgColor else colors.toneWarm.copy(alpha = 0.2f))
                    .border(0.5.dp, Color.White, RoundedCornerShape(22.dp))
                    .clickable { 
                        if (moodLabel != null) {
                            onNavigateToMood()
                        } else {
                            onNavigateToCheckIn()
                        }
                    }
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MOOD",
                            style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
                            color = if (moodLabel != null) Color.White.copy(alpha = 0.7f) else ZivaaTheme.colors.inkMute
                        )
                        if (moodLabel != null) {
                            FaceIcon(
                                moodIndex = getHeroIconIndex(moodLabel),
                                size = 18.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfied,
                                contentDescription = "Mood",
                                tint = ZivaaTheme.colors.clay,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        if (moodLabel != null) {
                            Text(
                                text = moodLabel,
                                style = ZivaaTheme.typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 24.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em),
                                color = Color.White
                            )
                            val emotionsText = if (mostRecentCheckin.emotions.isNotEmpty()) {
                                mostRecentCheckin.emotions.take(2).joinToString(" & ")
                            } else {
                                "Recorded today"
                            }
                            Text(
                                text = emotionsText,
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = "Log mood",
                                style = ZivaaTheme.typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 24.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em),
                                color = ZivaaTheme.colors.ink
                            )
                            Text(
                                text = "Check in today",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = ZivaaTheme.colors.inkSoft
                            )
                        }
                    }
                }
            }
        }
    }
    // Removed Oxygen Card from here
    }
}

@Composable
fun LabInProgressBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ZivaaTheme.colors.bgElev)
            .border(0.5.dp, ZivaaTheme.colors.line, RoundedCornerShape(22.dp))
    ) {
        // Drop shadow for the inner effect soft
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = ZivaaTheme.colors.sage.copy(alpha = 0.06f),
                    spotColor = ZivaaTheme.colors.sage.copy(alpha = 0.06f)
                )
        )
        // Inner highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(22.dp)
                )
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ZivaaIconBubble(
                    icon = Icons.Default.Bloodtype,
                    contentDescription = "Lab",
                    size = 48.dp,
                    iconSize = 24.dp,
                    backgroundColor = ZivaaTheme.colors.clay,
                    iconTint = Color.White
                )
                
                Column {
                    Text(
                        text = "FULL-BODY CHECK · IN PROGRESS",
                        style = ZivaaTheme.typography.meta.copy(fontSize = 10.sp, letterSpacing = 0.07.em),
                        color = ZivaaTheme.colors.inkMute
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Samples collected this morning.",
                        style = ZivaaTheme.typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 21.sp, lineHeight = (21 * 1.16).sp, letterSpacing = (-0.005).em),
                        color = ZivaaTheme.colors.ink
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(top = 18.dp),
                color = Color(0x141D211E),
                thickness = 0.5.dp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ZivaaTheme.colors.muted)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.38f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(ZivaaTheme.colors.sage)
                    )
                }
                Text(
                    text = "RESULTS IN ~24H",
                    style = ZivaaTheme.typography.meta.copy(fontSize = 10.5.sp, letterSpacing = 0.05.em),
                    color = ZivaaTheme.colors.inkMute
                )
            }
        }
    }
}

@Composable
fun getGoalIconAndColors(taskText: String): Triple<ImageVector, Color, Color> {
    val lower = taskText.lowercase()
    val colors = ZivaaTheme.colors
    return when {
        lower.contains("zivaa") || lower.contains("coach") || lower.contains("hello") -> {
            Triple(Icons.Default.ChatBubbleOutline, colors.sage, colors.sageInk)
        }
        lower.contains("vitals") || lower.contains("blood pressure") || lower.contains("pulse") -> {
            Triple(Icons.Default.FavoriteBorder, colors.clay, colors.bg)
        }
        lower.contains("yoga") || lower.contains("exercise") -> {
            Triple(Icons.Default.SelfImprovement, colors.leaf, colors.bg)
        }
        lower.contains("stretch") -> {
            Triple(Icons.Default.Accessibility, colors.leaf, colors.bg)
        }
        lower.contains("water") || lower.contains("drink") || lower.contains("hydration") -> {
            Triple(Icons.Default.WaterDrop, colors.sage, colors.sageInk)
        }
        lower.contains("breakfast") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("eat") || lower.contains("enjoy") || lower.contains("meal") || lower.contains("food") -> {
            Triple(Icons.Default.Restaurant, colors.amber, colors.bg)
        }
        lower.contains("medication") || lower.contains("metformin") || lower.contains("pill") || lower.contains("medicine") || lower.contains("take") || lower.contains("insulin") -> {
            Triple(Icons.Default.Medication, colors.clay, colors.bg)
        }
        lower.contains("walk") || lower.contains("steps") || lower.contains("activity") || lower.contains("movement") || lower.contains("stroll") -> {
            Triple(Icons.Default.DirectionsWalk, colors.sage, colors.sageInk)
        }
        lower.contains("read") || lower.contains("book") || lower.contains("puzzle") || lower.contains("brain") || lower.contains("journal") -> {
            Triple(Icons.Default.MenuBook, colors.clay, colors.bg)
        }
        lower.contains("call") || lower.contains("family") || lower.contains("talk") || lower.contains("friend") -> {
            Triple(Icons.Default.Phone, colors.amber, colors.bg)
        }
        lower.contains("clean") || lower.contains("wash") || lower.contains("chore") || lower.contains("tidy") -> {
            Triple(Icons.Default.CleaningServices, colors.muted, colors.ink)
        }
        lower.contains("sleep") || lower.contains("bedtime") || lower.contains("relax") || lower.contains("breathing") || lower.contains("wind down") || lower.contains("feet") || lower.contains("meditation") -> {
            Triple(Icons.Default.NightsStay, colors.muted, colors.ink)
        }
        else -> {
            Triple(Icons.Default.AutoAwesome, colors.sage, colors.sageInk)
        }
    }
}

@Composable
fun CircularProgressRing(
    progress: Float,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val trackColor = ZivaaTheme.colors.muted
    val progressColor = ZivaaTheme.colors.sage
    val textColor = ZivaaTheme.colors.ink
    val textMuteColor = ZivaaTheme.colors.inkMute
    
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radiusPx = 30.dp.toPx()
            val strokeWidthPx = 7.dp.toPx()
            
            // Draw background circle
            drawCircle(
                color = trackColor,
                radius = radiusPx,
                style = Stroke(width = strokeWidthPx)
            )
            
            // Draw progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(center.x - radiusPx, center.y - radiusPx),
                size = Size(radiusPx * 2, radiusPx * 2)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = completedCount.toString(),
                style = ZivaaTheme.typography.displayLarge.copy(fontSize = 27.sp, lineHeight = 27.sp),
                color = textColor
            )
            Text(
                text = "OF $totalCount",
                style = ZivaaTheme.typography.meta.copy(fontSize = 9.sp, letterSpacing = 0.06.em),
                color = textMuteColor
            )
        }
    }
}

@Composable
fun RoundCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkedColor = ZivaaTheme.colors.sage
    val uncheckedColor = ZivaaTheme.colors.lineStrong
    
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) checkedColor else Color.Transparent)
            .border(
                width = if (checked) 0.5.dp else 1.5.dp,
                color = if (checked) checkedColor else uncheckedColor,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ZivaaIconBubble(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 12.dp,
    backgroundColor: Color = ZivaaTheme.colors.muted,
    iconTint: Color = ZivaaTheme.colors.sage
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun AnimatedGeneratingPlanCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ZivaaTheme.colors.sage.copy(alpha = 0.08f * alpha))
            .border(0.5.dp, ZivaaTheme.colors.sage.copy(alpha = 0.3f * alpha), RoundedCornerShape(14.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Generating",
                tint = ZivaaTheme.colors.sage.copy(alpha = alpha),
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
            
            Text(
                text = "Putting your plan together just for you...",
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic
                ),
                color = ZivaaTheme.colors.sageInk.copy(alpha = alpha),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NudgeAlertCard(
    alert: com.zivaa.app.data.remote.SupabaseNudgeAlert,
    onSeeFullPicture: () -> Unit = {}
) {
    val isHighRisk = alert.risk_level.equals("HIGH", ignoreCase = true)
    
    val isDarkTheme = ZivaaTheme.colors.bg == Color(0xFF14171A)
    
    val bgColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFF2E282A) else Color(0xFFF2E7E3)
    } else {
        if (isDarkTheme) Color(0xFF2D2C29) else Color(0xFFF7EFE4)
    }
    
    val borderColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFFD27A6B).copy(alpha = 0.493f) else Color(0xFF98463A).copy(alpha = 0.493f)
    } else {
        if (isDarkTheme) Color(0xFF574A36) else Color(0xFFEADCC6)
    }
    
    val accentColor = if (isHighRisk) {
        if (isDarkTheme) Color(0xFFD27A6B) else Color(0xFFA1493A)
    } else {
        if (isDarkTheme) Color(0xFFD6A35A) else Color(0xFFC98A3A)
    }
    
    val inkDark = if (isHighRisk) {
        if (isDarkTheme) Color.White else Color(0xFF2F302D)
    } else {
        if (isDarkTheme) Color(0xFFF0F0F0) else Color(0xFF53544F)
    }
    
    val inkGray = if (isDarkTheme) Color(0xFFAAAAAA) else Color(0xFF8B8B84)
    
    val formattedDate = try {
        if (alert.created_at.isNotEmpty()) {
            val instant = java.time.Instant.parse(alert.created_at)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, h:mm a").withZone(java.time.ZoneId.systemDefault())
            formatter.format(instant)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
    
    val eyebrowBase = if (isHighRisk) "Needs Attention Now" else "A Pattern Worth A Look"
    val eyebrowText = if (formattedDate.isNotEmpty()) "$eyebrowBase • $formattedDate" else eyebrowBase
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Eyebrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(accentColor)
                    )
                }
                Text(
                    text = eyebrowText,
                    style = ZivaaTheme.typography.meta.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.em
                    ),
                    color = accentColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = alert.nudge_title ?: "Recent Anomaly Detected",
                style = ZivaaTheme.typography.cardTitle.copy(
                    fontStyle = FontStyle.Normal,
                    fontSize = 16.sp,
                    lineHeight = (16 * 1.14).sp,
                    letterSpacing = (-0.01).em
                ),
                color = inkDark
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // CTA Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accentColor)
                    .clickable { onSeeFullPicture() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "See the full picture",
                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
        }
    }
}

@Composable
fun LongevityCard(onNavigateToLongevity: () -> Unit) {
    Surface(
        color = ZivaaTheme.colors.bgElev,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToLongevity() }
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), ambientColor = ZivaaTheme.colors.ink.copy(alpha=0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "My Longevity Plan",
                    style = ZivaaTheme.typography.bodyLarge,
                    color = ZivaaTheme.colors.ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track your daily habits and strategy",
                    style = ZivaaTheme.typography.meta,
                    color = ZivaaTheme.colors.inkMute
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ZivaaTheme.colors.sage.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go to Longevity Plan",
                    tint = ZivaaTheme.colors.sage,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
