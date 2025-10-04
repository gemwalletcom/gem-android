package com.gemwallet.android.ui.components.list_item.property

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.getBalanceInfo
import com.gemwallet.android.ui.models.BalanceInfoUIModel
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.Asset

@Composable
fun PropertyAssetInfoItem(
    asset: Asset,
    availableAmount: String,
    onMaxAmount: () -> Unit,
) {
    ListItem(
        leading = { IconWithBadge(asset) },
        title = { ListItemTitleText(asset.name) },
        subtitle = { ListItemSupportText(stringResource(id = R.string.transfer_balance, availableAmount)) },
        listPosition = ListPosition.Single,
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
    )
}

@Composable
fun PropertyAssetBalanceItem(
    model: BalanceInfoUIModel,
    title: String?,
    listPosition: ListPosition = ListPosition.Single,
) {
    ListItem(
        leading = { IconWithBadge(model.asset) },
        title = { ListItemTitleText(title ?: model.asset.name) },
        listPosition = listPosition,
        trailing = {
            getBalanceInfo(model, model)()
        }
    )
}