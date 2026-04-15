package com.jimmy.photocleaner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimmy.photocleaner.data.model.OperationHistory
import com.jimmy.photocleaner.data.model.Photo
import com.jimmy.photocleaner.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder(val displayName: String) {
    NEWEST("最新照片优先"),
    OLDEST("最旧照片优先"),
    LARGEST("文件最大优先"),
    RANDOM("完全随机展示")
}

@HiltViewModel
class PhotoCleanerViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoCleanerUiState>(PhotoCleanerUiState.Loading)
    val uiState: StateFlow<PhotoCleanerUiState> = _uiState

    private val _statistics = MutableStateFlow<Pair<Int, Int>>(Pair(0, 0))
    val statistics: StateFlow<Pair<Int, Int>> = _statistics

    private val _historyList = MutableStateFlow<List<OperationHistory>>(emptyList())
    val historyList: StateFlow<List<OperationHistory>> = _historyList

    private val _currentSortOrder = MutableStateFlow(SortOrder.RANDOM)
    val currentSortOrder: StateFlow<SortOrder> = _currentSortOrder

    private val _minSizeFilterMB = MutableStateFlow(0)
    val minSizeFilterMB: StateFlow<Int> = _minSizeFilterMB

    private val allPhotos = mutableListOf<Photo>()
    private val remainingPhotos = mutableListOf<Photo>()
    private val handledUris = mutableSetOf<String>()

    fun updateSettings(order: SortOrder, minSizeMB: Int) {
        _currentSortOrder.value = order
        _minSizeFilterMB.value = minSizeMB
        applySettings()
    }

    fun loadHistory() {
        viewModelScope.launch { _historyList.value = repository.getAllHistory() }
    }

    // 👇 修复点：加上了 forceRefresh 参数，如果不是强制刷新且已经有照片了，就直接 return 保护当前照片
    fun loadPhotos(forceRefresh: Boolean = false) {
        if (!forceRefresh && allPhotos.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.value = PhotoCleanerUiState.Loading
            try {
                val history = repository.getAllHistory()
                handledUris.clear()
                handledUris.addAll(history.map { it.photoUri })
                updateStatistics()

                val photos = repository.getAllPhotos().filter { it.uri !in handledUris }
                allPhotos.clear()
                allPhotos.addAll(photos)

                applySettings()
            } catch (e: Exception) {
                _uiState.value = PhotoCleanerUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    private fun applySettings() {
        remainingPhotos.clear()
        val minBytes = _minSizeFilterMB.value * 1024L * 1024L

        var list = allPhotos.filter { it.uri !in handledUris && it.size >= minBytes }

        list = when (_currentSortOrder.value) {
            SortOrder.NEWEST -> list.sortedByDescending { it.dateModified }
            SortOrder.OLDEST -> list.sortedBy { it.dateModified }
            SortOrder.LARGEST -> list.sortedByDescending { it.size }
            SortOrder.RANDOM -> list.shuffled()
        }

        remainingPhotos.addAll(list)
        showNextPhoto()
    }

    private fun showNextPhoto() {
        if (remainingPhotos.isEmpty()) {
            _uiState.value = PhotoCleanerUiState.Empty
        } else {
            val photo = remainingPhotos.first()
            _uiState.value = PhotoCleanerUiState.ShowPhoto(photo)
        }
        updateStatistics()
    }

    fun swipeLeft(photo: Photo) {
        viewModelScope.launch {
            val deleted = repository.deletePhoto(photo)
            if (deleted) {
                handledUris.add(photo.uri)
                remainingPhotos.remove(photo)
                showNextPhoto()
            }
        }
    }

    fun swipeRight(photo: Photo) {
        viewModelScope.launch {
            repository.keepPhoto(photo)
            handledUris.add(photo.uri)
            remainingPhotos.remove(photo)
            showNextPhoto()
        }
    }

    private fun updateStatistics() {
        viewModelScope.launch {
            val (deleted, kept) = repository.getStatistics()
            _statistics.value = Pair(deleted, kept)
        }
    }

    fun reset() {
        viewModelScope.launch {
            repository.clearHistory()
            handledUris.clear()
            loadPhotos(forceRefresh = true) // 强制刷新
        }
    }

    fun clearDeletedRecords() {
        viewModelScope.launch {
            repository.clearDeletedRecords()
            loadHistory()
            updateStatistics()
        }
    }

    fun deletePermanentlyLegacy(paths: List<String>) {
        viewModelScope.launch {
            repository.deletePermanentlyLegacy(paths)
            loadHistory()
            updateStatistics()
        }
    }
}

sealed class PhotoCleanerUiState {
    object Loading : PhotoCleanerUiState()
    object Empty : PhotoCleanerUiState()
    data class ShowPhoto(val photo: Photo) : PhotoCleanerUiState()
    data class Error(val message: String) : PhotoCleanerUiState()
}