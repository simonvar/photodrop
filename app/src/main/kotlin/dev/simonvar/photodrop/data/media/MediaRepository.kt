package dev.simonvar.photodrop.data.media

import android.app.Activity
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import dev.simonvar.photodrop.data.MediaItem

interface MediaRepository {

    fun loadAllMedia(): List<MediaItem>

    fun loadFavoriteMedia(): List<MediaItem>

    fun findItemById(id: Long): MediaItem?

    fun createDeleteRequest(activity: Activity, uris: List<Uri>): IntentSenderRequest?

    fun createFavoriteRequest(activity: Activity, uris: List<Uri>, isFavorite: Boolean): IntentSenderRequest?
}
