package com.sirimocr.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sirim_records")
data class SirimRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "sirim_serial_no")
    val sirimSerialNo: String,
    @ColumnInfo(name = "batch_no")
    val batchNo: String? = null,
    @ColumnInfo(name = "brand_trademark")
    val brandTrademark: String? = null,
    @ColumnInfo(name = "model")
    val model: String? = null,
    @ColumnInfo(name = "type")
    val type: String? = null,
    @ColumnInfo(name = "rating")
    val rating: String? = null,
    @ColumnInfo(name = "pack_size")
    val packSize: String? = null,
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float = 0f,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "synced")
    val synced: Boolean = false,
    @ColumnInfo(name = "sync_timestamp")
    val syncTimestamp: Long? = null,
    @ColumnInfo(name = "user_id")
    val userId: String? = null,
    @ColumnInfo(name = "validation_status")
    val validationStatus: String = "pending"
)
