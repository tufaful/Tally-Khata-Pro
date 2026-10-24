package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = KhataEntity::class,
            parentColumns = ["id"],
            childColumns = ["khataId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["khataId"]),
        Index(value = ["phone"]),
        Index(value = ["isDeleted"]),
        Index(value = ["khataId", "isDeleted"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val khataId: Long,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val businessName: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val currentBalance: Long = 0L, // Positive = Customer owes us (পাওনা), Negative = We owe customer (দেনা)
    val totalCredit: Long = 0L,
    val totalPayment: Long = 0L,
    val totalTransactions: Int = 0,
    val lastTransactionDate: Long? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
