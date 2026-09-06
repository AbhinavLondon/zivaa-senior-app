package com.zivaa.app.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.R
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@Composable
fun OrderPlacedScreen(
    viewModel: OrderMedicineViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val selectedOptionId = viewModel.selectedDeliveryOptionId.value
    val option = MedicineMockData.deliveryOptions.find { it.id == selectedOptionId }
    val eta = option?.title ?: "Shortly"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bgElev,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.sage),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Text(
                text = "Order placed, Ranjit.",
                style = typography.displayMedium.copy(fontSize = 32.sp),
                color = colors.ink,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Text(
                text = "A pharmacist is preparing it now. You'll get a message when the rider sets off, and again at the door.",
                style = typography.bodyMedium.copy(textAlign = TextAlign.Center),
                color = colors.inkSoft,
                modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .zivaaShadow(cornerRadius = 22.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.bg)
                    .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
                    .padding(24.dp)
            ) {
                val totalQuantity = viewModel.basketItems.collectAsState().value.sumOf { it.quantity }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Items", style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.width(80.dp))
                    Text("$totalQuantity · ₹${viewModel.getTotalPrice()}", style = typography.bodyMedium, color = colors.ink)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.line)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Arrives", style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.width(80.dp))
                    Text("Today, ${option?.subtitle?.replace("between ", "") ?: ""}", style = typography.bodyMedium, color = colors.ink)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.line)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("At", style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.width(80.dp))
                    Text("12 Gulmohar Lane, Pune", style = typography.bodyMedium, color = colors.ink)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.sage.copy(alpha = 0.1f))
                    .drawBehind {
                        drawRoundRect(
                            color = colors.sage.copy(alpha = 0.3f),
                            style = Stroke(width = 1.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                        )
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.sage,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "We'll set gentle refill reminders so your regulars never run low again.",
                    style = typography.bodySmall,
                    color = colors.ink
                )
            }

            Button(
                onClick = {
                    viewModel.clearBasket()
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.sage, contentColor = Color.White),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("Done", style = typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }

            Text(
                text = "Track delivery",
                style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.inkSoft,
                modifier = Modifier.padding(top = 24.dp).clickable { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 130.dp))
        }
    }
}
