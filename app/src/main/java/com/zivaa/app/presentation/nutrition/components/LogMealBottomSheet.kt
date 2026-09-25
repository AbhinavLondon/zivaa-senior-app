package com.zivaa.app.presentation.nutrition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.toEyebrowTitleCase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMealBottomSheet(
    mealName: String,
    onMealChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentMeal by remember(mealName) { mutableStateOf(mealName) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.bgElev,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.lineStrong) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Add To ${currentMeal.toEyebrowTitleCase()}",
                style = typography.eyebrow,
                color = colors.eyebrow
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Which meal is this?",
                fontFamily = InstrumentSerif,
                fontSize = 30.sp,
                color = colors.textStrong
            )
            Spacer(modifier = Modifier.height(14.dp))

            // 4 Meal Selection Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Breakfast" to "☀️ Breakfast",
                    "Lunch" to "🍛 Lunch",
                    "Snacks" to "☕ Snacks",
                    "Dinner" to "🌙 Dinner"
                ).forEach { (type, label) ->
                    val isSelected = currentMeal.equals(type, ignoreCase = true)
                    val chipBg = if (isSelected) colors.sage else colors.bg
                    val chipText = if (isSelected) colors.sageInk else colors.textStrong
                    val chipBorder = if (isSelected) colors.sage else colors.lineStrong

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(999.dp))
                            .clickable {
                                currentMeal = type
                                onMealChange(type)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = typography.meta.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = chipText,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "How shall we log it?",
                style = typography.meta.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.06f, androidx.compose.ui.unit.TextUnitType.Em)
                ),
                color = colors.textMeta
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Options
            LogOptionItem(
                icon = Icons.Default.CameraAlt,
                title = "Take a photo",
                subtitle = "Point at the plate — we recognise the dish",
                iconBgColor = colors.amber.copy(alpha = 0.15f),
                iconTintColor = colors.amber,
                onClick = { onOptionSelected("photo") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            LogOptionItem(
                icon = Icons.Default.Image,
                title = "Upload a photo",
                subtitle = "From something you already took",
                iconBgColor = colors.clay.copy(alpha = 0.15f),
                iconTintColor = colors.clay,
                onClick = { onOptionSelected("upload") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            LogOptionItem(
                icon = Icons.Default.QrCodeScanner,
                title = "Scan a barcode",
                subtitle = "For packets, biscuits, milk cartons",
                iconBgColor = colors.rose.copy(alpha = 0.15f),
                iconTintColor = colors.rose,
                onClick = { onOptionSelected("barcode") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            LogOptionItem(
                icon = Icons.Default.Search,
                title = "Search for food",
                subtitle = "Type a dish — Indian meals included",
                iconBgColor = colors.leaf.copy(alpha = 0.15f),
                iconTintColor = colors.leaf,
                onClick = { onOptionSelected("search") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            LogOptionItem(
                icon = androidx.compose.material.icons.Icons.Default.Mic,
                title = "Voice",
                subtitle = "Tell us what you ate",
                iconBgColor = colors.sage.copy(alpha = 0.15f),
                iconTintColor = colors.sage,
                onClick = { onOptionSelected("voice") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismissRequest() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Not now",
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = colors.textMeta
                )
            }
        }
    }
}

@Composable
private fun LogOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bg)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textStrong
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.textBody
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textMeta
        )
    }
}
