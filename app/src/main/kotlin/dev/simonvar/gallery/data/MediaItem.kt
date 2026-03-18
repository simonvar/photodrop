package dev.simonvar.gallery.data

import android.net.Uri

enum class MediaType { IMAGE, VIDEO }

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val mediaType: MediaType,
    val displayName: String,
    val dateAdded: Long,
    val duration: Long = 0,
    val size: Long = 0,
)
