package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zivaa.app.ui.theme.ZivaaTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCaregiverForm(
    patientName: String = "",
    title: String? = "Add someone",
    subtitle: String? = "They'll get the morning report over WhatsApp, and a call if anything urgent comes up.",
    sectionHeader: String? = "Care Circle",
    saveButtonText: String = "Add to circle",
    onSave: (name: String, relation: String, phone: String, city: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var showCountryDropdown by remember { mutableStateOf(false) }

    val countryCodes = listOf(
        "🇮🇳 +91" to "+91",
        "🇺🇸 +1" to "+1",
        "🇬🇧 +44" to "+44",
        "🇦🇺 +61" to "+61"
    )
    val relations = listOf("Son", "Daughter", "Spouse", "Neighbour", "Doctor", "Friend")

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ZivaaTheme.colors.sage,
        focusedLabelColor = ZivaaTheme.colors.sage,
        cursorColor = ZivaaTheme.colors.sage,
        unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
        unfocusedLabelColor = ZivaaTheme.colors.inkMute,
        focusedTextColor = ZivaaTheme.colors.ink,
        unfocusedTextColor = ZivaaTheme.colors.ink,
        focusedContainerColor = ZivaaTheme.colors.bgElev,
        unfocusedContainerColor = ZivaaTheme.colors.bgElev
    )

    Column(modifier = modifier) {
        if (!sectionHeader.isNullOrBlank()) {
            Text(
                text = sectionHeader,
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.eyebrow
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = ZivaaTheme.typography.titleLarge,
                color = ZivaaTheme.colors.ink,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = ZivaaTheme.typography.bodyMedium,
                color = ZivaaTheme.colors.inkMute
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Their name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Country Code & Phone Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .height(56.dp)
                    .background(ZivaaTheme.colors.bgElev, RoundedCornerShape(12.dp))
                    .border(1.dp, ZivaaTheme.colors.lineStrong, RoundedCornerShape(12.dp))
                    .clickable { showCountryDropdown = true },
                contentAlignment = Alignment.Center
            ) {
                val displayCode = countryCodes.find { it.second == countryCode }?.first ?: countryCode
                Text(
                    text = displayCode,
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.ink
                )
                DropdownMenu(
                    expanded = showCountryDropdown,
                    onDismissRequest = { showCountryDropdown = false },
                    modifier = Modifier.background(ZivaaTheme.colors.bgElev)
                ) {
                    countryCodes.forEach { (label, code) ->
                        DropdownMenuItem(
                            text = { Text(label, color = ZivaaTheme.colors.ink) },
                            onClick = {
                                countryCode = code
                                showCountryDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("WhatsApp number") },
                modifier = Modifier.weight(0.65f),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Relationship Section
        val resolvedName = patientName.trim().substringBefore(" ").lowercase().replaceFirstChar { it.uppercase() }
        val whoTheyAreHeading = if (resolvedName.isNotBlank() && resolvedName != "User") {
            "Who They Are To $resolvedName"
        } else {
            "Who They Are To You"
        }

        Text(
            text = whoTheyAreHeading,
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.eyebrow
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            relations.forEach { rel ->
                val isSelected = relation == rel
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) ZivaaTheme.colors.sage else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.lineStrong,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { relation = rel }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = rel,
                        color = if (isSelected) Color.White else ZivaaTheme.colors.ink,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // City (optional) Field
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Actions: Cancel and Save
        val isFormValid = name.isNotBlank() && relation.isNotBlank()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
            ) {
                Text(
                    text = "Cancel",
                    color = ZivaaTheme.colors.ink,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }

            Button(
                onClick = {
                    if (isFormValid) {
                        val cleanedPhone = phone.trim()
                        val fullPhone = if (cleanedPhone.isBlank()) {
                            ""
                        } else if (cleanedPhone.startsWith("+")) {
                            cleanedPhone
                        } else {
                            "$countryCode$cleanedPhone"
                        }
                        onSave(name.trim(), relation.trim(), fullPhone, city.trim())
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZivaaTheme.colors.sage,
                    disabledContainerColor = ZivaaTheme.colors.sage.copy(alpha = 0.4f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = saveButtonText,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
