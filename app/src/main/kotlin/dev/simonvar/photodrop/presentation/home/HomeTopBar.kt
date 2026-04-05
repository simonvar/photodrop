package dev.simonvar.photodrop.presentation.home

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.simonvar.photodrop.R
import dev.simonvar.photodrop.data.MediaBucket
import kotlinx.collections.immutable.PersistentSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    buckets: List<MediaBucket>,
    enabledBuckets: PersistentSet<String>,
    totalCount: Int,
    filteredCount: Int,
    trashCount: Int,
    favoritesCount: Int,
    onTitleClick: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToFavorites: () -> Unit,
) {
    val bucketAll = stringResource(R.string.bucket_all)
    val bucketMultiple = stringResource(R.string.bucket_multiple)
    val titleText = remember(enabledBuckets, buckets, totalCount, filteredCount, bucketAll, bucketMultiple) {
        val allSelected = enabledBuckets.size == buckets.size
        when {
            allSelected -> "$bucketAll ($totalCount)"
            enabledBuckets.size == 1 -> "${enabledBuckets.first()} ($filteredCount)"
            else -> "$bucketMultiple ($filteredCount)"
        }
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.clickable(onClick = onTitleClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(titleText)
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_drop_down_24),
                    contentDescription = null,
                )
            }
        },
        actions = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                IconButton(onClick = onNavigateToFavorites) {
                    BadgedBox(
                        badge = {
                            if (favoritesCount > 0) {
                                Badge { Text("$favoritesCount") }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_favorite_24),
                            contentDescription = stringResource(R.string.favorites),
                        )
                    }
                }
            }
            IconButton(onClick = onNavigateToTrash) {
                BadgedBox(
                    badge = {
                        if (trashCount > 0) {
                            Badge { Text("$trashCount") }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = stringResource(R.string.trash),
                    )
                }
            }
        },
    )
}
