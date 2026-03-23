package dev.simonvar.photodrop.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.simonvar.photodrop.R
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import dev.simonvar.photodrop.data.MediaItem
import dev.simonvar.photodrop.data.MediaType
import dev.simonvar.photodrop.ui.block.VideoPlayer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD_FRACTION = 0.4f
private const val MAX_ROTATION_DEGREES = 15f
private const val FLY_OFF_DURATION_MS = 300

enum class SwipeDirection { LEFT, RIGHT }

@Composable
fun SwipeCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onToggleMute: () -> Unit,
    item: MediaItem,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    programmaticSwipe: SwipeDirection? = null,
    onProgrammaticSwipeConsumed: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var cardWidth by remember { mutableFloatStateOf(1f) }

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
    val rotation =
        (progress * MAX_ROTATION_DEGREES).coerceIn(-MAX_ROTATION_DEGREES, MAX_ROTATION_DEGREES)

    val infoState = rememberMediaInfoBarState(item)

    Column(
        modifier = modifier
            .onSizeChanged { cardWidth = it.width.toFloat() }
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .rotate(rotation)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .pointerInput(item.id) {
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
                            val swipedRight = offset.value.x > cardWidth * SWIPE_THRESHOLD_FRACTION
                            val swipedLeft = offset.value.x < -cardWidth * SWIPE_THRESHOLD_FRACTION

                            if (swipedRight || swipedLeft) {
                                val targetX = if (swipedRight) cardWidth * 2f else -cardWidth * 2f
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
            },
    ) {
        // Media content + swipe overlays
        Box(modifier = Modifier.weight(1f)) {
            if (item.mediaType == MediaType.VIDEO) {
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

            // Swipe overlays
            val absProgress = abs(progress)
            if (progress > 0.05f) {
                Icon(
                    imageVector = Icons.Default.Check,
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
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.delete),
                    tint = Color.Red.copy(alpha = (absProgress * 2f).coerceAtMost(1f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .graphicsLayer { scaleX = 2f; scaleY = 2f },
                )
            }
        }

        // Info bar below media
        MediaInfoBar(
            state = infoState,
            isMuted = isMuted,
            onToggleMute = onToggleMute,
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

@Composable
private fun rememberMediaInfoBarState(item: MediaItem): MediaInfoBarState {
    val context = LocalContext.current
    return remember(item) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(item.dateAdded * 1000))
        val sizeMb = item.size / 1_048_576.0
        val formattedSize = context.getString(R.string.size_mb, sizeMb)
        MediaInfoBarState(formattedDate, formattedSize, sizeMb, item.mediaType)
    }
}

@Composable
private fun MediaInfoBar(
    state: MediaInfoBarState,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
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
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (state.mediaType == MediaType.VIDEO) {
            IconButton(onClick = onToggleMute) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff
                        else Icons.AutoMirrored.Filled.VolumeUp,
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
