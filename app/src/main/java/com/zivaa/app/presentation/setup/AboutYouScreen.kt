package com.zivaa.app.presentation.setup

import com.zivaa.app.ui.theme.toEyebrowTitleCase
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import kotlin.math.roundToInt

@Composable
fun AboutYouScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showDobPicker by remember { mutableStateOf(false) }
    var showHeightPicker by remember { mutableStateOf(false) }
    var showWeightPicker by remember { mutableStateOf(false) }
    var showGoalWeightPicker by remember { mutableStateOf(false) }

    val closeKeyboard = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    if (showDobPicker) {
        SeniorDobBottomSheet(
            initialDate = state.dob,
            onDismissRequest = {
                showDobPicker = false
                closeKeyboard()
            },
            onDateSelected = {
                viewModel.updateAboutYou(state.name, it, state.gender)
                showDobPicker = false
                closeKeyboard()
            }
        )
    }

    if (showHeightPicker) {
        SeniorHeightBottomSheet(
            initialHeightInches = state.heightInches,
            onDismissRequest = {
                showHeightPicker = false
                closeKeyboard()
            },
            onHeightSelected = {
                viewModel.updateHeight(it)
                showHeightPicker = false
                closeKeyboard()
            }
        )
    }

    if (showWeightPicker) {
        SeniorWeightBottomSheet(
            initialWeightKg = state.weightKg,
            title = "What is your weight?",
            onDismissRequest = {
                showWeightPicker = false
                closeKeyboard()
            },
            onWeightSelected = {
                viewModel.updateWeight(it)
                showWeightPicker = false
                closeKeyboard()
            }
        )
    }

    if (showGoalWeightPicker) {
        SeniorWeightBottomSheet(
            initialWeightKg = state.goalWeightKg ?: state.weightKg,
            title = "Target weight (optional)",
            onDismissRequest = {
                showGoalWeightPicker = false
                closeKeyboard()
            },
            onWeightSelected = {
                viewModel.updateGoalWeight(it)
                showGoalWeightPicker = false
                closeKeyboard()
            }
        )
    }

    ZivaaSetupBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ZivaaTopBar(stepNo = 1, totalSteps = 5, onBack = onBack)

            ZivaaHeader(
                label = "Step 1 of 5 · About you",
                title = buildAnnotatedString {
                    append("Let's start with ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("you.")
                    }
                },
                subtitle = "Your name, date of birth, and physical measurements help us calibrate safe step goals and cardiovascular baselines."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Full Name (Standard 60dp box)
            ZivaaTextField(
                value = state.name,
                onValueChange = { viewModel.updateAboutYou(it, state.dob, state.gender) },
                label = "Your Full Name",
                placeholder = "e.g. Ranjit Kulkarni",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        closeKeyboard()
                        showDobPicker = true
                    }
                ),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 2. Date of Birth Picker (Standard 60dp box)
            StandardSelectorBox(
                label = "Date of Birth",
                displayText = if (state.dob.isNotBlank()) state.dob else "Tap to choose date of birth",
                isPlaceholder = state.dob.isBlank(),
                icon = Icons.Default.CalendarToday,
                iconDescription = "Select DOB",
                onClick = {
                    closeKeyboard()
                    showDobPicker = true
                },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 3. Gender (Standard 48dp segmented pills)
            ZivaaSegmentedGenderSelection(
                selectedOption = state.gender,
                onOptionSelected = { viewModel.updateAboutYou(state.name, state.dob, it) },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 4. Height Selector (Standard 60dp box with vertical scroll wheel)
            val heightDisplay = if (state.heightInches != null) {
                val ft = state.heightInches / 12
                val inc = state.heightInches % 12
                val cm = (state.heightInches * 2.54).roundToInt()
                "$ft ft $inc in  ($cm cm)"
            } else {
                "Tap to choose your height"
            }
            StandardSelectorBox(
                label = "How tall are you?",
                displayText = heightDisplay,
                isPlaceholder = state.heightInches == null,
                icon = Icons.Default.Height,
                iconDescription = "Select Height",
                onClick = {
                    closeKeyboard()
                    showHeightPicker = true
                },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 5. Weight Selector (Standard 60dp box with vertical scroll wheel)
            val weightDisplay = if (state.weightKg != null) {
                val lbs = (state.weightKg * 2.20462).roundToInt()
                "${state.weightKg} kg  ($lbs lbs)"
            } else {
                "Tap to choose your weight"
            }
            StandardSelectorBox(
                label = "Your weight",
                displayText = weightDisplay,
                isPlaceholder = state.weightKg == null,
                icon = Icons.Default.MonitorWeight,
                iconDescription = "Select Weight",
                onClick = {
                    closeKeyboard()
                    showWeightPicker = true
                },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 6. Optional Target Weight Selector (Standard 60dp box with vertical scroll wheel)
            val goalWeightDisplay = if (state.goalWeightKg != null) {
                val lbs = (state.goalWeightKg * 2.20462).roundToInt()
                "${state.goalWeightKg} kg  ($lbs lbs)"
            } else {
                "Tap to set target weight (optional)"
            }
            StandardSelectorBox(
                label = "Target weight (optional)",
                displayText = goalWeightDisplay,
                isPlaceholder = state.goalWeightKg == null,
                icon = Icons.Default.Flag,
                iconDescription = "Select Target Weight",
                onClick = {
                    closeKeyboard()
                    showGoalWeightPicker = true
                },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom Continue Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 16.dp)
        ) {
            ZivaaButton(
                text = "Continue",
                onClick = onNext,
                enabled = state.name.isNotBlank()
            )
        }
    }
}

@Composable
fun StandardSelectorBox(
    label: String,
    displayText: String,
    isPlaceholder: Boolean,
    icon: ImageVector,
    iconDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label.toEyebrowTitleCase(),
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.eyebrow,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(14.dp),
            color = ZivaaTheme.colors.bg,
            border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayText,
                    style = ZivaaTheme.typography.bodyLarge,
                    color = if (isPlaceholder) ZivaaTheme.colors.inkMute else ZivaaTheme.colors.ink
                )
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = ZivaaTheme.colors.sage,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
