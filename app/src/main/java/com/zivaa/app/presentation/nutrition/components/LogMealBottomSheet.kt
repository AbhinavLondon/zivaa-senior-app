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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMealBottomSheet(
    mealName: String,
    onDismissRequest: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.bgElev,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add To ${mealName.toEyebrowTitleCase()}",
                style = typography.eyebrow,
                color = colors.eyebrow
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "How shall we log it?",
                fontFamily = InstrumentSerif,
                fontSize = 32.sp,
                color = colors.textStrong
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Options
            LogOptionItem(
                icon = Icons.Default.CameraAlt,
                title = "Take a photo",
                subtitle = "Point at the plate — we recognise the dish",
                iconBgColor = colors.amber.copy(alpha = 0.15f),
                iconTintColor = colors.amber,
                onClick = { onOptionSelected("photo") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LogOptionItem(
                icon = Icons.Default.Image,
                title = "Upload a photo",
                subtitle = "From something you already took",
                iconBgColor = colors.clay.copy(alpha = 0.15f),
                iconTintColor = colors.clay,
                onClick = { onOptionSelected("upload") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LogOptionItem(
                icon = Icons.Default.QrCodeScanner,
                title = "Scan a barcode",
                subtitle = "For packets, biscuits, milk cartons",
                iconBgColor = colors.inkSoft.copy(alpha = 0.15f),
                iconTintColor = colors.inkSoft,
                onClick = { onOptionSelected("barcode") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LogOptionItem(
                icon = Icons.Default.Search,
                title = "Search for food",
                subtitle = "Type a dish — Indian meals included",
                iconBgColor = colors.leaf.copy(alpha = 0.15f),
                iconTintColor = colors.leaf,
                onClick = { onOptionSelected("search") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LogOptionItem(
                icon = androidx.compose.material.icons.Icons.Default.Mic,
                title = "Voice",
                subtitle = "Tell us what you ate",
                iconBgColor = colors.sage.copy(alpha = 0.15f),
                iconTintColor = colors.sage,
                onClick = { onOptionSelected("voice") }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismissRequest() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Not now",
                    style = typography.bodyMedium,
                    color = colors.textStrong
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
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bg)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InstrumentSerif,
                fontSize = 20.sp,
                color = colors.textStrong
            )
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
