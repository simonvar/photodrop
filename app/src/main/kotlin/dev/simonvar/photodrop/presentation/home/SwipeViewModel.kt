package dev.simonvar.photodrop.presentation.home

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.simonvar.photodrop.arch.SailViewModel
import dev.simonvar.photodrop.data.MediaRepositoryImpl
import dev.simonvar.photodrop.data.trash.TrashRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SwipeViewModel(
    application: Application,
    trashRepository: TrashRepository,
) : SailViewModel<SwipeState, SwipeEvent>(SwipeState()) {

    private val repository = MediaRepositoryImpl(application)

    override val dependencies = SwipeDependencies(
        coroutineScope = viewModelScope,
        repository = repository,
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

    companion object {
        fun factory(
            application: Application,
            trashRepository: TrashRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { SwipeViewModel(application, trashRepository) }
        }
    }
}
