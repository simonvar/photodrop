package dev.simonvar.photodrop.presentation.trash

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.simonvar.photodrop.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.simonvar.photodrop.data.MediaItem
import dev.simonvar.photodrop.data.MediaType
import dev.simonvar.photodrop.di.LocalDepScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashNode(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current!!
    val depScope = LocalDepScope.current
    val trashRepository = depScope.trashRepository
    val mediaRepository = depScope.mediaRepository
    val items by trashRepository.items.collectAsStateWithLifecycle(initialValue = emptyList())

    val animations = remember {
        listOf(
            R.raw.hero_fire_inferno,
            R.raw.hero_water_whirlpool,
            R.raw.hero_abstract_airstream,
        )
    }
    var isAnimationVisible by remember { mutableStateOf(false) }
    var animation by remember { mutableIntStateOf(animations.random()) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation))

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            trashRepository.clear()
            isAnimationVisible = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val count = items.size
                    val totalBytes = items.sumOf { it.size }
                    val totalMb = totalBytes / 1_048_576.0
                    val sizeText = if (totalMb >= 1024) {
                        stringResource(R.string.size_gb, totalMb / 1024)
                    } else {
                        stringResource(R.string.size_mb, totalMb)
                    }
                    Text(buildAnnotatedString {
                        append(stringResource(R.string.trash_title_count, count))
                        append(" — ")
                        withStyle(SpanStyle(color = if (totalMb > 50) Color.Red else Color.Unspecified)) {
                            append(sizeText)
                        }
                    })
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = {
                            val uris = items.map { it.uri }
                            val request = mediaRepository.createDeleteRequest(activity, uris)
                            if (request != null) {
                                deleteLauncher.launch(request)
                            } else {
                                trashRepository.clear()
                                animation = animations.random()
                                isAnimationVisible = true
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete_forever_24),
                                contentDescription = stringResource(R.string.delete_all),
                            )
                        }
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
                    text = stringResource(R.string.trash_empty),
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
                    TrashCell(
                        item = item,
                        onRestore = { trashRepository.remove(item) },
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }

        if (isAnimationVisible) {
            val progress by animateLottieCompositionAsState(
                composition,
                isPlaying = true,
                iterations = 1
            )

            LaunchedEffect(progress) {
                if (progress == 1f) {
                    isAnimationVisible = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    applyOpacityToLayers = true,
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

@Composable
private fun TrashCell(
    item: MediaItem,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                painter = painterResource(R.drawable.ic_video_file_24),
                contentDescription = stringResource(R.string.video),
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        IconButton(
            onClick = onRestore,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_restore_from_trash_24),
                contentDescription = stringResource(R.string.restore),
                tint = Color.White,
            )
        }
    }
}

