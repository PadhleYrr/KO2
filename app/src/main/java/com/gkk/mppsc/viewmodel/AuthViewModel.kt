package com.gkk.mppsc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkk.mppsc.data.repository.AuthRepository
import com.gkk.mppsc.data.repository.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentScreen: AuthScreen = AuthScreen.LOGIN,
    val userEmail: String? = null,
    val userName: String? = null
)

enum class AuthScreen {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUIState())
    val uiState = _uiState.asStateFlow()
    
    init {
        // Listen to auth state changes
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = authState is AuthState.Authenticated
                )
            }
        }
        
        // Listen to current user changes
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    userEmail = user?.email,
                    userName = user?.displayName
                )
            }
        }
    }
    
    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        // Validate input
        when {
            email.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Email cannot be empty",
                    isLoading = false
                )
                return
            }
            password.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Password cannot be empty",
                    isLoading = false
                )
                return
            }
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            
            val result = authRepository.signIn(email, password)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    successMessage = "Sign in successful!"
                )
            }.onFailure { exception ->
                val errorMsg = when {
                    exception.message?.contains("user not found", ignoreCase = true) == true -> 
                        "No account found with this email"
                    exception.message?.contains("wrong password", ignoreCase = true) == true -> 
                        "Incorrect password"
                    exception.message?.contains("invalid email", ignoreCase = true) == true -> 
                        "Invalid email format"
                    else -> exception.message ?: "Sign in failed. Try again."
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
    
    /**
     * Sign up with email, password, and name
     */
    fun signUp(email: String, password: String, confirmPassword: String, name: String) {
        // Validate input
        when {
            name.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Name cannot be empty"
                )
                return
            }
            email.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Email cannot be empty"
                )
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Invalid email format"
                )
                return
            }
            password.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Password cannot be empty"
                )
                return
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Password must be at least 6 characters"
                )
                return
            }
            password != confirmPassword -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Passwords do not match"
                )
                return
            }
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            
            val result = authRepository.signUp(email, password, name)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    successMessage = "Account created successfully!"
                )
            }.onFailure { exception ->
                val errorMsg = when {
                    exception.message?.contains("email already", ignoreCase = true) == true -> 
                        "This email is already registered"
                    exception.message?.contains("weak password", ignoreCase = true) == true -> 
                        "Password is too weak"
                    else -> exception.message ?: "Sign up failed. Try again."
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
    
    /**
     * Reset password with email
     */
    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter your email address"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            val result = authRepository.resetPassword(email)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Password reset email sent! Check your inbox."
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to send reset email"
                )
            }
        }
    }
    
    /**
     * Switch to signup screen
     */
    fun switchToSignUp() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AuthScreen.SIGNUP,
            errorMessage = null,
            successMessage = null
        )
    }
    
    /**
     * Switch to login screen
     */
    fun switchToLogin() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AuthScreen.LOGIN,
            errorMessage = null,
            successMessage = null
        )
    }
    
    /**
     * Switch to forgot password screen
     */
    fun switchToForgotPassword() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AuthScreen.FORGOT_PASSWORD,
            errorMessage = null,
            successMessage = null
        )
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUIState(currentScreen = AuthScreen.LOGIN)
        }
    }
}
