package com.gemwallet.features.settings.price_alerts.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.pricealerts.coordinators.ExcludePriceAlert
import com.gemwallet.android.application.pricealerts.coordinators.GetPriceAlerts
import com.gemwallet.android.application.pricealerts.coordinators.IncludePriceAlert
import com.gemwallet.android.application.pricealerts.coordinators.PriceAlertsStateCoordinator
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent
import com.gemwallet.android.ext.toAssetId
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    getPriceAlerts: GetPriceAlerts,
    private val priceAlertsStateCoordinator: PriceAlertsStateCoordinator,
    private val assetsRepository: AssetsRepository,
    private val includePriceAlert: IncludePriceAlert,
    private val excludePriceAlert: ExcludePriceAlert,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val forceSync = MutableStateFlow(false)

    val assetId = savedStateHandle.getStateFlow<String?>("assetId", null)
        .mapLatest { it?.toAssetId() }
        .onEach { priceAlertsStateCoordinator.changePriceAlertState(PriceAlertsStateEvent.Request()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val data = assetId.flatMapLatest { getPriceAlerts.getPriceAlerts(it) }
        .mapLatest { getPriceAlerts.groupByTargetAndAsset(it) }

        .combine(forceSync) { items, _ ->
            viewModelScope.launch(Dispatchers.IO) {
                delay(300)
                forceSync.update { false }
            }
            items
        }

        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val priceAlertState =  priceAlertsStateCoordinator.priceAlertState
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        refresh(false)
    }

    fun refresh(force: Boolean = true) {
        forceSync.update { force }
    }

    fun isAssetManage(): Boolean = assetId.value != null

    fun togglePriceAlerts(enable: Boolean) {
        val newState = if (enable) PriceAlertsStateEvent.Enable() else PriceAlertsStateEvent.Disable()
        priceAlertsStateCoordinator.changePriceAlertState(newState)
    }

    fun pushGranted() {
        priceAlertsStateCoordinator.changePriceAlertState(PriceAlertsStateEvent.PushGranted())
    }

    fun pushRejected() {
        priceAlertsStateCoordinator.changePriceAlertState(PriceAlertsStateEvent.PushRejected())
    }

    fun excludeAsset(priceAlertId: Int) = viewModelScope.launch(Dispatchers.IO) {
        excludePriceAlert.excludePriceAlert(priceAlertId)
    }

    fun includeAsset(assetId: AssetId, callback: (Asset) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        includePriceAlert.includePriceAlert(assetId)

        val assetInfo = assetsRepository.getTokenInfo(assetId).firstOrNull() ?: return@launch // TODO:
        viewModelScope.launch { callback(assetInfo.asset) }
    }
}