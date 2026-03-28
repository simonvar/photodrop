package dev.simonvar.photodrop.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.simonvar.photodrop.R
import dev.simonvar.photodrop.di.LocalDepScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNode(
    onNavigateToTrash: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SwipeViewModel = viewModel(
        factory = SwipeViewModel.factory(
            mediaRepository = LocalDepScope.current.mediaRepository,
            trashRepository = LocalDepScope.current.trashRepository,
        ),
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gallery_cleanup_title)) },
                actions = {
                    IconButton(onClick = onNavigateToTrash) {
                        BadgedBox(
                            badge = {
                                if (state.trashCount > 0) {
                                    Badge { Text("${state.trashCount}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.trash),
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
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.isEmpty -> {
                    Text(
                        text = stringResource(R.string.no_more_media),
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
                        state.currentItem?.let { item ->
                            key(item.id) {
                                SwipeCard(
                                    item = item,
                                    onSwipeLeft = viewModel::onSwipeLeft,
                                    onSwipeRight = viewModel::onSwipeRight,
                                    isMuted = state.isMuted,
                                    onToggleMute = viewModel::toggleMute,
                                    programmaticSwipe = programmaticSwipe,
                                    onProgrammaticSwipeConsumed = { programmaticSwipe = null },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(16.dp),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = { programmaticSwipe = SwipeDirection.LEFT }) {
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                            IconButton(
                                onClick = viewModel::onUndo,
                                enabled = state.canUndo,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = stringResource(R.string.action_undo),
                                )
                            }
                            TextButton(onClick = { programmaticSwipe = SwipeDirection.RIGHT }) {
                                Text(
                                    text = stringResource(R.string.action_keep),
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
