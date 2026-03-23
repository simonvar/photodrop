package dev.simonvar.photodrop.presentation.home

import dev.simonvar.photodrop.arch.ActionScope
import dev.simonvar.photodrop.arch.UiAction
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
        scope.currentState.currentItem?.let { item ->
            dependencies.trashManager.add(item)
            scope.setState {
                copy(
                    currentIndex = currentIndex + 1,
                    history = history + HistoryEntry(ActionType.TRASH, item),
                )
            }
        }
    }
}

class SwipeRightAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        scope.currentState.currentItem?.let { item ->
            scope.setState {
                copy(
                    currentIndex = currentIndex + 1,
                    history = history + HistoryEntry(ActionType.SKIP, item),
                )
            }
        }
    }
}

class UndoAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        val current = scope.currentState
        val lastEntry = current.history.lastOrNull() ?: return
        if (lastEntry.type == ActionType.TRASH) {
            dependencies.trashManager.remove(lastEntry.item)
        }
        scope.setState {
            copy(
                currentIndex = currentIndex - 1,
                history = history.dropLast(1),
            )
        }
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
