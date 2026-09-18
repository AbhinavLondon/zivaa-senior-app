package com.zivaa.app.presentation.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.nutrition.components.DayItem
import com.zivaa.app.presentation.nutrition.components.DaySelectorRow
import com.zivaa.app.presentation.nutrition.components.FoodItem
import com.zivaa.app.presentation.nutrition.components.LogMealBottomSheet
import com.zivaa.app.presentation.nutrition.components.MacroBalanceCard
import com.zivaa.app.presentation.nutrition.components.MealCard
import com.zivaa.app.presentation.nutrition.components.NutritionSummaryCard
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import android.net.Uri
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.window.Dialog
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat

@Composable
fun NutritionScreen(
    onNavigateBack: () -> Unit,
    initialShowLogSheet: Boolean = false,
    viewModel: NutritionViewModel = viewModel(factory = NutritionViewModelFactory())
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val context = LocalContext.current
    
    val analysisState by viewModel.analysisState.collectAsState()
    val goalsState by viewModel.goalsState.collectAsState()
    val mealsState by viewModel.mealsState.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    androidx.compose.runtime.LaunchedEffect(selectedDate) {
        viewModel.fetchMealsForDate(selectedDate)
    }
    var showLogSheetForMeal by rememberSaveable { mutableStateOf<String?>(if (initialShowLogSheet) "Snacks" else null) }
    var showBarcodeScanner by rememberSaveable { mutableStateOf(false) }
    
    var tempPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var tempPhotoUri: Uri? = tempPhotoUriString?.let { Uri.parse(it) }
    
    var activeMealForLogging by rememberSaveable { mutableStateOf<String?>(null) }
    var activeLoggingMethod by rememberSaveable { mutableStateOf("photo") }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uriToAnalyze = tempPhotoUriString?.let { Uri.parse(it) } ?: tempPhotoUri
        if (success && uriToAnalyze != null) {
            viewModel.analyzeMealPhoto(context, uriToAnalyze)
        }
    }

    val launchCamera: () -> Unit = {
        try {
            val imageDir = File(context.cacheDir, "camera_images")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            val file = File(imageDir, "meal_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            tempPhotoUriString = uri.toString()
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to start camera: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to take meal photos.", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            tempPhotoUriString = uri.toString()
            viewModel.analyzeMealPhoto(context, uri)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull()
            if (!text.isNullOrEmpty()) {
                viewModel.analyzeMealText(text)
            }
        }
    }

    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val breakfastFoods = mealsState["Breakfast"] ?: emptyList()
    val lunchFoods = mealsState["Lunch"] ?: emptyList()
    val snackFoods = mealsState["Snacks"] ?: emptyList()
    val dinnerFoods = mealsState["Dinner"] ?: emptyList()
    
    val allFoods = breakfastFoods + lunchFoods + snackFoods + dinnerFoods
    val currentCalories = allFoods.sumOf { it.calories }
    val proteinCurrent = allFoods.sumOf { it.protein }
    val carbsCurrent = allFoods.sumOf { it.carbs }
    val fatCurrent = allFoods.sumOf { it.fat }
    
    val loggedMealsCount = listOf(breakfastFoods, lunchFoods, snackFoods, dinnerFoods).count { it.isNotEmpty() }
    
    // Mock data for the selector
    val days = (0..6).map { 
        DayItem(
            date = LocalDate.now().minusDays(3).plusDays(it.toLong()),
            hasData = true
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Handle insets manually for edge-to-edge
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(
                bottom = 40.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.bgElev)
                            .border(0.5.dp, colors.line, CircleShape)
                            .clickable(onClick = onNavigateBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.ink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Food \u00B7 What You Ate",
                        style = typography.eyebrow,
                        color = colors.eyebrow
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Day Selector
                DaySelectorRow(
                    days = days,
                    selectedDate = selectedDate,
                    onDaySelected = { selectedDate = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                    // Summary Card
                    NutritionSummaryCard(
                        currentCalories = currentCalories,
                        totalCalories = goalsState.targetCalories,
                        title = if (currentCalories == 0) "No meals logged yet." else "Nicely on track.",
                        description = if (currentCalories == 0) "Start logging your meals to track your progress." else "Two meals in, dinner still to come. Protein is the one to watch."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Macros Card
                    MacroBalanceCard(
                        proteinCurrent = proteinCurrent, proteinTotal = goalsState.targetProteinG,
                        carbsCurrent = carbsCurrent, carbsTotal = goalsState.targetCarbsG,
                        fatCurrent = fatCurrent, fatTotal = goalsState.targetFatG
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    
                    // Meals Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meals Today",
                            style = typography.eyebrow,
                            color = colors.eyebrow
                        )
                        Text(
                            text = "$loggedMealsCount Of 4 Logged",
                            style = typography.meta.copy(fontSize = 11.sp),
                            color = colors.textMeta
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Breakfast
                    MealCard(
                        title = "Breakfast",
                        timeRange = "7:00 - 9:30 AM",
                        totalCalories = if (breakfastFoods.isEmpty()) null else breakfastFoods.sumOf { it.calories },
                        icon = Icons.Outlined.WbSunny,
                        iconTint = colors.amber,
                        iconBgColor = colors.amber.copy(alpha = 0.15f),
                        foods = breakfastFoods,
                        onAddClick = { showLogSheetForMeal = "Breakfast" },
                        onRemoveFood = { id -> viewModel.deleteMeal(id, selectedDate) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Lunch
                    MealCard(
                        title = "Lunch",
                        timeRange = "12:30 - 2:00 PM",
                        totalCalories = if (lunchFoods.isEmpty()) null else lunchFoods.sumOf { it.calories },
                        icon = Icons.Default.WbSunny,
                        iconTint = colors.clay,
                        iconBgColor = colors.clay.copy(alpha = 0.15f),
                        foods = lunchFoods,
                        onAddClick = { showLogSheetForMeal = "Lunch" },
                        onRemoveFood = { id -> viewModel.deleteMeal(id, selectedDate) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Snacks
                    MealCard(
                        title = "Snacks",
                        timeRange = "THROUGH THE DAY",
                        totalCalories = if (snackFoods.isEmpty()) null else snackFoods.sumOf { it.calories },
                        icon = Icons.Default.Coffee,
                        iconTint = colors.leaf,
                        iconBgColor = colors.leaf.copy(alpha = 0.15f),
                        foods = snackFoods,
                        onAddClick = { showLogSheetForMeal = "Snacks" },
                        onRemoveFood = { id -> viewModel.deleteMeal(id, selectedDate) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Dinner
                    MealCard(
                        title = "Dinner",
                        timeRange = "7:30 - 9:00 PM",
                        totalCalories = if (dinnerFoods.isEmpty()) null else dinnerFoods.sumOf { it.calories },
                        icon = Icons.Default.Nightlight,
                        iconTint = colors.sage,
                        iconBgColor = colors.sage.copy(alpha = 0.15f),
                        foods = dinnerFoods,
                        emptyMessage = if (dinnerFoods.isEmpty()) "Not eaten yet. We'll ask again around eight." else null,
                        onAddClick = { showLogSheetForMeal = "Dinner" },
                        onRemoveFood = { id -> viewModel.deleteMeal(id, selectedDate) }
                    )
                    
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        text = "A photo is enough. We'll work out the rest, and you can always correct us.",
                        style = typography.bodySmall,
                        color = colors.textMeta,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        
        // Bottom Sheet
        if (showLogSheetForMeal != null) {
            LogMealBottomSheet(
                mealName = showLogSheetForMeal!!,
                onDismissRequest = { showLogSheetForMeal = null },
                onOptionSelected = { option ->
                    activeMealForLogging = showLogSheetForMeal
                    activeLoggingMethod = option
                    when (option) {
                        "photo" -> {
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCameraPermission) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        "barcode" -> {
                            showBarcodeScanner = true
                        }
                        "upload" -> {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        "voice" -> {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            }
                            speechLauncher.launch(intent)
                        }
                        "search" -> {
                            showSearchDialog = true
                        }
                    }
                    showLogSheetForMeal = null
                }
            )
        }
        
        if (showBarcodeScanner) {
            com.zivaa.app.presentation.nutrition.components.BarcodeScannerScreen(
                onBarcodeDetected = { barcode ->
                    showBarcodeScanner = false
                    viewModel.analyzeBarcode(barcode)
                },
                onClose = {
                    showBarcodeScanner = false
                }
            )
        }
        
        // Show Loading/Success Dialogs for Image Analysis
        if (analysisState is NutritionAnalysisState.Analyzing) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                        .background(colors.bgElev)
                        .border(1.dp, colors.borderHairline, androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = colors.sage,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Analyzing meal...",
                            style = typography.bodyMedium,
                            color = colors.textStrong
                        )
                    }
                }
            }
        } else if (analysisState is NutritionAnalysisState.Success) {
            val state = (analysisState as NutritionAnalysisState.Success)
            var showConfirmation by remember { mutableStateOf(true) }

            if (showConfirmation) {
                // Show Confirmation Screen overlaid on top
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bg)
                        .padding(innerPadding)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    com.zivaa.app.presentation.nutrition.components.MealConfirmationScreen(
                        imageUri = tempPhotoUri,
                        mealName = state.mealName,
                        initialFoods = state.foods.map {
                            FoodItem(
                                id = System.currentTimeMillis().toString() + it.name,
                                name = it.name,
                                calories = it.calories.toInt(),
                                initials = it.name.take(2).uppercase(),
                                protein = it.protein.toInt(),
                                carbs = it.carbs.toInt(),
                                fat = it.fat.toInt()
                            )
                        },
                        analysisText = state.analysis,
                        onConfirm = { finalFoods, quantity ->
                            val mealType = activeMealForLogging ?: run {
                                val currentHour = java.time.LocalTime.now().hour
                                when {
                                    currentHour < 11 -> "Breakfast"
                                    currentHour < 15 -> "Lunch"
                                    currentHour < 18 -> "Snacks"
                                    else -> "Dinner"
                                }
                            }
                            viewModel.saveMealToDatabase(
                                mealName = state.mealName ?: "Meal",
                                mealQuantity = quantity,
                                mealType = mealType,
                                loggingMethod = activeLoggingMethod,
                                analysisText = state.analysis,
                                date = selectedDate,
                                foods = finalFoods.map {
                                    com.zivaa.app.data.remote.FoodItemResult(
                                        name = it.name,
                                        calories = it.calories.toDouble(),
                                        protein = it.protein.toDouble(),
                                        carbs = it.carbs.toDouble(),
                                        fat = it.fat.toDouble()
                                    )
                                }
                            )
                            showConfirmation = false
                            viewModel.resetState()
                            activeMealForLogging = null
                            tempPhotoUriString = null
                        },
                        onCancel = {
                            showConfirmation = false
                            viewModel.resetState()
                            activeMealForLogging = null
                            tempPhotoUriString = null
                        }
                    )
                }
            }
        } else if (analysisState is NutritionAnalysisState.Error) {
             androidx.compose.runtime.LaunchedEffect(analysisState) {
                  viewModel.resetState()
             }
        }

        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                containerColor = colors.bgElev,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                title = {
                    Text(
                        "Search for Food",
                        style = typography.titleLarge.copy(fontSize = 22.sp),
                        color = colors.textStrong
                    )
                },
                text = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "What did you eat?",
                                style = typography.bodyMedium,
                                color = colors.textMeta
                            )
                        },
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.sage,
                            unfocusedBorderColor = colors.borderStrong,
                            focusedTextColor = colors.textStrong,
                            unfocusedTextColor = colors.textStrong,
                            cursorColor = colors.sage,
                            focusedContainerColor = colors.bg,
                            unfocusedContainerColor = colors.bg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.analyzeMealText(searchQuery)
                            }
                            showSearchDialog = false
                            searchQuery = ""
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.sage,
                            contentColor = colors.sageInk
                        )
                    ) {
                        Text(
                            "Search",
                            style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showSearchDialog = false },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            "Cancel",
                            style = typography.bodyMedium,
                            color = colors.textMeta
                        )
                    }
                }
            )
        }
    }
}
