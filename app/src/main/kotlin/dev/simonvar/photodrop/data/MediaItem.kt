package dev.simonvar.photodrop.data

import android.net.Uri
import kotlin.time.Duration
import kotlin.time.Instant

enum class MediaType { IMAGE, VIDEO }

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val mediaType: MediaType,
    val displayName: String,
    val takenAt: Instant,
    val duration: Duration = Duration.ZERO,
    val size: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String = "",
    val bucketName: String = "",
    val orientation: Int = 0,
    val isFavorite: Boolean = false,
)
