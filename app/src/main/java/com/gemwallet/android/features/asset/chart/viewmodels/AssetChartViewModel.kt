package com.gemwallet.android.features.asset.chart.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.R
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.asset.chart.models.AssetMarketUIModel
import com.gemwallet.android.features.asset.navigation.assetIdArg
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.CellEntity
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigInteger
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetChartViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val assetIdStr = savedStateHandle.getStateFlow<String?>(assetIdArg, null)
    private val assetInfo = assetIdStr.flatMapLatest {
        assetsRepository.getAssetInfo(assetId = it?.toAssetId() ?: return@flatMapLatest emptyFlow())
    }

    val marketUIModel = assetInfo.map { assetInfo ->
        val asset = assetInfo.asset
        val currency = assetInfo.price?.currency ?: Currency.USD
        AssetMarketUIModel(
            assetId = asset.id,
            assetTitle = asset.name,
            assetLinks = assetInfo.links?.toModel() ?: emptyList(),
            currency = assetInfo.price?.currency ?: Currency.USD,
            marketCells = mapOf(
                R.string.asset_market_cap to (assetInfo.market?.marketCap ?: 0.0),
                R.string.asset_circulating_supply to (assetInfo.market?.circulatingSupply ?: 0.0),
                R.string.asset_total_supply to (assetInfo.market?.totalSupply ?: 0.0)
            ).filterValues { it > 0.0 }
                .map { (label, value) ->
                    CellEntity(
                        label = label,
                        data = when (label) {
                            R.string.asset_market_cap -> currency.format(value, 0)
                            R.string.asset_circulating_supply -> Crypto(BigInteger.valueOf(value.toLong()))
                                .format(0, asset.symbol, 0)

                            R.string.asset_total_supply -> Crypto(BigInteger.valueOf(value.toLong()))
                                .format(0, asset.symbol, 0)

                            else -> ""
                        }
                    )
                }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun AssetLinks.toModel() = listOfNotNull(
        if (coingecko.isNullOrEmpty()) null else AssetMarketUIModel.Link(
            "coingecko", coingecko!!, R.string.social_coingecko
        ),
        if (twitter.isNullOrEmpty()) null else AssetMarketUIModel.Link(
            "twitter", twitter!!, R.string.social_x
        ),
        if (telegram.isNullOrEmpty()) null else AssetMarketUIModel.Link(
            "telegram", telegram!!, R.string.social_telegram
        ),
        if (github.isNullOrEmpty()) null else AssetMarketUIModel.Link(
            "github", github!!, R.string.social_github
        ),
    )
}

