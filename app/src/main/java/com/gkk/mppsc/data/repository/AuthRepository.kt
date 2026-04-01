package com.gkk.mppsc.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: Flow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: Flow<FirebaseUser?> = _currentUser.asStateFlow()
    
    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            _authState.value = if (firebaseAuth.currentUser != null) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
            Log.d("AuthRepo", "Auth state: ${_authState.value}, User: ${firebaseAuth.currentUser?.email}")
        }
    }
    
    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String, name: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            
            // Update user profile with name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            result.user?.updateProfile(profileUpdates)?.await()
            
            _authState.value = AuthState.Authenticated
            _currentUser.value = result.user
            Log.d("AuthRepo", "Sign up successful: ${result.user?.email}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Sign up failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            _authState.value = AuthState.Authenticated
            _currentUser.value = result.user
            Log.d("AuthRepo", "Sign in successful: ${result.user?.email}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Sign in failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Sign out
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
            _currentUser.value = null
            Log.d("AuthRepo", "Sign out successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Sign out failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d("AuthRepo", "Password reset email sent to $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Password reset failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = auth.currentUser != null
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? = auth.currentUser?.email
    
    /**
     * Get current user display name
     */
    fun getCurrentUserName(): String? = auth.currentUser?.displayName
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
}
