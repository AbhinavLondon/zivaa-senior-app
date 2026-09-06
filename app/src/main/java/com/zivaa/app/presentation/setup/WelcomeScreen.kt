package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.InstrumentSerif

import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect

@Composable
fun WelcomeScreen(
    state: SetupState,
    onNext: () -> Unit,
    onSignIn: () -> Unit,
    onBypassSetup: () -> Unit
) {
    LaunchedEffect(state.isSetupComplete) {
        if (state.isSetupComplete) {
            onBypassSetup()
        }
    }

    ZivaaSetupBackground {
        Spacer(modifier = Modifier.weight(1f))
        
        // Logo Z
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = ZivaaTheme.colors.sage,
            border = BorderStroke(7.dp, ZivaaTheme.colors.sage.copy(alpha = 0.16f)) // Mocking the outer circles with a thick faint border
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Let's set things up together".uppercase(),
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.inkMute
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        Text(
            text = buildAnnotatedString {
                append("A few small ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("questions")
                }
                append(",")
                // We'll append name if we have it, else "Ranjit" as in prototype
                val name = if (state.name.isNotBlank()) state.name else "Ranjit"
                append("\n$name.")
            },
            style = ZivaaTheme.typography.displayLarge,
            color = ZivaaTheme.colors.ink,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "One at a time, no rush. We'll get to know you a little, so each morning feels made just for you.",
            style = ZivaaTheme.typography.bodyMedium,
            color = ZivaaTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 290.dp)
        )
        
        Spacer(modifier = Modifier.height(22.dp))
        
        Text(
            text = "Takes about 3 minutes · 5 steps",
            style = ZivaaTheme.typography.eyebrow,
            color = ZivaaTheme.colors.inkMute
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        ZivaaButton(text = "Let's begin", onClick = onNext)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onSignIn) {
            Text(
                text = "Already have an account? Sign in",
                style = ZivaaTheme.typography.bodyMedium,
                color = ZivaaTheme.colors.accent
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
