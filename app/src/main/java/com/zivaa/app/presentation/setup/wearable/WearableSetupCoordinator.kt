package com.zivaa.app.presentation.setup.wearable

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.HealthConnectSupport
import com.zivaa.app.presentation.setup.SetupState
import com.zivaa.app.presentation.setup.SetupViewModel
import com.zivaa.app.presentation.setup.ZivaaTopBar
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlinx.coroutines.launch

@Composable
fun WearableSetupCoordinator(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val healthConnectManager = remember { HealthConnectManager(context) }

    var currentStep by remember { mutableStateOf<WearableSetupStep>(WearableSetupStep.BrandPicker) }
    var selectedBrand by remember { mutableStateOf(WearableBrand.SAMSUNG) }
    var pendingBrandAfterPermission by remember { mutableStateOf<WearableBrand?>(null) }

    // Diagnostic check helper
    fun runDetection(brand: WearableBrand) {
        currentStep = WearableSetupStep.CheckingStatus(brand)
        coroutineScope.launch {
            val report = healthConnectManager.checkBrandSyncStatus(brand.packageName)
            if (report.hasHealthConnectRecords) {
                // Path A: Fast-Lane Bypass!
                viewModel.selectWearable(brand.displayName)
                currentStep = WearableSetupStep.AlreadyConnected(
                    brand = brand,
                    bpm = report.latestHeartRateBpm,
                    steps = report.totalStepsToday
                )
            } else if (report.isAppInstalled) {
                // Path B: Companion App is installed, but Health Connect sync is off
                viewModel.selectWearable(brand.displayName)
                currentStep = WearableSetupStep.BridgeRequired(brand)
            } else {
                // Path C: First-time setup / App not installed
                viewModel.selectWearable(brand.displayName)
                currentStep = WearableSetupStep.CompanionPairing(brand, isAppInstalled = false)
            }
        }
    }

    // Permission launcher
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissionsLauncher = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        val brand = pendingBrandAfterPermission ?: selectedBrand
        pendingBrandAfterPermission = null
        if (granted.containsAll(healthConnectManager.permissions)) {
            runDetection(brand)
        } else {
            // Even if only partial permissions were granted, continue detection
            runDetection(brand)
        }
    }

    fun handleBrandSelection(brand: WearableBrand) {
        selectedBrand = brand
        if (brand == WearableBrand.NONE) {
            viewModel.selectWearable("None")
            onNext()
            return
        }

        coroutineScope.launch {
            val support = healthConnectManager.checkHealthConnectSupportAndRedirect()
            if (support == HealthConnectSupport.AVAILABLE) {
                if (healthConnectManager.hasAllPermissions()) {
                    runDetection(brand)
                } else {
                    pendingBrandAfterPermission = brand
                    requestPermissionsLauncher.launch(healthConnectManager.permissions)
                }
            } else if (support == HealthConnectSupport.INSTALL_REQUIRED) {
                Toast.makeText(context, "Please install Health Connect from the Play Store.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Health Connect is not supported on this device.", Toast.LENGTH_LONG).show()
                viewModel.selectWearable("None")
                onNext()
            }
        }
    }

    when (val step = currentStep) {
        is WearableSetupStep.BrandPicker -> {
            WearableDevicePickerScreen(
                selectedBrand = selectedBrand,
                onBrandSelected = { brand ->
                    handleBrandSelection(brand)
                },
                onBack = onBack
            )
        }

        is WearableSetupStep.CheckingStatus -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = ZivaaTheme.colors.sage,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Checking ${step.brand.displayName} sync...",
                    style = ZivaaTheme.typography.titleLarge,
                    color = ZivaaTheme.colors.ink
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Looking for recent heart rate and step counts.",
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.inkSoft
                )
            }
        }

        is WearableSetupStep.AlreadyConnected -> {
            WearableAlreadyConnectedCard(
                brand = step.brand,
                bpm = step.bpm,
                steps = step.steps,
                onContinue = {
                    currentStep = WearableSetupStep.WearItRight(step.brand)
                },
                onBack = {
                    currentStep = WearableSetupStep.BrandPicker
                }
            )
        }

        is WearableSetupStep.BridgeRequired -> {
            WearableBridgeGuideScreen(
                brand = step.brand,
                onCheckAgain = {
                    runDetection(step.brand)
                },
                onSkip = {
                    viewModel.selectWearable("None")
                    onNext()
                },
                onBack = {
                    currentStep = WearableSetupStep.BrandPicker
                }
            )
        }

        is WearableSetupStep.CompanionPairing -> {
            WearableCompanionGuideScreen(
                brand = step.brand,
                isAppInstalled = step.isAppInstalled,
                onNext = {
                    currentStep = WearableSetupStep.BridgeRequired(step.brand)
                },
                onSkip = {
                    viewModel.selectWearable("None")
                    onNext()
                },
                onBack = {
                    currentStep = WearableSetupStep.BrandPicker
                }
            )
        }

        is WearableSetupStep.WearItRight -> {
            WearItRightCard(
                brand = step.brand,
                onFinish = onNext,
                onBack = {
                    currentStep = WearableSetupStep.BrandPicker
                }
            )
        }
    }
}
