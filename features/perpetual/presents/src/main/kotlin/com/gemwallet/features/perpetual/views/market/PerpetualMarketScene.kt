package com.gemwallet.features.perpetual.views.market

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDataAggregate
import com.gemwallet.android.domains.perpetual.values.PerpetualBalance
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_head.AmountHeadAction
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault

@Composable
fun PerpetualMarketScene(
    balance: PerpetualBalance,
    positions: List<PerpetualDataAggregate>,
    perpetualItems: List<PerpetualDataAggregate>,
    onWithdraw: () -> Unit,
    onDeposit: () -> Unit,
    onClose: () -> Unit,
) {
    var actionFontSize by remember { mutableStateOf(16.sp) }

    Scene(
        title = stringResource(R.string.perpetuals_title),
        onClose = onClose,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                AmountListHead(
                    amount = balance.deposit,
                    equivalent = stringResource(R.string.wallet_available_balance, balance.available)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(paddingDefault),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AmountHeadAction(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.wallet_withdraw),
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "withdraw",
                            fontSize = actionFontSize,
                            onNextFontSize = {
                                if (actionFontSize > it) actionFontSize = it
                            },
                            onClick = onWithdraw,
                        )
                        AmountHeadAction(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.wallet_deposit),
                            imageVector = Icons.Default.Add,
                            contentDescription = "deposit",
                            fontSize = actionFontSize,
                            onNextFontSize = {
                                if (actionFontSize > it) actionFontSize = it
                            },
                            onClick = onDeposit,
                        )
                    }
                }
            }
            item {
                SubheaderItem(stringResource(R.string.markets_title))
            }
            itemsPositioned(perpetualItems) { position, item ->
                PerpetualItem(item, listPosition = position)
            }
        }
    }
}

