package com.zivaa.app.presentation.nutrition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

import androidx.compose.ui.text.font.FontWeight

data class FoodItem(
    val id: String,
    val name: String,
    val calories: Int,
    val initials: String,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)

@Composable
fun MealCard(
    title: String,
    timeRange: String,
    totalCalories: Int?,
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    foods: List<FoodItem>,
    emptyMessage: String? = null,
    onAddClick: () -> Unit,
    onRemoveFood: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = InstrumentSerif,
                        fontSize = 24.sp,
                        color = colors.textStrong
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeRange.toEyebrowTitleCase(),
                        style = typography.meta.copy(
                            fontSize = 11.sp,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(0.05f, androidx.compose.ui.unit.TextUnitType.Em)
                        ),
                        color = colors.textMeta
                    )
                }
                if (totalCalories != null) {
                    Text(
                        text = "$totalCalories kcal",
                        style = typography.meta.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = colors.textStrong
                    )
                } else {
                    Text(
                        text = "-",
                        style = typography.meta,
                        color = colors.textMeta
                    )
                }
            }
            
            if (foods.isEmpty()) {
                if (emptyMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = emptyMessage,
                        style = typography.bodySmall,
                        color = colors.textBody
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                // Clean button style for empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.bg)
                        .border(1.dp, colors.borderHairline, RoundedCornerShape(999.dp))
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Add to ${title.lowercase()}",
                        style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.sage
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.borderHairline, thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(6.dp))
                
                foods.forEach { food ->
                    FoodItemRow(food = food, onRemove = { onRemoveFood(food.id) })
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                // Outlined button style for filled state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.bg)
                        .border(1.dp, colors.borderHairline, RoundedCornerShape(999.dp))
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Add something else",
                        style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textStrong
                    )
                }
            }
        }
    }
}

@Composable
fun FoodItemRow(
    food: FoodItem,
    onRemove: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textStrong
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${food.protein}g P",
                    style = typography.meta.copy(fontSize = 11.sp),
                    color = colors.sage
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${food.carbs}g C",
                    style = typography.meta.copy(fontSize = 11.sp),
                    color = colors.amber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${food.fat}g F",
                    style = typography.meta.copy(fontSize = 11.sp),
                    color = colors.clay
                )
            }
        }
        Text(
            text = "${food.calories} kcal",
            style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textStrong
        )
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = colors.textMeta.copy(alpha = 0.6f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
