package com.sirimocr.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sirimocr.app.data.database.entities.SirimRecord
import com.sirimocr.app.data.model.SirimOcrResult
import com.sirimocr.app.data.repository.SirimRepository
import com.sirimocr.app.firebase.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DashboardViewModel(
    private val repository: SirimRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _ocrResult = MutableStateFlow<SirimOcrResult?>(null)
    val ocrResult: StateFlow<SirimOcrResult?> = _ocrResult

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    fun updateOcrResult(result: SirimOcrResult) {
        _ocrResult.value = result
    }

    fun saveCurrentResult(imagePath: String? = null): Boolean {
        val result = _ocrResult.value ?: return false
        val data = result.sirimData ?: return false
        val user = authService.currentUser() ?: run {
            _saveError.value = "Sign in to save records"
            return false
        }
        if (data.sirimSerialNo.isNullOrBlank()) {
            _saveError.value = "Serial number is required"
            return false

        }
        val record = SirimRecord(
            id = UUID.randomUUID().toString(),
            sirimSerialNo = data.sirimSerialNo,
            batchNo = data.batchNo,
            brandTrademark = data.brandTrademark,
            model = data.model,
            type = data.type,
            rating = data.rating,
            packSize = data.packSize,
            imagePath = imagePath,
            confidenceScore = result.confidenceScore,
            userId = user.uid,
            validationStatus = result.validationResult?.let { if (it.isValid) "validated" else "pending" } ?: "pending"
        )
        viewModelScope.launch {
            _saving.value = true
            _saveError.value = null
            val saved = repository.saveRecord(record)
            _saving.value = false
            if (!saved) {
                _saveError.value = "Unable to save record"
            }
        }
        return true
    }
}

class DashboardViewModelFactory(
    private val repository: SirimRepository,
    private val authService: AuthService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
