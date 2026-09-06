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
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

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
            .clip(RoundedCornerShape(24.dp))
            .background(colors.bgElev)
            .padding(24.dp)
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
                        .clip(RoundedCornerShape(12.dp))
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
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = InstrumentSerif,
                        fontSize = 24.sp,
                        color = colors.textStrong
                    )
                    Text(
                        text = timeRange,
                        style = typography.eyebrow,
                        color = colors.textMeta
                    )
                }
                if (totalCalories != null) {
                    Text(
                        text = "$totalCalories kcal",
                        style = typography.meta,
                        color = colors.textStrong
                    )
                } else {
                    Text(
                        text = "-",
                        style = typography.meta,
                        color = colors.textStrong
                    )
                }
            }
            
            if (emptyMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = emptyMessage,
                    style = typography.bodyMedium,
                    color = colors.textBody
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Filled button style for empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.bg)
                        .border(1.dp, colors.borderStrong, RoundedCornerShape(24.dp))
                        .clickable { onAddClick() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Add to ${title.lowercase()}",
                        style = typography.bodyMedium,
                        color = colors.sage
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.borderHairline)
                Spacer(modifier = Modifier.height(8.dp))
                
                foods.forEach { food ->
                    FoodItemRow(food = food, onRemove = { onRemoveFood(food.id) })
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                // Outlined button style for filled state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, colors.clay.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .clickable { onAddClick() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Add something else",
                        style = typography.bodyMedium,
                        color = colors.clay
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                style = typography.bodyMedium,
                color = colors.textStrong
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${food.protein}g P",
                    style = typography.meta.copy(fontSize = 11.sp),
                    color = colors.sage
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${food.carbs}g C",
                    style = typography.meta.copy(fontSize = 11.sp),
                    color = colors.clay
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${food.fat}g F",
                    style = typography.meta.copy(fontSize = 11.sp),
                    color = colors.textMeta
                )
            }
        }
        Text(
            text = "${food.calories} kcal",
            style = typography.meta,
            color = colors.textMeta
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = colors.textMeta,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
