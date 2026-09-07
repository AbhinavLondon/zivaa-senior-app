package com.zivaa.app.ui.care

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

data class NudgeItem(
    val id: String,
    val glyph: String,
    val tone: Color,
    val textOnTone: Color,
    val title: String,
    val sub: String
)

data class ServiceItem(
    val id: String,
    val glyph: String,
    val tone: Color,
    val textOnTone: Color,
    val title: String,
    val sub: String,
    val meta: String,
    val price: String,
    val eta: String,
    val longDesc: String,
    val includes: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareScreen(
    onNavigateToDoctorVisit: () -> Unit = {},
    onNavigateToLabTests: () -> Unit = {},
    onNavigateToOrderMedicine: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val nudges = listOf(
        NudgeItem("doctor", "Rx", colors.sage, colors.sageInk, "Schedule a BP review", "Dr. Mehta · 15-min video · today or tomorrow"),
        NudgeItem("medicine", "Md", colors.clay, Color.White, "Re-order his medicines", "Metformin & 2 others · running low")
    )

    val services = listOf(
        ServiceItem(
            "doctor", "Dr", colors.leaf, Color.White, "Doctor visit", "Video or at-home, same day", "TODAY",
            "₹800 video · ₹1,500 home", "Today", "A consultation by video or at home — for a review, a concern, or a fresh prescription.",
            listOf("Choose video or at-home", "Prescription shared digitally", "Notes added to your record")
        ),
        ServiceItem(
            "medicine", "Md", colors.clay, Color.White, "Order medicine", "Refill, delivered to the door", "SAME-DAY DELIVERY",
            "Free delivery", "Same day", "Re-order his regular medicines, or upload a new prescription — delivered home, on time.",
            listOf("Refill from his prescription", "Upload a new prescription", "Delivered to the door, same day")
        ),
        ServiceItem(
            "lab", "Lb", colors.amber, Color.White, "Home lab tests", "Collection in 90 minutes", "90-MIN PICKUP",
            "Sample pickup ₹100", "In 90 minutes", "Sample collection at home and a plain-language summary once the results arrive.",
            listOf("Pickup at a time you choose", "Reports in the Health Wallet", "A simple summary of what changed")
        )
    )

    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var booked by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 130.dp)
        ) {
            item {
                TopHeader(
                    onMessageClick = { /* TODO */ }
                )
            }

            item {
                IntroSection()
            }

            item {
                UpcomingSection()
            }

            item {
                SuggestedSection(nudges) {
                    if (it.id == "doctor") {
                        onNavigateToDoctorVisit()
                    } else if (it.id == "lab") {
                        onNavigateToLabTests()
                    } else if (it.id == "medicine") {
                        onNavigateToOrderMedicine()
                    } else {
                        selectedService = services.find { s -> s.id == it.id } ?: services.first()
                        booked = false
                        showSheet = true
                    }
                }
            }

            item {
                ServicesGrid(services) {
                    if (it.id == "doctor") {
                        onNavigateToDoctorVisit()
                    } else if (it.id == "lab") {
                        onNavigateToLabTests()
                    } else if (it.id == "medicine") {
                        onNavigateToOrderMedicine()
                    } else {
                        selectedService = it
                        booked = false
                        showSheet = true
                    }
                }
            }

            item {
                SosSection()
            }

            item {
                FooterText()
            }
        }

        if (showSheet && selectedService != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = colors.bg,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(colors.lineStrong)
                    )
                },
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                if (booked) {
                    BookedSuccessView(onDone = { showSheet = false })
                } else {
                    BookingDetailsView(
                        service = selectedService!!,
                        onRequestVisit = { booked = true },
                        onCancel = { showSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopHeader(onMessageClick: () -> Unit) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Care",
            style = typography.eyebrow,
            color = Color(0xFF111111)
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.bgElev)
                .clickable { onMessageClick() }
                .drawBehind {
                    drawCircle(
                        color = colors.line,
                        style = Stroke(width = 1.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward, // Placeholder for message icon
                contentDescription = "Messages",
                tint = colors.ink,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun IntroSection() {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = "Small steps that help you.",
            style = typography.displayMedium,
            color = colors.ink
        )
        Text(
            text = "Today's nudges, and the home-care services we trust for the long run.",
            style = typography.bodyMedium,
            color = colors.inkSoft,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun UpcomingSection() {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming For You",
                style = typography.eyebrow,
                color = Color(0xFF111111)
            )
            Text(
                text = "SEE ALL 3 ›",
                style = typography.meta.copy(fontWeight = FontWeight.Medium),
                color = colors.accent
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surfaceHero)
                .drawBehind {
                    val gradient = Brush.radialGradient(
                        colors = listOf(Color(0x21F6F3EE), Color.Transparent),
                        center = Offset(size.width + 40.dp.toPx(), -50.dp.toPx()),
                        radius = 190.dp.toPx()
                    )
                    drawRect(brush = gradient)
                }
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x24F6F3EE))
                            .padding(start = 9.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD9E8D2))
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0x2ED9E8D2),
                                        radius = 3.5.dp.toPx() + 4.dp.toPx()
                                    )
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEXT UP · IN 2 HOURS",
                            style = typography.meta,
                            color = Color(0xD1F6F3EE)
                        )
                    }
                    Text(
                        text = "Today · 4:00 PM",
                        style = typography.eyebrow.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111111)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x29F6F3EE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            style = typography.cardTitle,
                            color = colors.sageInk
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "BP review with Dr. Mehta",
                            style = typography.cardTitle.copy(fontStyle = FontStyle.Normal),
                            color = colors.sageInk
                        )
                        Text(
                            text = "A 15-minute video call",
                            style = typography.bodySmall,
                            color = Color(0xC7F6F3EE),
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.sageInk, contentColor = colors.sage),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check, // Placeholder
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Join", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.sageInk),
                        border = BorderStroke(1.dp, Color(0x47F6F3EE)),
                        shape = CircleShape
                    ) {
                        Text("Reschedule", style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedSection(nudges: List<NudgeItem>, onClick: (NudgeItem) -> Unit) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "Suggested Today",
            style = typography.eyebrow,
            color = Color(0xFF111111),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            nudges.forEach { nudge ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.bgElev)
                        .clickable { onClick(nudge) }
                        .drawBehind {
                            drawRoundRect(
                                color = colors.line,
                                style = Stroke(width = 0.5.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                            )
                        }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(nudge.tone),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = nudge.glyph, style = typography.cardTitle.copy(fontSize = 22.sp), color = nudge.textOnTone)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = nudge.title, style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.ink)
                        Text(text = nudge.sub, style = typography.bodySmall, color = colors.inkMute, modifier = Modifier.padding(top = 2.dp))
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = colors.inkMute,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServicesGrid(services: List<ServiceItem>, onClick: (ServiceItem) -> Unit) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Column(modifier = Modifier.padding(top = 26.dp)) {
        Text(
            text = "Home Care Services",
            style = typography.eyebrow,
            color = Color(0xFF111111),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
        )

        // Using a vertical Column with a Row inside for grid since LazyVerticalGrid cannot easily be inside LazyColumn without fixed height
        val chunked = services.chunked(2)
        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chunked.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in 0 until 2) {
                        if (i < rowItems.size) {
                            val service = rowItems[i]
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(colors.bgElev)
                                    .clickable { onClick(service) }
                                    .drawBehind {
                                        drawRoundRect(
                                            color = colors.line,
                                            style = Stroke(width = 0.5.dp.toPx()),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx())
                                        )
                                    }
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 15.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(service.tone),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = service.glyph, style = typography.cardTitle.copy(fontSize = 20.sp), color = service.textOnTone)
                                }
                                Text(
                                    text = service.title,
                                    style = typography.bodyMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                                    color = colors.ink,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                Text(
                                    text = service.sub,
                                    style = typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                    color = colors.inkMute,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                                Text(
                                    text = service.meta,
                                    style = typography.meta.copy(fontSize = 9.5.sp),
                                    color = colors.accent,
                                    modifier = Modifier.padding(top = 11.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SosSection() {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.rose.copy(alpha = 0.08f))
            .clickable { /* TODO */ }
            .drawBehind {
                drawRoundRect(
                    color = colors.rose.copy(alpha = 0.22f),
                    style = Stroke(width = 0.5.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx())
                )
            }
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.rose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // Placeholder for phone SOS
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Need help right now?",
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.rose
                )
                Text(
                    text = "Reach the care team, or raise an SOS.",
                    style = typography.bodySmall,
                    color = colors.inkMute,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FooterText() {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Text(
        text = "Every visit is by a verified professional. You approve the time before anyone arrives.",
        style = typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
        color = colors.inkMute,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 16.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
private fun BookingDetailsView(service: ServiceItem, onRequestVisit: () -> Unit, onCancel: () -> Unit) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 26.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(service.tone),
                contentAlignment = Alignment.Center
            ) {
                Text(text = service.glyph, style = typography.cardTitle.copy(fontSize = 26.sp), color = service.textOnTone)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = service.title, style = typography.displayMedium.copy(fontSize = 25.sp), color = colors.ink)
                Text(text = service.longDesc, style = typography.bodySmall.copy(fontSize = 14.sp), color = colors.inkSoft, modifier = Modifier.padding(top = 5.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.bgElev)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.line,
                            style = Stroke(width = 0.5.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                Text(text = "Typical Cost", style = typography.meta.copy(fontSize = 9.sp), color = Color(0xFF111111))
                Text(text = service.price, style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.ink, modifier = Modifier.padding(top = 4.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.bgElev)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.line,
                            style = Stroke(width = 0.5.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                Text(text = "Soonest", style = typography.meta.copy(fontSize = 9.sp), color = Color(0xFF111111))
                Text(text = service.eta, style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.ink, modifier = Modifier.padding(top = 4.dp))
            }
        }

        Text(
            text = "What's Included",
            style = typography.meta.copy(fontSize = 10.sp),
            color = Color(0xFF111111),
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            service.includes.forEach { inc ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colors.sage.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.sage,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(11.dp))
                    Text(text = inc, style = typography.bodySmall.copy(fontSize = 14.sp), color = colors.ink)
                }
            }
        }

        Button(
            onClick = onRequestVisit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White),
            shape = CircleShape
        ) {
            Text("Request a visit", style = typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(48.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = colors.inkSoft)
        ) {
            Text("Not now", style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun BookedSuccessView(onDone: () -> Unit) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 14.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.sage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.sageInk,
                modifier = Modifier.size(30.dp)
            )
        }
        
        Text(
            text = "Request received.",
            style = typography.displayMedium.copy(fontSize = 26.sp),
            color = colors.ink
        )
        
        Text(
            text = "We'll call to confirm a time that suits you — usually within the hour. You'll get a message once it's set.",
            style = typography.bodySmall.copy(fontSize = 14.5.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            color = colors.inkSoft,
            modifier = Modifier.padding(top = 10.dp)
        )

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White),
            shape = CircleShape
        ) {
            Text("Done", style = typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
        }
    }
}
