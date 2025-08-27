package com.gemwallet.features.wallets.presents.views.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.smallPadding

@Composable
internal fun WalletsActions(
    onCreate: () -> Unit,
    onImport: () -> Unit,
) {
    Container {
        Column {
            WalletsAction(
                text = R.string.wallet_create_new_wallet,
                Icons.Default.Add,
                onClick = onCreate,
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 58.dp),
                thickness = 0.4.dp
            )
            WalletsAction(
                text = R.string.wallet_import_existing_wallet,
                Icons.Default.ArrowDownward,
                onClick = onImport,
            )
        }
    }
}

@Composable
private fun WalletsAction(
    @StringRes text: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
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