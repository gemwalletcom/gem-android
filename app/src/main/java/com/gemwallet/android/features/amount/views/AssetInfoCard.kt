package com.gemwallet.android.features.amount.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.ListItemSupportText
import com.gemwallet.android.ui.components.ListItemTitle
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.image.getSupportIconUrl
import com.wallet.core.primitives.Asset

@Composable
fun AssetInfoCard(
    asset: Asset,
    availableAmount: String,
    onMaxAmount: () -> Unit,
) {
    Container {
        ListItem(
            icon = asset.getIconUrl(),
            supportIcon = asset.getSupportIconUrl(),
            placeholder = asset.type.string,
            dividerShowed = false,
            trailing = {
                Button(
                    onClick = onMaxAmount,
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = stringResource(id = R.string.transfer_max))
                }
            }
        ) {
            ListItemTitle(
                title = asset.name,
                subtitle = { ListItemSupportText(stringResource(id = R.string.transfer_balance, availableAmount)) },
            )
        }
    }
}