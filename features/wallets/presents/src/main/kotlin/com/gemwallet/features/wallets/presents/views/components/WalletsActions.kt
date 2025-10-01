package com.gemwallet.features.wallets.presents.views.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.smallPadding

@Composable
internal fun WalletsActions(
    onCreate: () -> Unit,
    onImport: () -> Unit,
) {
    Column {
        WalletsAction(
            text = R.string.wallet_create_new_wallet,
            Icons.Default.Add,
            listPosition = ListPosition.First,
            onClick = onCreate,
        )
        WalletsAction(
            text = R.string.wallet_import_existing_wallet,
            Icons.Default.ArrowDownward,
            listPosition = ListPosition.Last,
            onClick = onImport,
        )
    }
}

@Composable
private fun WalletsAction(
    @StringRes text: Int,
    icon: ImageVector,
    listPosition: ListPosition,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .listItem(listPosition)
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .defaultPadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.smallPadding(),
            imageVector = icon,
            contentDescription = icon.name,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer8()
        Text(
            text = stringResource(id = text),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}