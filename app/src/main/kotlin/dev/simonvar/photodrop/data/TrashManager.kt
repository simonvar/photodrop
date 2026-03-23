package dev.simonvar.photodrop.data

import androidx.compose.runtime.mutableStateListOf

object TrashManager {

    val items = mutableStateListOf<MediaItem>()

    fun add(item: MediaItem) {
        items.add(item)
    }

    fun remove(item: MediaItem) {
        items.remove(item)
    }

    fun clear() {
        items.clear()
    }
}
