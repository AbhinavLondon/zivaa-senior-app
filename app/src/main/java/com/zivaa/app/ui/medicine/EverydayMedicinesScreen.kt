package com.zivaa.app.ui.medicine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.R
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import com.zivaa.app.ui.theme.zivaaShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EverydayMedicinesScreen(
    categoryId: String = "everyday",
    viewModel: OrderMedicineViewModel,
    onBack: () -> Unit,
    onNavigateToBasket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val basketItems by viewModel.basketItems.collectAsState()
    
    val filters = listOf("All", "Pain & fever", "Cold & cough", "Digestion", "Vitamins", "First aid")
    var selectedFilter by remember { mutableStateOf("All") }

    val category = MedicineMockData.categories.find { it.id == categoryId } ?: MedicineMockData.categories.first()
    val baseMedicineList = when (categoryId) {
        "homeopathy" -> MedicineMockData.homeopathyMedicines
        "ayurvedic" -> MedicineMockData.ayurvedicMedicines
        else -> MedicineMockData.everydayMedicines
    }

    val medicines = if (selectedFilter == "All") {
        baseMedicineList
    } else {
        val mappedId = when(selectedFilter) {
            "Pain & fever" -> "pain_fever"
            "Cold & cough" -> "cold_cough"
            "Digestion" -> "digestion"
            "Vitamins" -> "vitamins"
            "First aid" -> "first_aid"
            else -> ""
        }
        baseMedicineList.filter { it.categoryId == mappedId }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.bgElev)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            bottomBar = {
                if (basketItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(colors.sage)
                            .clickable { onNavigateToBasket() }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.sage),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${basketItems.sumOf { it.quantity }}",
                                    style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            
                            Text(
                                text = "View order",
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₹${basketItems.sumOf { it.medicine.price * it.quantity }}",
                                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 130.dp) // space for bottom bar
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.bgElev)
                                .clickable { onBack() }
                                .drawBehind {
                                    drawCircle(
                                        color = colors.line,
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.ink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = category.title.toEyebrowTitleCase(),
                            style = typography.eyebrow,
                            color = colors.eyebrow,
                            modifier = Modifier.padding(end = 40.dp)
                        )
                        Spacer(modifier = Modifier.width(38.dp))
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                append("What do you ")
                                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.accent)) {
                                    append("need?")
                                }
                            },
                            style = typography.displayMedium.copy(fontSize = 32.sp),
                            color = colors.ink
                        )
                        Text(
                            text = "No prescription needed for these. Tap to add — adjust how many in your order.",
                            style = typography.bodyMedium,
                            color = colors.inkSoft,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filters) { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) colors.sage else colors.bg)
                                    .clickable { selectedFilter = filter }
                                    .border(1.dp, if (isSelected) Color.Transparent else colors.line, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    style = typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium),
                                    color = if (isSelected) Color.White else colors.ink
                                )
                            }
                        }
                    }
                }

                items(medicines) { med ->
                    val quantity = basketItems.find { it.medicine.id == med.id }?.quantity ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .zivaaShadow(cornerRadius = 22.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.bg)
                            .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(med.tone),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(med.iconGlyph.take(1).uppercase(), style = typography.cardTitle.copy(fontSize = 20.sp, color = Color.White))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = med.title, style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = colors.ink)
                            Text(text = med.subtitle, style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.padding(top = 2.dp))
                            Text(text = "₹${med.price}", style = typography.meta.copy(color = colors.ink, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(top = 6.dp))
                        }

                        if (quantity > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.bgElev)
                                    .border(1.dp, colors.line, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = colors.inkSoft,
                                    modifier = Modifier.size(18.dp).clickable { viewModel.updateQuantity(med.id, quantity - 1) }
                                )
                                Text(
                                    text = quantity.toString(),
                                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.ink,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.inkSoft,
                                    modifier = Modifier.size(18.dp).clickable { viewModel.updateQuantity(med.id, quantity + 1) }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.bgElev)
                                    .clickable { viewModel.addToBasket(med) }
                                    .border(1.dp, colors.sage, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Add", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.sage)
                            }
                        }
                    }
                }
            }
        }
    }
}
