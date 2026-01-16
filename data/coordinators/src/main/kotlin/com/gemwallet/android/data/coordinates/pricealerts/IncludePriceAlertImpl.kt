package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.IncludePriceAlert
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection

class IncludePriceAlertImpl(
    private val gemApiClient: GemApiClient,
    private val getDeviceIdImpl: GetDeviceId,
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
        if (!priceAlertRepository.hasSamePriceAlert(priceAlert)) {
            priceAlertRepository.addPriceAlert(priceAlert)
        }

        try {
            gemApiClient.includePriceAlert(
                deviceId = getDeviceIdImpl.getDeviceId(),
                alerts = listOf(priceAlert),
            )
        } catch (_: Throwable) {}
    }
}