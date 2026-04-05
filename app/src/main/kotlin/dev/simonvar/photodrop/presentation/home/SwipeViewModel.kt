package dev.simonvar.photodrop.presentation.home

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.simonvar.photodrop.arch.SailViewModel
import dev.simonvar.photodrop.data.media.MediaRepository
import dev.simonvar.photodrop.data.trash.TrashRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SwipeViewModel(
    mediaRepository: MediaRepository,
    trashRepository: TrashRepository,
) : SailViewModel<SwipeState, SwipeEvent>(SwipeState()) {

    override val dependencies = SwipeDependencies(
        coroutineScope = viewModelScope,
        repository = mediaRepository,
        trashRepository = trashRepository,
    )

    init {
        dispatch(LoadMediaAction(), Dispatchers.IO)

        viewModelScope.launch {
            trashRepository.items
                .map { it.size }
                .collect { count -> updateState { copy(trashCount = count) } }
        }
    }

    fun onSwipeLeft() = dispatch(SwipeLeftAction(), Dispatchers.Main)

    fun onSwipeRight() = dispatch(SwipeRightAction(), Dispatchers.Main)

    fun onUndo() = dispatch(UndoAction(), Dispatchers.Main)

    fun toggleMute() = dispatch(ToggleMuteAction(), Dispatchers.Main)

    fun toggleBucket(bucketName: String? = null) = dispatch(ToggleBucketAction(bucketName), Dispatchers.Main)

    fun onFavoriteChanged(itemId: Long, isFavorite: Boolean) {
        updateState {
            val updatedAll = allItems.map { if (it.id == itemId) it.copy(isFavorite = isFavorite) else it }
            val updatedFiltered = items.map { if (it.id == itemId) it.copy(isFavorite = isFavorite) else it }
            copy(
                allItems = updatedAll,
                items = updatedFiltered,
                favoritesCount = updatedAll.count { it.isFavorite },
            )
        }
    }

    companion object {
        fun factory(
            mediaRepository: MediaRepository,
            trashRepository: TrashRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { SwipeViewModel(mediaRepository, trashRepository) }
        }
    }
}
