package dev.simonvar.gallery.ui.fullscreen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dev.simonvar.gallery.data.MediaItem
import dev.simonvar.gallery.data.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FullscreenViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private val itemId: Long = savedStateHandle["itemId"]!!

    var item: MediaItem? by mutableStateOf(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        viewModelScope.launch {
            isLoading = true
            item = withContext(Dispatchers.IO) {
                repository.findItemById(itemId)
            }
            isLoading = false
        }
    }
}
