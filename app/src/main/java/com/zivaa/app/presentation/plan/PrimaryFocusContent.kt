package com.zivaa.app.presentation.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
                body = "We'll tune your daily nutrition, movement and hydration to smooth out the afternoon slump and naturally lift your everyday stamina."
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
                body = "We'll design a calming evening wind-down and gentle morning habits — fall asleep easier, stay asleep longer, wake up feeling sharp."
            ),
            FocusOption(
                id = "condition",
                icon = Icons.Default.Shield,
                toneBg = { colors.clay },
                toneOnBg = { Color.White },
                title = "Manage a Chronic Condition",
                tag = "WITH YOUR DOCTOR'S ADVICE",
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
                lead = "We're with you.",
                body = "Safe, gradual rehabilitation habits that respect your recovery timeline — rebuilding your baseline strength, day by day."
            )
        )
    }

    val selectedOption = focusOptions.find { it.id == selectedId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            header()
        }

        items(focusOptions, key = { it.id }) { option ->
            FocusCard(
                option = option,
                isSelected = selectedId == option.id,
                onClick = { onOptionSelected(option.id) }
            )
        }

        item {
            AnimatedVisibility(
                visible = selectedOption != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (selectedOption != null) {
                    ValidationBlock(selectedOption = selectedOption)
                }
            }
            if (selectedOption == null) {
                footer()
            }
        }
    }
}

@Composable
fun FocusCard(
    option: FocusOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        color = colors.bgElev,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colors.sage else colors.lineStrong
        ),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Bubble (Intact original icon and tone)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(option.toneBg()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = option.toneOnBg(),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Subtitle Tag
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.title,
                    style = typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = colors.ink
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = option.tag,
                    style = typography.eyebrow.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = if (isSelected) colors.sage else colors.inkMute
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Sleek Circular Selection Checkmark (Zivaa Green when selected)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.sage else Color.Transparent)
                    .border(
                        width = if (isSelected) 0.dp else 1.5.dp,
                        color = if (isSelected) Color.Transparent else colors.lineStrong,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = colors.sageInk,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ValidationBlock(
    selectedOption: FocusOption,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = colors.bgElev,
        border = BorderStroke(1.5.dp, colors.sage),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(colors.sage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.sageInk,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = selectedOption.lead,
                    fontFamily = typography.displayLarge.fontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = colors.ink
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = selectedOption.body,
                style = typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    fontSize = 14.5.sp
                ),
                color = colors.inkSoft
            )
        }
    }
}
