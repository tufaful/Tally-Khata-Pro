package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.KhataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataDao {
    @Query("SELECT * FROM khatas WHERE isDeleted = 0 ORDER BY isDefault DESC, createdAt ASC")
    fun getAllActiveKhatas(): Flow<List<KhataEntity>>

    @Query("SELECT * FROM khatas WHERE id = :id LIMIT 1")
    suspend fun getKhataById(id: Long): KhataEntity?

    @Query("SELECT * FROM khatas WHERE isDefault = 1 AND isDeleted = 0 LIMIT 1")
    suspend fun getDefaultKhata(): KhataEntity?

    @Query("SELECT COUNT(*) FROM khatas WHERE isDeleted = 0")
    suspend fun getActiveKhataCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhata(khata: KhataEntity): Long

    @Update
    suspend fun updateKhata(khata: KhataEntity)

    @Query("UPDATE khatas SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteKhata(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE khatas SET isDefault = 0")
    suspend fun clearDefaultKhata()

    @Query("UPDATE khatas SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultKhata(id: Long)

    @Query("SELECT * FROM khatas")
    suspend fun getAllKhatasSync(): List<KhataEntity>
}
