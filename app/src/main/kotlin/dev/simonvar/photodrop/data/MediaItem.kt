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
    val addedAt: Instant,
    val duration: Duration = Duration.ZERO,
    val size: Long = 0,
)
