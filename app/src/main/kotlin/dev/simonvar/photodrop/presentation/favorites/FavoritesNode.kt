package dev.simonvar.photodrop.presentation.favorites

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.simonvar.photodrop.R
import dev.simonvar.photodrop.data.MediaItem
import dev.simonvar.photodrop.data.MediaType
import dev.simonvar.photodrop.di.LocalDepScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesNode(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current!!
    val depScope = LocalDepScope.current
    val mediaRepository = depScope.mediaRepository

    var items by remember { mutableStateOf(emptyList<MediaItem>()) }
    var pendingUnfavoriteItem by remember { mutableStateOf<MediaItem?>(null) }

    LaunchedEffect(Unit) {
        items = withContext(Dispatchers.IO) { mediaRepository.loadFavoriteMedia() }
    }

    val unfavoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingUnfavoriteItem?.let { item ->
                items = items.filter { it.id != item.id }
            }
        }
        pendingUnfavoriteItem = null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.favorites) + " (${items.size})")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.favorites_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(
                    items = items,
                    key = { it.id },
                ) { item ->
                    FavoriteCell(
                        item = item,
                        onUnfavorite = {
                            val request = mediaRepository.createFavoriteRequest(
                                activity, listOf(item.uri), false
                            )
                            if (request != null) {
                                pendingUnfavoriteItem = item
                                unfavoriteLauncher.launch(request)
                            }
                        },
                        modifier = Modifier.aspectRatio(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCell(
    item: MediaItem,
    onUnfavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(2.dp),
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (item.mediaType == MediaType.VIDEO) {
            Icon(
                painter = painterResource(R.drawable.ic_video_file_24),
                contentDescription = stringResource(R.string.video),
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        IconButton(
            onClick = onUnfavorite,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite_24),
                contentDescription = stringResource(R.string.remove_from_favorites),
                tint = Color.White,
            )
        }
    }
}
