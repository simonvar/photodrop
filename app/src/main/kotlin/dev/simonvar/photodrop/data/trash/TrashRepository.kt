package dev.simonvar.photodrop.data.trash

import dev.simonvar.photodrop.data.MediaItem
import kotlinx.coroutines.flow.Flow

interface TrashRepository {

    val items: Flow<List<MediaItem>>

    fun add(item: MediaItem)

    fun remove(item: MediaItem)

    fun clear()
}