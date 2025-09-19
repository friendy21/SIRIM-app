package com.sirimocr.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sirimocr.app.data.database.converters.Converters
import com.sirimocr.app.data.database.dao.SirimRecordDao
import com.sirimocr.app.data.database.dao.UserDao
import com.sirimocr.app.data.database.entities.SirimRecord
import com.sirimocr.app.data.database.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SirimRecord::class, User::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SirimDatabase : RoomDatabase() {

    abstract fun sirimRecordDao(): SirimRecordDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: SirimDatabase? = null

        fun getInstance(context: Context): SirimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SirimDatabase::class.java,
                    "sirim_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.execSQL("CREATE INDEX IF NOT EXISTS index_sirim_records_user_id ON sirim_records(user_id)")
                            db.execSQL("CREATE INDEX IF NOT EXISTS index_sirim_records_synced ON sirim_records(synced)")
                            db.execSQL("CREATE INDEX IF NOT EXISTS index_sirim_records_serial_no ON sirim_records(sirim_serial_no)")
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
