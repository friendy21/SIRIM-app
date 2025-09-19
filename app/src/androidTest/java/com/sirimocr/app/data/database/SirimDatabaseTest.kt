package com.sirimocr.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sirimocr.app.data.database.entities.SirimRecord
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SirimDatabaseTest {

    private lateinit var database: SirimDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SirimDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadRecord() = runBlocking {
        val record = SirimRecord(
            id = UUID.randomUUID().toString(),
            sirimSerialNo = "TA7654321",
            userId = "tester"
        )
        database.sirimRecordDao().insertRecord(record)
        val loaded = database.sirimRecordDao().getRecordById(record.id)
        assertEquals(record.sirimSerialNo, loaded?.sirimSerialNo)
    }
}
