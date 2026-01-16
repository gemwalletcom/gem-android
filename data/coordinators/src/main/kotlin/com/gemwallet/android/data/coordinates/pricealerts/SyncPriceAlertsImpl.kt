package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.SyncPriceAlerts
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SyncPriceAlertsImpl(
    private val gemApiClient: GemApiClient,
    private val getDeviceId: GetDeviceId,
    private val priceAlertRepository: PriceAlertRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : SyncPriceAlerts {
    override fun syncPriceAlerts() {
        val deviceId = getDeviceId.getDeviceId()

        scope.launch {
            val local = priceAlertRepository.getPriceAlerts().firstOrNull() ?: emptyList()
            try {
                gemApiClient.includePriceAlert(deviceId, local.map { it.priceAlert })
            } catch (_: Throwable) {}

            val remote = try {
                gemApiClient.getPriceAlerts(deviceId)
            } catch (_: Throwable) { return@launch }

            val toExclude = remote.filter { remote ->
                local.map { it.priceAlert }.firstOrNull { local ->
                    local.assetId == remote.assetId
                            && local.price == remote.price
                            && local.priceDirection == remote.priceDirection
                            && local.pricePercentChange == remote.pricePercentChange
                            && local.currency == remote.currency
                } == null
            }

            try {
                gemApiClient.excludePriceAlert(deviceId, toExclude)
            } catch (_: Throwable) { }
            val toUpdate = (remote - toExclude.toSet())
            priceAlertRepository.update(toUpdate)
        }
    }
}