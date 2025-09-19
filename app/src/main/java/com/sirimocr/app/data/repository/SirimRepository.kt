package com.sirimocr.app.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.sirimocr.app.data.database.dao.SirimRecordDao
import com.sirimocr.app.data.database.entities.SirimRecord
import com.sirimocr.app.firebase.FirestoreService
import com.sirimocr.app.utils.NetworkUtils

class SirimRepository(
    private val sirimRecordDao: SirimRecordDao,
    private val firestoreService: FirestoreService,
    private val networkUtils: NetworkUtils
) {

    fun getAllRecords(userId: String): LiveData<List<SirimRecord>> =
        sirimRecordDao.getAllRecords(userId)

    suspend fun saveRecord(record: SirimRecord): Boolean {
        return try {
            sirimRecordDao.insertRecord(record)
            if (networkUtils.isConnected()) {
                val saved = firestoreService.saveRecord(record)
                if (saved) {
                    sirimRecordDao.markAsSynced(record.id, System.currentTimeMillis())
                }
                saved
            } else {
                true
            }
        } catch (t: Throwable) {
            Log.e("SirimRepository", "Failed to save record", t)
            false
        }
    }

    suspend fun updateRecord(record: SirimRecord): Boolean {
        return try {
            sirimRecordDao.updateRecord(record.copy(updatedAt = System.currentTimeMillis(), synced = false))
            if (networkUtils.isConnected()) {
                val updated = firestoreService.updateRecord(record)
                if (updated) {
                    sirimRecordDao.markAsSynced(record.id, System.currentTimeMillis())
                }
                updated
            } else {
                true
            }
        } catch (t: Throwable) {
            Log.e("SirimRepository", "Failed to update record", t)
            false
        }
    }

    suspend fun deleteRecord(record: SirimRecord): Boolean {
        return try {
            sirimRecordDao.deleteRecord(record)
            if (networkUtils.isConnected()) {
                firestoreService.deleteRecord(record.id)
            }
            true
        } catch (t: Throwable) {
            Log.e("SirimRepository", "Failed to delete record", t)
            false
        }
    }

    suspend fun syncPendingRecords(): SyncResult {
        if (!networkUtils.isConnected()) {
            return SyncResult.NoConnection
        }
        return try {
            val pending = sirimRecordDao.getUnsyncedRecords()
            var success = 0
            var failure = 0
            pending.forEach { record ->
                try {
                    if (firestoreService.saveRecord(record)) {
                        sirimRecordDao.markAsSynced(record.id, System.currentTimeMillis())
                        success++
                    } else {
                        failure++
                    }
                } catch (e: Exception) {
                    Log.e("SirimRepository", "Sync failed for ${record.id}", e)
                    failure++
                }
            }
            SyncResult.Complete(success, failure)
        } catch (t: Throwable) {
            Log.e("SirimRepository", "Sync failed", t)
            SyncResult.Error(t.message ?: "Sync failed")
        }
    }

    suspend fun pullFromCloud(userId: String): Boolean {
        if (!networkUtils.isConnected()) return false
        return try {
            val remoteRecords = firestoreService.getAllRecords()
            remoteRecords.forEach { cloudRecord ->
                val local = sirimRecordDao.getRecordById(cloudRecord.id)
                if (local == null) {
                    sirimRecordDao.insertRecord(cloudRecord.copy(userId = userId, synced = true))
                } else if (cloudRecord.updatedAt > local.updatedAt) {
                    sirimRecordDao.updateRecord(cloudRecord.copy(userId = userId, synced = true))
                }
            }
            true
        } catch (t: Throwable) {
            Log.e("SirimRepository", "Pull failed", t)
            false
        }
    }
}

sealed class SyncResult {
    object NoConnection : SyncResult()
    data class Complete(val successCount: Int, val failureCount: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
