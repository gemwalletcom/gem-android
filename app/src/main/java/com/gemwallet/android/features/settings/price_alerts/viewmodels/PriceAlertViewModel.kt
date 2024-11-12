package com.gemwallet.android.features.settings.price_alerts.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.device.SyncDeviceInfoCase
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.PriceAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    assetsRepository: AssetsRepository,
    getPriceAlertsCase: GetPriceAlertsCase,
    val sessionRepository: SessionRepository,
    private val enablePriceAlertCase: EnablePriceAlertCase,
    private val putPriceAlertCase: PutPriceAlertCase,
    private val syncDeviceInfoCase: SyncDeviceInfoCase,
) : ViewModel() {

    val forceSync = MutableStateFlow(false)

    val enabled = MutableStateFlow(enablePriceAlertCase.isPriceAlertEnabled())

    @OptIn(ExperimentalCoroutinesApi::class)
    val alertingAssets = getPriceAlertsCase.getPriceAlerts().flatMapLatest { alerts ->
        val ids = alerts.map { it.assetId }
        assetsRepository.getAssetsInfoByAllWallets(ids)
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
            enablePriceAlertCase.setPriceAlertEnabled(enabled)
            syncDeviceInfoCase.syncDeviceInfo()
        }
        this.enabled.update { enabled }
    }

    fun excludeAsset(assetId: AssetId) = viewModelScope.launch {
        enablePriceAlertCase.setAssetPriceAlertEnabled(assetId, false)
    }

    fun addAsset(assetId: AssetId) = viewModelScope.launch {
        putPriceAlertCase.putPriceAlert(PriceAlert(assetId.toIdentifier()))
    }
}