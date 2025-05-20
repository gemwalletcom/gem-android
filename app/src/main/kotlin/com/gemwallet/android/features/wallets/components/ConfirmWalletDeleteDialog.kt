package com.gemwallet.android.features.wallets.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R

@Composable
fun ConfirmWalletDeleteDialog(
    walletName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(stringResource(R.string.common_warning))
        },
        text = {
            Text(
                text = stringResource(R.string.common_delete_confirmation, walletName),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors().copy(contentColor = MaterialTheme.colorScheme.error),
                onClick = onConfirm,
            ) {
                Text(text = stringResource(id = R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss)  {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}