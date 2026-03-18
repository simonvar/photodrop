package dev.simonvar.gallery.ui.trash

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.simonvar.gallery.data.MediaItem
import dev.simonvar.gallery.data.MediaRepository
import dev.simonvar.gallery.data.TrashManager

class TrashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    val items: List<MediaItem>
        get() = TrashManager.items

    fun restoreItem(item: MediaItem) {
        TrashManager.remove(item)
    }

    fun createDeleteAllRequest(activity: Activity) =
        repository.createDeleteRequest(activity, TrashManager.items.map { it.uri })

    fun onDeleteConfirmed() {
        TrashManager.clear()
    }
}
