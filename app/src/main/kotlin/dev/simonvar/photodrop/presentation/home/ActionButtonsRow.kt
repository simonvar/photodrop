package dev.simonvar.photodrop.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.simonvar.photodrop.R

@Composable
fun ActionButtonsRow(
    onDelete: () -> Unit,
    onUndo: () -> Unit,
    onKeep: () -> Unit,
    canUndo: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onDelete) {
            Text(
                text = stringResource(R.string.action_delete),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        IconButton(
            onClick = onUndo,
            enabled = canUndo,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_undo_24),
                contentDescription = stringResource(R.string.action_undo),
            )
        }
        TextButton(onClick = onKeep) {
            Text(
                text = stringResource(R.string.action_keep),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
