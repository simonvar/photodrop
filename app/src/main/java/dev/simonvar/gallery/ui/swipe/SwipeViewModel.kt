package dev.simonvar.gallery.ui.swipe

import android.app.Application
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.simonvar.gallery.arch.SailViewModel
import dev.simonvar.gallery.data.MediaRepository
import dev.simonvar.gallery.data.TrashManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SwipeViewModel(application: Application) : SailViewModel<SwipeState, SwipeEvent>(SwipeState()) {

    private val repository = MediaRepository(application)

    override val dependencies = SwipeDependencies(
        coroutineScope = viewModelScope,
        repository = repository,
        trashManager = TrashManager,
    )

    init {
        dispatch(LoadMediaAction(), Dispatchers.IO)

        viewModelScope.launch {
            snapshotFlow { TrashManager.items.size }
                .collect { count -> updateState { copy(trashCount = count) } }
        }
    }

    fun onSwipeLeft() = dispatch(SwipeLeftAction(), Dispatchers.Main)

    fun onSwipeRight() = dispatch(SwipeRightAction(), Dispatchers.Main)

    fun toggleMute() = dispatch(ToggleMuteAction(), Dispatchers.Main)

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer { SwipeViewModel(application) }
        }
    }
}
