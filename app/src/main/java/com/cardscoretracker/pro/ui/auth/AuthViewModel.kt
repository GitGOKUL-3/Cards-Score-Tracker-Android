package com.cardscoretracker.pro.ui.auth

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cardscoretracker.pro.R
import com.cardscoretracker.pro.auth.AuthRepository
import com.cardscoretracker.pro.auth.AuthState
import com.cardscoretracker.pro.auth.FirebaseAuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing user authentication state, email/password login,
 * and Google Sign-In using Android's Credential Manager API.
 */
open class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AuthRepository = FirebaseAuthRepository()
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(
        repository.currentUser?.let { AuthState.LoggedIn(it) } ?: AuthState.LoggedOut
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authState.collect { state ->
                if (_authState.value !is AuthState.Loading || state is AuthState.LoggedIn) {
                    _authState.value = state
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.login(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.LoggedIn(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Login failed")
                }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.register(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.LoggedIn(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Registration failed")
                }
        }
    }

    /**
     * Initiates Google Sign-In using Android's Credential Manager API.
     * Exchanges GoogleIdTokenCredential with GoogleAuthProvider.getCredential()
     * and authenticates into FirebaseAuth. Gracefully handles cancellation without crashing.
     */
    fun signInWithGoogle(context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = context.getString(R.string.default_web_client_id)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    repository.signInWithCredential(authCredential)
                        .onSuccess { user ->
                            _authState.value = AuthState.LoggedIn(user)
                        }
                        .onFailure { error ->
                            _authState.value = AuthState.Error(error.localizedMessage ?: "Google sign-in failed")
                        }
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type returned")
                }
            } catch (e: GetCredentialCancellationException) {
                // User cancelled or dismissed sign-in bottom sheet gracefully without crashing or showing error banner
                _authState.value = repository.currentUser?.let { AuthState.LoggedIn(it) } ?: AuthState.LoggedOut
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google sign-in failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.LoggedOut
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = repository.currentUser?.let { AuthState.LoggedIn(it) } ?: AuthState.LoggedOut
        }
    }
}
