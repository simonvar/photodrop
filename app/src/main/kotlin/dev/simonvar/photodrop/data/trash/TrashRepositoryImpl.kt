package dev.simonvar.photodrop.data.trash

import dev.simonvar.photodrop.data.MediaItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrashRepositoryImpl : TrashRepository {

    private val _items = MutableStateFlow(persistentListOf<MediaItem>())
    override val items: Flow<List<MediaItem>> = _items.asStateFlow()

    override fun add(item: MediaItem) {
        _items.update { it.add(item) }
    }

    override fun remove(item: MediaItem) {
        _items.update { it.remove(item) }
    }

    override fun clear() {
        _items.update { persistentListOf() }
    }
}