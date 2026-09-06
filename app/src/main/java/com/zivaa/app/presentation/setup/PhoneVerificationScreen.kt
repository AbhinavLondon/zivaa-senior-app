package com.zivaa.app.presentation.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.R
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun PhoneVerificationScreen(
    state: SetupState,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(state.isSetupComplete) {
        if (state.isSetupComplete) {
            onNext()
        }
    }

    ZivaaSetupBackground {
        if (state.isEmailVerified) {
            // Success Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = ZivaaTheme.colors.leaf.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = ZivaaTheme.colors.leaf
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✓", color = ZivaaTheme.colors.sageInk, fontSize = 40.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Google Account connected!",
                    style = ZivaaTheme.typography.titleLarge,
                    color = ZivaaTheme.colors.ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your account is securely connected.",
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.inkSoft,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f))
                ZivaaButton(text = "Continue", onClick = onNext)
            }
        } else {
            // Google Sign-in Screen
            ZivaaTopBar(stepNo = 2, totalSteps = 6, onBack = onBack)
            ZivaaHeader(
                title = "Secure your account",
                subtitle = "We use Google to securely back up your health data and keep it private.",
                label = "Account Info"
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Google Sign In Button
            Surface(
                onClick = onSendOtp, // We will map onSendOtp to signInWithGoogle in NavHost
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ZivaaTheme.colors.ink,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Opening Browser...",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = ZivaaTheme.colors.ink
                        )
                    } else {
                        // Google 'G' icon placeholder (ideally use a real Google logo drawable, using generic text for now)
                        Text(
                            text = "G",
                            style = ZivaaTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4285F4) // Google Blue
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Sign in with Google",
                            style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = ZivaaTheme.colors.ink
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = ZivaaTheme.colors.rose,
                    style = ZivaaTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
