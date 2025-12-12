package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDataAggregate
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.priceColor
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.features.perpetual.views.models.color
import com.gemwallet.features.perpetual.views.models.text
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.PerpetualDirection

@Composable
fun PerpetualPositionItem(
    data: PerpetualPositionDataAggregate,
    modifier: Modifier = Modifier,
    listPosition: ListPosition = ListPosition.Single,
) {
    ListItem(
        modifier = modifier,//onClick?.let { modifier.clickable({ onClick(data.perpetualId) }) } ?: modifier,
        listPosition = listPosition,
        leading = @Composable { IconWithBadge(data.asset) },
        title = @Composable { ListItemTitleText(data.name) },
        subtitle = { Text(data.direction.text(data.leverage), color = data.direction.color()) },
        trailing = {
            Column(
                modifier = Modifier.defaultMinSize(40.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(paddingHalfSmall)
            ) {
                ListItemTitleText(data.marginAmount, color = MaterialTheme.colorScheme.onSurface)
                ListItemSupportText(data.pnlWithPercentage, color = priceColor(data.pnlState))
            }
        },
    )
}

@Preview
@Composable
private fun PerpetualPositionLongItemPreview() {
    val sampleAsset = Asset(
        id = AssetId(Chain.Bitcoin),
        name = "Bitcoin",
        symbol = "BTC",
        decimals = 8,
        type = AssetType.NATIVE
    )

    val sampleData = object : PerpetualPositionDataAggregate {
        override val positionId: String = "pos_btc_001"
        override val perpetualId: String = "BTC-PERP"
        override val asset: Asset = sampleAsset
        override val name: String = "BTC"
        override val direction: PerpetualDirection = PerpetualDirection.Long
        override val leverage: Int = 40
        override val marginAmount: String = "$1,000.00"
        override val pnlWithPercentage: String = "+$1,250.00 (+12.50%)"
        override val pnlState: PriceState = PriceState.Up
    }

    WalletTheme {
        PerpetualPositionItem(data = sampleData)
    }
}

@Preview
@Composable
private fun PerpetualPositionShortItemPreview() {
    val sampleAsset = Asset(
        id = AssetId(Chain.Bitcoin),
        name = "Bitcoin",
        symbol = "BTC",
        decimals = 8,
        type = AssetType.NATIVE
    )

    val sampleData = object : PerpetualPositionDataAggregate {
        override val positionId: String = "pos_btc_001"
        override val perpetualId: String = "BTC-PERP"
        override val asset: Asset = sampleAsset
        override val name: String = "BTC"
        override val direction: PerpetualDirection = PerpetualDirection.Short
        override val leverage: Int = 40
        override val marginAmount: String = "$1,000.00"
        override val pnlWithPercentage: String = "-$1,250.00 (+12.50%)"
        override val pnlState: PriceState = PriceState.Down
    }

    WalletTheme {
        PerpetualPositionItem(data = sampleData)
    }
}