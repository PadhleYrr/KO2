package com.gkk.mppsc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gkk.mppsc.data.repository.AuthRepository
import com.gkk.mppsc.ui.navigation.AuthNavGraph
import com.gkk.mppsc.ui.screens.DashboardScreen
import com.gkk.mppsc.ui.theme.GKKThemeWrapper
import com.gkk.mppsc.viewmodel.AuthViewModel
import com.gkk.mppsc.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val authRepository by lazy { AuthRepository() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GKKThemeWrapper {
                RootNavigation(authRepository)
            }
        }
    }
}

/**
 * Root Navigation - decides whether to show Auth screens or Main App
 */
@Composable
fun RootNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val authViewModel = remember { AuthViewModel(authRepository) }
    val authState = authViewModel.uiState.collectAsState().value
    
    if (authState.isAuthenticated) {
        // Show main app
        MainAppNavigation()
    } else {
        // Show auth screens
        AuthNavGraph(
            navController = navController,
            viewModel = authViewModel,
            onAuthSuccess = {
                // Auth successful - app will recompose and show main app
            }
        )
    }
}

/**
 * Main App Navigation - all the app screens after login
 */
@Composable
fun MainAppNavigation(
    mainViewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(nav = navController)
        }
        
        // Add other screens here as you build them
        // composable("test") { TestScreen(nav = navController) }
        // composable("notes") { NotesScreen(nav = navController) }
        // etc...
    }
}

/* 
SETUP INSTRUCTIONS:

1. Add AuthRepository to your data/repository/ folder
2. Add AuthViewModel to your viewmodel/ folder
3. Add LoginScreen, SignUpScreen, ForgotPasswordScreen to ui/screens/auth/
4. Add AuthNavGraph to ui/navigation/
5. Replace your MainActivity.kt with this code

6. Make sure your build.gradle.kts has:
   - Firebase Auth: implementation("com.google.firebase:firebase-auth-ktx")
   - Coroutines: implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services")

7. Test by running the app - you should see Login screen first

8. Google-services.json is already configured in your app
*/
