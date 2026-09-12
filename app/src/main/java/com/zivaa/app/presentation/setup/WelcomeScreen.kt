package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.InstrumentSerif

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    state: SetupState,
    onNext: () -> Unit,
    onSignIn: () -> Unit,
    onBypassSetup: () -> Unit,
    onLanguageSelected: (String) -> Unit = {}
) {
    LaunchedEffect(state.isSetupComplete) {
        if (state.isSetupComplete) {
            onBypassSetup()
        }
    }

    LaunchedEffect(state.isEmailVerified) {
        if (state.isEmailVerified && !state.isSetupComplete) {
            onNext()
        }
    }

    var showLanguageSheet by remember { mutableStateOf(false) }

    val languages = remember {
        listOf(
            "English" to "English (Default)",
            "Hindi" to "हिंदी (Hindi)",
            "Bengali" to "বাংলা (Bengali)",
            "Marathi" to "मराठी (Marathi)",
            "Telugu" to "తెలుగు (Telugu)",
            "Tamil" to "தமிழ் (Tamil)",
            "Gujarati" to "ગુજરાતી (Gujarati)",
            "Kannada" to "ಕನ್ನಡ (Kannada)",
            "Malayalam" to "മലയാളം (Malayalam)",
            "Punjabi" to "ਪੰਜਾਬੀ (Punjabi)",
            "Odia" to "ଓଡ଼ିଆ (Odia)"
        )
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = ZivaaTheme.colors.bgElev,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Choose Preferred Language",
                    style = ZivaaTheme.typography.titleLarge,
                    color = ZivaaTheme.colors.ink,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Zivaa's AI coach and daily plan will adapt to your language.",
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.inkSoft,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(languages) { (langCode, displayLabel) ->
                        val isSelected = state.preferredLanguage.equals(langCode, ignoreCase = true)
                        Surface(
                            onClick = {
                                onLanguageSelected(langCode)
                                showLanguageSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.12f) else ZivaaTheme.colors.bgElev,
                            border = if (isSelected) BorderStroke(1.dp, ZivaaTheme.colors.sage) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = displayLabel,
                                        style = ZivaaTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) ZivaaTheme.colors.sageInk else ZivaaTheme.colors.ink
                                    )
                                }
                                if (isSelected) {
                                    Text("✓", color = ZivaaTheme.colors.sage, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    ZivaaSetupBackground {
        // Top row with Language selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = { showLanguageSheet = true },
                shape = RoundedCornerShape(20.dp),
                color = ZivaaTheme.colors.bgElev,
                border = BorderStroke(1.dp, ZivaaTheme.colors.line)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌐", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.preferredLanguage,
                        style = ZivaaTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ZivaaTheme.colors.ink
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("▾", fontSize = 12.sp, color = ZivaaTheme.colors.inkMute)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        // Logo Z
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = ZivaaTheme.colors.sage,
            border = BorderStroke(7.dp, ZivaaTheme.colors.sage.copy(alpha = 0.16f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Z",
                    color = ZivaaTheme.colors.sageInk,
                    fontFamily = InstrumentSerif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 28.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        Text(
            text = "Zivaa",
            color = ZivaaTheme.colors.sage,
            fontFamily = InstrumentSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 24.sp
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = "Welcome to your longevity companion",
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.eyebrow
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = buildAnnotatedString {
                append("Care tailored around ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("your life.")
                }
            },
            style = ZivaaTheme.typography.displayLarge,
            color = ZivaaTheme.colors.ink,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        Text(
            text = "A few small questions to tailor your daily health, joint comfort, and peace of mind.",
            style = ZivaaTheme.typography.bodyMedium,
            color = ZivaaTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )
        
        Spacer(modifier = Modifier.height(18.dp))
        
        Text(
            text = "Takes About 2 Minutes · 5 Gentle Steps",
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.eyebrow
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Primary CTA: Continue with Google (1-Tap Entry)
        Surface(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(ZivaaTheme.spacing.radiusPill),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ZivaaTheme.colors.sage,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Connecting with Google...",
                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = ZivaaTheme.colors.ink
                    )
                } else {
                    Text(
                        text = "G",
                        style = ZivaaTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF4285F4),
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1F1F1F)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Secondary CTA: Alternative for seniors without a Google account
        TextButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Or enter details manually",
                style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = ZivaaTheme.colors.inkSoft
            )
        }

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error,
                color = ZivaaTheme.colors.rose,
                style = ZivaaTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
