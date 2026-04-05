package dev.simonvar.photodrop.data.media

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
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
        val projection = buildImageProjection()

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val orientationCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)
            val favoriteCol = favoriteColumnIndex(cursor)

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
                        takenAt = parseTakenAt(cursor, dateTakenCol, dateAddedCol),
                        size = cursor.getLong(sizeCol),
                        width = cursor.getInt(widthCol),
                        height = cursor.getInt(heightCol),
                        mimeType = cursor.getString(mimeCol) ?: "",
                        bucketName = cursor.getString(bucketCol) ?: "",
                        orientation = cursor.getInt(orientationCol),
                        isFavorite = favoriteCol != -1 && cursor.getInt(favoriteCol) != 0,
                    )
                )
            }
        }
        return items
    }

    private fun queryVideos(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = buildVideoProjection()

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val favoriteCol = favoriteColumnIndex(cursor)

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
                        takenAt = parseTakenAt(cursor, dateTakenCol, dateAddedCol),
                        duration = cursor.getLong(durCol).milliseconds,
                        size = cursor.getLong(sizeCol),
                        width = cursor.getInt(widthCol),
                        height = cursor.getInt(heightCol),
                        mimeType = cursor.getString(mimeCol) ?: "",
                        bucketName = cursor.getString(bucketCol) ?: "",
                        isFavorite = favoriteCol != -1 && cursor.getInt(favoriteCol) != 0,
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
        context.contentResolver
            .query(imageUri, buildImageProjection(), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                    val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                    val favoriteCol = favoriteColumnIndex(cursor)
                    return MediaItem(
                        id = id,
                        uri = imageUri,
                        mediaType = MediaType.IMAGE,
                        displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
                        takenAt = parseTakenAt(cursor, dateTakenCol, dateAddedCol),
                        size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
                        width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)),
                        height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)),
                        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)) ?: "",
                        bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)) ?: "",
                        orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)),
                        isFavorite = favoriteCol != -1 && cursor.getInt(favoriteCol) != 0,
                    )
                }
            }

        // Try videos
        val videoUri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
        )
        context.contentResolver
            .query(videoUri, buildVideoProjection(), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                    val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    val favoriteCol = favoriteColumnIndex(cursor)
                    return MediaItem(
                        id = id,
                        uri = videoUri,
                        mediaType = MediaType.VIDEO,
                        displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)),
                        takenAt = parseTakenAt(cursor, dateTakenCol, dateAddedCol),
                        duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)).milliseconds,
                        size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)),
                        width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)),
                        height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)),
                        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)) ?: "",
                        bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)) ?: "",
                        isFavorite = favoriteCol != -1 && cursor.getInt(favoriteCol) != 0,
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

    private fun buildImageProjection(): Array<String> = buildList {
        add(MediaStore.Images.Media._ID)
        add(MediaStore.Images.Media.DISPLAY_NAME)
        add(MediaStore.Images.Media.DATE_TAKEN)
        add(MediaStore.Images.Media.DATE_ADDED)
        add(MediaStore.Images.Media.SIZE)
        add(MediaStore.Images.Media.WIDTH)
        add(MediaStore.Images.Media.HEIGHT)
        add(MediaStore.Images.Media.MIME_TYPE)
        add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        add(MediaStore.Images.Media.ORIENTATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            add(MediaStore.Images.Media.IS_FAVORITE)
        }
    }.toTypedArray()

    private fun buildVideoProjection(): Array<String> = buildList {
        add(MediaStore.Video.Media._ID)
        add(MediaStore.Video.Media.DISPLAY_NAME)
        add(MediaStore.Video.Media.DATE_TAKEN)
        add(MediaStore.Video.Media.DATE_ADDED)
        add(MediaStore.Video.Media.DURATION)
        add(MediaStore.Video.Media.SIZE)
        add(MediaStore.Video.Media.WIDTH)
        add(MediaStore.Video.Media.HEIGHT)
        add(MediaStore.Video.Media.MIME_TYPE)
        add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            add(MediaStore.Video.Media.IS_FAVORITE)
        }
    }.toTypedArray()

    private fun parseTakenAt(cursor: Cursor, dateTakenCol: Int, dateAddedCol: Int): Instant {
        val dateTakenMs = cursor.getLong(dateTakenCol)
        return if (dateTakenMs != 0L) {
            Instant.fromEpochMilliseconds(dateTakenMs)
        } else {
            Instant.fromEpochSeconds(cursor.getLong(dateAddedCol))
        }
    }

    private fun favoriteColumnIndex(cursor: Cursor): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            cursor.getColumnIndex(MediaStore.MediaColumns.IS_FAVORITE)
        } else {
            -1
        }
    }
}
