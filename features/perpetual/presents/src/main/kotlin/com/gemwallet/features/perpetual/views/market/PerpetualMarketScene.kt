package com.gemwallet.features.perpetual.views.market

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDataAggregate
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDataAggregate
import com.gemwallet.android.domains.perpetual.values.PerpetualBalance
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.domains.price.values.PriceableValue
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.features.perpetual.views.components.MarketHeadActions
import com.gemwallet.features.perpetual.views.components.PerpetualItem
import com.gemwallet.features.perpetual.views.components.PerpetualPositionItem
import com.wallet.core.primitives.PerpetualDirection

@Composable
fun PerpetualMarketScene(
    balance: PerpetualBalance,
    positions: List<PerpetualPositionDataAggregate>,
    perpetuals: List<PerpetualDataAggregate>,
    onWithdraw: () -> Unit,
    onDeposit: () -> Unit,
    onClose: () -> Unit,
) {
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
                    MarketHeadActions(
                        onWithdraw = onWithdraw,
                        onDeposit = onDeposit
                    )
                }
            }
            positions.takeIf { it.isNotEmpty() }?.let {
                item { SubheaderItem(stringResource(R.string.perpetual_positions)) }
                itemsPositioned(positions) { position, item ->
                    PerpetualPositionItem(item, listPosition = position)
                }
            }
            item {
                SubheaderItem(stringResource(R.string.markets_title))
            }
            itemsPositioned(perpetuals) { position, item ->
                PerpetualItem(item, listPosition = position)
            }
        }
    }
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
                object : PerpetualPositionDataAggregate {
                    override val positionId: String = "pos_btc_001"
                    override val perpetualId: String = "BTC-PERP"
                    override val asset: Asset = Asset(
                        id = AssetId(Chain.Bitcoin),
                        name = "Bitcoin",
                        symbol = "BTC",
                        decimals = 8,
                        type = AssetType.NATIVE
                    )
                    override val name: String = "BTC/USD 10x Long"
                    override val direction: PerpetualDirection = PerpetualDirection.Long
                    override val leverage: Int = 10
                    override val marginAmount: String = "$10,000.00"
                    override val pnlWithPercentage: String = "+$1,250.00 (+12.50%)"
                    override val pnlState: PriceState = PriceState.Up
                },
                object : PerpetualPositionDataAggregate {
                    override val positionId: String = "pos_eth_002"
                    override val perpetualId: String = "ETH-PERP"
                    override val asset: Asset = Asset(
                        id = AssetId(Chain.Ethereum),
                        name = "Ethereum",
                        symbol = "ETH",
                        decimals = 18,
                        type = AssetType.NATIVE
                    )
                    override val name: String = "ETH/USD 5x Short"
                    override val direction: PerpetualDirection = PerpetualDirection.Short
                    override val leverage: Int = 20
                    override val marginAmount: String = "$5,000.00"
                    override val pnlWithPercentage: String = "-$180.00 (-3.60%)"
                    override val pnlState: PriceState = PriceState.Down
                }
            ),
            perpetuals = listOf(
                object : PerpetualDataAggregate {
                    override val id: String = "BTC-PERP"
                    override val name: String = "BTC/USD"
                    override val icon: Any = "BTC"
                    override val price = object : PriceableValue {
                        override val currency = Currency.USD
                        override val priceValue: Double = 95420.50
                        override val dayChangePercentage: Double = 2.5
                    }
                    override val volume: String = "15234567890123"
                    override val asset = Asset(
                        id = AssetId(Chain.Bitcoin),
                        name = "Bitcoin",
                        symbol = "BTC",
                        decimals = 8,
                        type = AssetType.NATIVE
                    )
                },
                object : PerpetualDataAggregate {
                    override val id: String = "ETH-PERP"
                    override val name: String = "ETH/USD"
                    override val icon: Any = "ETH"
                    override val price = object : PriceableValue {
                        override val currency = Currency.USD
                        override val priceValue: Double = 3625.75
                        override val dayChangePercentage: Double = 1.8
                    }
                    override val volume: String = "8456789012345"
                    override val asset = Asset(
                        id = AssetId(Chain.Ethereum),
                        name = "Ethereum",
                        symbol = "ETH",
                        decimals = 18,
                        type = AssetType.NATIVE
                    )
                },
                object : PerpetualDataAggregate {
                    override val id: String = "SOL-PERP"
                    override val name: String = "SOL/USD"
                    override val price = object : PriceableValue {
                        override val currency = Currency.USD
                        override val priceValue: Double = 235.40
                        override val dayChangePercentage: Double = -0.5
                    }
                    override val volume: String = "3123847573745"
                    override val asset = Asset(
                        id = AssetId(Chain.Solana),
                        name = "Solana",
                        symbol = "SOL",
                        decimals = 9,
                        type = AssetType.NATIVE
                    )
                    override val icon: Any = asset
                },
                object : PerpetualDataAggregate {
                    override val id: String = "AVAX-PERP"
                    override val name: String = "AVAX/USD"
                    override val icon: Any = "AVAX"
                    override val price = object : PriceableValue {
                        override val currency = Currency.USD
                        override val priceValue: Double = 41.85
                        override val dayChangePercentage: Double = 4.1
                    }
                    override val volume: String = "1234567890123"
                    override val asset = Asset(
                        id = AssetId(Chain.AvalancheC),
                        name = "Avalanche",
                        symbol = "AVAX",
                        decimals = 18,
                        type = AssetType.NATIVE
                    )
                },
                object : PerpetualDataAggregate {
                    override val id: String = "LINK-PERP"
                    override val name: String = "LINK/USD"
                    override val icon: Any = "LINK"
                    override val price = object : PriceableValue {
                        override val currency = Currency.USD
                        override val priceValue: Double = 21.45
                        override val dayChangePercentage: Double = 2.7
                    }
                    override val volume: String = "987654321098"
                    override val asset = Asset(
                        id = AssetId(Chain.Ethereum, "0x514910771af9ca656af840dff83e8264ecf986ca"),
                        name = "Chainlink",
                        symbol = "LINK",
                        decimals = 18,
                        type = AssetType.ERC20
                    )
                }
            ),
            onWithdraw = {},
            onDeposit = {},
            onClose = {}
        )
    }
}