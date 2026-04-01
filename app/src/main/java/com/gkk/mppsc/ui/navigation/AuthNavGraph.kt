package com.gkk.mppsc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gkk.mppsc.data.repository.AuthRepository
import com.gkk.mppsc.ui.screens.auth.ForgotPasswordScreen
import com.gkk.mppsc.ui.screens.auth.LoginScreen
import com.gkk.mppsc.ui.screens.auth.SignUpScreen
import com.gkk.mppsc.viewmodel.AuthScreen
import com.gkk.mppsc.viewmodel.AuthViewModel

/**
 * Authentication Navigation Graph
 * Handles Login, Signup, and Forgot Password screens
 * 
 * Usage:
 * val authViewModel = AuthViewModel(authRepository)
 * AuthNavGraph(navController, authViewModel) { /* onAuthSuccess */ }
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState = authViewModel.uiState.collectAsState().value
    
    // Listen for successful authentication
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = AuthScreen.LOGIN.name
    ) {
        // Login Screen
        composable(AuthScreen.LOGIN.name) {
            LoginScreen(
                onSignInClick = { email, password ->
                    authViewModel.signIn(email, password)
                },
                onSignUpClick = {
                    authViewModel.switchToSignUp()
                    navController.navigate(AuthScreen.SIGNUP.name) {
                        popUpTo(AuthScreen.LOGIN.name) { saveState = true }
                        launchSingleTop = true
                    }
                },
                onForgotPasswordClick = {
                    authViewModel.switchToForgotPassword()
                    navController.navigate(AuthScreen.FORGOT_PASSWORD.name) {
                        launchSingleTop = true
                    }
                },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                successMessage = uiState.successMessage
            )
        }
        
        // Sign Up Screen
        composable(AuthScreen.SIGNUP.name) {
            SignUpScreen(
                onSignUpClick = { email, password, confirmPassword, name ->
                    authViewModel.signUp(email, password, confirmPassword, name)
                },
                onSignInClick = {
                    authViewModel.switchToLogin()
                    navController.popBackStack()
                },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage
            )
        }
        
        // Forgot Password Screen
        composable(AuthScreen.FORGOT_PASSWORD.name) {
            ForgotPasswordScreen(
                onResetClick = { email ->
                    authViewModel.resetPassword(email)
                },
                onBackClick = {
                    authViewModel.switchToLogin()
                    navController.popBackStack()
                },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                successMessage = uiState.successMessage
            )
        }
    }
}

// Navigation routes
object AuthRoutes {
    const val LOGIN = "auth_login"
    const val SIGNUP = "auth_signup"
    const val FORGOT_PASSWORD = "auth_forgot_password"
}
