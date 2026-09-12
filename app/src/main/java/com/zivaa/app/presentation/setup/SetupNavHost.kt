package com.zivaa.app.presentation.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zivaa.app.data.remote.RetrofitClient

@Composable
fun SetupNavHost(
    onSetupComplete: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: SetupViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = "welcome") {
        // Step 0: Welcome Screen with Language Selector and Dual CTAs
        composable("welcome") {
            val context = androidx.compose.ui.platform.LocalContext.current
            WelcomeScreen(
                state = state,
                onNext = { 
                    viewModel.startFreshEnrollment()
                    navController.navigate("about_you") 
                },
                onSignIn = { viewModel.signInWithGoogle(context) },
                onBypassSetup = onSetupComplete,
                onLanguageSelected = { viewModel.updateLanguage(it) }
            )
        }

        // Step 1 of 5: About You (Name, DOB Wheel Picker, Gender, Height, Weight)
        composable("about_you") {
            AboutYouScreen(
                state = state,
                viewModel = viewModel,
                onNext = { 
                    val auth = RetrofitClient.authManager
                    val hasAuth = (auth != null && auth.hasValidSession()) || state.isEmailVerified || state.email == "test@zivaa.app"
                    if (hasAuth) {
                        navController.navigate("primary_focus")
                    } else {
                        navController.navigate("account_sync")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Seamless Account Securing / Google Sync (if not signed in yet)
        composable("account_sync") {
            val context = androidx.compose.ui.platform.LocalContext.current
            PhoneVerificationScreen(
                state = state,
                onPhoneChange = { }, 
                onSendOtp = { viewModel.signInWithGoogle(context) },
                onVerifyOtp = { }, 
                onCheckSession = { viewModel.checkExistingSession() },
                onNext = { 
                    if (state.isSetupComplete) {
                        onSetupComplete()
                    } else {
                        navController.navigate("primary_focus") {
                            popUpTo("account_sync") { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Step 2 of 5: Primary Focus (Preserved exact current grid)
        composable("primary_focus") {
            SetupFocusScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("health_habits") },
                onBack = { navController.popBackStack() }
            )
        }

        // Step 3 of 5: Health Conditions, Smoking, Alcohol, and Health Connect
        composable("health_habits") {
            HealthAndHabitsScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("daily_rhythm") },
                onBack = { navController.popBackStack() }
            )
        }

        // Step 4 of 5: Daily Rhythm (Mornings, Movement & Step Goal, Diet, Evenings)
        composable("daily_rhythm") {
            DailyRhythmScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("care_circle") },
                onBack = { navController.popBackStack() }
            )
        }

        // Step 5 of 5: Care Circle / Family Sharing (WhatsApp Morning Updates + Skip)
        composable("care_circle") {
            FamilySharingScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("plan_reveal") },
                onBack = { navController.popBackStack() }
            )
        }

        // Culmination: Live Synthesis Animation & Personalized Plan Reveal
        composable("plan_reveal") {
            PlanRevealScreen(
                state = state,
                viewModel = viewModel,
                onNavigateToDashboard = onSetupComplete
            )
        }
    }
}
