package com.zivaa.app.presentation.setup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.zivaa.app.data.health.HealthConnectManager
import com.zivaa.app.data.health.HealthConnectSupport
import com.zivaa.app.presentation.plan.PlanSetupTheme
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.SelectionCard
import com.zivaa.app.presentation.plan.SelectionChip
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthAndHabitsScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val healthConnectManager = remember { HealthConnectManager(context) }

    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val requestPermissionsLauncher = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.permissions)) {
            viewModel.selectWearable("Google Health Connect")
            Toast.makeText(context, "Health Connect connected successfully!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.selectWearable("Google Health Connect")
            Toast.makeText(context, "Some permissions granted. You can adjust them anytime.", Toast.LENGTH_SHORT).show()
        }
    }

    val isConnected = state.selectedWearable == "Google Health Connect"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.zivaa.app.ui.theme.LocalZivaaColors.current.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            ZivaaTopBar(stepNo = 3, totalSteps = 5, onBack = onBack)
            ZivaaHeader(
                label = "Step 3 of 5 · Baseline",
                title = "Health & Lifestyle",
                subtitle = "Helps Zivaa personalize daily activity thresholds, reminders, and gentle safety guardrails."
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Pre-existing Conditions
                SelectionCard(
                    tone = PlanSetupTones.Clay,
                    icon = Icons.Default.Favorite,
                    eyebrow = "Health · pick all that apply",
                    question = "Anything to plan around?",
                    hint = "Timings, meals and movement adjust quietly around these.",
                    options = listOf(
                        "Diabetes",
                        "Blood pressure",
                        "Heart condition",
                        "High cholesterol",
                        "Knee or joint pain",
                        "Back pain",
                        "Thyroid",
                        "Acid reflux / GERD",
                        "Asthma / Breathing",
                        "Light sleep",
                        "Low appetite",
                        "Fatigue",
                        "None"
                    ),
                    selectedOptions = state.selectedConditions,
                    onOptionToggled = { viewModel.toggleCondition(it) }
                )

                // Section 2: Lifestyle Habits (Smoking & Alcohol)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color(0x0A000000),
                            spotColor = Color(0x10000000)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = PlanSetupTheme.BgElev,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, PlanSetupTheme.Line)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PlanSetupTones.Amber.bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmokingRooms,
                                    contentDescription = null,
                                    tint = PlanSetupTones.Amber.fg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Lifestyle · Daily Habits".toEyebrowTitleCase(),
                                    fontFamily = Manrope,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.2.sp,
                                    color = PlanSetupTheme.Eyebrow,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Daily Habits",
                                    fontFamily = Manrope,
                                    fontSize = 21.sp,
                                    lineHeight = 23.sp,
                                    letterSpacing = (-0.1).sp,
                                    color = PlanSetupTheme.Ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = "Confidential & optional. Used to fine-tune your hydration and metabolic suggestions.",
                                    fontFamily = Manrope,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = PlanSetupTheme.InkSoft,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        // Smoking Chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Smoking",
                                fontFamily = Manrope,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlanSetupTheme.Ink
                            )
                            val smokingOptions = listOf("Non-smoker", "Former smoker", "Occasional", "Regular")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                smokingOptions.forEach { option ->
                                    val isSelected = state.smokingStatus == option
                                    SelectionChip(
                                        label = option,
                                        isSelected = isSelected,
                                        tone = PlanSetupTones.Amber,
                                        onClick = {
                                            viewModel.updateSmokingStatus(if (isSelected) "" else option)
                                        }
                                    )
                                }
                            }
                        }

                        // Alcohol Chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Alcohol intake",
                                fontFamily = Manrope,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlanSetupTheme.Ink
                            )
                            val alcoholOptions = listOf("Never / Teetotaler", "Occasional", "Moderate", "Regular")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                alcoholOptions.forEach { option ->
                                    val isSelected = state.alcoholStatus == option
                                    SelectionChip(
                                        label = option,
                                        isSelected = isSelected,
                                        tone = PlanSetupTones.Amber,
                                        onClick = {
                                            viewModel.updateAlcoholStatus(if (isSelected) "" else option)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Wearable & Health Connect
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color(0x0A000000),
                            spotColor = Color(0x10000000)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = PlanSetupTheme.BgElev,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isConnected) 1.dp else 0.5.dp,
                        if (isConnected) PlanSetupTones.Sage.bg else PlanSetupTheme.Line
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PlanSetupTones.Sage.bg.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sync,
                                    contentDescription = null,
                                    tint = PlanSetupTones.Sage.bg,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Wearable & Activity Sync".toEyebrowTitleCase(),
                                    fontFamily = Manrope,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.2.sp,
                                    color = PlanSetupTheme.Eyebrow,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Google Health Connect",
                                    fontFamily = Manrope,
                                    fontSize = 20.sp,
                                    lineHeight = 22.sp,
                                    color = PlanSetupTheme.Ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sync steps, sleep, and heart rate quietly in the background from your watch or phone.",
                                    fontFamily = Manrope,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp,
                                    color = PlanSetupTheme.InkSoft
                                )
                            }
                        }

                        if (isConnected) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PlanSetupTones.Sage.bg.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PlanSetupTones.Sage.bg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Connected · Vitals & steps will sync automatically",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PlanSetupTones.Sage.bg
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val support = healthConnectManager.checkHealthConnectSupportAndRedirect()
                                        if (support == HealthConnectSupport.AVAILABLE) {
                                            if (healthConnectManager.hasAllPermissions()) {
                                                viewModel.selectWearable("Google Health Connect")
                                                Toast.makeText(context, "Health Connect is already connected!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                requestPermissionsLauncher.launch(healthConnectManager.permissions)
                                            }
                                        } else if (support == HealthConnectSupport.INSTALL_REQUIRED) {
                                            Toast.makeText(context, "Please install Health Connect from the Play Store.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PlanSetupTones.Sage.bg)
                            ) {
                                Text(
                                    text = "Connect Health Connect (Optional)",
                                    fontFamily = Manrope,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PlanSetupTones.Sage.bg
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ZivaaButton(
                text = "Continue",
                onClick = onNext,
                enabled = true
            )
        }
    }
}
