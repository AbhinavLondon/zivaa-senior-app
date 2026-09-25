package com.zivaa.app.presentation.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.data.remote.DailyPlanTask
import com.zivaa.app.ui.theme.ZivaaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomFeedbackBottomSheet(
    task: DailyPlanTask,
    onFeedbackSubmitted: (feedback: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = ZivaaTheme.colors
    val haptic = LocalHapticFeedback.current

    val badgeText = task.provenance?.badge_text ?: "SYMPTOM RELIEF"
    val taskTitle = task.task

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.line.copy(alpha = 0.6f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Provenance Tag
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.sage.copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(
                    text = badgeText.uppercase(),
                    style = ZivaaTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = colors.sage,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Question Title
            Text(
                text = "How does it feel now?",
                style = ZivaaTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                ),
                color = colors.ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle with completed task
            Text(
                text = "Quick check after completing: \"$taskTitle\"",
                style = ZivaaTheme.typography.bodySmall.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                ),
                color = colors.inkMute,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Option 1: Better
            FeedbackOptionCard(
                title = "Feeling Better",
                subtitle = "Discomfort has noticeably eased up",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF2E7D32),
                backgroundColor = if (colors.isDark) Color(0xFF1B3B2B) else Color(0xFFE8F5E9),
                borderColor = Color(0xFF81C784),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFeedbackSubmitted("better")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option 2: Same
            FeedbackOptionCard(
                title = "About the Same",
                subtitle = "No significant change yet",
                icon = Icons.Default.RemoveCircleOutline,
                iconColor = Color(0xFFE65100),
                backgroundColor = if (colors.isDark) Color(0xFF3E2723) else Color(0xFFFFF3E0),
                borderColor = Color(0xFFFFB74D),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFeedbackSubmitted("same")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option 3: Worse
            FeedbackOptionCard(
                title = "Feeling More Uncomfortable",
                subtitle = "Discomfort increased or feels irritated",
                icon = Icons.Default.WarningAmber,
                iconColor = Color(0xFFC62828),
                backgroundColor = if (colors.isDark) Color(0xFF3E1A1A) else Color(0xFFFFEBEE),
                borderColor = Color(0xFFE57373),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFeedbackSubmitted("worse")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Skip Option
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onFeedbackSubmitted("skip")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = "Skip for now",
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.inkMute
                )
            }
        }
    }
}

@Composable
private fun FeedbackOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = ZivaaTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = ZivaaTheme.colors.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = ZivaaTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = ZivaaTheme.colors.inkMute
                )
            }
        }
    }
}
