package com.gemwallet.android.features.settings.price_alerts.viewmodels

import androidx.compose.ui.util.fastFirstOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.repositories.asset.AssetsRepository
import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.interactors.sync.SyncDevice
import com.gemwallet.android.services.GemApiClient
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.PriceAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    gemApiClient: GemApiClient,
    assetsRepository: AssetsRepository,
    getPriceAlertsCase: GetPriceAlertsCase,
    val sessionRepository: SessionRepository,
    private val enablePriceAlertCase: EnablePriceAlertCase,
    private val putPriceAlertCase: PutPriceAlertCase,
    configRepository: ConfigRepository,
) : ViewModel() {

    private val syncDevice: SyncDevice = SyncDevice(gemApiClient, configRepository, sessionRepository, enablePriceAlertCase)

    val forceSync = MutableStateFlow(false)

    val enabled = MutableStateFlow(enablePriceAlertCase.isPriceAlertEnabled())

    val alertingAssets = combine(assetsRepository.getAssetsInfo(), getPriceAlertsCase.getPriceAlerts(), forceSync) { assets, alerts, sync ->
        viewModelScope.launch(Dispatchers.IO) {
            delay(300)
            forceSync.update { false }
        }
        assets.filter { asset -> alerts.fastFirstOrNull { it.assetId == asset.asset.id.toIdentifier() } != null }
            .map { AssetInfoUIModel(it) }
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
            syncDevice()
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