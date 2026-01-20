package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.ExcludePriceAlert
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection

class ExcludePriceAlertImpl(
    private val getDeviceId: GetDeviceId,
    private val gemApiClient: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val priceAlertRepository: PriceAlertRepository,
) : ExcludePriceAlert {
    override suspend fun excludePriceAlert(priceAlertId: Int) {
        priceAlertRepository.getPriceAlert(priceAlertId)?.priceAlert?.let { priceAlert ->
            excludePriceAlert(
                priceAlert.assetId,
                Currency.entries.firstOrNull { it.string == priceAlert.currency },
                priceAlert.price,
                priceAlert.pricePercentChange,
                priceAlert.priceDirection
            )
        }
    }

    override suspend fun excludePriceAlert(
        assetId: AssetId,
        currency: Currency?,
        price: Double?,
        percentage: Double?,
        direction: PriceAlertDirection?,
    ) {
        val currency = currency?.string ?: sessionRepository.getCurrentCurrency().string
        val priceAlert = PriceAlert(
            assetId = assetId,
            currency = currency,
            price = price,
            pricePercentChange = percentage,
            priceDirection = direction,
        )
        val priceAlertInfo = priceAlertRepository.getSamePriceAlert(priceAlert) ?: return
        val deviceId = getDeviceId.getDeviceId()
        priceAlertRepository.disable(priceAlertInfo.id)
        runCatching { gemApiClient.excludePriceAlert(deviceId, listOf(priceAlertInfo.priceAlert)) }
    }
}