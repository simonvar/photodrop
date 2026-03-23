package dev.simonvar.photodrop.presentation.home

import dev.simonvar.photodrop.data.MediaItem

enum class ActionType { TRASH, SKIP }

data class HistoryEntry(val type: ActionType, val item: MediaItem)
