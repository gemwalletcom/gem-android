package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.IncludePriceAlert
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection

class IncludePriceAlertImpl(
    private val gemDeviceApiClient: GemDeviceApiClient,
    private val sessionRepository: SessionRepository,
    private val priceAlertRepository: PriceAlertRepository,
) : IncludePriceAlert {

    override suspend fun includePriceAlert(
        assetId: AssetId,
        currency: Currency?,
        price: Double?,
        percentage: Double?,
        direction: PriceAlertDirection?
    ) {
        val currency = currency?.string ?: sessionRepository.getCurrentCurrency().string
        val priceAlert = PriceAlert(
            assetId = assetId,
            currency = currency,
            price = price,
            pricePercentChange = percentage,
            priceDirection = direction,
        )
        priceAlertRepository.getSamePriceAlert(priceAlert)?.let {
            priceAlertRepository.enable(it.id)
        } ?: priceAlertRepository.addPriceAlert(priceAlert)

        try {
            gemDeviceApiClient.includePriceAlert(alerts = listOf(priceAlert))
        } catch (_: Throwable) {}
    }
}