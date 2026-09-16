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
import com.zivaa.app.presentation.profile.components.AddCaregiverForm
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun FamilySharingScreen(
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
            ZivaaTopBar(stepNo = 6, totalSteps = 6, onBack = onBack)

            ZivaaHeader(
                label = "STEP 6 OF 6 · CARE CIRCLE",
                title = buildAnnotatedString {
                    append("Keep your ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ZivaaTheme.colors.sage)) {
                        append("family")
                    }
                    append(" in the\nloop.")
                },
                subtitle = "They'll get your morning update so they worry less. You choose who, and you can switch anyone off whenever you like."
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!state.isAddingFamilyMember) {
                // List View State
                state.familyMembers.forEachIndexed { index, member ->
                    val locationSuffix = if (!member.city.isNullOrBlank()) " · ${member.city}" else ""
                    ZivaaFamilyMemberCard(
                        name = member.name,
                        relationAndLocation = "${member.relation}$locationSuffix",
                        isActive = member.isActive,
                        onActiveChange = { viewModel.toggleFamilyMemberActive(member) },
                        colorIndex = index,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ZivaaDashedButton(
                    text = "Add a family member",
                    onClick = {
                        viewModel.setIsAddingFamilyMember(true)
                    }
                )
            } else {
                // Add Member Form State - uses the shared AddCaregiverForm
                AddCaregiverForm(
                    patientName = state.name,
                    title = "Who shall we add?",
                    subtitle = "They'll get the morning report over WhatsApp, and a call if anything urgent comes up.",
                    sectionHeader = null,
                    saveButtonText = "Add to my circle",
                    onSave = { name, relation, phone, city ->
                        viewModel.addFamilyMember(
                            FamilyMember(
                                name = name,
                                phone = phone,
                                relation = relation,
                                city = city,
                                isActive = true
                            )
                        )
                    },
                    onCancel = {
                        viewModel.setIsAddingFamilyMember(false)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (!state.isAddingFamilyMember) {
            Spacer(modifier = Modifier.height(16.dp))

            ZivaaButton(
                text = "Continue",
                onClick = onNext
            )

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "I'll set this up later",
                    color = ZivaaTheme.colors.inkMute,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

