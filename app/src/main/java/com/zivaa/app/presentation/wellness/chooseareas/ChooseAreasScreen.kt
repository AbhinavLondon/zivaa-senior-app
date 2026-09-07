package com.zivaa.app.presentation.wellness.chooseareas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.wellness.chooseareas.theme.ChooseAreasTheme

data class BodyPart(
    val id: String,
    val name: String,
    val sub: String,
    val mins: Int
)


data class PresetSet(
    val id: String,
    val name: String,
    val whenDesc: String,
    val areaLine: String,
    val mins: Int,
    val areas: List<String>,
    val icon: ImageVector,
    val tone: PresetTone
)

enum class PresetTone {
    AMBER, MUTED, CLAY, LEAF
}

val partsList = listOf(
    BodyPart("neck", "Neck", "Slow turns and tilts, seated", 3),
    BodyPart("shoulders", "Shoulders", "Rolls, and arms overhead", 5),
    BodyPart("back", "Back", "Cat-cow and side reaches", 5),
    BodyPart("hands", "Hands", "Wrist circles, finger spreads", 3),
    BodyPart("hips", "Hips", "Marches and gentle openers", 6),
    BodyPart("knees", "Knees", "Raises and heel slides", 6),
    BodyPart("feet", "Feet", "Ankle circles and pointing", 3)
)

val setsList = listOf(
    PresetSet("wake_up", "Wake up", "First thing, sitting on the bed", "NECK · SHOULDERS · BACK", 13, listOf("neck", "shoulders", "back"), Icons.Rounded.WbSunny, PresetTone.AMBER),
    PresetSet("everything", "Everything", "All seven areas, head to toe", "ALL SEVEN AREAS", 32, listOf("neck", "shoulders", "back", "hands", "hips", "knees", "feet"), Icons.Rounded.Person, PresetTone.MUTED),
    PresetSet("sat_too_long", "Sat too long", "After an afternoon in the chair", "BACK · HIPS · KNEES", 17, listOf("back", "hips", "knees"), Icons.Rounded.Chair, PresetTone.CLAY),
    PresetSet("steady_feet", "Steady feet", "Balance, before you go out", "HIPS · KNEES · FEET", 16, listOf("hips", "knees", "feet"), Icons.Rounded.Shield, PresetTone.LEAF),
    PresetSet("after_the_walk", "After the walk", "Cool down from your 7:30 round", "KNEES · FEET", 10, listOf("knees", "feet"), Icons.AutoMirrored.Rounded.DirectionsWalk, PresetTone.LEAF),
    PresetSet("wind_down", "Wind down", "Quiet moves before bed", "NECK · SHOULDERS · HANDS", 11, listOf("neck", "shoulders", "hands"), Icons.Rounded.DarkMode, PresetTone.MUTED)
)

