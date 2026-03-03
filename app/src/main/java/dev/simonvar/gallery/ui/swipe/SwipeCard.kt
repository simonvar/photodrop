package dev.simonvar.gallery.ui.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.simonvar.gallery.data.MediaItem
import dev.simonvar.gallery.data.MediaType
import dev.simonvar.gallery.ui.components.VideoPlayer
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD_FRACTION = 0.4f
private const val MAX_ROTATION_DEGREES = 15f
private const val FLY_OFF_DURATION_MS = 300

@Composable
fun SwipeCard(
    item: MediaItem,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var cardWidth by remember { mutableFloatStateOf(1f) }

    val progress = if (cardWidth > 0f) offset.value.x / cardWidth else 0f
    val rotation = (progress * MAX_ROTATION_DEGREES).coerceIn(-MAX_ROTATION_DEGREES, MAX_ROTATION_DEGREES)

    Box(
        modifier = modifier
            .onSizeChanged { cardWidth = it.width.toFloat() }
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .rotate(rotation)
            .clip(RoundedCornerShape(16.dp))
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
        // Media content
        if (item.mediaType == MediaType.VIDEO) {
            VideoPlayer(
                uri = item.uri,
                isMuted = isMuted,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model = item.uri,
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Swipe overlays
        val absProgress = abs(progress)
        if (progress > 0.05f) {
            // Swiping right — keep
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Keep",
                tint = Color.Green.copy(alpha = (absProgress * 2f).coerceAtMost(1f)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
                    .graphicsLayer { scaleX = 2f; scaleY = 2f },
            )
        }
        if (progress < -0.05f) {
            // Swiping left — delete
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.Red.copy(alpha = (absProgress * 2f).coerceAtMost(1f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .graphicsLayer { scaleX = 2f; scaleY = 2f },
            )
        }

        // Mute/unmute toggle for videos
        if (item.mediaType == MediaType.VIDEO) {
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape,
                    )
                    .size(40.dp),
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = Color.White,
                )
            }
        }
    }
}
