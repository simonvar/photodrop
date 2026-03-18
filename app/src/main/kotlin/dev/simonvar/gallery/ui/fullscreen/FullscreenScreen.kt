package dev.simonvar.gallery.ui.fullscreen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.simonvar.gallery.data.MediaType
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage

@Composable
fun FullscreenScreen(
    onBack: () -> Unit,
    viewModel: FullscreenViewModel = viewModel(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            viewModel.isLoading -> {
                CircularProgressIndicator(color = Color.White)
            }

            viewModel.item != null -> {
                val item = viewModel.item!!
                if (item.mediaType == MediaType.VIDEO) {
                    FullscreenVideoPlayer(
                        uri = item.uri,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ZoomableAsyncImage(
                        model = item.uri,
                        contentDescription = item.displayName,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, MaterialTheme.colorScheme.outline),
                    )
                }
            }
        }

        // Close button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape,
                )
                .size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
            )
        }
    }
}
