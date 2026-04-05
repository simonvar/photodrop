package dev.simonvar.photodrop.presentation.home

import dev.simonvar.photodrop.arch.ViewState
import dev.simonvar.photodrop.data.MediaItem

data class SwipeState(
    val items: List<MediaItem> = emptyList(),
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
