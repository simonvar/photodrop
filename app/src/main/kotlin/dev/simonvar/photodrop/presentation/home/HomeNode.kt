package dev.simonvar.photodrop.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
            HomeTopBar(
                trashCount = state.trashCount,
                onNavigateToTrash = onNavigateToTrash,
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

                        ActionButtonsRow(
                            onDelete = { programmaticSwipe = SwipeDirection.LEFT },
                            onUndo = viewModel::onUndo,
                            onKeep = { programmaticSwipe = SwipeDirection.RIGHT },
                            canUndo = state.canUndo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
