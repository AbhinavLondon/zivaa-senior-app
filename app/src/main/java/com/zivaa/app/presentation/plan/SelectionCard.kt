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
    stepNumber: Int? = null,
    totalSteps: Int? = null,
    bottomContent: (@Composable () -> Unit)? = null
) {
    val isAnswered = selectedOptions.isNotEmpty()
    val borderColor = if (isAnswered) tone.bg.copy(alpha = 0.35f) else PlanSetupTheme.Line
    val borderWidth = if (isAnswered) 1.dp else 0.5.dp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(PlanSetupTheme.BgElev)
            .border(borderWidth, borderColor, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (stepNumber != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isAnswered) tone.bg.copy(alpha = 0.15f) else PlanSetupTheme.LineStrong.copy(alpha = 0.35f))
                                .border(
                                    0.5.dp,
                                    if (isAnswered) tone.bg.copy(alpha = 0.45f) else PlanSetupTheme.Line,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = "STEP $stepNumber" + (totalSteps?.let { " OF $it" } ?: ""),
                                fontFamily = Manrope,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = if (isAnswered) tone.bg else PlanSetupTheme.InkMute,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Text(
                            text = eyebrow.toEyebrowTitleCase(),
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            letterSpacing = 0.2.sp,
                            color = PlanSetupTheme.Eyebrow,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    if (isAnswered) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PlanSetupTheme.Leaf.copy(alpha = 0.12f))
                                .border(0.5.dp, PlanSetupTheme.Leaf.copy(alpha = 0.35f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PlanSetupTheme.Leaf,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Answered",
                                fontFamily = Manrope,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlanSetupTheme.Leaf
                            )
                        }
                    }
                }
            }
            
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
                    if (stepNumber == null) {
                        Text(
                            text = eyebrow.toEyebrowTitleCase(),
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            letterSpacing = 0.2.sp,
                            color = PlanSetupTheme.Eyebrow,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    
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
                
                // Checkmark if answered and stepNumber is null
                if (isAnswered && stepNumber == null) {
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
