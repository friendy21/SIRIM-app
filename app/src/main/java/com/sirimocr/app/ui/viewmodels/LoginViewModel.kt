package com.sirimocr.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sirimocr.app.firebase.AuthResult
import com.sirimocr.app.firebase.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val authenticated: Boolean = false
)

class LoginViewModel(private val authService: AuthService) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(authenticated = authService.currentUser() != null))
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val result = authService.signIn(email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(authenticated = true)
                is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val result = authService.register(email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(authenticated = true)
                is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }
}

class LoginViewModelFactory(private val authService: AuthService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
