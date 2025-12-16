package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault
import com.wallet.core.primitives.PerpetualDirection

@Composable
fun PerpetualActions(
    onOpenPosition: (PerpetualDirection) -> Unit,
) {
    Row(
        modifier = Modifier.listItem().padding(paddingDefault),
        horizontalArrangement = Arrangement.spacedBy(paddingDefault),
    ) {
        Button(
            onClick = { onOpenPosition(PerpetualDirection.Long) },
            colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.tertiary),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.perpetual_long))
        }
        Button(
            onClick = { onOpenPosition(PerpetualDirection.Short) },
            colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.perpetual_short))
        }
    }
}

@Composable
internal fun PerpetualPositionActions(
    onModify: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.listItem().padding(paddingDefault),
        horizontalArrangement = Arrangement.spacedBy(paddingDefault),
    ) {
        Button(
            onClick = onModify,
            colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.perpetual_modify))
        }
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.perpetual_close_position))
        }
    }
}

@Preview
@Composable
private fun PerpetualActionsPreview() {
    WalletTheme {
        PerpetualActions(
            onOpenPosition = {},
        )
    }
}

@Preview
@Composable
private fun PositionActionsPreview() {
    WalletTheme {
        PerpetualPositionActions(
            onClose =  {},
            onModify = {}
        )
    }
}
