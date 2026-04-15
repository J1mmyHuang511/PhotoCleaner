package com.jimmy.photocleaner.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jimmy.photocleaner.data.model.OperationHistory

@Dao
interface OperationHistoryDao {
    @Insert
    suspend fun insert(history: OperationHistory)

    @Query("SELECT * FROM operation_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastOperation(): OperationHistory?

    @Query("SELECT COUNT(*) FROM operation_history WHERE operation = 'DELETED'")
    suspend fun getDeletedCount(): Int

    @Query("SELECT COUNT(*) FROM operation_history WHERE operation = 'KEPT'")
    suspend fun getKeptCount(): Int

    @Query("SELECT * FROM operation_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<OperationHistory>

    @Query("DELETE FROM operation_history WHERE operation = 'DELETED'")
    suspend fun clearDeletedHistory()

    @Query("DELETE FROM operation_history")
    suspend fun clearAll()
}