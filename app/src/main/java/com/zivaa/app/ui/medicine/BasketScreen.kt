package com.zivaa.app.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketScreen(
    viewModel: OrderMedicineViewModel,
    onBack: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val basketItems by viewModel.basketItems.collectAsState()
    val totalItemsPrice = viewModel.getTotalItemsPrice()

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
                            .clickable { onNavigateToDelivery() }
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Choose delivery",
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹$totalItemsPrice",
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 130.dp)
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
                            text = "Your Order",
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
                            text = "${basketItems.sumOf { it.quantity }} in your basket.",
                            style = typography.displayMedium.copy(fontSize = 32.sp),
                            color = colors.ink
                        )
                        Text(
                            text = "Review the quantities, then choose when it should arrive.",
                            style = typography.bodyMedium,
                            color = colors.inkSoft,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                if (basketItems.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Your basket is empty", style = typography.bodyMedium, color = colors.inkSoft)
                        }
                    }
                } else {
                    items(basketItems) { item ->
                        val med = item.medicine
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
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "₹${med.price}", style = typography.meta.copy(color = colors.ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp), modifier = Modifier.padding(bottom = 6.dp, end = 4.dp))
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
                                        modifier = Modifier.size(18.dp).clickable { viewModel.updateQuantity(med.id, item.quantity - 1) }
                                    )
                                    Text(
                                        text = item.quantity.toString(),
                                        style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.ink,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = colors.inkSoft,
                                        modifier = Modifier.size(18.dp).clickable { viewModel.updateQuantity(med.id, item.quantity + 1) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onBack() }
                                .drawBehind {
                                    drawRoundRect(
                                        color = colors.line,
                                        style = Stroke(width = 1.dp.toPx()),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                                    )
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Add more medicines",
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.inkSoft
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(colors.bgElev)
                                .drawBehind {
                                    drawRoundRect(
                                        color = colors.line,
                                        style = Stroke(width = 0.5.dp.toPx()),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx())
                                    )
                                }
                                .padding(24.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Items (${basketItems.sumOf { it.quantity }})", style = typography.bodyMedium, color = colors.inkSoft)
                                Text("₹$totalItemsPrice", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.ink)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.line)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery", style = typography.bodyMedium, color = colors.inkSoft)
                                Text("Free", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.sageInk)
                            }
                        }
                    }
                }
            }
        }
    }
}
