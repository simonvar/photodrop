package dev.simonvar.gallery.ui.swipe

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.simonvar.gallery.data.MediaItem
import dev.simonvar.gallery.data.MediaRepository
import dev.simonvar.gallery.data.TrashManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    private var allItems: List<MediaItem> = emptyList()

    var currentIndex by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(true)
        private set

    val currentItem: MediaItem?
        get() = allItems.getOrNull(currentIndex)

    val isEmpty: Boolean
        get() = !isLoading && currentIndex >= allItems.size

    var isMuted by mutableStateOf(true)
        private set

    val trashCount: Int
        get() = TrashManager.items.size

    fun toggleMute() {
        isMuted = !isMuted
    }

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            isLoading = true
            allItems = withContext(Dispatchers.IO) {
                repository.loadAllMedia()
            }
            isLoading = false
        }
    }

    fun onSwipeLeft() {
        currentItem?.let { TrashManager.add(it) }
        advance()
    }

    fun onSwipeRight() {
        advance()
    }

    private fun advance() {
        currentIndex++
    }
}
