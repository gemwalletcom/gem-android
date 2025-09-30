package com.gemwallet.features.asset_select.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ui.components.list_item.AssetInfoUIModel
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.features.asset_select.viewmodels.models.SelectSearch
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
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

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseAssetSelectViewModel(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val searchTokensCase: SearchTokensCase,
    val search: SelectSearch,
) : ViewModel() {

    val queryState = TextFieldState()
    private val searchState = MutableStateFlow<SearchState>(SearchState.Init)
    val availableChains = sessionRepository.session()
        .map { session -> session?.wallet?.accounts?.map { it.chain } ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val chainFilter = MutableStateFlow<List<Chain>>(emptyList())
    val balanceFilter = MutableStateFlow<Boolean>(false)

    private val queryFlow = snapshotFlow<String> { queryState.text.toString() }
        .onEach {
            searchState.update { if (it != SearchState.Init) SearchState.Searching else it }
            viewModelScope.launch(Dispatchers.IO) {
                delay(250)
                searchTokensCase.search(it, sessionRepository.getSession()?.wallet?.accounts?.map { it.chain } ?: emptyList())
            }
        }
        .mapLatest { query -> query }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val assets = combine(
        chainFilter,
        balanceFilter,
        search(sessionRepository.session(), queryFlow)
    ) { chainFilter, balanceFilter, items ->
        val hasChainFilter = chainFilter.isNotEmpty()
        items.filter { // TODO: Move to model
            (!hasChainFilter || chainFilter.contains(it.id().chain))
                    && (!balanceFilter || it.balance.totalAmount > 0.0)
        }
    }
    .map { items -> items.map { AssetInfoUIModel(it) } }
    .onEach { searchState.update { SearchState.Idle } }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>())

    val pinned = assets.map { items: List<AssetItemUIModel> ->
        items.filter { it.metadata?.isPinned == true }.toImmutableList()
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>().toImmutableList())

    val unpinned = assets.map { items: List<AssetItemUIModel> ->
        items.filter { it.metadata?.isPinned != true }.toImmutableList()
    }
    .flowOn(Dispatchers.Default)
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
        session?.wallet?.accounts?.any { it.chain.assetType() != null } == true
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onChangeVisibility(assetId: AssetId, visible: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        val account = session.wallet.getAccount(assetId.chain) ?: return@launch
        assetsRepository.switchVisibility(session.wallet.id, account, assetId, visible)
    }

    fun onChainFilter(chain: Chain) {
        chainFilter.update {
            val chains = it.toMutableList()
            if (!chains.remove(chain)) {
                chains.add(chain)
            }
            chains.toList()
        }
    }

    fun onBalanceFilter(onlyWithBalance: Boolean) {
        balanceFilter.update { onlyWithBalance }
    }

    fun onClearFilres() {
        chainFilter.update { emptyList() }
        balanceFilter.update { false }
    }

    fun getAccount(assetId: AssetId): Account? {
        return sessionRepository.getSession()?.wallet?.getAccount(assetId)
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
