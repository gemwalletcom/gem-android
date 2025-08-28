package com.gemwallet.features.settings.price_alerts.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
import com.gemwallet.android.cases.pricealerts.GetPriceAlerts
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.components.list_item.AssetInfoUIModel
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    getPriceAlerts: GetPriceAlerts,
    private val assetsRepository: AssetsRepository,
    val sessionRepository: SessionRepository,
    private val enablePriceAlert: EnablePriceAlert,
    private val putPriceAlertCase: PutPriceAlertCase,
    private val syncDeviceInfo: SyncDeviceInfo,
) : ViewModel() {

    val forceSync = MutableStateFlow(false)

    val enabled = MutableStateFlow(enablePriceAlert.isPriceAlertEnabled())

    @OptIn(ExperimentalCoroutinesApi::class)
    val alertingAssets = getPriceAlerts.getPriceAlerts().flatMapLatest { alerts ->
        val ids = alerts.map { it.assetId }
        refreshPrices(ids)
        assetsRepository.getTokensInfo(ids.map { it.toIdentifier() }).map { it.distinctBy { it.id() } }
    }
    .map { it.map { AssetInfoUIModel(it) } }
    .combine(forceSync) { items, sync ->
        viewModelScope.launch(Dispatchers.IO) {
            delay(300)
            forceSync.update { false }
        }
        items
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refresh(false)
    }

    fun refresh(force: Boolean = true) {
        forceSync.update { force }
    }

    fun onEnablePriceAlerts(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            enablePriceAlert.setPriceAlertEnabled(enabled)
            syncDeviceInfo.syncDeviceInfo()
        }
        this.enabled.update { enabled }
    }

    fun excludeAsset(assetId: AssetId) = viewModelScope.launch {
        enablePriceAlert.setAssetPriceAlertEnabled(assetId, false)
    }

    fun addAsset(assetId: AssetId, callback: (Asset) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
//        assetsRepository.updatePrices(sessionRepository.getSession()?.currency ?: return@launch, assetId)
        putPriceAlertCase.putPriceAlert(PriceAlert(assetId, Currency.USD.string)) // TODO: Add user selected currency
        val assetInfo = assetsRepository.getAssetsInfo(listOf(assetId)).firstOrNull()?.firstOrNull() ?: return@launch

        viewModelScope.launch {
            callback(assetInfo.asset)
        }
    }

    private fun refreshPrices(ids: List<AssetId>) = viewModelScope.launch(Dispatchers.IO) {
//        assetsRepository.updatePrices(
//            sessionRepository.getSession()?.currency ?: Currency.USD,
//            *ids.toTypedArray(),
//        )
    }
}