package com.zivaa.app.presentation.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SetupNavHost(
    onSetupComplete: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: SetupViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            val context = androidx.compose.ui.platform.LocalContext.current
            WelcomeScreen(
                state = state,
                onNext = { navController.navigate("namedob") },
                onSignIn = { viewModel.signInWithGoogle(context) },
                onBypassSetup = onSetupComplete
            )
        }
        composable("namedob") {
            NameDobScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("phone") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("phone") {
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
                        navController.navigate("health_connect")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("primary_focus") {
            SetupFocusScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("mornings") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("mornings") {
            SetupMorningsScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("movement") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("movement") {
            SetupMovementScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("diet") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("diet") {
            SetupDietScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("metrics") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("metrics") {
            SetupMetricsScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("conditions") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("conditions") {
            HealthConditionsScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("evenings") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("evenings") {
            SetupEveningsScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("reminders") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("reminders") {
            SetupRemindersScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("family") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("health_connect") {
            SetupHealthConnectScreen(
                state = state,
                viewModel = viewModel,
                onNext = { navController.navigate("primary_focus") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("family") {
            FamilySharingScreen(
                state = state,
                viewModel = viewModel,
                onNext = { 
                    viewModel.finishSetup()
                    navController.navigate("success") 
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("success") {
            SetupSuccessScreen(
                state = state,
                onNavigateToDashboard = {
                    if (!state.isSetupComplete && !state.isSubmitting) {
                        viewModel.finishSetup()
                    }
                    onSetupComplete()
                }
            )
        }
    }
}
