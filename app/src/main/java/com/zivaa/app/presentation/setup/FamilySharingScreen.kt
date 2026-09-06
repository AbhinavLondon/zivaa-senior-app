package com.zivaa.app.presentation.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun FamilySharingScreen(
    state: SetupState,
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    // Local state for the new member being added
    var newName by remember { mutableStateOf("") }
    var newRelation by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    ZivaaSetupBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ZivaaTopBar(stepNo = 10, totalSteps = 10, onBack = onBack)

            ZivaaHeader(
                label = "STEP 10 · YOUR FAMILY",
                title = buildAnnotatedString {
                    append("Keep your family ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("in the\nloop.")
                    }
                },
                subtitle = "They'll get your morning update so they worry less. You choose who, and you can switch anyone off whenever you like."
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!state.isAddingFamilyMember) {
                // List View State
                state.familyMembers.forEach { member ->
                    ZivaaFamilyMemberCard(
                        name = member.name,
                        relationAndLocation = member.relation,
                        isActive = member.isActive,
                        onActiveChange = { viewModel.toggleFamilyMemberActive(member) },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ZivaaDashedButton(
                    text = "Add a family member",
                    onClick = {
                        newName = ""
                        newRelation = ""
                        newPhone = ""
                        viewModel.setIsAddingFamilyMember(true)
                    }
                )
            } else {
                // Add Member Form State
                Text(
                    text = "Who shall we add?",
                    style = ZivaaTheme.typography.titleLarge,
                    color = ZivaaTheme.colors.ink,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                ZivaaTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = "THEIR NAME",
                    modifier = Modifier.padding(bottom = 24.dp)
                ) // Placeholder "e.g. Aarav" not directly supported by our ZivaaTextField yet, using value

                ZivaaTextField(
                    value = newRelation,
                    onValueChange = { newRelation = it },
                    label = "HOW ARE THEY RELATED?",
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                ZivaaTextField(
                    value = newPhone,
                    onValueChange = { newPhone = it },
                    label = "THEIR MOBILE NUMBER",
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                ZivaaButton(
                    text = "Add to my circle",
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            viewModel.addFamilyMember(
                                FamilyMember(
                                    name = newName,
                                    phone = newPhone,
                                    relation = newRelation
                                )
                            )
                        } else {
                            viewModel.setIsAddingFamilyMember(false)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { viewModel.setIsAddingFamilyMember(false) }) {
                        Text(
                            text = "Cancel",
                            color = ZivaaTheme.colors.inkMute,
                            style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ZivaaButton(
            text = if (state.isAddingFamilyMember) "Save & continue" else "Continue",
            onClick = {
                if (state.isAddingFamilyMember) {
                    if (newName.isNotBlank() && newPhone.isNotBlank()) {
                        viewModel.addFamilyMember(
                            FamilyMember(
                                name = newName,
                                phone = newPhone,
                                relation = newRelation
                            )
                        )
                    } else {
                        viewModel.setIsAddingFamilyMember(false)
                    }
                } else {
                    onNext()
                }
            }
        )
    }
}
