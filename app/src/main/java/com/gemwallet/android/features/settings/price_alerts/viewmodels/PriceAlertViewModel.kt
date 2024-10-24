package com.gemwallet.android.features.settings.price_alerts.viewmodels

import androidx.compose.ui.util.fastFirstOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.config.ConfigRepository
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    gemApiClient: GemApiClient,
    private val assetsRepository: AssetsRepository,
    private val configRepository: ConfigRepository,
    val sessionRepository: SessionRepository,
) : ViewModel() {

    private val syncDevice: SyncDevice = SyncDevice(gemApiClient, configRepository, sessionRepository)

    val forceSync = MutableStateFlow(false)

    val enabled = MutableStateFlow(configRepository.isPriceAlertEnabled())

    private val alerts = MutableStateFlow(emptyList<PriceAlert>())
    val alertingAssets = combine(assetsRepository.getAssetsInfo(), alerts, forceSync) { assets, alerts, sync ->
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
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.updatePriceAlerts()
            alerts.update { assetsRepository.getPriceAlerts() }
        }
    }

    fun onEnablePriceAlerts(enabled: Boolean) {
        configRepository.setEnablePriceAlerts(enabled)
        viewModelScope.launch(Dispatchers.IO) { syncDevice() }
        this.enabled.update { enabled }
    }

    fun excludeAsset(assetId: AssetId) = viewModelScope.launch {
        assetsRepository.excludeAssetAlert(assetId)
        refresh()
    }

    fun addAsset(assetId: AssetId) = viewModelScope.launch {
        assetsRepository.includeAssetAlert(assetId)
        refresh()
    }
}