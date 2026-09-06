package com.zivaa.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.plan.PlanSetupTheme
import com.zivaa.app.ui.theme.Manrope
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun StepsGoalSection(
    stepsGoal: Int?,
    onStepsGoalChanged: (Int?) -> Unit
) {
    Spacer(modifier = Modifier.height(20.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(PlanSetupTheme.Line)
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DAILY STEPS GOAL - OPTIONAL",
            fontFamily = Manrope,
            fontSize = 10.sp,
            letterSpacing = 0.7.sp,
            color = PlanSetupTheme.InkMute,
            fontWeight = FontWeight.SemiBold
        )
        
        Box(
            modifier = Modifier
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color.Transparent)
                .border(1.dp, PlanSetupTheme.LineStrong, RoundedCornerShape(13.dp))
                .clickable { onStepsGoalChanged(null) }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Clear",
                fontFamily = Manrope,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = PlanSetupTheme.InkSoft
            )
        }
    }
    
    Spacer(modifier = Modifier.height(18.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minus button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PlanSetupTheme.Bg)
                .border(1.dp, PlanSetupTheme.LineStrong, CircleShape)
                .clickable {
                    val currentGoal = stepsGoal ?: 10000
                    onStepsGoalChanged((currentGoal - 500).coerceAtLeast(0))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "−", // minus sign
                fontFamily = Manrope,
                fontSize = 20.sp,
                color = PlanSetupTheme.Ink,
                modifier = Modifier.padding(bottom = 2.dp) // Optical center adjustment
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val currentGoal = stepsGoal ?: 10000
            val formattedSteps = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(currentGoal)
            
            Text(
                text = "$formattedSteps steps",
                fontFamily = Manrope,
                fontSize = 28.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.1).sp,
                color = PlanSetupTheme.Ink
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val minutes = currentGoal / 100
            
            Text(
                text = "≈ $minutes MIN OF EASY WALKING",
                fontFamily = Manrope,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                color = PlanSetupTheme.InkMute,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Plus button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PlanSetupTheme.Bg)
                .border(1.dp, PlanSetupTheme.LineStrong, CircleShape)
                .clickable {
                    val currentGoal = stepsGoal ?: 10000
                    onStepsGoalChanged(currentGoal + 500)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                fontFamily = Manrope,
                fontSize = 20.sp,
                color = PlanSetupTheme.Ink,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsGoalBottomSheet(
    initialGoal: Int?,
    onDismiss: () -> Unit,
    onSave: (Int?) -> Unit
) {
    var tempGoal by remember { mutableStateOf(initialGoal) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PlanSetupTheme.Bg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlanSetupTheme.LineStrong) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
        ) {
            Text(
                text = "Set your daily goal",
                fontFamily = Manrope,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PlanSetupTheme.Ink
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We recommend starting with a goal that's slightly challenging but easily achievable on most days.",
                fontFamily = Manrope,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = PlanSetupTheme.InkSoft
            )

            StepsGoalSection(
                stepsGoal = tempGoal,
                onStepsGoalChanged = { tempGoal = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onSave(tempGoal) },
                colors = ButtonDefaults.buttonColors(containerColor = ZivaaTheme.colors.sageInk),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Save Goal",
                    fontFamily = Manrope,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PlanSetupTheme.Bg
                )
            }
        }
    }
}
