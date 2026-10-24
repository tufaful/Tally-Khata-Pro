package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entities.BackupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupDao {
    @Query("SELECT * FROM backup_records ORDER BY createdAt DESC")
    fun getAllBackups(): Flow<List<BackupEntity>>

    @Query("SELECT * FROM backup_records ORDER BY createdAt DESC")
    suspend fun getAllBackupsSync(): List<BackupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupEntity): Long

    @Delete
    suspend fun deleteBackup(backup: BackupEntity)

    @Query("DELETE FROM backup_records WHERE id = :id")
    suspend fun deleteBackupById(id: Long)
}
