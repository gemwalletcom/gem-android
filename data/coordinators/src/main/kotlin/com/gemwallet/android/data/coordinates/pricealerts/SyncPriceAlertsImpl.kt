package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.SyncPriceAlerts
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SyncPriceAlertsImpl(
    private val gemDeviceApiClient: GemDeviceApiClient,
    private val priceAlertRepository: PriceAlertRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : SyncPriceAlerts {

    override fun syncPriceAlerts() {

        scope.launch {
            val all = priceAlertRepository.getEnablePriceAlerts()

            val remote = try {
                gemDeviceApiClient.getPriceAlerts()
            } catch (_: Throwable) { return@launch }

            val toExclude = remote.filter { remote ->
                remote.lastNotifiedAt ?: true

                all.map { it.priceAlert }.firstOrNull { local ->
                    local.assetId == remote.assetId
                            && local.price == remote.price
                            && local.priceDirection == remote.priceDirection
                            && local.pricePercentChange == remote.pricePercentChange
                            && local.currency == remote.currency
                } == null
            }

            val toUpdate = (remote - toExclude.toSet())
            priceAlertRepository.update(toUpdate)

            if (toExclude.isNotEmpty()) {
                try {
                    gemDeviceApiClient.excludePriceAlert(toExclude)
                } catch (_: Throwable) {
                }
            }

            try {
                val local = priceAlertRepository.getPriceAlerts().firstOrNull() ?: emptyList()
                gemDeviceApiClient.includePriceAlert(local.map { it.priceAlert })
            } catch (_: Throwable) {}
        }
    }
}