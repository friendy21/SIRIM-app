package com.sirimocr.app.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.sirimocr.app.data.database.SirimDatabase
import com.sirimocr.app.data.repository.SirimRepository
import com.sirimocr.app.data.repository.SyncResult
import com.sirimocr.app.firebase.FirestoreService
import com.sirimocr.app.utils.NetworkUtils

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val user = Firebase.auth.currentUser ?: return Result.success()
        return try {
            val database = SirimDatabase.getInstance(applicationContext)
            val repository = SirimRepository(
                database.sirimRecordDao(),
                FirestoreService(applicationContext),
                NetworkUtils(applicationContext)
            )
            val pull = repository.pullFromCloud(user.uid)
            val push = repository.syncPendingRecords()
            if (pull && push !is SyncResult.Error) {
                Result.success()
            } else if (push is SyncResult.NoConnection) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (t: Throwable) {
            Log.e("SyncWorker", "Sync failed", t)
            Result.failure()
        }
    }
}
