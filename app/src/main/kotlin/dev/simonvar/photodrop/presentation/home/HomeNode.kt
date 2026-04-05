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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.simonvar.photodrop.R
import dev.simonvar.photodrop.di.LocalDepScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeNode(
    onNavigateToTrash: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SwipeViewModel = viewModel(
        factory = SwipeViewModel.factory(
            mediaRepository = LocalDepScope.current.mediaRepository,
            trashRepository = LocalDepScope.current.trashRepository,
        ),
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBucketPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            HomeTopBar(
                buckets = state.buckets,
                enabledBuckets = state.enabledBuckets,
                totalCount = state.allItems.size,
                filteredCount = state.items.size,
                trashCount = state.trashCount,
                favoritesCount = state.favoritesCount,
                onTitleClick = { showBucketPicker = true },
                onNavigateToTrash = onNavigateToTrash,
                onNavigateToFavorites = onNavigateToFavorites,
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
                    var swipeProgress by remember { mutableFloatStateOf(0f) }
                    val isSlotAFront = state.currentIndex % 2 == 0

                    val slotAItem = if (isSlotAFront) state.currentItem else state.nextItem
                    val slotBItem = if (isSlotAFront) state.nextItem else state.currentItem

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            slotAItem?.let { item ->
                                key("slotA") {
                                    SwipeCard(
                                        item = item,
                                        isFront = isSlotAFront,
                                        backCardProgress = if (!isSlotAFront) swipeProgress else 0f,
                                        onSwipeLeft = viewModel::onSwipeLeft,
                                        onSwipeRight = viewModel::onSwipeRight,
                                        onSwipeProgress = { swipeProgress = it },
                                        isMuted = state.isMuted,
                                        onToggleMute = viewModel::toggleMute,
                                        onFavoriteChanged = viewModel::onFavoriteChanged,
                                        programmaticSwipe = if (isSlotAFront) programmaticSwipe else null,
                                        onProgrammaticSwipeConsumed = { programmaticSwipe = null },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .zIndex(if (isSlotAFront) 1f else 0f),
                                    )
                                }
                            }

                            slotBItem?.let { item ->
                                key("slotB") {
                                    SwipeCard(
                                        item = item,
                                        isFront = !isSlotAFront,
                                        backCardProgress = if (isSlotAFront) swipeProgress else 0f,
                                        onSwipeLeft = viewModel::onSwipeLeft,
                                        onSwipeRight = viewModel::onSwipeRight,
                                        onSwipeProgress = { swipeProgress = it },
                                        isMuted = state.isMuted,
                                        onToggleMute = viewModel::toggleMute,
                                        onFavoriteChanged = viewModel::onFavoriteChanged,
                                        programmaticSwipe = if (!isSlotAFront) programmaticSwipe else null,
                                        onProgrammaticSwipeConsumed = { programmaticSwipe = null },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .zIndex(if (!isSlotAFront) 1f else 0f),
                                    )
                                }
                            }
                        }

                        ActionButtonsRow(
                            onDelete = { programmaticSwipe = SwipeDirection.LEFT },
                            onUndo = viewModel::onUndo,
                            onKeep = { programmaticSwipe = SwipeDirection.RIGHT },
                            canUndo = state.canUndo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }

    if (showBucketPicker) {
        BucketPickerBottomSheet(
            buckets = state.buckets,
            enabledBuckets = state.enabledBuckets,
            totalCount = state.allItems.size,
            onToggleBucket = viewModel::toggleBucket,
            onDismiss = { showBucketPicker = false },
        )
    }
}
