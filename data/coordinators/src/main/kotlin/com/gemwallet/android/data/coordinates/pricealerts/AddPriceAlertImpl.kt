package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.AddPriceAlert
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPriceAlertImpl(
    private val gemApiClient: GemApiClient,
    private val getDeviceIdImpl: GetDeviceId,
    private val priceAlertRepository: PriceAlertRepository,
) : AddPriceAlert {

    override suspend fun addPriceAlert(
        assetId: AssetId,
        currency: Currency,
        price: Double?,
        percentage: Double?,
        direction: PriceAlertDirection?
    ) {
        val priceAlert = PriceAlert(
            assetId = assetId,
            currency = currency.string,
            price = price,
            pricePercentChange = percentage,
            priceDirection = direction,
        )
        if (priceAlertRepository.hasSamePriceAlert(priceAlert)) {
            return
        }
        priceAlertRepository.addPriceAlert(priceAlert)
        try {
            gemApiClient.includePriceAlert(
                deviceId = getDeviceIdImpl.getDeviceId(),
                listOf(priceAlert),
            )
        } catch (_: Throwable) {}
    }
}