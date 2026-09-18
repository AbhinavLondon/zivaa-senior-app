package com.zivaa.app.presentation.nutrition.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.zivaa.app.ui.theme.InstrumentSerif

@Composable
fun MealConfirmationScreen(
    imageUri: Uri?,
    mealName: String?,
    initialFoods: List<FoodItem>,
    analysisText: String?,
    onConfirm: (List<FoodItem>, Float) -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    // Keep track of quantities for each food item index
    var quantities by remember { mutableStateOf(initialFoods.map { 1 }) }
    var mealQuantity by remember { mutableStateOf(1.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // Image Header
        if (imageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Meal Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .padding(14.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.bg.copy(alpha = 0.85f))
                        .border(0.5.dp, colors.line, CircleShape)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.ink,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
        ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.bgElev)
                            .border(1.dp, colors.borderHairline, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "CONFIRM MEAL",
                            style = typography.meta.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = androidx.compose.ui.unit.TextUnit(0.06f, androidx.compose.ui.unit.TextUnitType.Em)
                            ),
                            color = colors.clay
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mealName ?: "Meal",
                            fontFamily = InstrumentSerif,
                            fontSize = 28.sp,
                            color = colors.textStrong
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Portion size",
                                style = typography.bodyMedium,
                                color = colors.textMeta,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(colors.bg)
                                    .border(1.dp, colors.borderHairline, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Portion",
                                    tint = if (mealQuantity > 0.25f) colors.textStrong else colors.textMeta.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable(enabled = mealQuantity > 0.25f) {
                                            mealQuantity -= 0.25f
                                        }
                                )
                                Text(
                                    text = String.format("%.2fx", mealQuantity),
                                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textStrong,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Portion",
                                    tint = colors.textStrong,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable {
                                            mealQuantity += 0.25f
                                        }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Identified Ingredients",
                        style = typography.eyebrow,
                        color = colors.eyebrow,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }

                itemsIndexed(initialFoods) { index, food ->
                    val quantity = quantities[index]
                    val adjustedCal = (food.calories * quantity * mealQuantity).toInt()
                    val adjustedPro = (food.protein * quantity * mealQuantity).toInt()
                    val adjustedCarbs = (food.carbs * quantity * mealQuantity).toInt()
                    val adjustedFat = (food.fat * quantity * mealQuantity).toInt()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.bgElev)
                            .border(1.dp, colors.borderHairline, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = food.name,
                                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textStrong
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$adjustedCal kcal",
                                    style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textStrong
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${adjustedPro}g P",
                                    style = typography.meta.copy(fontSize = 11.sp),
                                    color = colors.sage
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${adjustedCarbs}g C",
                                    style = typography.meta.copy(fontSize = 11.sp),
                                    color = colors.amber
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${adjustedFat}g F",
                                    style = typography.meta.copy(fontSize = 11.sp),
                                    color = colors.clay
                                )
                            }
                        }

                        // Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.bg)
                                .border(1.dp, colors.borderHairline, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (quantity > 1) colors.textStrong else colors.textMeta.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(enabled = quantity > 1) {
                                        val newQuantities = quantities.toMutableList()
                                        newQuantities[index] = quantity - 1
                                        quantities = newQuantities
                                    }
                            )
                            Text(
                                text = quantity.toString(),
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textStrong,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = colors.textStrong,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        val newQuantities = quantities.toMutableList()
                                        newQuantities[index] = quantity + 1
                                        quantities = newQuantities
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (!analysisText.isNullOrBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.sage.copy(alpha = 0.12f)),
                            border = BorderStroke(0.5.dp, colors.sage.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI Analysis",
                                    style = typography.eyebrow,
                                    color = colors.sage
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = analysisText,
                                    style = typography.bodyMedium,
                                    color = colors.textStrong
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Fixed Bottom Bar docked below LazyColumn (never overlaps AI analysis or ingredients)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.bgElev,
            border = BorderStroke(0.5.dp, colors.borderHairline),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, colors.borderStrong),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textStrong
                    )
                ) {
                    Text(
                        "Cancel",
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Button(
                    onClick = {
                        val finalFoods = initialFoods.mapIndexed { index, food ->
                            val qty = quantities[index]
                            food.copy(
                                calories = food.calories * qty,
                                protein = food.protein * qty,
                                carbs = food.carbs * qty,
                                fat = food.fat * qty
                            )
                        }
                        onConfirm(finalFoods, mealQuantity)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.sage,
                        contentColor = colors.sageInk
                    )
                ) {
                    Text(
                        "Add to Log",
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
