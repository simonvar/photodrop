package dev.simonvar.photodrop.data.media

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import dev.simonvar.photodrop.data.MediaItem
import dev.simonvar.photodrop.data.MediaType
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class MediaRepositoryImpl(private val context: Context) : MediaRepository {

    override fun loadAllMedia(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        items.addAll(queryImages())
        items.addAll(queryVideos())
        items.shuffle()
        return items
    }

    private fun queryImages(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
        )

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                items.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        mediaType = MediaType.IMAGE,
                        displayName = cursor.getString(nameCol),
                        addedAt = Instant.fromEpochSeconds(cursor.getLong(dateCol)),
                        size = cursor.getLong(sizeCol),
                    )
                )
            }
        }
        return items
    }

    private fun queryVideos(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                items.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        mediaType = MediaType.VIDEO,
                        displayName = cursor.getString(nameCol),
                        addedAt = Instant.fromEpochSeconds(cursor.getLong(dateCol)),
                        duration = cursor.getLong(durCol).milliseconds,
                        size = cursor.getLong(sizeCol),
                    )
                )
            }
        }
        return items
    }

    override fun findItemById(id: Long): MediaItem? {
        // Try images first
        val imageUri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
        )
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
        )
        context.contentResolver
            .query(imageUri, imageProjection, null, null, null)
            ?.use { cursor ->
                val dateAddedIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                if (cursor.moveToFirst()) {
                    return MediaItem(
                        id = id,
                        uri = imageUri,
                        mediaType = MediaType.IMAGE,
                        displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
                        addedAt = Instant.fromEpochSeconds(cursor.getLong(dateAddedIndex)),
                        size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
                    )
                }
            }

        // Try videos
        val videoUri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
        )
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
        )
        context.contentResolver
            .query(videoUri, videoProjection, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateAddedIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    val durationIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    return MediaItem(
                        id = id,
                        uri = videoUri,
                        mediaType = MediaType.VIDEO,
                        displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)),
                        addedAt = Instant.fromEpochSeconds(cursor.getLong(dateAddedIndex)),
                        duration = cursor.getLong(durationIndex).milliseconds,
                        size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)),
                    )
                }
            }

        return null
    }

    override fun createDeleteRequest(
        activity: Activity,
        uris: List<Uri>,
    ): IntentSenderRequest? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(
                activity.contentResolver, uris
            )
            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
        } else {
            // API 29: delete directly, handle RecoverableSecurityException
            for (uri in uris) {
                try {
                    activity.contentResolver.delete(uri, null, null)
                } catch (e: RecoverableSecurityException) {
                    val intentSender = e.userAction.actionIntent.intentSender
                    return IntentSenderRequest.Builder(intentSender).build()
                }
            }
            null
        }
    }
}
