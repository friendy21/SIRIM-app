package com.sirimocr.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sirimocr.app.data.database.entities.SirimRecord

@Dao
interface SirimRecordDao {
    @Query("SELECT * FROM sirim_records WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllRecords(userId: String): LiveData<List<SirimRecord>>

    @Query("SELECT * FROM sirim_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<SirimRecord>

    @Query("SELECT * FROM sirim_records WHERE sirim_serial_no = :serialNo")
    suspend fun getRecordBySerial(serialNo: String): SirimRecord?

    @Query("SELECT * FROM sirim_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: String): SirimRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SirimRecord)

    @Update
    suspend fun updateRecord(record: SirimRecord)

    @Query("UPDATE sirim_records SET synced = 1, sync_timestamp = :timestamp WHERE id = :recordId")
    suspend fun markAsSynced(recordId: String, timestamp: Long)

    @Delete
    suspend fun deleteRecord(record: SirimRecord)

    @Query("DELETE FROM sirim_records WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}
