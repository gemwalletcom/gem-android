package com.gemwallet.features.perpetual.views.position

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDetailsDataAggregate
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDetailsDataAggregate
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.features.perpetual.views.components.PerpetualActions
import com.gemwallet.features.perpetual.views.components.PerpetualPositionActions
import com.gemwallet.features.perpetual.views.components.candyChart
import com.gemwallet.features.perpetual.views.components.perpetualInfo
import com.gemwallet.features.perpetual.views.components.positionProperties
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod
import com.wallet.core.primitives.PerpetualDirection

@Composable
fun PerpetualPositionScene(
    perpetual: PerpetualDetailsDataAggregate,
    position: PerpetualPositionDetailsDataAggregate?,
    chartData: List<ChartCandleStick>,
    period: ChartPeriod,
    onChartPeriodSelect: (ChartPeriod) -> Unit,
    onClose: () -> Unit,
) {
    Scene(
        title = perpetual.name,
        onClose = onClose,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            candyChart(
                data = chartData,
                period = period,
                entry = position?.entryValue,
                liquidation = position?.liquidationValue,
                stopLoss = position?.stopLoss,
                takeProfit = position?.takeProfit,
                onPeriodSelect = onChartPeriodSelect,
            )
            positionProperties(position)
            item {
                if (position == null) {
                    PerpetualActions({}) { }
                } else {
                    PerpetualPositionActions({}) {}
                }
            }
            perpetualInfo(perpetual)
        }
    }
}

@Preview
@Composable
private fun PerpetualPositionScenePreview() {
    val sampleAsset = Asset(
        id = AssetId(Chain.Bitcoin),
        name = "Bitcoin",
        symbol = "BTC",
        decimals = 8,
        type = AssetType.NATIVE
    )

    val samplePerpetual = object : PerpetualDetailsDataAggregate {
        override val id: String = "BTC-PERP"
        override val asset: Asset = sampleAsset
        override val name: String = "Bitcoin Perpetual"
        override val dayVolume: String = "$15.00B"
        override val openInterest: String = "$2.50B"
        override val funding: String = "0.01%"
    }

    val samplePosition = object : PerpetualPositionDetailsDataAggregate {
        override val positionId: String = "pos-btc-001"
        override val perpetualId: String = "BTC-PERP"
        override val asset: Asset = sampleAsset
        override val name: String = "BTC"
        override val direction: PerpetualDirection = PerpetualDirection.Long
        override val leverage: Int = 10
        override val marginAmount: String = "$4,771.03"
        override val pnlWithPercentage: String = "+$460.25 (+9.64%)"
        override val pnlState: PriceState = PriceState.Up
        override val autoClose: String = "None"
        override val size: String = "0.5 BTC"
        override val entryPrice: String = "$94,500.00"
        override val entryValue: Double = 94500.00
        override val liquidationPrice: String = "$85,050.00"
        override val liquidationValue: Double = 85050.00
        override val margin: String = "$4,771.03"
        override val fundingPayments: String = "+$12.50"
        override val stopLoss: Double = 90050.00
        override val takeProfit: Double = 95000.00
    }

    val now = System.currentTimeMillis()
    val hourInMillis = 60 * 60 * 1000L

    val chartData = List(24) { index ->
        val basePrice = 95000.0
        val variance = (index % 3 - 1) * 500.0
        ChartCandleStick(
            date = now - (23 - index) * hourInMillis,
            open = basePrice + variance,
            high = basePrice + variance + 300.0,
            low = basePrice + variance - 200.0,
            close = basePrice + variance + 100.0,
            volume = 500000000.0 + (index * 10000000.0)
        )
    }

    WalletTheme {
        PerpetualPositionScene(
            perpetual = samplePerpetual,
            position = samplePosition,
            chartData = chartData,
            period = ChartPeriod.Day,
            onChartPeriodSelect = {},
            onClose = {}
        )
    }
}