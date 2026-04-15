package com.jimmy.photocleaner.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jimmy.photocleaner.data.dao.OperationHistoryDao
import com.jimmy.photocleaner.data.model.OperationHistory

@Database(
    entities = [OperationHistory::class],
    version = 1,
    exportSchema = false
)
abstract class PhotoCleanerDatabase : RoomDatabase() {
    abstract fun operationHistoryDao(): OperationHistoryDao
}