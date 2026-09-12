package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthAndHabitsScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var customConditionText by remember { mutableStateOf("") }

    val baseConditions = remember {
        listOf(
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
        )
    }

    // Combine base conditions with any custom conditions added by the user
    val customConditions = remember(state.selectedConditions) {
        state.selectedConditions.filter { it !in baseConditions }
    }
    val allConditions = remember(customConditions) {
        customConditions + baseConditions
    }

    val smokingOptions = remember {
        listOf("Non-smoker", "Former smoker", "Occasional", "Regular")
    }

    val alcoholOptions = remember {
        listOf("Never / Teetotaler", "Occasional", "Moderate", "Regular")
    }

    val commitCustomCondition = {
        if (customConditionText.trim().isNotBlank()) {
            viewModel.addCustomCondition(customConditionText.trim())
            customConditionText = ""
        }
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val handleNext = {
        if (customConditionText.trim().isNotBlank()) {
            viewModel.addCustomCondition(customConditionText.trim())
            customConditionText = ""
        }
        onNext()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZivaaTheme.colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            ZivaaTopBar(stepNo = 3, totalSteps = 5, onBack = onBack)

            ZivaaHeader(
                label = "Step 3 of 5 · Baseline",
                title = buildAnnotatedString {
                    append("Health & ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ZivaaTheme.colors.sage)) {
                        append("lifestyle")
                    }
                },
                subtitle = "Helps Zivaa calibrate your daily checklist, safe physical baselines, and nutrition advice."
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 1. Pre-existing Conditions (No Card, Clean Surface)
            Text(
                text = "Anything to plan around? (Optional)".toEyebrowTitleCase(),
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.eyebrow,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Timings, meals and safe movement adjust quietly around these.",
                style = ZivaaTheme.typography.bodyMedium,
                color = ZivaaTheme.colors.inkSoft,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Condition Pills
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                allConditions.forEach { condition ->
                    val isSelected = state.selectedConditions.contains(condition)
                    Surface(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.toggleCondition(condition)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.12f) else ZivaaTheme.colors.bgElev,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.lineStrong
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ZivaaTheme.colors.sage,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = condition,
                                style = ZivaaTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.ink
                            )
                        }
                    }
                }
            }

            // Free-Text Option to add any other condition
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customConditionText,
                    onValueChange = { customConditionText = it },
                    placeholder = {
                        Text(
                            text = "Add another condition or note...",
                            style = ZivaaTheme.typography.bodyMedium,
                            color = ZivaaTheme.colors.inkMute
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZivaaTheme.colors.sage,
                        unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
                        focusedContainerColor = ZivaaTheme.colors.bgElev,
                        unfocusedContainerColor = ZivaaTheme.colors.bgElev,
                        cursorColor = ZivaaTheme.colors.sage,
                        focusedTextColor = ZivaaTheme.colors.ink,
                        unfocusedTextColor = ZivaaTheme.colors.ink
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { commitCustomCondition() }
                    ),
                    textStyle = ZivaaTheme.typography.bodyMedium
                )

                Surface(
                    onClick = { commitCustomCondition() },
                    modifier = Modifier
                        .height(54.dp)
                        .width(76.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (customConditionText.trim().isNotBlank()) ZivaaTheme.colors.sage else ZivaaTheme.colors.bgElev,
                    border = BorderStroke(
                        1.dp,
                        if (customConditionText.trim().isNotBlank()) ZivaaTheme.colors.sage else ZivaaTheme.colors.lineStrong
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+ Add",
                            style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (customConditionText.trim().isNotBlank()) Color.White else ZivaaTheme.colors.inkMute
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Smoking Habit (Optional - No Default Selection)
            Text(
                text = "Smoking Habit (Optional)".toEyebrowTitleCase(),
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.eyebrow,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Confidential. Used to calibrate circulation and respiratory baselines.",
                style = ZivaaTheme.typography.bodyMedium,
                color = ZivaaTheme.colors.inkSoft,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                smokingOptions.forEach { option ->
                    val isSelected = state.smokingStatus == option
                    Surface(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.updateSmokingStatus(if (isSelected) "" else option)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.12f) else ZivaaTheme.colors.bgElev,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.lineStrong
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ZivaaTheme.colors.sage,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = option,
                                style = ZivaaTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.ink
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Alcohol Intake (Optional - No Default Selection)
            Text(
                text = "Alcohol Intake (Optional)".toEyebrowTitleCase(),
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.eyebrow,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Confidential. Helps calibrate hydration and metabolic suggestions.",
                style = ZivaaTheme.typography.bodyMedium,
                color = ZivaaTheme.colors.inkSoft,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                alcoholOptions.forEach { option ->
                    val isSelected = state.alcoholStatus == option
                    Surface(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.updateAlcoholStatus(if (isSelected) "" else option)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.12f) else ZivaaTheme.colors.bgElev,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.lineStrong
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ZivaaTheme.colors.sage,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = option,
                                style = ZivaaTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.ink
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Continue Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ZivaaButton(
                text = "Continue",
                onClick = handleNext,
                enabled = true
            )
        }
    }
}
