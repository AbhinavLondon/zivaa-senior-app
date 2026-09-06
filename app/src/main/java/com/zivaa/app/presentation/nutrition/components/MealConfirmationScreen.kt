package com.zivaa.app.presentation.nutrition.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Leave space for bottom bar
        ) {
            // Image Header
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Meal Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 130.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.clay.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = mealName ?: "Meal",
                            style = typography.titleLarge,
                            color = colors.textStrong
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Portion Size",
                                style = typography.bodyMedium,
                                color = colors.textMeta,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(colors.bg, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Portion",
                                    tint = if (mealQuantity > 0.25f) colors.textStrong else colors.textMeta,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable(enabled = mealQuantity > 0.25f) {
                                            mealQuantity -= 0.25f
                                        }
                                )
                                Text(
                                    text = String.format("%.2fx", mealQuantity),
                                    style = typography.bodyLarge,
                                    color = colors.textStrong,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Portion",
                                    tint = colors.textStrong,
                                    modifier = Modifier
                                        .size(24.dp)
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
                        style = typography.cardTitle,
                        color = colors.textStrong,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                            .background(colors.bgElev, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = food.name,
                                style = typography.bodyLarge,
                                color = colors.textStrong
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$adjustedCal kcal | ${adjustedPro}g P | ${adjustedCarbs}g C | ${adjustedFat}g F",
                                style = typography.bodySmall,
                                color = colors.textMeta
                            )
                        }

                        // Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(colors.bgElev, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (quantity > 1) colors.textStrong else colors.textMeta,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(enabled = quantity > 1) {
                                        val newQuantities = quantities.toMutableList()
                                        newQuantities[index] = quantity - 1
                                        quantities = newQuantities
                                    }
                            )
                            Text(
                                text = quantity.toString(),
                                style = typography.bodyLarge,
                                color = colors.textStrong,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = colors.textStrong,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        val newQuantities = quantities.toMutableList()
                                        newQuantities[index] = quantity + 1
                                        quantities = newQuantities
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!analysisText.isNullOrBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.sage.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI Analysis",
                                    style = typography.bodyLarge,
                                    color = colors.sage,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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
        }

        // Bottom Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = colors.bgElev,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textStrong
                    )
                ) {
                    Text("Cancel")
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
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.clay,
                        contentColor = colors.bg
                    )
                ) {
                    Text("Add to Log")
                }
            }
        }
    }
}
