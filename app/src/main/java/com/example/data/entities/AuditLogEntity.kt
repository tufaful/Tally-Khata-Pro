package com.example.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["entityType"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val khataId: Long? = null,
    val action: String,
    val entityType: String,
    val entityId: Long? = null,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
