package com.gemwallet.android.features.settings.price_alerts.viewmodels

import com.gemwallet.android.cases.pricealerts.GetPriceAlerts
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.models.SelectSearch
import com.gemwallet.android.features.asset_select.viewmodels.BaseAssetSelectViewModel
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PriceAlertsSelectViewModel @Inject constructor(
    getPriceAlerts: GetPriceAlerts,
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
) : BaseAssetSelectViewModel(
    sessionRepository,
    assetsRepository,
    searchTokensCase,
    PriceAlertSelectSearch(assetsRepository, getPriceAlerts),
)

@OptIn(ExperimentalCoroutinesApi::class)
open class PriceAlertSelectSearch(
    private val assetsRepository: AssetsRepository,
    getPriceAlerts: GetPriceAlerts,
) : SelectSearch {

    val addedPriceAlerts = getPriceAlerts.getPriceAlerts().map { it.map { it.assetId } }

    override fun invoke(
        session: Flow<Session?>,
        query: Flow<String>
    ): Flow<List<AssetInfo>> {
        return combine(query, addedPriceAlerts) { query, alerts -> Pair(query, alerts) }
            .flatMapLatest {
                val (query, alerts) = it
                assetsRepository.search(query, true)
            }
            .map { it.distinctBy { it.asset.id.toIdentifier() } }
            .flowOn(Dispatchers.IO)
    }
}
