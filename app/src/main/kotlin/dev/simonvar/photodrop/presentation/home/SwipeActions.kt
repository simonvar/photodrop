package dev.simonvar.photodrop.presentation.home

import dev.simonvar.photodrop.arch.ActionScope
import dev.simonvar.photodrop.arch.UiAction
import dev.simonvar.photodrop.data.MediaBucket
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal const val UNKNOWN_BUCKET = "Unknown"

class LoadMediaAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        scope.setState { copy(isLoading = true) }
        val allItems = withContext(Dispatchers.IO) {
            dependencies.repository.loadAllMedia()
        }
        val buckets = allItems
            .groupingBy { it.bucketName.ifEmpty { UNKNOWN_BUCKET } }
            .eachCount()
            .map { (name, count) -> MediaBucket(name, count) }
            .sortedByDescending { it.count }
        val allBucketNames = buckets.map { it.name }.toPersistentSet()
        scope.setState {
            copy(
                allItems = allItems,
                items = allItems,
                buckets = buckets,
                enabledBuckets = allBucketNames,
                currentIndex = 0,
                history = emptyList(),
                favoritesCount = allItems.count { it.isFavorite },
                isLoading = false,
            )
        }
    }
}

class ToggleBucketAction(
    private val bucketName: String?,
) : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        val current = scope.currentState
        val allBucketNames = current.buckets.map { it.name }.toPersistentSet()

        val newEnabled: PersistentSet<String> = if (bucketName == null) {
            if (current.enabledBuckets == allBucketNames) persistentSetOf() else allBucketNames
        } else {
            if (bucketName in current.enabledBuckets) {
                current.enabledBuckets.remove(bucketName)
            } else {
                current.enabledBuckets.add(bucketName)
            }
        }

        val filteredItems = if (newEnabled == allBucketNames) {
            current.allItems
        } else {
            current.allItems.filter { item ->
                val resolved = item.bucketName.ifEmpty { UNKNOWN_BUCKET }
                resolved in newEnabled
            }
        }

        scope.setState {
            copy(
                enabledBuckets = newEnabled,
                items = filteredItems,
                currentIndex = 0,
                history = emptyList(),
            )
        }
    }
}

class SwipeLeftAction : UiAction<SwipeDependencies, SwipeState, SwipeEvent> {
    override suspend fun execute(
        dependencies: SwipeDependencies,
        scope: ActionScope<SwipeState, SwipeEvent>,
    ) {
        scope.currentState.currentItem?.let { item ->
            dependencies.trashRepository.add(item)
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
            dependencies.trashRepository.remove(lastEntry.item)
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
