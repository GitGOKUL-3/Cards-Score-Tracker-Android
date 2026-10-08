package com.cardscoretracker.pro.auth

import com.google.firebase.auth.FirebaseUser

/**
 * Represents the current authentication state of the application.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data class LoggedIn(val user: FirebaseUser) : AuthState
    data object LoggedOut : AuthState
    data class Error(val message: String) : AuthState
}
