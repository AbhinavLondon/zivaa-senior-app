package com.zivaa.app.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderMedicineScreen(
    onBack: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

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
                        text = "Order Medicine",
                        style = typography.eyebrow,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(end = 40.dp) // optical center
                    )
                    Spacer(modifier = Modifier.width(38.dp))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "Medicine, to the door.",
                        style = typography.displayMedium,
                        color = colors.ink
                    )
                    Text(
                        text = "No pharmacy run, no waiting. Send us a prescription, or pick up the everyday things. How shall we start?",
                        style = typography.bodyMedium,
                        color = colors.inkSoft,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                // Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.sage)
                        .drawBehind {
                            val gradient = Brush.radialGradient(
                                colors = listOf(Color(0x3DF6F3EE), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f),
                                radius = size.width * 0.7f
                            )
                            drawRect(brush = gradient)
                        }
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x29F6F3EE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Rx",
                                    style = typography.cardTitle.copy(fontStyle = FontStyle.Italic, fontSize = 22.sp),
                                    color = colors.sageInk
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "The Easiest Way",
                                style = typography.meta.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
                                color = colors.sageInk.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = "Have a doctor's prescription?",
                            style = typography.cardTitle.copy(fontStyle = FontStyle.Normal, fontSize = 24.sp),
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Hold it up to the camera. We'll read every medicine on it and have a pharmacist fill the order — no typing.",
                            style = typography.bodyMedium,
                            color = Color(0xD6F6F3EE),
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )

                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.bg, contentColor = colors.sage),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan a prescription", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "No Prescription? No Problem",
                    style = typography.eyebrow,
                    color = Color(0xFF111111),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MedicineMockData.categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zivaaShadow(cornerRadius = 18.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.bg)
                                .border(0.5.dp, colors.line, RoundedCornerShape(18.dp))
                                .clickable { onNavigateToCategory(category.id) }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(category.tone),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    // Normally we would map icon glyph to vector. Placeholder for now.
                                    text = if(category.id == "everyday") "[-]" else if (category.id == "homeopathy") "O" else "*", 
                                    style = typography.cardTitle.copy(fontSize = 18.sp, color = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = category.title, style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = colors.ink)
                                Text(text = category.subtitle, style = typography.bodySmall, color = colors.inkSoft, modifier = Modifier.padding(top = 2.dp))
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

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Re-order regular
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .zivaaShadow(cornerRadius = 18.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.bg)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = colors.inkSoft,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Re-order your regulars — ",
                        style = typography.bodyMedium,
                        color = colors.inkSoft
                    )
                    Text(
                        text = "Metformin & 2...",
                        style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.ink
                    )
                }
            }
        }
    }
}
