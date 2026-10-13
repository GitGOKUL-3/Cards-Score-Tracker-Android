package com.cardscoretracker.pro.auth

import android.app.Application

/**
 * Backward-compatible reference to [com.cardscoretracker.pro.ui.auth.AuthViewModel].
 */
class AuthViewModel @JvmOverloads constructor(
    application: Application,
    repository: AuthRepository = FirebaseAuthRepository()
) : com.cardscoretracker.pro.ui.auth.AuthViewModel(application, repository)
