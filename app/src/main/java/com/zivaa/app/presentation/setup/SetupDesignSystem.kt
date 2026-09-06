package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme

// --- Components ---

@Composable
fun ZivaaSetupBackground(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ZivaaTheme.colors.bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = ZivaaTheme.spacing.padScreen, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun ZivaaHeader(title: String, subtitle: String? = null, label: String? = null) {
    ZivaaHeader(
        title = androidx.compose.ui.text.AnnotatedString(title),
        subtitle = subtitle,
        label = label
    )
}

@Composable
fun ZivaaHeader(title: androidx.compose.ui.text.AnnotatedString, subtitle: String? = null, label: String? = null) {
    Column(
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp).fillMaxWidth()
    ) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = ZivaaTheme.typography.eyebrow,
                color = ZivaaTheme.colors.leaf,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        Text(
            text = title,
            style = ZivaaTheme.typography.titleLarge,
            color = ZivaaTheme.colors.ink,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = ZivaaTheme.typography.bodyMedium,
                color = ZivaaTheme.colors.inkSoft
            )
        }
    }
}

@Composable
fun ZivaaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp).fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = ZivaaTheme.colors.sage,
            contentColor = ZivaaTheme.colors.sageInk,
            disabledContainerColor = ZivaaTheme.colors.muted,
            disabledContentColor = ZivaaTheme.colors.inkMute
        ),
        shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
        enabled = enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = ZivaaTheme.colors.sageInk, modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = text,
                style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ZivaaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.inkMute,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = ZivaaTheme.typography.bodyLarge, color = ZivaaTheme.colors.inkMute) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZivaaTheme.colors.lineStrong,
                unfocusedBorderColor = ZivaaTheme.colors.lineStrong,
                focusedTextColor = ZivaaTheme.colors.ink,
                unfocusedTextColor = ZivaaTheme.colors.ink,
                focusedContainerColor = ZivaaTheme.colors.bg,
                unfocusedContainerColor = ZivaaTheme.colors.bg,
                cursorColor = ZivaaTheme.colors.sage
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            trailingIcon = trailingIcon,
            textStyle = ZivaaTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ZivaaSegmentedGenderSelection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Male", "Female", "Other")
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "GENDER",
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.inkMute,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { text ->
                val isSelected = selectedOption == text
                Surface(
                    onClick = { onOptionSelected(text) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else ZivaaTheme.colors.bgElev,
                    border = BorderStroke(1.dp, if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.line)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = text,
                            color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.inkSoft,
                            style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZivaaWearableCard(
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(ZivaaTheme.spacing.radiusSoft),
        color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else ZivaaTheme.colors.bgElev,
        border = if (isSelected) BorderStroke(1.dp, ZivaaTheme.colors.sage) else BorderStroke(1.dp, ZivaaTheme.colors.line)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon wrapper
            if (icon != null) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ZivaaTheme.colors.bgElev, // usually white or light
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                // Empty circle or something if no icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor
                ) { }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = ZivaaTheme.colors.ink,
                    style = ZivaaTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = ZivaaTheme.colors.inkMute,
                        style = ZivaaTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            // Checkmark
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.bgElev,
                border = if (!isSelected) BorderStroke(1.dp, ZivaaTheme.colors.lineStrong) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = ZivaaTheme.colors.bgElev,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = ZivaaTheme.colors.bgElev.copy(alpha = 0f), // invisible
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZivaaSelectionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(ZivaaTheme.spacing.radiusSoft),
        color = if (isSelected) ZivaaTheme.colors.sage else ZivaaTheme.colors.bgElev,
        border = if (!isSelected) BorderStroke(1.dp, ZivaaTheme.colors.line) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = if (isSelected) ZivaaTheme.colors.sageInk else ZivaaTheme.colors.ink,
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun ZivaaTopBar(
    stepNo: Int,
    totalSteps: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Surface(
            onClick = onBack,
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = ZivaaTheme.colors.bgElev,
            border = BorderStroke(0.5.dp, ZivaaTheme.colors.line)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Using a simple text back arrow as fallback for the icon
                Text(text = "←", fontSize = 18.sp, color = ZivaaTheme.colors.ink, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Dots
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalSteps) {
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(if (i == stepNo) 3f else 1f)
                        .background(
                            color = if (i <= stepNo) ZivaaTheme.colors.sage else ZivaaTheme.colors.inkMute.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "$stepNo/$totalSteps",
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.inkMute
        )
    }
}

@Composable
fun ZivaaDashedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = ZivaaTheme.colors.lineStrong
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = ZivaaTheme.colors.sageInk,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = ZivaaTheme.colors.bgElev,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = ZivaaTheme.colors.ink,
                style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
fun ZivaaFamilyMemberCard(
    name: String,
    relationAndLocation: String,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = ZivaaTheme.colors.bgElev,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = ZivaaTheme.colors.clay
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.firstOrNull()?.toString()?.uppercase() ?: "",
                        style = ZivaaTheme.typography.titleLarge,
                        color = ZivaaTheme.colors.inkMute
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = ZivaaTheme.colors.ink,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = relationAndLocation,
                    color = ZivaaTheme.colors.inkMute,
                    style = ZivaaTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = isActive,
                onCheckedChange = onActiveChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ZivaaTheme.colors.bgElev,
                    checkedTrackColor = ZivaaTheme.colors.sageInk,
                    uncheckedThumbColor = ZivaaTheme.colors.bgElev,
                    uncheckedTrackColor = ZivaaTheme.colors.lineStrong,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}
