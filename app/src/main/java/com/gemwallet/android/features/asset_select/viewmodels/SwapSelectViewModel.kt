package com.gemwallet.android.features.asset_select.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.viewmodels.BaseAssetSelectViewModel.SearchState
import com.gemwallet.android.features.asset_select.viewmodels.BaseAssetSelectViewModel.UIState
import com.gemwallet.android.features.swap.models.SwapPairSelect
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.gemwallet.android.ui.models.AssetItemUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SwapSelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val searchTokensCase: SearchTokensCase,
) : ViewModel() {

    private val preSetPair: MutableStateFlow<SwapPairSelect?> = MutableStateFlow(null)

    val queryState = TextFieldState()
    private val searchState = MutableStateFlow<SearchState>(SearchState.Searching)

    private val queryFlow = snapshotFlow<String> { queryState.text.toString() }
        .onEach { searchState.update { SearchState.Searching } }
        .mapLatest { query ->
            delay(250)
            searchTokensCase.search(query)
            query
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val assets = combine(sessionRepository.session(), queryFlow, preSetPair) { session, query, pair ->
        Triple(session, query, pair)
    }
    .flatMapLatest {
        val (session, query, pair) = it
        val wallet = it.first?.wallet ?: return@flatMapLatest emptyFlow()
        pair ?: return@flatMapLatest flow { emit(emptyList()) }
        assetsRepository.search(wallet, query, false, listOf(pair.oppositeId()?.toIdentifier() ?: ""))
    }
    .map { it.distinctBy { it.asset.id.toIdentifier() } }
    .map { assets -> assets.filter(::filterAsset).map { AssetInfoUIModel(it) }.toImmutableList() } // TODO: Change filter: to db
    .onEach { searchState.update { SearchState.Idle } }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>().toImmutableList())

    val uiState = assets.combine(searchState) { assets, searchState ->
        when {
            searchState != SearchState.Idle -> UIState.Loading
            assets.isEmpty() -> UIState.Empty
            else -> UIState.Idle
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, UIState.Idle)


    fun filterAsset(assetInfo: AssetInfo): Boolean {
        return assetInfo.metadata?.isSwapEnabled == true
                && if (preSetPair.value is SwapPairSelect.From) assetInfo.balance.totalAmount > 0.0 else true
    }

    fun setPair(select: SwapPairSelect) {
        preSetPair.update { select }
    }
}