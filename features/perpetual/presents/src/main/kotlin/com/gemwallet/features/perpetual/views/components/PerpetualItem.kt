package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDataAggregate
import com.gemwallet.android.domains.price.values.PriceableValue
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.DropDownContextItem
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency

@Composable
fun PerpetualItem(
    item: PerpetualDataAggregate,
    modifier: Modifier = Modifier,
    listPosition: ListPosition = ListPosition.Single,
    longPressState: MutableState<String?>,
    onTogglePin: (String) -> Unit,
    onClick: (String) -> Unit,
) {
    DropDownContextItem(
        modifier = modifier,
        isExpanded = longPressState.value == item.id,
        imeCompensate = false,
        onDismiss = { longPressState.value = null },
        content = {
            PerpetualItem(
                modifier = it,
                item = item,
                listPosition = listPosition
            )
        },
        menuItems = {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = if (item.isPinned) R.string.common_unpin else R.string.common_pin)) },
                trailingIcon = {
                    if (item.isPinned) Icon(painterResource(R.drawable.keep_off), "unpin")
                    else Icon(Icons.Default.PushPin, "pin")

                },
                onClick = {
                    onTogglePin(item.id)
                    longPressState.value = null
                },
            )
        },
        onLongClick = { longPressState.value = item.id },
    ) { onClick(item.id) }
}

@Composable
fun PerpetualItem(
    item: PerpetualDataAggregate,
    modifier: Modifier = Modifier,
    listPosition: ListPosition = ListPosition.Single
) {
    ListItem(
        modifier = modifier,
        listPosition = listPosition,
        leading = @Composable { IconWithBadge(item.asset) },
        title = @Composable { ListItemTitleText(item.asset.name) },
        subtitle = if (item.price.priceValue == null || item.price.priceValue == 0.0) {
            null
        } else {
            {
                PriceInfo(
                    price = item.price.priceValueFormated,
                    changes = item.price.dayChangePercentageFormatted,
                    state = item.price.state,
                    style = MaterialTheme.typography.bodyMedium,
                    internalPadding = 4.dp
                )
            }
        },
        trailing = {
            Column(
                modifier = Modifier.defaultMinSize(40.dp),
                horizontalAlignment = Alignment.End
            ) {
                ListItemTitleText(item.volume, color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

@Preview
@Composable
private fun PerpetualItemPreview() {
    val sampleData = object : PerpetualDataAggregate {
        override val id = "BTC-PERP"
        override val name = "Bitcoin Perpetual"
        override val asset = Asset(
            id = AssetId(Chain.Bitcoin),
            name = "Bitcoin",
            symbol = "BTC",
            decimals = 8,
            type = AssetType.NATIVE
        )
        override val isPinned: Boolean = true
        override val icon: Any = asset
        override val price = object : PriceableValue {
            override val currency = Currency.USD
            override val priceValue = 95420.50
            override val dayChangePercentage = 2.5
        }
        override val volume = "$15.0B"
    }

    WalletTheme {
        PerpetualItem(item = sampleData)
    }
}