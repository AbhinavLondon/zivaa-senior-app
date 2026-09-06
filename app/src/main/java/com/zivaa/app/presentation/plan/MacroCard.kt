package com.zivaa.app.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.Manrope
import kotlin.math.roundToInt

enum class MacroSplitOption(val protein: Float, val carbs: Float, val fat: Float) {
    BALANCED(0.3f, 0.4f, 0.3f),
    MORE_PROTEIN(0.4f, 0.3f, 0.3f),
    LIGHTER_CARBS(0.3f, 0.2f, 0.5f),
    KETO(0.25f, 0.05f, 0.7f)
}

@Composable
fun MacroCard(
    modifier: Modifier = Modifier,
    targetCalories: Int?,
    proteinPct: Float,
    carbsPct: Float,
    fatPct: Float,
    onCaloriesChanged: (Int) -> Unit,
    onClearCalories: () -> Unit,
    onSplitChanged: (MacroSplitOption) -> Unit,
    onProteinChanged: (Float) -> Unit,
    onCarbsChanged: (Float) -> Unit,
    onFatChanged: (Float) -> Unit,
) {
    val isAnswered = targetCalories != null
    val borderColor = if (isAnswered) PlanSetupTones.Amber.bg else PlanSetupTheme.Line
    val borderWidth = if (isAnswered) 1.dp else 0.5.dp
    
    val safeCalories = targetCalories ?: 1800

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(PlanSetupTheme.BgElev)
            .border(borderWidth, borderColor, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // --- TOP SECTION ---
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
                        .background(PlanSetupTones.Amber.bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = null,
                        tint = PlanSetupTones.Amber.fg,
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
                        text = "NUTRITION",
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        letterSpacing = 0.7.sp,
                        color = PlanSetupTheme.InkMute,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    
                    Text(
                        text = "How much fuel for the day?",
                        fontFamily = Manrope, // Can use serif if available in theme
                        fontSize = 21.sp,
                        lineHeight = 23.sp,
                        letterSpacing = (-0.1).sp,
                        color = PlanSetupTheme.Ink,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "A gentle target — the plan portions your meals to fit it.",
                        fontFamily = Manrope,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = PlanSetupTheme.InkSoft
                    )
                }
                
                // Checkmark
                if (isAnswered) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PlanSetupTones.Amber.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PlanSetupTones.Amber.fg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- CALORIES SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALORIES A DAY",
                    fontFamily = Manrope,
                    fontSize = 10.sp,
                    letterSpacing = 0.7.sp,
                    color = PlanSetupTheme.InkMute,
                    fontWeight = FontWeight.SemiBold
                )
                
                Box(
                    modifier = Modifier
                        .border(0.5.dp, PlanSetupTheme.Line, CircleShape)
                        .clickable { onClearCalories() }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Clear",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        color = PlanSetupTheme.InkMute
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepperButton(
                    icon = Icons.Default.Remove,
                    onClick = { onCaloriesChanged(safeCalories - 100) }
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%,d", safeCalories)} kcal",
                        fontFamily = Manrope, // Or serif
                        fontSize = 32.sp,
                        color = PlanSetupTheme.Ink,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MEALS GET PORTIONED TO THIS",
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = PlanSetupTheme.InkMute,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                StepperButton(
                    icon = Icons.Default.Add,
                    onClick = { onCaloriesChanged(safeCalories + 100) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = PlanSetupTheme.Line, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- SPLIT SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOW TO SPLIT IT · YOUR MIX",
                    fontFamily = Manrope,
                    fontSize = 10.sp,
                    letterSpacing = 0.7.sp,
                    color = PlanSetupTheme.InkMute,
                    fontWeight = FontWeight.SemiBold
                )
                
                Box(
                    modifier = Modifier
                        .border(0.5.dp, PlanSetupTheme.Line, CircleShape)
                        .clickable { onSplitChanged(MacroSplitOption.BALANCED) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Reset",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        color = PlanSetupTheme.InkMute
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val currentSplit = MacroSplitOption.values().find {
                kotlin.math.abs(it.protein - proteinPct) < 0.01f &&
                kotlin.math.abs(it.carbs - carbsPct) < 0.01f &&
                kotlin.math.abs(it.fat - fatPct) < 0.01f
            }
            
            // Allow wrapping if we add more pills (e.g. Keto)
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillButton(text = "Balanced", isSelected = currentSplit == MacroSplitOption.BALANCED, onClick = { onSplitChanged(MacroSplitOption.BALANCED) })
                PillButton(text = "More protein", isSelected = currentSplit == MacroSplitOption.MORE_PROTEIN, onClick = { onSplitChanged(MacroSplitOption.MORE_PROTEIN) })
                PillButton(text = "Lighter carbs", isSelected = currentSplit == MacroSplitOption.LIGHTER_CARBS, onClick = { onSplitChanged(MacroSplitOption.LIGHTER_CARBS) })
                PillButton(text = "Keto", isSelected = currentSplit == MacroSplitOption.KETO, onClick = { onSplitChanged(MacroSplitOption.KETO) })
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(modifier = Modifier.weight(proteinPct).fillMaxHeight().background(PlanSetupTheme.Sage))
                Box(modifier = Modifier.weight(carbsPct).fillMaxHeight().background(PlanSetupTones.Amber.bg))
                Box(modifier = Modifier.weight(fatPct).fillMaxHeight().background(PlanSetupTheme.Clay))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro Rows
            val proteinGrams = (safeCalories * proteinPct / 4).roundToInt()
            val carbsGrams = (safeCalories * carbsPct / 4).roundToInt()
            val fatGrams = (safeCalories * fatPct / 9).roundToInt()
            
            MacroRow(
                color = PlanSetupTheme.Sage,
                name = "Protein",
                pct = proteinPct,
                grams = proteinGrams,
                onDecrease = { onProteinChanged(proteinPct - 0.05f) },
                onIncrease = { onProteinChanged(proteinPct + 0.05f) }
            )
            MacroRow(
                color = PlanSetupTones.Amber.bg,
                name = "Carbs",
                pct = carbsPct,
                grams = carbsGrams,
                onDecrease = { onCarbsChanged(carbsPct - 0.05f) },
                onIncrease = { onCarbsChanged(carbsPct + 0.05f) }
            )
            MacroRow(
                color = PlanSetupTheme.Clay,
                name = "Fat",
                pct = fatPct,
                grams = fatGrams,
                onDecrease = { onFatChanged(fatPct - 0.05f) },
                onIncrease = { onFatChanged(fatPct + 0.05f) }
            )
        }
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(0.5.dp, PlanSetupTheme.Line, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PlanSetupTheme.Ink,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun PillButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) PlanSetupTones.Amber.bg else Color.Transparent
    val textColor = if (isSelected) PlanSetupTones.Amber.fg else PlanSetupTheme.Ink
    val borderColor = if (isSelected) PlanSetupTones.Amber.bg else PlanSetupTheme.Line
    
    Box(
        modifier = Modifier
            .background(bgColor, CircleShape)
            .border(0.5.dp, borderColor, CircleShape)
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontFamily = Manrope,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@Composable
private fun MacroRow(
    color: Color,
    name: String,
    pct: Float,
    grams: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontFamily = Manrope,
            fontSize = 16.sp,
            color = PlanSetupTheme.Ink,
            modifier = Modifier.weight(1f)
        )
        
        StepperButton(icon = Icons.Default.Remove, onClick = onDecrease)
        
        Text(
            text = "${(pct * 100).roundToInt()}% · ${grams}G",
            fontFamily = Manrope,
            fontSize = 12.sp,
            color = PlanSetupTheme.InkMute,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
        
        StepperButton(icon = Icons.Default.Add, onClick = onIncrease)
    }
}
