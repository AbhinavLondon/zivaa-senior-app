package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.text.input.KeyboardType
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

    // Unit selectors for Height & Weight
    var heightUnit by remember { mutableStateOf("ft/in") }
    var weightUnit by remember { mutableStateOf("kg") }

    // Local text states synced with SetupState
    var feetText by remember(state.heightInches, heightUnit) {
        mutableStateOf(
            if (state.heightInches != null && heightUnit == "ft/in") {
                (state.heightInches / 12).toString()
            } else ""
        )
    }
    var inchesText by remember(state.heightInches, heightUnit) {
        mutableStateOf(
            if (state.heightInches != null && heightUnit == "ft/in") {
                (state.heightInches % 12).toString()
            } else ""
        )
    }
    var cmText by remember(state.heightInches, heightUnit) {
        mutableStateOf(
            if (state.heightInches != null && heightUnit == "cm") {
                (state.heightInches * 2.54).roundToInt().toString()
            } else ""
        )
    }

    var weightText by remember(state.weightKg, weightUnit) {
        mutableStateOf(
            if (state.weightKg != null) {
                if (weightUnit == "lbs") (state.weightKg * 2.20462).roundToInt().toString()
                else state.weightKg.toString()
            } else ""
        )
    }
    var goalWeightText by remember(state.goalWeightKg, weightUnit) {
        mutableStateOf(
            if (state.goalWeightKg != null) {
                if (weightUnit == "lbs") (state.goalWeightKg * 2.20462).roundToInt().toString()
                else state.goalWeightKg.toString()
            } else ""
        )
    }

    val openDobPicker = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        showDobPicker = true
    }

    if (showDobPicker) {
        SeniorDobBottomSheet(
            initialDate = state.dob,
            onDismissRequest = {
                showDobPicker = false
                focusManager.clearFocus(force = true)
            },
            onDateSelected = {
                viewModel.updateAboutYou(state.name, it, state.gender)
                showDobPicker = false
                focusManager.clearFocus(force = true)
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

            // 1. Full Name
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
                    onNext = { openDobPicker() }
                ),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 2. Date of Birth Picker (Standard 60dp height, matching Full Name exactly)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "DATE OF BIRTH",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.eyebrow,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Surface(
                    onClick = openDobPicker,
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
                            text = if (state.dob.isNotBlank()) state.dob else "Tap to choose date of birth",
                            style = ZivaaTheme.typography.bodyLarge,
                            color = if (state.dob.isNotBlank()) ZivaaTheme.colors.ink else ZivaaTheme.colors.inkMute
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select DOB",
                            tint = ZivaaTheme.colors.sage,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. Gender (Standard 48dp segmented pills)
            ZivaaSegmentedGenderSelection(
                selectedOption = state.gender,
                onOptionSelected = { viewModel.updateAboutYou(state.name, state.dob, it) },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 4. Height (Simplified with ft/in & cm toggle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HOW TALL ARE YOU?",
                        style = ZivaaTheme.typography.eyebrow,
                        color = ZivaaTheme.colors.eyebrow
                    )
                    UnitTogglePill(
                        options = listOf("ft/in", "cm"),
                        selectedOption = heightUnit,
                        onSelect = { newUnit ->
                            heightUnit = newUnit
                        }
                    )
                }

                if (heightUnit == "ft/in") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Feet
                        OutlinedTextField(
                            value = feetText,
                            onValueChange = { input ->
                                val cleaned = input.filter { it.isDigit() }.take(1)
                                feetText = cleaned
                                val ft = cleaned.toIntOrNull()
                                val inc = inchesText.toIntOrNull() ?: 0
                                if (ft != null) {
                                    viewModel.updateHeight((ft * 12) + inc)
                                } else if (cleaned.isEmpty() && inchesText.isEmpty()) {
                                    viewModel.updateHeight(null)
                                }
                            },
                            placeholder = { Text("5", style = ZivaaTheme.typography.bodyLarge, color = ZivaaTheme.colors.inkMute) },
                            trailingIcon = {
                                Text(
                                    text = "ft",
                                    style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = ZivaaTheme.colors.inkSoft,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZivaaTheme.colors.lineStrong,
                                unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
                                focusedTextColor = ZivaaTheme.colors.ink,
                                unfocusedTextColor = ZivaaTheme.colors.ink,
                                focusedContainerColor = ZivaaTheme.colors.bg,
                                unfocusedContainerColor = ZivaaTheme.colors.bg,
                                cursorColor = ZivaaTheme.colors.sage
                            ),
                            singleLine = true,
                            textStyle = ZivaaTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        )

                        // Inches
                        OutlinedTextField(
                            value = inchesText,
                            onValueChange = { input ->
                                val cleaned = input.filter { it.isDigit() }.take(2)
                                inchesText = cleaned
                                val inc = cleaned.toIntOrNull()
                                val ft = feetText.toIntOrNull() ?: 0
                                if (inc != null) {
                                    viewModel.updateHeight((ft * 12) + inc)
                                } else if (cleaned.isEmpty() && feetText.isEmpty()) {
                                    viewModel.updateHeight(null)
                                }
                            },
                            placeholder = { Text("8", style = ZivaaTheme.typography.bodyLarge, color = ZivaaTheme.colors.inkMute) },
                            trailingIcon = {
                                Text(
                                    text = "in",
                                    style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = ZivaaTheme.colors.inkSoft,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZivaaTheme.colors.lineStrong,
                                unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
                                focusedTextColor = ZivaaTheme.colors.ink,
                                unfocusedTextColor = ZivaaTheme.colors.ink,
                                focusedContainerColor = ZivaaTheme.colors.bg,
                                unfocusedContainerColor = ZivaaTheme.colors.bg,
                                cursorColor = ZivaaTheme.colors.sage
                            ),
                            singleLine = true,
                            textStyle = ZivaaTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                } else {
                    // cm
                    OutlinedTextField(
                        value = cmText,
                        onValueChange = { input ->
                            val cleaned = input.filter { it.isDigit() }.take(3)
                            cmText = cleaned
                            val cm = cleaned.toIntOrNull()
                            if (cm != null) {
                                viewModel.updateHeight((cm / 2.54).roundToInt())
                            } else {
                                viewModel.updateHeight(null)
                            }
                        },
                        placeholder = { Text("172", style = ZivaaTheme.typography.bodyLarge, color = ZivaaTheme.colors.inkMute) },
                        trailingIcon = {
                            Text(
                                text = "cm",
                                style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ZivaaTheme.colors.inkSoft,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZivaaTheme.colors.lineStrong,
                            unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
                            focusedTextColor = ZivaaTheme.colors.ink,
                            unfocusedTextColor = ZivaaTheme.colors.ink,
                            focusedContainerColor = ZivaaTheme.colors.bg,
                            unfocusedContainerColor = ZivaaTheme.colors.bg,
                            cursorColor = ZivaaTheme.colors.sage
                        ),
                        singleLine = true,
                        textStyle = ZivaaTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    )
                }
            }

            // 5. Weight (Simplified with kg & lbs toggle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR WEIGHT",
                        style = ZivaaTheme.typography.eyebrow,
                        color = ZivaaTheme.colors.eyebrow
                    )
                    UnitTogglePill(
                        options = listOf("kg", "lbs"),
                        selectedOption = weightUnit,
                        onSelect = { newUnit ->
                            weightUnit = newUnit
                        }
                    )
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }.take(3)
                        weightText = cleaned
                        val num = cleaned.toIntOrNull()
                        if (num != null) {
                            val kg = if (weightUnit == "lbs") (num * 0.45359237).roundToInt() else num
                            viewModel.updateWeight(kg)
                        } else {
                            viewModel.updateWeight(null)
                        }
                    },
                    placeholder = { Text(if (weightUnit == "lbs") "154" else "70", style = ZivaaTheme.typography.bodyLarge, color = ZivaaTheme.colors.inkMute) },
                    trailingIcon = {
                        Text(
                            text = weightUnit,
                            style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ZivaaTheme.colors.inkSoft,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZivaaTheme.colors.lineStrong,
                        unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
                        focusedTextColor = ZivaaTheme.colors.ink,
                        unfocusedTextColor = ZivaaTheme.colors.ink,
                        focusedContainerColor = ZivaaTheme.colors.bg,
                        unfocusedContainerColor = ZivaaTheme.colors.bg,
                        cursorColor = ZivaaTheme.colors.sage
                    ),
                    singleLine = true,
                    textStyle = ZivaaTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Target Weight
                Text(
                    text = "TARGET WEIGHT (OPTIONAL)",
                    style = ZivaaTheme.typography.eyebrow,
                    color = ZivaaTheme.colors.eyebrow,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = goalWeightText,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }.take(3)
                        goalWeightText = cleaned
                        val num = cleaned.toIntOrNull()
                        if (num != null) {
                            val kg = if (weightUnit == "lbs") (num * 0.45359237).roundToInt() else num
                            viewModel.updateGoalWeight(kg)
                        } else {
                            viewModel.updateGoalWeight(null)
                        }
                    },
                    placeholder = { Text(if (weightUnit == "lbs") "e.g. 148" else "e.g. 68", style = ZivaaTheme.typography.bodyLarge, color = ZivaaTheme.colors.inkMute) },
                    trailingIcon = {
                        Text(
                            text = weightUnit,
                            style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ZivaaTheme.colors.inkSoft,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZivaaTheme.colors.lineStrong,
                        unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
                        focusedTextColor = ZivaaTheme.colors.ink,
                        unfocusedTextColor = ZivaaTheme.colors.ink,
                        focusedContainerColor = ZivaaTheme.colors.bg,
                        unfocusedContainerColor = ZivaaTheme.colors.bg,
                        cursorColor = ZivaaTheme.colors.sage
                    ),
                    singleLine = true,
                    textStyle = ZivaaTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium)
                )
            }

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
fun UnitTogglePill(
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ZivaaTheme.colors.bgElev,
        border = BorderStroke(1.dp, ZivaaTheme.colors.line)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { opt ->
                val isSelected = opt.equals(selectedOption, ignoreCase = true)
                Surface(
                    onClick = { onSelect(opt) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) ZivaaTheme.colors.sage else Color.Transparent
                ) {
                    Text(
                        text = opt,
                        style = ZivaaTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) ZivaaTheme.colors.sageInk else ZivaaTheme.colors.inkSoft,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
