package com.gemwallet.android.features.asset_select.viewmodels

import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.models.SelectSearch
import com.gemwallet.android.features.swap.models.SwapPairSelect
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SwapSelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
) : BaseAssetSelectViewModel(
    sessionRepository = sessionRepository,
    assetsRepository = assetsRepository,
    searchTokensCase = searchTokensCase,
    search = SwapSelectSearch(assetsRepository)
) {
    fun setPair(select: SwapPairSelect) {
        (search as? SwapSelectSearch)?.preSetPair?.update { select }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SwapSelectSearch(
    private val assetsRepository: AssetsRepository,
) : SelectSearch {

    val preSetPair = MutableStateFlow<SwapPairSelect?>(null)

    override fun invoke(
        session: Flow<Session?>,
        query: Flow<String>
    ): Flow<List<AssetInfo>> {
        return combine(session, query, preSetPair) { session, query, pair -> Triple(session, query, pair) }
            .flatMapLatest {
                val (session, query, pair) = it
                val wallet = session?.wallet ?: return@flatMapLatest emptyFlow()
                pair ?: return@flatMapLatest flow { emit(emptyList()) }
                assetsRepository.search(wallet, query, false, listOf(pair.oppositeId()?.toIdentifier() ?: ""))
            }
            .map { items ->
                items.filter { assetInfo ->
                    assetInfo.metadata?.isSwapEnabled == true
                        && if (preSetPair.value is SwapPairSelect.From) assetInfo.balance.totalAmount > 0.0 else true
                }
            }
            .map { it.distinctBy { it.asset.id.toIdentifier() } }
    }
}