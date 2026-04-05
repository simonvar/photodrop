package dev.simonvar.photodrop.presentation.home

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.simonvar.photodrop.R
import dev.simonvar.photodrop.di.LocalDepScope
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import dev.simonvar.photodrop.data.MediaItem
import dev.simonvar.photodrop.data.MediaType
import dev.simonvar.photodrop.ui.block.VideoPlayer
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

private const val SWIPE_THRESHOLD_FRACTION = 0.4f
private const val MAX_ROTATION_DEGREES = 15f
private const val FLY_OFF_DURATION_MS = 300

private val LocalizedDateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

enum class SwipeDirection { LEFT, RIGHT }

@Composable
fun SwipeCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onToggleMute: () -> Unit,
    onFavoriteChanged: (itemId: Long, isFavorite: Boolean) -> Unit,
    item: MediaItem,
    isMuted: Boolean,
    isFront: Boolean,
    modifier: Modifier = Modifier,
    backCardProgress: Float = 0f,
    programmaticSwipe: SwipeDirection? = null,
    onProgrammaticSwipeConsumed: () -> Unit = {},
    onSwipeProgress: (Float) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var cardWidth by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(isFront) {
        if (isFront) offset.snapTo(Offset.Zero)
    }

    LaunchedEffect(programmaticSwipe) {
        if (programmaticSwipe == null) return@LaunchedEffect
        val targetX =
            if (programmaticSwipe == SwipeDirection.RIGHT) cardWidth * 2f else -cardWidth * 2f
        offset.animateTo(
            Offset(targetX, 0f),
            animationSpec = tween(FLY_OFF_DURATION_MS),
        )
        if (programmaticSwipe == SwipeDirection.LEFT) onSwipeLeft() else onSwipeRight()
        offset.snapTo(Offset.Zero)
        onProgrammaticSwipeConsumed()
    }

    val progress = if (cardWidth > 0f) offset.value.x / cardWidth else 0f
    if (isFront) {
        SideEffect { onSwipeProgress(progress) }
    }
    val rotation =
        (progress * MAX_ROTATION_DEGREES).coerceIn(-MAX_ROTATION_DEGREES, MAX_ROTATION_DEGREES)

    val infoState = rememberMediaInfoBarState(item)
    var showDetails by remember { mutableStateOf(false) }
    val detailState = rememberMediaDetailState(item)

    Column(
        modifier = modifier
            .onSizeChanged { cardWidth = it.width.toFloat() }
            .graphicsLayer {
                if (isFront) {
                    translationX = offset.value.x
                    translationY = offset.value.y
                    rotationZ = rotation
                } else {
                    val factor = abs(backCardProgress).coerceIn(0f, 1f)
                    scaleX = 0.9f + 0.1f * factor
                    scaleY = 0.9f + 0.1f * factor
                    alpha = 0.6f + 0.4f * factor
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(
                if (isFront) {
                    Modifier.pointerInput(item.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offset.snapTo(
                                        Offset(
                                            offset.value.x + dragAmount.x,
                                            offset.value.y + dragAmount.y,
                                        )
                                    )
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val swipedRight =
                                        offset.value.x > cardWidth * SWIPE_THRESHOLD_FRACTION
                                    val swipedLeft =
                                        offset.value.x < -cardWidth * SWIPE_THRESHOLD_FRACTION

                                    if (swipedRight || swipedLeft) {
                                        val targetX =
                                            if (swipedRight) cardWidth * 2f else -cardWidth * 2f
                                        offset.animateTo(
                                            Offset(targetX, offset.value.y),
                                            animationSpec = tween(FLY_OFF_DURATION_MS),
                                        )
                                        if (swipedLeft) onSwipeLeft() else onSwipeRight()
                                        offset.snapTo(Offset.Zero)
                                    } else {
                                        offset.animateTo(
                                            Offset.Zero,
                                            animationSpec = spring(),
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    offset.animateTo(Offset.Zero, animationSpec = spring())
                                }
                            },
                        )
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        // Media content + swipe overlays
        Box(modifier = Modifier.weight(1f)) {
            if (item.mediaType == MediaType.VIDEO && isFront) {
                VideoPlayer(
                    uri = item.uri,
                    isMuted = isMuted,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ZoomableAsyncImage(
                    model = item.uri,
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Swipe overlays (front only)
            if (isFront) {
                val absProgress = abs(progress)
                if (progress > 0.05f) {
                    Icon(
                        painter = painterResource(R.drawable.ic_expand_circle_right_24),
                        contentDescription = stringResource(R.string.keep),
                        tint = Color.Green.copy(alpha = (absProgress * 2f).coerceAtMost(1f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                            .graphicsLayer { scaleX = 2f; scaleY = 2f },
                    )
                }
                if (progress < -0.05f) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.Red.copy(alpha = (absProgress * 2f).coerceAtMost(1f)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .graphicsLayer { scaleX = 2f; scaleY = 2f },
                    )
                }
            }
        }

        // Info bar below media
        MediaInfoBar(
            state = infoState,
            isMuted = isMuted,
            onToggleMute = onToggleMute,
            onDateClick = if (isFront) {
                { showDetails = true }
            } else null,
        )
    }

    val activity = LocalActivity.current!!
    val mediaRepository = LocalDepScope.current.mediaRepository
    var currentFavoriteState by remember(item.id) { mutableStateOf(item.isFavorite) }

    val favoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentFavoriteState = !currentFavoriteState
            onFavoriteChanged(item.id, currentFavoriteState)
        }
    }

    val adjustedDetailState = remember(detailState, currentFavoriteState) {
        detailState.copy(isFavorite = currentFavoriteState)
    }

    if (showDetails) {
        MediaDetailBottomSheet(
            state = adjustedDetailState,
            onDismiss = { showDetails = false },
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = item.mimeType
                    putExtra(Intent.EXTRA_STREAM, item.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, null))
            },
            onFavoriteToggle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                {
                    val request = mediaRepository.createFavoriteRequest(
                        activity, listOf(item.uri), !currentFavoriteState
                    )
                    if (request != null) {
                        favoriteLauncher.launch(request)
                    }
                }
            } else null,
        )
    }
}

@Immutable
private data class MediaInfoBarState(
    val formattedDate: String,
    val formattedSize: String,
    val sizeMb: Double,
    val mediaType: MediaType,
)

@Immutable
private data class MediaDetailState(
    val formattedDate: String,
    val displayName: String,
    val formattedSize: String,
    val mimeType: String,
    val resolution: String,
    val bucketName: String,
    val orientation: String,
    val isFavorite: Boolean,
    val formattedDuration: String,
    val mediaType: MediaType,
)

@Composable
private fun rememberMediaInfoBarState(item: MediaItem): MediaInfoBarState {
    val res = LocalResources.current
    return remember(item) {
        val javaLocalDate = item.takenAt
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
            .toJavaLocalDate()
        val formattedDate = javaLocalDate.format(LocalizedDateFormat)
        val sizeMb = item.size / 1_048_576.0
        val formattedSize = res.getString(R.string.size_mb, sizeMb)
        MediaInfoBarState(formattedDate, formattedSize, sizeMb, item.mediaType)
    }
}

@Composable
private fun rememberMediaDetailState(item: MediaItem): MediaDetailState {
    val res = LocalResources.current
    return remember(item) {
        val javaLocalDate = item.takenAt
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
            .toJavaLocalDate()
        val formattedDate = javaLocalDate.format(LocalizedDateFormat)
        val sizeMb = item.size / 1_048_576.0
        val formattedSize = if (sizeMb >= 1024) {
            res.getString(R.string.size_gb, sizeMb / 1024)
        } else {
            res.getString(R.string.size_mb, sizeMb)
        }
        val resolution = if (item.width > 0 && item.height > 0) {
            res.getString(R.string.resolution_format, item.width, item.height)
        } else ""
        val bucket = item.bucketName.ifEmpty { res.getString(R.string.label_unknown) }
        val orientation = if (item.mediaType == MediaType.IMAGE && item.orientation != 0) {
            "${item.orientation}\u00B0"
        } else ""
        val formattedDuration =
            if (item.mediaType == MediaType.VIDEO && item.duration.inWholeSeconds > 0) {
                val totalSec = item.duration.inWholeSeconds
                res.getString(R.string.duration_format, totalSec / 60, totalSec % 60)
            } else ""
        MediaDetailState(
            formattedDate = formattedDate,
            displayName = item.displayName,
            formattedSize = formattedSize,
            mimeType = item.mimeType.ifEmpty { res.getString(R.string.label_unknown) },
            resolution = resolution,
            bucketName = bucket,
            orientation = orientation,
            isFavorite = item.isFavorite,
            formattedDuration = formattedDuration,
            mediaType = item.mediaType,
        )
    }
}

@Composable
private fun MediaInfoBar(
    state: MediaInfoBarState,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onDateClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.formattedDate,
            style = MaterialTheme.typography.bodySmall,
            color = if (onDateClick != null) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            modifier = if (onDateClick != null) Modifier.clickable(onClick = onDateClick) else Modifier,
        )
        if (state.mediaType == MediaType.VIDEO) {
            IconButton(onClick = onToggleMute) {
                Icon(
                    painter =
                        if (isMuted) painterResource(R.drawable.ic_volume_off_24)
                        else painterResource(R.drawable.ic_volume_up_24),
                    contentDescription = stringResource(if (isMuted) R.string.unmute else R.string.mute),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            text = state.formattedSize,
            style = MaterialTheme.typography.bodySmall,
            color = if (state.sizeMb > 50) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaDetailBottomSheet(
    state: MediaDetailState,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onFavoriteToggle: (() -> Unit)?,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.media_details),
                    style = MaterialTheme.typography.titleLarge,
                )
                Row {
                    if (onFavoriteToggle != null) {
                        IconButton(onClick = onFavoriteToggle) {
                            Icon(
                                painter = painterResource(
                                    if (state.isFavorite) R.drawable.ic_favorite_24
                                    else R.drawable.ic_favorite_border_24
                                ),
                                contentDescription = stringResource(
                                    if (state.isFavorite) R.string.remove_from_favorites
                                    else R.string.add_to_favorites
                                ),
                            )
                        }
                    }
                    IconButton(onClick = onShare) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share_24),
                            contentDescription = stringResource(R.string.share),
                        )
                    }
                }
            }
            DetailCell(
                label = stringResource(R.string.label_file_name),
                value = state.displayName,
            )
            DetailRow(
                label1 = stringResource(R.string.label_date),
                value1 = state.formattedDate,
            )
            DetailRow(
                label1 = stringResource(R.string.label_size),
                value1 = state.formattedSize,
                label2 = stringResource(R.string.label_type),
                value2 = state.mimeType,
            )
            DetailRow(
                label1 = stringResource(R.string.label_resolution),
                value1 = state.resolution.ifEmpty { stringResource(R.string.label_unknown) },
                label2 = stringResource(R.string.label_album),
                value2 = state.bucketName,
            )
            if (state.orientation.isNotEmpty()) {
                DetailRow(
                    label1 = stringResource(R.string.label_orientation),
                    value1 = state.orientation,
                    label2 = stringResource(R.string.label_favorite),
                    value2 = stringResource(if (state.isFavorite) R.string.label_yes else R.string.label_no),
                )
            } else {
                DetailRow(
                    label1 = stringResource(R.string.label_favorite),
                    value1 = stringResource(if (state.isFavorite) R.string.label_yes else R.string.label_no),
                    label2 = if (state.formattedDuration.isNotEmpty()) stringResource(R.string.label_duration) else null,
                    value2 = state.formattedDuration.ifEmpty { null },
                )
            }
            if (state.orientation.isNotEmpty() && state.formattedDuration.isNotEmpty()) {
                DetailRow(
                    label1 = stringResource(R.string.label_duration),
                    value1 = state.formattedDuration,
                    label2 = null,
                    value2 = null,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label1: String,
    value1: String,
    label2: String? = null,
    value2: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DetailCell(
            label = label1,
            value = value1,
            modifier = Modifier.weight(1f),
        )
        if (label2 != null && value2 != null) {
            DetailCell(
                label = label2,
                value = value2,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DetailCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
