package com.gemwallet.android.features.asset_select.viewmodels

import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.models.BaseSelectSearch
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class BuySelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
    buyRepository: BuyRepository,
) : BaseAssetSelectViewModel(
    sessionRepository,
    assetsRepository,
    searchTokensCase,
    BuySelectSearch(assetsRepository, buyRepository)
)

class BuySelectSearch(
    assetsRepository: AssetsRepository,
    val buyRepository: BuyRepository,
) : BaseSelectSearch(assetsRepository) {
    override fun invoke(
        session: Flow<Session?>,
        query: Flow<String>
    ): Flow<List<AssetInfo>> {
        return super.invoke(session, query).map { list ->
            val available = buyRepository.getAvailable()
            list.filter { available.contains(it.asset.id.toIdentifier()) }
        }
    }

}