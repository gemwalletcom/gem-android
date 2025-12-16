package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualChartData
import com.gemwallet.android.blockchain.services.PerpetualService
import com.gemwallet.android.blockchain.services.toDTO
import com.gemwallet.android.ext.twoSubtokenIds
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod
import uniffi.gemstone.GemGateway

class GetPerpetualChartDataImpl(
    private val perpetualService: PerpetualService,
) : GetPerpetualChartData {

    override suspend fun getPerpetualChartData(
        assetId: AssetId,
        period: ChartPeriod
    ): List<ChartCandleStick> {
        val symbol = assetId.twoSubtokenIds()?.second ?: return emptyList()
        return perpetualService.getCandleSticks(symbol = symbol, period = period)
    }
}