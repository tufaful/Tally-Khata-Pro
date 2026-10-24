package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = KhataEntity::class,
            parentColumns = ["id"],
            childColumns = ["khataId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["khataId"]),
        Index(value = ["customerId"]),
        Index(value = ["date"]),
        Index(value = ["isDeleted"]),
        Index(value = ["khataId", "isDeleted", "date"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val khataId: Long,
    val customerId: Long,
    val type: String, // "CREDIT" (পাওনা / বাকি বিক্রি) or "PAYMENT" (জমা / গ্রহণ)
    val amount: Long, // Exact amount in Taka
    val previousBalance: Long = 0L,
    val newBalance: Long = 0L,
    val quantity: Double? = null,
    val unitPrice: Long? = null,
    val discount: Long? = null,
    val note: String = "",
    val category: String = "",
    val attachmentUri: String? = null,
    val date: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
