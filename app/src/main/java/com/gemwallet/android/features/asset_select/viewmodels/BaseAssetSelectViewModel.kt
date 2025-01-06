package com.gemwallet.android.features.asset_select.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.features.asset_select.models.SelectSearch
import com.gemwallet.android.features.asset_select.viewmodels.BaseAssetSelectViewModel.SearchState
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.AssetId
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.map

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseAssetSelectViewModel(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val searchTokensCase: SearchTokensCase,
    internal val search: SelectSearch,
) : ViewModel() {

    val queryState = TextFieldState()
    private val searchState = MutableStateFlow<SearchState>(SearchState.Init)

    private val queryFlow = snapshotFlow<String> { queryState.text.toString() }
        .onEach {
            searchState.update {
                if (it != SearchState.Init) {
                    SearchState.Searching
                } else {
                    it
                }
            }
        }
        .mapLatest { query ->
            delay(250)
            searchTokensCase.search(query)
            query
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val assets = search(sessionRepository.session(), queryFlow)
        .map { items -> items.map { AssetInfoUIModel(it) } }
        .onEach { searchState.update { SearchState.Idle } }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>())

    val pinned = assets.map { items: List<AssetItemUIModel> ->
        items.filter { it.metadata?.isPinned == true }.toImmutableList()
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>().toImmutableList())

    val unpinned = assets.map { items: List<AssetItemUIModel> ->
        items.filter { it.metadata?.isPinned != true }.toImmutableList()
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>().toImmutableList())

    val uiState = assets.combine(searchState) { assets, searchState ->
        when {
            searchState != SearchState.Idle && searchState != SearchState.Init -> UIState.Loading
            assets.isEmpty() && searchState == SearchState.Idle -> UIState.Empty
            else -> UIState.Idle
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, UIState.Idle)

    val isAddAssetAvailable = sessionRepository.session().map { session ->
        session?.wallet?.accounts?.filter { it.chain.assetType() != null }?.isNotEmpty() == true
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onChangeVisibility(assetId: AssetId, visible: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        val account = session.wallet.getAccount(assetId.chain) ?: return@launch
        assetsRepository.switchVisibility(session.wallet.id, account, assetId, visible, session.currency)
    }

    enum class SearchState {
        Init,
        Idle,
        Searching,
    }

    sealed interface UIState {
        data object Idle : UIState
        data object Empty : UIState
        data object Loading : UIState
    }
}
