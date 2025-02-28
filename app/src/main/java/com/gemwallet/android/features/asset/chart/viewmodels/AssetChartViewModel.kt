package com.gemwallet.android.features.asset.chart.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.asset.chart.models.AssetMarketUIModel
import com.gemwallet.android.features.asset.navigation.assetIdArg
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.AssetLink
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetChartViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val assetIdStr = savedStateHandle.getStateFlow<String?>(assetIdArg, null)
    private val assetInfo = assetIdStr.flatMapLatest { assetId ->
            val assetId = assetId ?: return@flatMapLatest emptyFlow()
            assetsRepository.getAssetsInfoByAllWallets(listOf(assetId))
                .map { it.firstOrNull() }
                .filterNotNull()
        }

    private val links = assetIdStr.filterNotNull()
        .flatMapLatest { assetsRepository.getAssetLinks(it.toAssetId() ?: return@flatMapLatest emptyFlow()) }

    private val market = assetIdStr.filterNotNull()
        .flatMapLatest { assetsRepository.getAssetMarket(it.toAssetId() ?: return@flatMapLatest emptyFlow()) }

    val marketUIModel = combine(assetInfo, links, market) { assetInfo, links, market ->
        val asset = assetInfo.asset
        val currency = assetInfo.price?.currency ?: Currency.USD
        AssetMarketUIModel(
            asset = asset,
            assetTitle = asset.name,
            assetLinks = links.toModel(),
            currency = assetInfo.price?.currency ?: Currency.USD,
            marketInfo = market,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val sync = assetIdStr.flatMapLatest { assetId ->
        flow {
            emit(true)
            assetsRepository.syncMarketInfo(assetId?.toAssetId() ?: return@flow, null)
            emit(false)
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private fun List<AssetLink>.toModel() = mapNotNull {
        return@mapNotNull when (it.name) {
            "coingecko" -> AssetMarketUIModel.Link(it.name, it.url, R.string.social_coingecko, R.drawable.coingecko)
            "twitter" -> AssetMarketUIModel.Link(it.name, it.url, R.string.social_x, R.drawable.twitter)
            "telegram" -> AssetMarketUIModel.Link(it.name, it.url, R.string.social_telegram, R.drawable.telegram)
            "github" -> AssetMarketUIModel.Link(it.name, it.url, R.string.social_github, R.drawable.github)
            else -> null
        }
    }
}

