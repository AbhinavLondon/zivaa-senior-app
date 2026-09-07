package com.zivaa.app.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.toEyebrowTitleCase

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionCard(
    modifier: Modifier = Modifier,
    tone: PlanSetupTone,
    icon: ImageVector,
    eyebrow: String,
    question: String,
    hint: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionToggled: (String) -> Unit,
    bottomContent: (@Composable () -> Unit)? = null
) {
    val isAnswered = selectedOptions.isNotEmpty()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icon Bubble
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tone.bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tone.fg,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Text Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 1.dp)
                ) {
                    Text(
                        text = eyebrow.toEyebrowTitleCase(),
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        letterSpacing = 0.2.sp,
                        color = Color(0xFF111111),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    
                    Text(
                        text = question,
                        fontFamily = Manrope,
                        fontSize = 21.sp,
                        lineHeight = 23.sp, // ~1.12
                        letterSpacing = (-0.1).sp,
                        color = PlanSetupTheme.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(5.dp))
                    
                    Text(
                        text = hint,
                        fontFamily = Manrope,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp, // ~1.45
                        color = PlanSetupTheme.InkSoft,
                        fontWeight = FontWeight.Normal
                    )
                }
                
                // Checkmark if answered
                if (isAnswered) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(tone.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = tone.fg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = selectedOptions.contains(option)
                    SelectionChip(
                        label = option,
                        isSelected = isSelected,
                        tone = tone,
                        onClick = { onOptionToggled(option) }
                    )
                }
            }
            
            if (bottomContent != null) {
                bottomContent()
            }
        }
    }
}

@Composable
fun SelectionChip(
    label: String,
    isSelected: Boolean,
    tone: PlanSetupTone,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) tone.bg else PlanSetupTheme.Bg
    val contentColor = if (isSelected) tone.fg else PlanSetupTheme.Ink
    val borderColor = if (isSelected) tone.bg else PlanSetupTheme.LineStrong
    val elevation = if (isSelected) 4.dp else 0.dp
    
    Row(
        modifier = Modifier
            .shadow(elevation, CircleShape) // shadow when picked
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Custom ripple could be added if needed
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = label,
            fontFamily = Manrope,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            letterSpacing = 0.07.sp
        )
    }
}
