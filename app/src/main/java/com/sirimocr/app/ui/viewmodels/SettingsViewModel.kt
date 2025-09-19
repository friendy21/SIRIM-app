package com.sirimocr.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sirimocr.app.firebase.AuthService
import com.sirimocr.app.utils.SecurePreferences
import com.sirimocr.app.work.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authService: AuthService,
    private val securePreferences: SecurePreferences,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _autoSync = MutableStateFlow(securePreferences.getBoolean(KEY_AUTO_SYNC, true))
    val autoSync: StateFlow<Boolean> = _autoSync

    fun setAutoSync(enabled: Boolean) {
        securePreferences.putBoolean(KEY_AUTO_SYNC, enabled)
        _autoSync.value = enabled
        if (enabled) {
            syncManager.startPeriodicSync()
        } else {
            syncManager.stopPeriodicSync()
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authService.signOut()
            syncManager.stopPeriodicSync()
            onComplete()
        }
    }

    companion object {
        private const val KEY_AUTO_SYNC = "auto_sync"
    }
}

class SettingsViewModelFactory(
    private val authService: AuthService,
    private val securePreferences: SecurePreferences,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(authService, securePreferences, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
