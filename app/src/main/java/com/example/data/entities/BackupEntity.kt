package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_records")
data class BackupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val customerCount: Int,
    val transactionCount: Int,
    val khataCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)
