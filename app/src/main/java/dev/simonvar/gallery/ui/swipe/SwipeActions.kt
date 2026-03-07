package dev.simonvar.gallery.ui.swipe

import dev.simonvar.gallery.arch.ActionScope
import dev.simonvar.gallery.arch.UiAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadMediaAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        scope.setState { copy(isLoading = true) }
        val items = withContext(Dispatchers.IO) {
            dependencies.repository.loadAllMedia()
        }
        scope.setState { copy(items = items, isLoading = false) }
    }
}

class SwipeLeftAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        scope.currentState.currentItem?.let { dependencies.trashManager.add(it) }
        scope.setState { copy(currentIndex = currentIndex + 1) }
    }
}

class SwipeRightAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        scope.setState { copy(currentIndex = currentIndex + 1) }
    }
}

class ToggleMuteAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        scope.setState { copy(isMuted = !isMuted) }
    }
}
