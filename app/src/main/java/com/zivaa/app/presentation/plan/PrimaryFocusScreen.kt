package com.zivaa.app.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

@Composable
fun PrimaryFocusScreen(
    viewModel: PlanSetupViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit = {}
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current

    val uiState by viewModel.state.collectAsState()
    val selectedId = uiState.primaryFocus

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.bg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bg)
            ) {
                HorizontalDivider(color = colors.line, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 14.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp)
                ) {
                    if (selectedId != null) {
                        Button(
                            onClick = onNavigateNext,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    ambientColor = colors.sage,
                                    spotColor = colors.sage
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.sage,
                                contentColor = colors.sageInk
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Shape the rest of my day",
                                    fontFamily = typography.bodyMedium.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(CircleShape)
                                .background(colors.muted)
                                .padding(1.dp)
                                .background(colors.muted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Choose one to continue",
                                fontFamily = typography.bodyMedium.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = colors.inkMute
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            PrimaryFocusContent(
                selectedId = selectedId,
                onOptionSelected = { viewModel.updatePrimaryFocus(it) },
                header = {
                    Column {
                        // Top spacing and eyebrow
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Step 1 of 2 · Your plan",
                            style = typography.eyebrow,
                            color = colors.inkMute,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        
                        // Headline
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("What’s your ")
                                withStyle(style = SpanStyle(color = colors.sage, fontStyle = FontStyle.Italic)) {
                                    append("primary focus?")
                                }
                            },
                            style = typography.displayLarge,
                            color = colors.ink,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "One answer shapes the whole plan. You can change it any time.",
                            style = typography.bodySmall.copy(fontSize = 13.5.sp),
                            color = colors.inkSoft,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                },
                footer = {
                    Text(
                        text = "Tap the one that matters most right now.",
                        style = typography.bodySmall.copy(fontSize = 12.5.sp),
                        color = colors.inkMute,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}
