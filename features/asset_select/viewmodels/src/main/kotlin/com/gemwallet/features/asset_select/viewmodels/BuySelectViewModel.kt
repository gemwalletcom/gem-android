package com.gemwallet.features.asset_select.viewmodels

import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import com.gemwallet.features.asset_select.viewmodels.models.BaseSelectSearch
import com.wallet.core.primitives.AssetTag
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
)

class BuySelectSearch(
    assetsRepository: AssetsRepository,
) : BaseSelectSearch(assetsRepository) {
    override fun invoke(
        session: Flow<Session?>,
        query: Flow<String>,
        tag: Flow<AssetTag?>,
    ): Flow<List<AssetInfo>> {
        return super.invoke(session, query, tag).map { list ->
            list.filter { it.metadata?.isBuyEnabled == true }
        }
        .flowOn(Dispatchers.Default)
    }
}