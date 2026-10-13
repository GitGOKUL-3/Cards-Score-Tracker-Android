package com.cardscoretracker.pro.ui.screens

import androidx.compose.runtime.Composable
import com.cardscoretracker.pro.ui.auth.AuthScreen
import com.cardscoretracker.pro.ui.auth.AuthViewModel

/**
 * Backward-compatible entrypoint delegating to [AuthScreen].
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onLoginSuccess: () -> Unit
) {
    AuthScreen(
        authViewModel = authViewModel,
        isDarkTheme = isDarkTheme,
        onLoginSuccess = onLoginSuccess
    )
}
