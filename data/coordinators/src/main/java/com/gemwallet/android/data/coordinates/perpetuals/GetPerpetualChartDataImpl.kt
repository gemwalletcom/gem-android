package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualChartData
import com.gemwallet.android.blockchain.services.toDTO
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod
import uniffi.gemstone.GemGateway

class GetPerpetualChartDataImpl(
    private val gemGateway: GemGateway,
) : GetPerpetualChartData {

    override suspend fun getPerpetualChartData(chain: Chain, symbol: String, period: ChartPeriod): List<ChartCandleStick> {
        return gemGateway.getCandlesticks(chain.string, symbol, period.string).map { it.toDTO() }
    }
}