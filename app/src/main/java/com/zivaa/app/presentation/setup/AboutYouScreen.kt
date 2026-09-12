package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.zivaa.app.presentation.plan.HeightCard
import com.zivaa.app.presentation.plan.PlanSetupTones
import com.zivaa.app.presentation.plan.WeightCard
import com.zivaa.app.ui.theme.ZivaaTheme

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

            // 2. Date of Birth Picker
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
                    .height(60.dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = ZivaaTheme.colors.bgElev,
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

            // 3. Gender
            ZivaaSegmentedGenderSelection(
                selectedOption = state.gender,
                onOptionSelected = { viewModel.updateAboutYou(state.name, state.dob, it) },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 4. Physical Baseline: Height & Weight Cards
            Text(
                text = "BODY MEASUREMENTS",
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.eyebrow,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HeightCard(
                modifier = Modifier.padding(bottom = 14.dp),
                tone = PlanSetupTones.Sage,
                icon = Icons.Default.Accessibility,
                eyebrow = "Body Metrics",
                heightInches = state.heightInches,
                onHeightChanged = { viewModel.updateHeight(it) }
            )

            WeightCard(
                modifier = Modifier.padding(bottom = 20.dp),
                tone = PlanSetupTones.Clay,
                icon = Icons.Default.MonitorWeight,
                eyebrow = "Body Metrics",
                weightKg = state.weightKg,
                onWeightChanged = { viewModel.updateWeight(it) },
                goalWeightKg = state.goalWeightKg,
                onGoalWeightChanged = { viewModel.updateGoalWeight(it) }
            )
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
