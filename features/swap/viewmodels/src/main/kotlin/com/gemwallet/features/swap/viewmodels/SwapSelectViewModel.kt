package com.gemwallet.features.swap.viewmodels

import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.swap.GetSwapSupported
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toChain
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import com.gemwallet.features.asset_select.viewmodels.BaseAssetSelectViewModel
import com.gemwallet.features.asset_select.viewmodels.models.SelectSearch
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetTag
import com.wallet.core.primitives.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SwapSelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
    getSwapSupported: GetSwapSupported,
    val savedStateHandle: SavedStateHandle,
) : BaseAssetSelectViewModel(
    sessionRepository = sessionRepository,
    assetsRepository = assetsRepository,
    searchTokensCase = searchTokensCase,
    search = SwapSelectSearch(assetsRepository, getSwapSupported)
) {

    val payAssetId = savedStateHandle.getStateFlow<String?>("payAssetId", null)
        .mapLatest { it?.toAssetId() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val receiveAssetId = savedStateHandle.getStateFlow<String?>("receiveAssetId", null)
        .mapLatest { it?.toAssetId() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val select = savedStateHandle.getStateFlow<SwapItemType?>("select", null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state = combine(payAssetId, receiveAssetId, select.filterNotNull()) { pay, receive, select ->
        setPair(select, pay, receive)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onSelect(assetId: AssetId) {
        when (select.value) {
            SwapItemType.Pay -> savedStateHandle.set("from", assetId.toIdentifier())
            SwapItemType.Receive -> savedStateHandle.set("to", assetId.toIdentifier())
            null -> return
        }
    }

    fun setPair(type: SwapItemType, payId: AssetId?, receiveId: AssetId?) {
        queryState.clearText()
        (search as? SwapSelectSearch)?.apply {
            this.swapItemType.update { type }
            this.payId.update { payId }
            this.receiveId.update { receiveId }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SwapSelectSearch(
    private val assetsRepository: AssetsRepository,
    private val getSwapSupported: GetSwapSupported,
) : SelectSearch {

    val swapItemType = MutableStateFlow<SwapItemType?>(null)
    val payId = MutableStateFlow<AssetId?>(null)
    val receiveId = MutableStateFlow<AssetId?>(null)

    override fun invoke(
        session: Flow<Session?>,
        query: Flow<String>,
        tag: Flow<AssetTag?>,
    ): Flow<List<AssetInfo>> {
        return combine(session, query, swapItemType, payId, receiveId, tag) { params/*session, query, type, payId, receiveId, tag*/ ->
            val session: Session? = params[0] as? Session?
            val query: String = params[1] as? String ?: ""
            val type: SwapItemType? = params[2] as? SwapItemType?
            val payId: AssetId? = params[3] as? AssetId?
            val receiveId: AssetId? = params[4] as? AssetId?
            val tag: AssetTag? = params[5] as? AssetTag?
            val oppositeId = getOppositeAssetId(type, payId, receiveId)
            SearchParams(session?.wallet, query, oppositeId, tag)
        }
        .flatMapLatest { params ->
            if (params.oppositeAssetId == null || params.wallet == null) {
                return@flatMapLatest emptyFlow()
            }

            val supported = getSwapSupported.getSwapSupportChains(params.oppositeAssetId)
            assetsRepository.swapSearch(
                params.wallet,
                params.query,
                supported.chains.mapNotNull { item -> item.toChain() },
                supported.assetIds.mapNotNull { it.toAssetId() }
            )
        }
        .map { items ->
            items.filter { assetInfo ->
                assetInfo.metadata?.isSwapEnabled == true
                    && if (swapItemType.value == SwapItemType.Pay) {
                        assetInfo.balance.totalAmount > 0.0
                    } else {
                        true
                    }
            }
        }
        .map { items -> items.distinctBy { it.asset.id.toIdentifier() } }
    }

    private fun getOppositeAssetId(type: SwapItemType?, payId: AssetId?, receiveId: AssetId?) =  when (type) {
        SwapItemType.Pay -> receiveId
        SwapItemType.Receive -> payId
        null -> null
    }

    private class SearchParams(
        val wallet: Wallet?,
        val query: String,
        val oppositeAssetId: AssetId?,
        val tag: AssetTag?
    )
}