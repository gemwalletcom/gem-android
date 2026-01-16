package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.ExcludePriceAlert
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient

class ExcludePriceAlertImpl(
    private val getDeviceId: GetDeviceId,
    private val gemApiClient: GemApiClient,
    private val priceAlertRepository: PriceAlertRepository,
) : ExcludePriceAlert {
    override suspend fun excludePriceAlert(priceAlertId: Int) {
        val deviceId = getDeviceId.getDeviceId()
        val priceAlert = priceAlertRepository.getPriceAlert(priceAlertId) ?: return
        priceAlertRepository.disable(priceAlertId)
        runCatching { gemApiClient.excludePriceAlert(deviceId, listOf(priceAlert.priceAlert)) }
    }
}