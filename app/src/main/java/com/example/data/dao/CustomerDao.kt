package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE khataId = :khataId AND isDeleted = 0 ORDER BY lastTransactionDate DESC, name ASC")
    fun getCustomersByKhata(khataId: Long): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?>

    @Query("""
        SELECT * FROM customers 
        WHERE khataId = :khataId AND isDeleted = 0 AND 
        (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR businessName LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchCustomers(khataId: Long, query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getTrashCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers WHERE khataId = :khataId AND isDeleted = 0")
    fun getCustomerCountByKhata(khataId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE isDeleted = 0")
    suspend fun getTotalActiveCustomerCount(): Int

    @Query("SELECT SUM(currentBalance) FROM customers WHERE khataId = :khataId AND isDeleted = 0 AND currentBalance > 0")
    fun getTotalReceivable(khataId: Long): Flow<Long?>

    @Query("SELECT SUM(ABS(currentBalance)) FROM customers WHERE khataId = :khataId AND isDeleted = 0 AND currentBalance < 0")
    fun getTotalPayable(khataId: Long): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteCustomer(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restoreCustomer(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun permanentlyDeleteCustomer(id: Long)

    @Query("DELETE FROM customers WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("SELECT * FROM customers WHERE isDeleted = 0")
    suspend fun getAllActiveCustomersSync(): List<CustomerEntity>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersSync(): List<CustomerEntity>
}
