package com.jimmy.photocleaner.di

import android.content.Context
import androidx.room.Room
import com.jimmy.photocleaner.data.dao.OperationHistoryDao
import com.jimmy.photocleaner.data.database.PhotoCleanerDatabase
import com.jimmy.photocleaner.data.repository.PhotoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1. 教 Hilt 如何创建数据库
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhotoCleanerDatabase {
        return Room.databaseBuilder(
            context,
            PhotoCleanerDatabase::class.java,
            "photo_cleaner_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // 2. 教 Hilt 如何提供 Dao（数据访问对象）
    @Provides
    @Singleton
    fun provideOperationHistoryDao(database: PhotoCleanerDatabase): OperationHistoryDao {
        return database.operationHistoryDao()
    }

    // 3. 教 Hilt 如何创建 PhotoRepository
    @Provides
    @Singleton
    fun providePhotoRepository(
        @ApplicationContext context: Context,
        dao: OperationHistoryDao
    ): PhotoRepository {
        return PhotoRepository(context, dao)
    }
}