package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuditLogDao
import com.example.data.dao.BackupDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.KhataDao
import com.example.data.dao.ReminderDao
import com.example.data.dao.TransactionDao
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.BackupEntity
import com.example.data.entities.CustomerEntity
import com.example.data.entities.KhataEntity
import com.example.data.entities.ReminderEntity
import com.example.data.entities.TransactionEntity

@Database(
    entities = [
        KhataEntity::class,
        CustomerEntity::class,
        TransactionEntity::class,
        ReminderEntity::class,
        AuditLogEntity::class,
        BackupEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun khataDao(): KhataDao
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun backupDao(): BackupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tally_khata_pro.db"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
