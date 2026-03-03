package dev.simonvar.gallery.ui.swipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    onNavigateToTrash: () -> Unit,
    onNavigateToFullscreen: (Long) -> Unit,
    viewModel: SwipeViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery Cleanup") },
                actions = {
                    IconButton(onClick = onNavigateToTrash) {
                        BadgedBox(
                            badge = {
                                if (viewModel.trashCount > 0) {
                                    Badge { Text("${viewModel.trashCount}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Trash",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator()
                }
                viewModel.isEmpty -> {
                    Text(
                        text = "No more media to review!",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp),
                    )
                }
                else -> {
                    var programmaticSwipe by remember { mutableStateOf<SwipeDirection?>(null) }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        viewModel.currentItem?.let { item ->
                            SwipeCard(
                                item = item,
                                onSwipeLeft = viewModel::onSwipeLeft,
                                onSwipeRight = viewModel::onSwipeRight,
                                isMuted = viewModel.isMuted,
                                onToggleMute = viewModel::toggleMute,
                                onTap = { onNavigateToFullscreen(item.id) },
                                programmaticSwipe = programmaticSwipe,
                                onProgrammaticSwipeConsumed = { programmaticSwipe = null },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(16.dp),
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            TextButton(onClick = { programmaticSwipe = SwipeDirection.LEFT }) {
                                Text(
                                    text = "\u2190 Delete",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                            TextButton(onClick = { programmaticSwipe = SwipeDirection.RIGHT }) {
                                Text(
                                    text = "Keep \u2192",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
