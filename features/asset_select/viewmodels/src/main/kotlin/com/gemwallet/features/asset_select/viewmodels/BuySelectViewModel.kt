package com.gemwallet.features.asset_select.viewmodels

import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.RecentType
import com.gemwallet.features.asset_select.viewmodels.models.BaseSelectSearch
import com.gemwallet.features.asset_select.viewmodels.models.SelectAssetFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class BuySelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
) : BaseAssetSelectViewModel(
    sessionRepository,
    assetsRepository,
    searchTokensCase,
    BuySelectSearch(assetsRepository)
) {
    override fun getRecentType(): RecentType = RecentType.Buy
}

class BuySelectSearch(
    assetsRepository: AssetsRepository,
) : BaseSelectSearch(assetsRepository) {

    override fun items(filters: Flow<SelectAssetFilters?>): Flow<List<AssetInfo>> {
        return super.items(filters).map { items -> filter(items) }
            .flowOn(Dispatchers.Default)
    }

    override fun filter(items: List<AssetInfo>): List<AssetInfo>
        = items.filter { it.metadata?.isBuyEnabled == true }
}