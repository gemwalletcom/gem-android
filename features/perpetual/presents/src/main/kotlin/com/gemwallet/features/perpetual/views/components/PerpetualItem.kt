package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDataAggregate
import com.gemwallet.android.domains.price.values.PriceableValue
import com.gemwallet.android.ui.components.image.IconWithBadge
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
    data: PerpetualDataAggregate,
    modifier: Modifier = Modifier.Companion,
    listPosition: ListPosition = ListPosition.Single
) {
    ListItem(
        modifier = modifier,
        listPosition = listPosition,
        leading = @Composable { IconWithBadge(data.asset) },
        title = @Composable { ListItemTitleText(data.asset.name) },
        subtitle = if (data.price.priceValue == null || data.price.priceValue == 0.0) {
            null
        } else {
            {
                PriceInfo(
                    price = data.price.priceValueFormated,
                    changes = data.price.dayChangePercentageFormatted,
                    state = data.price.state,
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
                ListItemTitleText(data.volume, color = MaterialTheme.colorScheme.onSurface)
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
        override val icon: Any = asset
        override val price = object : PriceableValue {
            override val currency = Currency.USD
            override val priceValue = 95420.50
            override val dayChangePercentage = 2.5
        }
        override val volume = "$15.0B"
    }

    WalletTheme {
        PerpetualItem(data = sampleData)
    }
}