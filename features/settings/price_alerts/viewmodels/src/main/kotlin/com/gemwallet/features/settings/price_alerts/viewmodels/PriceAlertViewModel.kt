package com.gemwallet.features.settings.price_alerts.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.pricealerts.coordinators.GetPriceAlerts
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
import com.gemwallet.android.cases.pricealerts.PutPriceAlert
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.toAssetId
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    getPriceAlerts: GetPriceAlerts,
    private val assetsRepository: AssetsRepository,
    val sessionRepository: SessionRepository,
    private val enablePriceAlert: EnablePriceAlert,
    private val putPriceAlert: PutPriceAlert,
    private val syncDeviceInfo: SyncDeviceInfo,
    private val getPushEnabled: GetPushEnabled,
    private val switchPushEnabled: SwitchPushEnabled,
    private val walletsRepository: WalletsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val pushEnabled = getPushEnabled.getPushEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val forceSync = MutableStateFlow(false)

    val enabled = MutableStateFlow(enablePriceAlert.isPriceAlertEnabled())

    val assetId = savedStateHandle.getStateFlow<String?>("assetId", null)
        .mapLatest { it?.toAssetId() }
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

    init {
        refresh(false)
    }

    fun refresh(force: Boolean = true) {
        forceSync.update { force }
    }

    fun isAssetView(): Boolean = assetId.value != null

    fun onEnablePriceAlerts(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            enablePriceAlert.setPriceAlertEnabled(enabled)
            syncDeviceInfo.syncDeviceInfo()
        }
        this.enabled.update { enabled }
    }

    fun notificationEnable() {
        onEnablePriceAlerts(true)
        viewModelScope.launch(Dispatchers.IO) {
            switchPushEnabled.switchPushEnabledCase(
                true,
                walletsRepository.getAll().firstOrNull() ?: emptyList()
            )
        }
    }

    fun excludeAsset(priceAlertId: Int) = viewModelScope.launch {
        val currency = sessionRepository.getCurrentCurrency()
        val priceAlert = data.value.values.flatten().firstOrNull { it.id == priceAlertId } ?: return@launch
        enablePriceAlert.setAssetPriceAlertEnabled(priceAlert.assetId, currency, false)
    }

    fun addAsset(assetId: AssetId, callback: (Asset) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            putPriceAlert.putPriceAlert(
                PriceAlert(
                    assetId,
                    Currency.USD.string
                )
            ) // TODO: Add user selected currency
            val assetInfo = assetsRepository.getAssetsInfo(listOf(assetId)).firstOrNull()?.firstOrNull()
                    ?: return@launch

            viewModelScope.launch {
                callback(assetInfo.asset)
            }
        }
}