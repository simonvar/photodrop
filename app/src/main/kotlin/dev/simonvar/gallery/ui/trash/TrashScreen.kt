package dev.simonvar.gallery.ui.trash

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import dev.simonvar.gallery.data.MediaItem
import dev.simonvar.gallery.data.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    viewModel: TrashViewModel = viewModel(),
) {
    val activity = LocalContext.current as Activity

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onDeleteConfirmed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val count = viewModel.items.size
                    val totalBytes = viewModel.items.sumOf { it.size }
                    val totalMb = totalBytes / 1_048_576.0
                    val sizeText = if (totalMb >= 1024) {
                        String.format(java.util.Locale.US, "%.1f GB", totalMb / 1024)
                    } else {
                        String.format(java.util.Locale.US, "%.1f MB", totalMb)
                    }
                    Text(buildAnnotatedString {
                        append("Trash ($count) — ")
                        withStyle(SpanStyle(color = if (totalMb > 50) Color.Red else Color.Unspecified)) {
                            append(sizeText)
                        }
                    })
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (viewModel.items.isNotEmpty()) {
                        IconButton(onClick = {
                            val request = viewModel.createDeleteAllRequest(activity)
                            if (request != null) {
                                deleteLauncher.launch(request)
                            } else {
                                viewModel.onDeleteConfirmed()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete All",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (viewModel.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Trash is empty",
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
                    items = viewModel.items.toList(),
                    key = { it.id },
                ) { item ->
                    TrashCell(
                        item = item,
                        onRestore = { viewModel.restoreItem(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrashCell(
    item: MediaItem,
    onRestore: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (item.mediaType == MediaType.VIDEO) {
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        IconButton(
            onClick = onRestore,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Icon(
                imageVector = Icons.Default.RestoreFromTrash,
                contentDescription = "Restore",
                tint = Color.White,
            )
        }
    }
}
