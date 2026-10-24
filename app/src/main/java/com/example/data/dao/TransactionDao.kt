package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE khataId = :khataId AND isDeleted = 0 ORDER BY date DESC, id DESC")
    fun getTransactionsForKhata(khataId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE khataId = :khataId AND isDeleted = 0 ORDER BY date DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(khataId: Long, limit: Int = 15): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND isDeleted = 0 ORDER BY date DESC, id DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE khataId = :khataId AND isDeleted = 0 AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsInRange(khataId: Long, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE khataId = :khataId AND isDeleted = 0 AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getTransactionsInRangeSync(khataId: Long, startDate: Long, endDate: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getTrashTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
    suspend fun getTotalActiveTransactionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteTransaction(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restoreTransaction(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun permanentlyDeleteTransaction(id: Long)

    @Query("DELETE FROM transactions WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("SELECT * FROM transactions WHERE isDeleted = 0")
    suspend fun getAllActiveTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>
}