@Composable
fun PerpetualItem(
    data: PerpetualDataAggregate,
    modifier: Modifier = Modifier,
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

@Composable
@Preview
fun PreviewPerpetualMarketScene() {
    WalletTheme {
        PerpetualMarketScene(
            balance = object : PerpetualBalance {
                override val deposit: String = "$50,000.00"
                override val available: String = "$45,000.00"
                override val withdrawable: String = "$42,000.00"
            },
            positions = listOf(
                object : PerpetualDataAggregate {
                    override val id: String = "BTC-PERP-POS"
                    override val name: String = "BTC/USD 10x Long"
                    override val icon: Any = "BTC"
                    override val price = object : com.gemwallet.android.domains.price.values.PriceableValue {
                        override val currency = com.wallet.core.primitives.Currency.USD
                        override val priceValue: Double = 95420.50
                        override val dayChangePercentage: Double = 2.5
                    }
                    override val volume: String = "15234567890123"
                    override val asset = com.wallet.core.primitives.Asset(
                        id = com.wallet.core.primitives.AssetId(com.wallet.core.primitives.Chain.Bitcoin),
                        name = "Bitcoin",
                        symbol = "BTC",
                        decimals = 8,
                        type = com.wallet.core.primitives.AssetType.NATIVE
                    )
                },
                object : PerpetualDataAggregate {
                    override val id: String = "ETH-PERP-POS"
                    override val name: String = "ETH/USD 5x Short"
                    override val icon: Any = "ETH"
                    override val price = object : com.gemwallet.android.domains.price.values.PriceableValue {
                        override val currency = com.wallet.core.primitives.Currency.USD
                        override val priceValue: Double = 3625.75
                        override val dayChangePercentage: Double = -1.2
                    }
                    override val volume: String = "8456789012345"
                    override val asset = com.wallet.core.primitives.Asset(
                        id = com.wallet.core.primitives.AssetId(com.wallet.core.primitives.Chain.Ethereum),
                        name = "Ethereum",
                        symbol = "ETH",
                        decimals = 18,
                        type = com.wallet.core.primitives.AssetType.NATIVE
                    )
                }
            ),
            perpetualItems = listOf(
                object : PerpetualDataAggregate {
                    override val id: String = "BTC-PERP"
                    override val name: String = "BTC/USD"
                    override val icon: Any = "BTC"
                    override val price = object : com.gemwallet.android.domains.price.values.PriceableValue {
                        override val currency = com.wallet.core.primitives.Currency.USD
                        override val priceValue: Double = 95420.50
                        override val dayChangePercentage: Double = 2.5
                    }
                    override val volume: String = "15234567890123"
                    override val asset = com.wallet.core.primitives.Asset(
                        id = com.wallet.core.primitives.AssetId(com.wallet.core.primitives.Chain.Bitcoin),
                        name = "Bitcoin",
                        symbol = "BTC",
                        decimals = 8,
                        type = com.wallet.core.primitives.AssetType.NATIVE
                    )
                },
                object : PerpetualDataAggregate {
                    override val id: String = "ETH-PERP"
                    override val name: String = "ETH/USD"
                    override val icon: Any = "ETH"
                    override val price = object : com.gemwallet.android.domains.price.values.PriceableValue {
                        override val currency = com.wallet.core.primitives.Currency.USD
                        override val priceValue: Double = 3625.75
                        override val dayChangePercentage: Double = 1.8
                    }
                    override val volume: String = "8456789012345"
                    override val asset = com.wallet.core.primitives.Asset(
                        id = com.wallet.core.primitives.AssetId(com.wallet.core.primitives.Chain.Ethereum),
                        name = "Ethereum",
                        symbol = "ETH",
                        decimals = 18,
                        type = com.wallet.core.primitives.AssetType.NATIVE
                    )
                },
                object : PerpetualDataAggregate {
                    override val id: String = "SOL-PERP"
                    override val name: String = "SOL/USD"
                    override val price = object : com.gemwallet.android.domains.price.values.PriceableValue {
                        override val currency = com.wallet.core.primitives.Currency.USD
                        override val priceValue: Double = 235.40
                        override val dayChangePercentage: Double = -0.5
                    }
                    override val volume: String = "3123847573745"
                    override val asset = com.wallet.core.primitives.Asset(
                        id = com.wallet.core.primitives.AssetId(com.wallet.core.primitives.Chain.Solana),
                        name = "Solana",
                        symbol = "SOL",
                        decimals = 9,
                        type = com.wallet.core.primitives.AssetType.NATIVE
                    )
                    override val icon: Any = asset
                },
                object : PerpetualDataAggregate {
                    override val id: String = "AVAX-PERP"
                    override val name: String = "AVAX/USD"
                    override val icon: Any = "AVAX"
                    override val price = object : com.gemwallet.android.domains.price.values.PriceableValue {
                        override val currency = com.wallet.core.primitives.Currency.USD
                        override val priceValue: Double = 41.85
                        override val dayChangePercentage: Double = 4.1
                    }
                    override val volume: String = "1234567890123"
                    override val asset = com.wallet.core.primitives.Asset(
                        id = com.wallet.core.primitives.AssetId(com.wallet.core.primitives.Chain.AvalancheC),
                        name = "Avalanche",
                        symbol = "AVAX",
                        decimals = 18,
                        type = com.wallet.core.primitives.AssetType.NATIVE
                    )
                },
                object : PerpetualDataAggregate {
                    override val id: String = "LINK-PERP"
                    override val name: String = "LINK/USD"
                    override val icon: Any = "LINK"
                    override val price = object : com.gemwallet.android.domains.price.values.PriceableValue {
                        override val currency = com.wallet.core.primitives.Currency.USD
                        override val priceValue: Double = 21.45
                        override val dayChangePercentage: Double = 2.7
                    }
                    override val volume: String = "987654321098"
                    override val asset = com.wallet.core.primitives.Asset(
                        id = com.wallet.core.primitives.AssetId(com.wallet.core.primitives.Chain.Ethereum, "0x514910771af9ca656af840dff83e8264ecf986ca"),
                        name = "Chainlink",
                        symbol = "LINK",
                        decimals = 18,
                        type = com.wallet.core.primitives.AssetType.ERC20
                    )
                }
            ),
            onWithdraw = {},
            onDeposit = {},
            onClose = {}
        )
    }
}