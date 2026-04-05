package dev.simonvar.photodrop.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.simonvar.photodrop.R
import dev.simonvar.photodrop.data.MediaBucket
import kotlinx.collections.immutable.PersistentSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketPickerBottomSheet(
    buckets: List<MediaBucket>,
    enabledBuckets: PersistentSet<String>,
    totalCount: Int,
    onToggleBucket: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val allSelected = enabledBuckets.size == buckets.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BucketRow(
                name = "${stringResource(R.string.bucket_all)} ($totalCount)",
                isEnabled = allSelected,
                onToggle = { onToggleBucket(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
            ) {
                items(buckets) { bucket ->
                    BucketRow(
                        name = "${bucket.name} (${bucket.count})",
                        isEnabled = bucket.name in enabledBuckets,
                        onToggle = { onToggleBucket(bucket.name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BucketRow(
    name: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(onClick = onToggle),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
        )
    }
}
