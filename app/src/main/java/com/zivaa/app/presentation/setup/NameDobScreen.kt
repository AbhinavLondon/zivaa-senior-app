package com.zivaa.app.presentation.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.R
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun NameDobScreen(
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

    LaunchedEffect(showDobPicker) {
        if (!showDobPicker) {
            focusManager.clearFocus(force = true)
        }
    }

    ZivaaSetupBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ZivaaTopBar(stepNo = 1, totalSteps = 6, onBack = onBack)

            ZivaaHeader(
                label = "Step 1 · About you",
                title = buildAnnotatedString {
                    append("Let's start with ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("you.")
                    }
                },
                subtitle = "Just the basics — your name, your date of birth, and how we should refer to you."
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Wrapper Box to intercept clicks on the TextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { openDobPicker() }
            ) {
                ZivaaTextField(
                    value = state.dob,
                    onValueChange = { }, // Read-only via overlay click
                    label = "Date of Birth",
                    placeholder = "dd-mm-yyyy",
                    readOnly = true,
                    trailingIcon = {
                        Text("📅", fontSize = 16.sp)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // Transparent overlay to consume clicks
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { openDobPicker() }
                )
            }

            ZivaaSegmentedGenderSelection(
                selectedOption = state.gender,
                onOptionSelected = { viewModel.updateAboutYou(state.name, state.dob, it) },
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ZivaaButton(
            text = "Continue",
            onClick = onNext
        )
    }
}