@Composable
fun ChooseAreasScreen(
    isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    onNavigateBack: () -> Unit,
    onSessionBuilt: (List<String>) -> Unit
) {
    val selectedParts = remember { mutableStateListOf<String>() }

    fun togglePart(id: String) {
        if (selectedParts.contains(id)) {
            selectedParts.remove(id)
        } else {
            selectedParts.add(id)
        }
    }

    ChooseAreasTheme(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChooseAreasTheme.colors.bg)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                HeaderSection(
                    selectedParts = selectedParts,
                    onBackClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(22.dp))
                
                // Ready-made sets
                Text(
                    text = "Ready-Made Sets",
                    style = ChooseAreasTheme.typography.eyebrow,
                    color = Color(0xFF111111),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = "Tap one and we'll fill in the areas for you.",
                    style = ChooseAreasTheme.typography.sm,
                    color = ChooseAreasTheme.colors.inkSoft,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    setsList.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { preset ->
                                Box(modifier = Modifier.weight(1f)) {
                                    PresetCard(
                                        preset = preset,
                                        onClick = {
                                            selectedParts.clear()
                                            selectedParts.addAll(preset.areas)
                                        }
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Choose your own
                Spacer(modifier = Modifier.height(26.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Or Choose Your Own · Head To Toe",
                        style = ChooseAreasTheme.typography.eyebrow,
                        color = Color(0xFF111111)
                    )
                    Text(
                        text = "Clear",
                        style = ChooseAreasTheme.typography.body,
                        color = ChooseAreasTheme.colors.sage,
                        modifier = Modifier.clickable { selectedParts.clear() }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    partsList.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { part ->
                                Box(modifier = Modifier.weight(1f)) {
                                    BodyPartCard(
                                        part = part,
                                        isSelected = selectedParts.contains(part.id),
                                        onClick = { onSessionBuilt(listOf(part.id)) }
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Text(
                    text = "Nothing here is compulsory. A single area, done kindly, beats all seven rushed.",
                    style = ChooseAreasTheme.typography.sm,
                    color = ChooseAreasTheme.colors.inkMute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 34.dp, vertical = 26.dp)
                )
                Spacer(modifier = Modifier.height(130.dp))
            }

            // Footer
            FooterSection(
                selectedCount = selectedParts.size,
                totalMins = partsList.filter { selectedParts.contains(it.id) }.sumOf { it.mins },
                onBuildClick = { onSessionBuilt(selectedParts) }
            )
        }
    }
}

@Composable
fun HeaderSection(selectedParts: List<String>, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            // Breadcrumbs
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ChooseAreasTheme.colors.bgElev)
                        .border(1.dp, ChooseAreasTheme.colors.line, CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = ChooseAreasTheme.colors.ink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Body · Choose Areas",
                    style = ChooseAreasTheme.typography.eyebrow,
                    color = Color(0xFF111111)
                )
            }

            // Headline + Figure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildAnnotatedString {
                            append("Where shall we ")
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ChooseAreasTheme.colors.sage)) {
                                append("begin")
                            }
                            append("?")
                        },
                        style = ChooseAreasTheme.typography.display,
                        color = ChooseAreasTheme.colors.ink
                    )
                    Spacer(modifier = Modifier.height(9.dp))
                    Text(
                        text = "Tap wherever feels stiff this morning. Pick one, or a few — we'll put them in a sensible order.",
                        style = ChooseAreasTheme.typography.body,
                        color = ChooseAreasTheme.colors.inkSoft
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                
                // SVG Figure Placeholder (simplified rendering)
                FigureDrawing(
                    modifier = Modifier.size(82.dp, 172.dp),
                    selectedParts = selectedParts
                )
            }
        }
    }
}

