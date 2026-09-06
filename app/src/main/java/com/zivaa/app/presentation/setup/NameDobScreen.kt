package com.zivaa.app.presentation.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
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

            var showDobPicker by remember { mutableStateOf(false) }

            if (showDobPicker) {
                SeniorDobBottomSheet(
                    initialDate = state.dob,
                    onDismissRequest = { showDobPicker = false },
                    onDateSelected = { viewModel.updateAboutYou(state.name, it, state.gender) }
                )
            }

            ZivaaTextField(
                value = state.name,
                onValueChange = { viewModel.updateAboutYou(it, state.dob, state.gender) },
                label = "Your Full Name",
                placeholder = "e.g. Ranjit Kulkarni",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Wrapper Box to intercept clicks on the TextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { showDobPicker = true }
            ) {
                ZivaaTextField(
                    value = state.dob,
                    onValueChange = { }, // Read-only via overlay click
                    label = "Date of Birth",
                    placeholder = "dd-mm-yyyy",
                    trailingIcon = {
                        Text("📅", fontSize = 16.sp)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // Transparent overlay to consume clicks
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDobPicker = true }
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
