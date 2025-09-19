package com.sirimocr.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.sirimocr.app.data.database.SirimDatabase
import com.sirimocr.app.data.repository.SirimRepository
import com.sirimocr.app.firebase.AuthService
import com.sirimocr.app.firebase.FirestoreService
import com.sirimocr.app.utils.ExportUtils
import com.sirimocr.app.utils.NetworkUtils
import com.sirimocr.app.utils.SecurePreferences
import com.sirimocr.app.work.SyncManager

class SirimOcrApplication : Application(), Configuration.Provider {

    val database: SirimDatabase by lazy { SirimDatabase.getInstance(this) }
    val authService: AuthService by lazy { AuthService() }
    val firestoreService: FirestoreService by lazy { FirestoreService(this) }
    val networkUtils: NetworkUtils by lazy { NetworkUtils(this) }
    val repository: SirimRepository by lazy {
        SirimRepository(database.sirimRecordDao(), firestoreService, networkUtils)
    }
    val exportUtils: ExportUtils by lazy { ExportUtils(this) }
    val securePreferences: SecurePreferences by lazy { SecurePreferences(this) }
    val syncManager: SyncManager by lazy { SyncManager(this) }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        syncManager.startPeriodicSync()
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
