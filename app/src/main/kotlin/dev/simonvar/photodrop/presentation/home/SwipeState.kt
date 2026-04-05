package dev.simonvar.photodrop.presentation.home

import dev.simonvar.photodrop.arch.ViewState
import dev.simonvar.photodrop.data.MediaBucket
import dev.simonvar.photodrop.data.MediaItem
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class SwipeState(
    val allItems: List<MediaItem> = emptyList(),
    val items: List<MediaItem> = emptyList(),
    val buckets: List<MediaBucket> = emptyList(),
    val enabledBuckets: PersistentSet<String> = persistentSetOf(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val isMuted: Boolean = true,
    val trashCount: Int = 0,
    val favoritesCount: Int = 0,
    val history: List<HistoryEntry> = emptyList(),
) : ViewState {

    val currentItem: MediaItem?
        get() = items.getOrNull(currentIndex)

    val nextItem: MediaItem?
        get() = items.getOrNull(currentIndex + 1)

    val isEmpty: Boolean
        get() = !isLoading && currentIndex >= items.size

    val canUndo: Boolean
        get() = history.isNotEmpty()
}
