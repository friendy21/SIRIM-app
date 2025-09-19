package com.sirimocr.app.firebase

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sirimocr.app.data.database.entities.SirimRecord
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Date

class FirestoreService(private val context: Context) {

    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    suspend fun saveRecord(record: SirimRecord): Boolean {
        val uid = Firebase.auth.currentUser?.uid ?: return false
        return try {
            val imageUrl = record.imagePath?.let { uploadImage(uid, record.id, it) }
            val payload = hashMapOf(
                "id" to record.id,
                "sirimSerialNo" to record.sirimSerialNo,
                "batchNo" to record.batchNo,
                "brandTrademark" to record.brandTrademark,
                "model" to record.model,
                "type" to record.type,
                "rating" to record.rating,
                "packSize" to record.packSize,
                "imageUrl" to imageUrl,
                "confidenceScore" to record.confidenceScore,
                "createdAt" to Timestamp(Date(record.createdAt)),
                "updatedAt" to Timestamp(Date(record.updatedAt)),
                "validationStatus" to record.validationStatus,
                "deviceId" to deviceId(),
                "metadata" to hashMapOf(
                    "ocrEngine" to "mlkit",
                    "deviceModel" to Build.MODEL,
                    "processingTime" to 0
                )
            )
            firestore.collection("users").document(uid)
                .collection("records").document(record.id)
                .set(payload).await()
            true
        } catch (t: Throwable) {
            Log.e("FirestoreService", "Save record failed", t)
            false
        }
    }

    suspend fun updateRecord(record: SirimRecord): Boolean {
        val uid = Firebase.auth.currentUser?.uid ?: return false
        return try {
            val updates = hashMapOf<String, Any>(
                "sirimSerialNo" to record.sirimSerialNo,
                "batchNo" to (record.batchNo ?: ""),
                "brandTrademark" to (record.brandTrademark ?: ""),
                "model" to (record.model ?: ""),
                "type" to (record.type ?: ""),
                "rating" to (record.rating ?: ""),
                "packSize" to (record.packSize ?: ""),
                "confidenceScore" to record.confidenceScore,
                "updatedAt" to Timestamp(Date(record.updatedAt)),
                "validationStatus" to record.validationStatus
            )
            firestore.collection("users").document(uid)
                .collection("records").document(record.id)
                .update(updates).await()
            true
        } catch (t: Throwable) {
            Log.e("FirestoreService", "Update record failed", t)
            false
        }
    }

    suspend fun deleteRecord(recordId: String): Boolean {
        val uid = Firebase.auth.currentUser?.uid ?: return false
        return try {
            firestore.collection("users").document(uid)
                .collection("records").document(recordId)
                .delete().await()
            runCatching {
                storage.reference.child("users/$uid/images/$recordId.jpg").delete().await()
            }
            true
        } catch (t: Throwable) {
            Log.e("FirestoreService", "Delete record failed", t)
            false
        }
    }

    suspend fun getAllRecords(): List<SirimRecord> {
        val uid = Firebase.auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("records")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { document ->
                try {
                    SirimRecord(
                        id = document.getString("id") ?: return@mapNotNull null,
                        sirimSerialNo = document.getString("sirimSerialNo") ?: return@mapNotNull null,
                        batchNo = document.getString("batchNo"),
                        brandTrademark = document.getString("brandTrademark"),
                        model = document.getString("model"),
                        type = document.getString("type"),
                        rating = document.getString("rating"),
                        packSize = document.getString("packSize"),
                        imagePath = document.getString("imageUrl"),
                        confidenceScore = document.getDouble("confidenceScore")?.toFloat() ?: 0f,
                        createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        updatedAt = document.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        synced = true,
                        syncTimestamp = System.currentTimeMillis(),
                        userId = uid,
                        validationStatus = document.getString("validationStatus") ?: "pending"
                    )
                } catch (t: Throwable) {
                    Log.e("FirestoreService", "Failed to parse record", t)
                    null
                }
            }
        } catch (t: Throwable) {
            Log.e("FirestoreService", "Fetch records failed", t)
            emptyList()
        }
    }

    private suspend fun uploadImage(userId: String, recordId: String, path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val storageRef = storage.reference.child("users/$userId/images/$recordId.jpg")
            storageRef.putFile(Uri.fromFile(file)).await()
            storageRef.downloadUrl.await().toString()
        } catch (t: Throwable) {
            Log.e("FirestoreService", "Upload failed", t)
            null
        }
    }

    private fun deviceId(): String {
        return runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull() ?: "unknown_device"
    }
}
