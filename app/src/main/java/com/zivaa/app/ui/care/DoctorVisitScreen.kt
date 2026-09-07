package com.zivaa.app.ui.care

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import com.zivaa.app.ui.theme.zivaaHeroShadow
import com.zivaa.app.ui.theme.zivaaShadow

data class SymptomChip(val icon: String, val label: String)

data class Specialist(
    val id: String,
    val name: String,
    val photoUrl: String,
    val specShort: String,
    val rating: String,
    val hospital: String,
    val reviews: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoctorVisitScreen(
    onBack: () -> Unit,
    onNavigateToDoctorProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val symptoms = listOf(
        SymptomChip("♡", "Chest pain"),
        SymptomChip("🏃", "Knee or joints"),
        SymptomChip("🩸", "Sugar trouble"),
        SymptomChip("😵", "Dizziness"),
        SymptomChip("🫁", "Breathing"),
        SymptomChip("🤒", "Fever / unwell")
    )

    val specialists = listOf(
        Specialist(
            id = "1",
            name = "Dr. Sunita Desai",
            photoUrl = "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?q=80&w=300&auto=format&fit=crop",
            specShort = "Endocrinologist",
            rating = "4.9",
            hospital = "Jehangir Hospital, Pune",
            reviews = 210
        ),
        Specialist(
            id = "2",
            name = "Dr. Rohan Kulkarni",
            photoUrl = "https://images.unsplash.com/photo-1612349317150-e410f624c427?q=80&w=300&auto=format&fit=crop",
            specShort = "Cardiologist",
            rating = "4.8",
            hospital = "Ruby Hall Clinic, Pune",
            reviews = 410
        )
    )

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
                        .padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.bgElev)
                            .border(0.5.dp, colors.line, CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Talk To A Doctor",
                        style = typography.eyebrow,
                        color = Color(0xFF111111)
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)) {
                    Text(
                        text = "Who would you like to see?",
                        style = typography.displayMedium,
                        color = colors.ink
                    )
                    Text(
                        text = "Tell me what's troubling you and I'll suggest the right doctor — or choose someone yourself.",
                        style = typography.bodyMedium,
                        color = colors.inkSoft,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            // Symptom Describe Card
            item {
                Box(modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zivaaShadow(cornerRadius = 22.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.bgElev)
                            .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(colors.sage),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", style = typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = colors.sageInk)
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Text(
                                text = "Not Sure Who To See?",
                                style = typography.meta.copy(letterSpacing = 0.08.sp),
                                color = colors.inkMute
                            )
                        }

                        Text(
                            text = "Tell me what's troubling you.",
                            style = typography.cardTitle,
                            color = colors.ink,
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .border(1.5.dp, colors.lineStrong, RoundedCornerShape(999.dp))
                                .background(colors.bg)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.inkMute,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "e.g. \"my knee hurts when I walk\"",
                                style = typography.bodySmall.copy(fontSize = 14.sp),
                                color = colors.inkMute,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = "Or Tap A Common One",
                            style = typography.meta.copy(fontSize = 9.5.sp),
                            color = colors.inkMute,
                            modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            symptoms.forEach { chip ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(colors.bg)
                                        .border(1.dp, colors.lineStrong, RoundedCornerShape(999.dp))
                                        .clickable { }
                                        .padding(horizontal = 15.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = chip.icon, color = colors.sage, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = chip.label,
                                        style = typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                        color = colors.ink
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Or Choose Someone Yourself",
                    style = typography.eyebrow,
                    color = Color(0xFF111111),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
                )
            }

            // Family Doctor Hero Card
            item {
                Box(modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zivaaHeroShadow()
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.surfaceHero)
                            .drawBehind {
                                val gradient = Brush.radialGradient(
                                    colors = listOf(Color(0x1FF6F3EE), Color.Transparent),
                                    center = Offset(size.width + 40.dp.toPx(), -50.dp.toPx()),
                                    radius = 190.dp.toPx()
                                )
                                drawRect(brush = gradient)
                            }
                            .padding(22.dp)
                    ) {
                        Text(
                            text = "YOUR FAMILY DOCTOR",
                            style = typography.meta.copy(fontSize = 10.sp),
                            color = Color(0xB8F6F3EE)
                        )

                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x29F6F3EE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AM",
                                    style = typography.cardTitle.copy(fontSize = 22.sp),
                                    color = colors.sageInk
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Dr. Anjali Mehta",
                                    style = typography.titleLarge.copy(fontSize = 24.sp),
                                    color = colors.sageInk
                                )
                                Text(
                                    text = "General physician · knows your history",
                                    style = typography.bodySmall.copy(fontSize = 13.5.sp),
                                    color = Color(0xD1F6F3EE),
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFFD9E8D2),
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You've consulted her 14 times — she has all your reports.",
                                style = typography.bodySmall.copy(fontSize = 13.sp),
                                color = Color(0xE0F6F3EE)
                            )
                        }

                        Button(
                            onClick = { onNavigateToDoctorProfile() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F3EE), contentColor = colors.sage),
                            shape = CircleShape
                        ) {
                            Text("Talk to Dr. Mehta", style = typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Specialists Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Need A Specialist?",
                        style = typography.eyebrow,
                        color = Color(0xFF111111)
                    )
                    Text(
                        text = "All Verified",
                        style = typography.meta.copy(fontSize = 9.5.sp),
                        color = colors.inkMute
                    )
                }
            }

            // Specialists List
            items(specialists) { specialist ->
                Box(modifier = Modifier.padding(horizontal = 22.dp, vertical = 5.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zivaaShadow(cornerRadius = 22.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.bgElev)
                            .border(0.5.dp, colors.line, RoundedCornerShape(22.dp))
                            .clickable { onNavigateToDoctorProfile() }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        ) {
                            AsyncImage(
                                model = specialist.photoUrl,
                                contentDescription = specialist.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0x52000000)),
                                            startY = 100f
                                        )
                                    )
                            )
                            // Verified Badge
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xF0FBF9F5))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.sage,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Verified",
                                    style = typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                                    color = colors.sage
                                )
                            }
                            // Spec Chip
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (specialist.specShort.contains("Cardio")) colors.clay else colors.leaf)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = specialist.specShort,
                                    style = typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = specialist.name,
                                    style = typography.titleLarge.copy(fontSize = 24.sp),
                                    color = colors.ink
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("★", color = colors.amber, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = specialist.rating,
                                        style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.ink
                                    )
                                }
                            }
                            
                            Text(
                                text = "📍 ${specialist.hospital}",
                                style = typography.bodySmall.copy(fontSize = 14.sp),
                                color = colors.inkSoft,
                                modifier = Modifier.padding(top = 10.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .drawBehind {
                                        drawLine(
                                            color = colors.line,
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = 0.5.dp.toPx()
                                        )
                                    }
                                    .padding(top = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${specialist.reviews} reviews",
                                    style = typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = colors.inkMute
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(colors.lineStrong))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Referred by Dr. Mehta",
                                    style = typography.bodySmall.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                                    color = colors.clay
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "View ›",
                                    style = typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                    color = colors.sage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
