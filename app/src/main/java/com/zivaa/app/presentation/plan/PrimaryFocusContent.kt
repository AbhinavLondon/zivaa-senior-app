package com.zivaa.app.presentation.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

data class FocusOption(
    val id: String,
    val icon: ImageVector,
    val toneBg: @Composable () -> Color,
    val toneOnBg: @Composable () -> Color,
    val title: String,
    val tag: String,
    val lead: String,
    val body: String
)

@Composable
fun PrimaryFocusContent(
    selectedId: String?,
    onOptionSelected: (String) -> Unit,
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {}
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val focusOptions = remember {
        listOf(
            FocusOption(
                id = "mobility",
                icon = Icons.Default.DirectionsWalk,
                toneBg = { colors.leaf },
                toneOnBg = { Color.White },
                title = "Maintain Mobility & Balance",
                tag = "ON YOUR OWN TERMS",
                lead = "Excellent choice.",
                body = "Your daily plan will focus on joint-friendly mobility routines and functional core strength — so you keep doing the things you love, completely on your own terms."
            ),
            FocusOption(
                id = "energy",
                icon = Icons.Default.Bolt,
                toneBg = { colors.amber },
                toneOnBg = { Color.White },
                title = "Improve Energy & Vitality",
                tag = "FUEL FOR THE DAY",
                lead = "Got it.",
                body = "We’ll tune your daily nutrition, movement and hydration to smooth out the afternoon slump and naturally lift your everyday stamina."
            ),
            FocusOption(
                id = "longevity",
                icon = Icons.Default.FavoriteBorder,
                toneBg = { colors.sage },
                toneOnBg = { colors.sageInk },
                title = "Support Healthy Aging & Longevity",
                tag = "PROACTIVE & DIGNIFIED",
                lead = "A vital focus.",
                body = "Your plan will build balanced daily habits that support heart health, steady blood sugar and long-term wellness."
            ),
            FocusOption(
                id = "sleep",
                icon = Icons.Default.DarkMode,
                toneBg = { colors.ink },
                toneOnBg = { colors.bg },
                title = "Enhance Sleep & Mental Clarity",
                tag = "REST & SHARPNESS",
                lead = "Perfect.",
                body = "We’ll design a calming evening wind-down and gentle morning habits — fall asleep easier, stay asleep longer, wake up feeling sharp."
            ),
            FocusOption(
                id = "condition",
                icon = Icons.Default.Shield,
                toneBg = { colors.clay },
                toneOnBg = { Color.White },
                title = "Manage a Chronic Condition",
                tag = "WITH YOUR DOCTOR’S ADVICE",
                lead = "A very smart focus.",
                body = "Your daily checklist will lean on low-impact movement and supportive meals — keeping joints safe, bones dense and your body feeling strong."
            ),
            FocusOption(
                id = "recovery",
                icon = Icons.Default.History,
                toneBg = { colors.rose },
                toneOnBg = { Color.White },
                title = "Recover from Injury or Surgery",
                tag = "DAY BY DAY",
                lead = "We’re with you.",
                body = "Safe, gradual rehabilitation habits that respect your recovery timeline — rebuilding your baseline strength, day by day."
            )
        )
    }
    
    val selectedOption = focusOptions.find { it.id == selectedId }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            header()
        }

        items(focusOptions) { option ->
            FocusCard(
                option = option,
                isSelected = selectedId == option.id,
                isAnythingSelected = selectedId != null,
                onClick = {
                    onOptionSelected(if (selectedId == option.id) "" else option.id)
                }
            )
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .defaultMinSize(minHeight = 118.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (selectedOption != null) {
                    ValidationBlock(selectedOption = selectedOption)
                } else {
                    footer()
                }
            }
        }
    }
}

@Composable
fun FocusCard(
    option: FocusOption,
    isSelected: Boolean,
    isAnythingSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val opacity by animateFloatAsState(
        targetValue = if (isAnythingSelected && !isSelected) 0.55f else 1f,
        label = "opacity"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .scale(scale)
            .alpha(opacity)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.bgElev
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 4.dp
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) option.toneBg() else colors.line
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 15.dp, end = 15.dp, bottom = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Icon Bubble
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(option.toneBg()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = option.toneOnBg(),
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = option.title,
                    fontFamily = typography.displayLarge.fontFamily,
                    fontSize = 19.sp,
                    lineHeight = (19 * 1.14).sp,
                    color = colors.ink,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = option.tag,
                    style = typography.meta,
                    color = colors.inkMute,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Checkmark
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .offset(x = 3.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(option.toneBg()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = option.toneOnBg(),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ValidationBlock(
    selectedOption: FocusOption
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgElev),
        border = BorderStroke(1.dp, selectedOption.toneBg()),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(selectedOption.toneBg())
                    .padding(top = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = selectedOption.toneOnBg(),
                    modifier = Modifier.size(15.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedOption.lead,
                    fontFamily = typography.displayLarge.fontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    color = colors.ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedOption.body,
                    style = typography.bodySmall,
                    color = colors.inkSoft
                )
            }
        }
    }
}
