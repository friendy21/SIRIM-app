package com.sirimocr.app.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sirimocr.app.data.database.entities.SirimRecord
import com.sirimocr.app.data.repository.SirimRepository
import com.sirimocr.app.firebase.AuthService
import com.sirimocr.app.utils.ExportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class HistoryViewModel(
    private val repository: SirimRepository,
    private val authService: AuthService,
    private val exportUtils: ExportUtils
) : ViewModel() {

    private val userIdLiveData = MutableLiveData(authService.currentUser()?.uid)
    val records: LiveData<List<SirimRecord>> = Transformations.switchMap(userIdLiveData) { uid ->
        if (uid == null) MutableLiveData(emptyList()) else repository.getAllRecords(uid)
    }

    private val _exportResult = MutableStateFlow<Uri?>(null)
    val exportResult: StateFlow<Uri?> = _exportResult

    private val _exportError = MutableStateFlow<String?>(null)
    val exportError: StateFlow<String?> = _exportError

    fun refreshUser() {
        userIdLiveData.value = authService.currentUser()?.uid
    }

    fun exportCsv(records: List<SirimRecord>) {
        viewModelScope.launch {
            _exportResult.value = null
            _exportError.value = null
            val uri = exportUtils.exportToCsv(records, generateFileName())
            if (uri != null) {
                _exportResult.value = uri
            } else {
                _exportError.value = "Export failed"
            }
        }
    }

    fun exportExcel(records: List<SirimRecord>) {
        viewModelScope.launch {
            _exportResult.value = null
            _exportError.value = null
            val uri = exportUtils.exportToExcel(records, generateFileName())
            if (uri != null) {
                _exportResult.value = uri
            } else {
                _exportError.value = "Export failed"
            }
        }
    }

    fun exportPdf(records: List<SirimRecord>) {
        viewModelScope.launch {
            _exportResult.value = null
            _exportError.value = null
            val uri = exportUtils.exportToPdf(records, generateFileName())
            if (uri != null) {
                _exportResult.value = uri
            } else {
                _exportError.value = "Export failed"
            }
        }
    }

    private fun generateFileName(): String = "sirim_records_${Date().time}"

    fun clearExportState() {
        _exportResult.value = null
        _exportError.value = null
    }
}

class HistoryViewModelFactory(
    private val repository: SirimRepository,
    private val authService: AuthService,
    private val exportUtils: ExportUtils
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository, authService, exportUtils) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