@Composable
fun FigureDrawing(modifier: Modifier = Modifier, selectedParts: List<String>) {
    val defaultColor = ChooseAreasTheme.colors.line
    val selectedColor = ChooseAreasTheme.colors.sage

    // Draws the skeleton figure
    Canvas(modifier = modifier) {
        
        fun getColor(part: String) = if (selectedParts.contains(part)) selectedColor else defaultColor

        // Head
        drawCircle(
            color = getColor("neck"),
            radius = 10.5f.dp.toPx(),
            center = Offset(40f.dp.toPx(), 15f.dp.toPx()),
            style = Stroke(width = 1.7f.dp.toPx())
        )
        
        // Simple lines to simulate the figure layout based on SVG
        // Note: For a pixel-perfect rendition, we would translate the exact SVG paths here.
        // For brevity and compile-ability, we draw a stick figure approximation that gets styled.
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        
        // Spine / Back
        drawLine(
            getColor("back"),
            Offset(40f.dp.toPx(), 25f.dp.toPx()),
            Offset(40f.dp.toPx(), 80f.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        // Shoulders
        drawLine(
            getColor("shoulders"),
            Offset(22f.dp.toPx(), 41f.dp.toPx()),
            Offset(58f.dp.toPx(), 41f.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        // Arms
        drawLine(getColor("shoulders"), Offset(22f.dp.toPx(), 41f.dp.toPx()), Offset(15f.dp.toPx(), 71f.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(getColor("shoulders"), Offset(58f.dp.toPx(), 41f.dp.toPx()), Offset(65f.dp.toPx(), 71f.dp.toPx()), strokeWidth = 2.dp.toPx())
        // Hands
        drawCircle(getColor("hands"), radius = 5f.dp.toPx(), center = Offset(13f.dp.toPx(), 77f.dp.toPx()), style = stroke)
        drawCircle(getColor("hands"), radius = 5f.dp.toPx(), center = Offset(67f.dp.toPx(), 77f.dp.toPx()), style = stroke)
        // Hips
        drawLine(getColor("hips"), Offset(26f.dp.toPx(), 81f.dp.toPx()), Offset(54f.dp.toPx(), 81f.dp.toPx()), strokeWidth = 2.dp.toPx())
        // Legs (Knees)
        drawLine(getColor("knees"), Offset(29.5f.dp.toPx(), 86f.dp.toPx()), Offset(29.5f.dp.toPx(), 109f.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(getColor("knees"), Offset(50.5f.dp.toPx(), 86f.dp.toPx()), Offset(50.5f.dp.toPx(), 109f.dp.toPx()), strokeWidth = 2.dp.toPx())
        // Feet
        drawLine(getColor("feet"), Offset(29.5f.dp.toPx(), 118f.dp.toPx()), Offset(29.5f.dp.toPx(), 140f.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(getColor("feet"), Offset(50.5f.dp.toPx(), 118f.dp.toPx()), Offset(50.5f.dp.toPx(), 140f.dp.toPx()), strokeWidth = 2.dp.toPx())
    }
}

@Composable
fun PresetCard(preset: PresetSet, onClick: () -> Unit) {
    val toneColor = when (preset.tone) {
        PresetTone.AMBER -> ChooseAreasTheme.colors.amber
        PresetTone.MUTED -> ChooseAreasTheme.colors.inkSoft
        PresetTone.CLAY -> ChooseAreasTheme.colors.clay
        PresetTone.LEAF -> ChooseAreasTheme.colors.leaf
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = ChooseAreasTheme.colors.bgElev,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = toneColor.copy(alpha = 0.5f),
                spotColor = toneColor.copy(alpha = 0.5f)
            )
            .border(0.5.dp, ChooseAreasTheme.colors.line, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(toneColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = preset.icon,
                        contentDescription = null,
                        tint = toneColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${preset.mins} MIN",
                    style = ChooseAreasTheme.typography.meta,
                    color = ChooseAreasTheme.colors.inkMute
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = preset.name,
                style = ChooseAreasTheme.typography.card,
                color = ChooseAreasTheme.colors.ink
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preset.whenDesc,
                style = ChooseAreasTheme.typography.sm,
                color = ChooseAreasTheme.colors.inkSoft
            )
            Spacer(modifier = Modifier.height(9.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(ChooseAreasTheme.colors.line)
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = preset.areaLine,
                style = ChooseAreasTheme.typography.meta,
                color = ChooseAreasTheme.colors.inkMute
            )
        }
    }
}

@Composable
fun BodyPartCard(part: BodyPart, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) ChooseAreasTheme.colors.sage else ChooseAreasTheme.colors.line

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = ChooseAreasTheme.colors.bgElev,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = if (isSelected) 2.dp else 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ChooseAreasTheme.colors.sage,
                spotColor = ChooseAreasTheme.colors.sage
            )
            .border(if (isSelected) 1.5.dp else 0.5.dp, borderColor, RoundedCornerShape(22.dp))
    ) {
        val innerBg = if (isSelected) ChooseAreasTheme.colors.sage.copy(alpha = 0.04f) else Color.Transparent
        Column(modifier = Modifier.background(innerBg).padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChooseAreasTheme.colors.sage.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom SVG glyph placeholder (could use Painter or compose Path)
                    Box(modifier = Modifier.size(16.dp).background(ChooseAreasTheme.colors.sage, CircleShape))
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ChooseAreasTheme.colors.sage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = ChooseAreasTheme.colors.sageInk,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.dp, ChooseAreasTheme.colors.lineStrong, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(13.dp))
            Text(
                text = part.name,
                style = ChooseAreasTheme.typography.card,
                color = ChooseAreasTheme.colors.ink
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = part.sub,
                style = ChooseAreasTheme.typography.sm,
                color = ChooseAreasTheme.colors.inkSoft
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = ChooseAreasTheme.colors.inkMute,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "${part.mins} MIN",
                    style = ChooseAreasTheme.typography.meta,
                    color = ChooseAreasTheme.colors.inkMute
                )
            }
        }
    }
}

@Composable
fun FooterSection(selectedCount: Int, totalMins: Int, onBuildClick: () -> Unit) {
    Surface(
        color = ChooseAreasTheme.colors.bgElev,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, ambientColor = ChooseAreasTheme.colors.sage)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, start = 22.dp, end = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedCount == 0) "Nothing Selected" else "$selectedCount Areas · $totalMins Mins",
                        style = ChooseAreasTheme.typography.meta,
                        color = ChooseAreasTheme.colors.inkMute
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (selectedCount == 0) "Pick at least one area" else "Ready to build session",
                        style = ChooseAreasTheme.typography.lead,
                        color = ChooseAreasTheme.colors.ink
                    )
                }
                
                Button(
                    onClick = onBuildClick,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChooseAreasTheme.colors.sage,
                        contentColor = ChooseAreasTheme.colors.sageInk,
                        disabledContainerColor = ChooseAreasTheme.colors.lineStrong,
                        disabledContentColor = ChooseAreasTheme.colors.inkMute
                    ),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text("Build", style = ChooseAreasTheme.typography.bodyLg)
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Build",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .width(124.dp)
                    .height(4.dp)
                    .background(ChooseAreasTheme.colors.ink.copy(alpha = 0.26f), RoundedCornerShape(4.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
