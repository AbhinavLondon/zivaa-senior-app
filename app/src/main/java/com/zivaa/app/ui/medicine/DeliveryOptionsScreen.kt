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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryOptionsScreen(
    viewModel: OrderMedicineViewModel,
    onBack: () -> Unit,
    onNavigateToConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val selectedDeliveryId by viewModel.selectedDeliveryOptionId.collectAsState()
    val totalPrice = viewModel.getTotalPrice()

    Box(modifier = modifier.fillMaxSize().background(colors.bgElev)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            bottomBar = {
                if (selectedDeliveryId != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.bgElev)
                            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 12.dp, bottom = 12.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(colors.sage)
                                .clickable { onNavigateToConfirmation() }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Place the order",
                                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "Pay on delivery. A pharmacist verifies any prescription before dispatch.",
                            style = typography.meta,
                            color = colors.inkSoft,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 32.dp, end = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 180.dp)
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
                            text = "When & Where",
                            style = typography.eyebrow,
                            color = Color(0xFF111111),
                            modifier = Modifier.padding(end = 40.dp)
                        )
                        Spacer(modifier = Modifier.width(38.dp))
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                append("When should it ")
                                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.accent)) {
                                    append("arrive?")
                                }
                            },
                            style = typography.displayMedium.copy(fontSize = 32.sp),
                            color = colors.ink
                        )
                        Text(
                            text = "A rider brings it to your door. Pay on delivery, or with the card on file.",
                            style = typography.bodyMedium,
                            color = colors.inkSoft,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                item {
                    Text(
                        text = "Delivery Time",
                        style = typography.eyebrow,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                    )
                }

                items(MedicineMockData.deliveryOptions) { option ->
                    val isSelected = selectedDeliveryId == option.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                            .zivaaShadow(cornerRadius = 18.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) colors.sage.copy(alpha = 0.1f) else colors.bg)
                            .clickable { viewModel.selectDeliveryOption(option.id) }
                            .border(if (isSelected) 1.dp else 0.5.dp, if (isSelected) colors.sage else colors.line, RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.inkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = option.title, style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = colors.ink)
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append(if (option.price == 0) "Free " else "₹${option.price} ")
                                    }
                                    append("· ${option.subtitle}")
                                },
                                style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (isSelected) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(colors.sage), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).drawBehind {
                                drawCircle(color = colors.line, style = Stroke(width = 1.dp.toPx()))
                            })
                        }
                    }
                }

                item {
                    Text(
                        text = "Deliver To",
                        style = typography.eyebrow,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.bgElev)
                            .drawBehind {
                                drawRoundRect(
                                    color = colors.line,
                                    style = Stroke(width = 0.5.dp.toPx()),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx())
                                )
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(colors.sage.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = colors.ink, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.ink)) { append("Home") }
                                    withStyle(style = SpanStyle(color = colors.inkSoft)) { append(" · Ranjit Iyer") }
                                },
                                style = typography.bodyMedium,
                            )
                            Text(text = "12 Gulmohar Lane, Koregaon Park, Pune", style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.padding(top = 4.dp))
                        }
                        Text(
                            text = "Change",
                            style = typography.eyebrow.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF111111),
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
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
                        val basketItems by viewModel.basketItems.collectAsState()
                        val selectedDeliveryOption = MedicineMockData.deliveryOptions.find { it.id == selectedDeliveryId }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items (${basketItems.sumOf { it.quantity }})", style = typography.bodyMedium, color = colors.inkSoft)
                            Text("₹${viewModel.getTotalItemsPrice()}", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.ink)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery · ${selectedDeliveryOption?.title?.lowercase() ?: ""}", style = typography.bodyMedium, color = colors.inkSoft)
                            Text(
                                text = if (selectedDeliveryOption?.price == 0) "Free" else "₹${selectedDeliveryOption?.price ?: 0}",
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedDeliveryOption?.price == 0) colors.sage else colors.ink
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.line)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("To pay", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.ink)
                            Text("₹$totalPrice", style = typography.displayMedium.copy(fontSize = 28.sp), color = colors.ink)
                        }
                    }
                }
            }
        }
    }
}
