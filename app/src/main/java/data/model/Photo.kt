package com.jimmy.photocleaner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Photo(
    val id: Long,
    val uri: String,
    val name: String,
    val size: Long,
    val dateModified: Long,
    val path: String
)

@Entity(tableName = "operation_history")
data class OperationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val photoUri: String,
    val photoName: String,
    val photoPath: String, // 👈 新增：保存真实路径
    val operation: String,
    val timestamp: Long
)