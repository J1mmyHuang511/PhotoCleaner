package com.jimmy.photocleaner.data.repository

import android.content.Context
import android.provider.MediaStore
import com.jimmy.photocleaner.data.dao.OperationHistoryDao
import com.jimmy.photocleaner.data.model.OperationHistory
import com.jimmy.photocleaner.data.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PhotoRepository(
    private val context: Context,
    private val historyDao: OperationHistoryDao
) {
    suspend fun getAllPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<Photo>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.DATA
        )
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val pathCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val uri = "content://media/external/images/media/$id"
                photos.add(Photo(id, uri, it.getString(nameCol), it.getLong(sizeCol), it.getLong(dateCol), it.getString(pathCol)))
            }
        }
        photos
    }

    suspend fun deletePhoto(photo: Photo): Boolean = withContext(Dispatchers.IO) {
        historyDao.insert(OperationHistory(photoUri = photo.uri, photoName = photo.name, photoPath = photo.path, operation = "DELETED", timestamp = System.currentTimeMillis()))
        true
    }

    suspend fun keepPhoto(photo: Photo) = withContext(Dispatchers.IO) {
        historyDao.insert(OperationHistory(photoUri = photo.uri, photoName = photo.name, photoPath = photo.path, operation = "KEPT", timestamp = System.currentTimeMillis()))
    }

    suspend fun undoLastOperation(): OperationHistory? = withContext(Dispatchers.IO) { historyDao.getLastOperation() }
    suspend fun getStatistics(): Pair<Int, Int> = withContext(Dispatchers.IO) { Pair(historyDao.getDeletedCount(), historyDao.getKeptCount()) }
    suspend fun getAllHistory(): List<OperationHistory> = withContext(Dispatchers.IO) { historyDao.getAllHistory() }
    suspend fun clearHistory() = withContext(Dispatchers.IO) { historyDao.clearAll() }

    // 清除数据库里的已删除记录
    suspend fun clearDeletedRecords() = withContext(Dispatchers.IO) { historyDao.clearDeletedHistory() }

    // 老手机的直接删除方法
    suspend fun deletePermanentlyLegacy(paths: List<String>) = withContext(Dispatchers.IO) {
        paths.forEach { path -> try { File(path).delete() } catch(e: Exception) {} }
        historyDao.clearDeletedHistory()
    }
}