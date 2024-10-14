package com.gemwallet.android.features.settings.price_alerts.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.features.assets.model.toUIModel
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
) : ViewModel() {

    val alertingAssets = assetsRepository.getAssetsInfo()
        .map { items ->
            items.filter { it.priceAlerting }.map { it.toUIModel() }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun excludeAsset(assetId: AssetId) {
//        assetsRepository.excludeAssetAllert(assetId)
    }

    fun addAsset(assetId: AssetId) {
//        assetsRepository.includeAssetAllert(assetId)
    }
}