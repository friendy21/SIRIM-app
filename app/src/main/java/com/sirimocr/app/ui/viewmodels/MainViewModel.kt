package com.sirimocr.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sirimocr.app.firebase.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val authService: AuthService) : ViewModel() {

    private val _authState = MutableStateFlow(authService.currentUser() != null)
    val authState: StateFlow<Boolean> = _authState

    fun refreshAuthState() {
        _authState.value = authService.currentUser() != null
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _authState.value = false
        }
    }
}

class MainViewModelFactory(private val authService: AuthService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
