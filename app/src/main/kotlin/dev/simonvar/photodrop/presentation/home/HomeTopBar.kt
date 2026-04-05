package dev.simonvar.photodrop.presentation.home

import android.os.Build
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.simonvar.photodrop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    trashCount: Int,
    favoritesCount: Int,
    onNavigateToTrash: () -> Unit,
    onNavigateToFavorites: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.gallery_cleanup_title)) },
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
