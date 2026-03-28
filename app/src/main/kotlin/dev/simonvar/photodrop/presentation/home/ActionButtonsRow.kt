package dev.simonvar.photodrop.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.simonvar.photodrop.R

@Composable
fun ActionButtonsRow(
    onDelete: () -> Unit,
    onUndo: () -> Unit,
    onKeep: () -> Unit,
    canUndo: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onDelete,
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_backspace_24),
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
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
            OutlinedButton(
                onClick = onKeep,
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
            ) {
                Text(
                    text = stringResource(R.string.action_keep),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Icon(
                    painter = painterResource(R.drawable.ic_expand_circle_right_24),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
