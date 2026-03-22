package dev.simonvar.gallery.ui.block

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier,
    isMuted: Boolean = true,
) {
    val context = LocalContext.current
    val volume = if (isMuted) 0f else 1f

    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            this.volume = volume
            prepare()
            play()
        }
    }

    LaunchedEffect(isMuted) {
        player.volume = volume
    }

    DisposableEffect(uri) {
        onDispose {
            player.release()
        }
    }

    ContentFrame(
        player = player,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
