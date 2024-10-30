package com.gemwallet.android.features.asset_select.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositories.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.tokenAvailableChains
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.features.assets.model.toUIModel
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetSelectViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val searchTokensCase: SearchTokensCase,
) : ViewModel() {
    val queryState = TextFieldState()
    private val queryFlow = snapshotFlow<String> { queryState.text.toString() }
        .filter { it.isNotEmpty() }
        .onEach { searchState.update { SearchState.Searching } }
        .mapLatest { query ->
            delay(250)
            searchTokensCase.search(query)
            query
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val searchState = MutableStateFlow<SearchState>(SearchState.Searching)

    val assets = sessionRepository.session().combine(queryFlow) { session, query ->
        Pair(session, query)
    }
    .flatMapLatest {
        val wallet = it.first?.wallet ?: return@flatMapLatest emptyFlow()
        assetsRepository.search(wallet, it.second)
    }
    .map {
        it.distinctBy { it.asset.id.toIdentifier() }
            .sortedByDescending {
                it.balances.available()
                    .convert(it.asset.decimals, it.price?.price?.price ?: 0.0)
                    .atomicValue
            }
    }
    .map { assets -> assets.map { it.toUIModel() }.toImmutableList() }
    .onEach { searchState.update { SearchState.Idle } }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetUIState>().toImmutableList())

    val uiState = assets.combine(searchState) { assets, searchState ->
        when {
            searchState != SearchState.Idle -> UIState.Loading
            assets.isEmpty() -> UIState.Empty
            else -> UIState.Idle
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, UIState.Idle)

    val isAddAssetAvailable = sessionRepository.session().map { session ->
        val availableAccounts = session?.wallet?.accounts?.map { it.chain } ?: emptyList()
        tokenAvailableChains.any { availableAccounts.contains(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onChangeVisibility(assetId: AssetId, visible: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        val account = session.wallet.getAccount(assetId.chain) ?: return@launch
        assetsRepository.switchVisibility(session.wallet.id, account, assetId, visible, session.currency)
    }

    enum class SearchState {
        Idle,
        Searching,
    }

    sealed interface UIState {
        data object Idle : UIState
        data object Empty : UIState
        data object Loading : UIState
    }
}
