package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.application.pricealerts.coordinators.IncludePriceAlert
import com.gemwallet.android.application.pricealerts.coordinators.PriceAlertsStateCoordinator
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent.Disable
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent.Enable
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent.PushGranted
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent.PushRejected
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent.PushRequested
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class PriceAlertsStateCoordinatorImpl(
    private val getPushEnabled: GetPushEnabled,
    private val priceAlertRepository: PriceAlertRepository,
    private val includePriceAlert: IncludePriceAlert,
    private val syncDeviceInfo: SyncDeviceInfo,
    private val switchPushEnabled: SwitchPushEnabled,
    private val walletsRepository: WalletsRepository,
) : PriceAlertsStateCoordinator {
    private val event = MutableStateFlow<PriceAlertsStateEvent?>(null)
    private val assetIdEnabled: Flow<Boolean> = event.flatMapLatest { event -> event?.assetId?.let { priceAlertRepository.getAssetPriceAlert(it).mapLatest { it != null } } ?: flowOf(false) }

    override val priceAlertState: Flow<PriceAlertsStateEvent?> = combine(
        event,
        getPushEnabled.getPushEnabled(),
        priceAlertRepository.isPriceAlertsEnabled(),
        assetIdEnabled,
    ) { event: PriceAlertsStateEvent?, pushState: Boolean, priceAlertsEnabled: Boolean, assetState: Boolean ->
        if (event == null) {
            return@combine null
        }
        togglePriceAlerts(event, pushState)
        when (event) {
            is Request -> {
                when {
                    !pushState
                            || !priceAlertsEnabled
                            || event.assetId != null && !assetState -> Disable(event.assetId)
                    else -> Enable(event.assetId)
                }
            }
            is Enable -> when {
                !pushState -> PushRequested(event.assetId)
                else -> Enable(event.assetId)
            }
            is PushRejected,
            is Disable,
            is PushRequested -> Disable(event.assetId)
            is PushGranted -> Enable(event.assetId)
        }
    }
    .flowOn(Dispatchers.IO)

    private suspend fun togglePriceAlerts(event: PriceAlertsStateEvent, pushState: Boolean) {
        when (event) {
            is Enable,
            is PushGranted -> {
                if (!pushState && event !is PushGranted) {
                    return
                }

                priceAlertRepository.togglePriceAlerts(true)
                switchPushEnabled.switchPushEnabled( // TODO: Move to coordinators
                    true,
                    walletsRepository.getAll().firstOrNull() ?: emptyList()
                )
                event.assetId?.let { assetId -> includePriceAlert.includePriceAlert(assetId) }
                this.event.update { Request(event.assetId) }
            }
            is Disable -> {
                priceAlertRepository.togglePriceAlerts(false)
                syncDeviceInfo.syncDeviceInfo()

                this.event.update { Request(event.assetId) }
            }
            else -> {}
        }
    }

    override fun changePriceAlertState(state: PriceAlertsStateEvent) {
        event.update { state }
    }
}