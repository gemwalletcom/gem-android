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
import com.gemwallet.android.model.RecentType
import com.gemwallet.android.model.Session
import com.gemwallet.android.ui.components.list_item.AssetInfoUIModel
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.features.asset_select.viewmodels.models.SearchState
import com.gemwallet.features.asset_select.viewmodels.models.SelectAssetFilters
import com.gemwallet.features.asset_select.viewmodels.models.SelectSearch
import com.gemwallet.features.asset_select.viewmodels.models.UIState
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetTag
import com.wallet.core.primitives.Chain
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseAssetSelectViewModel(
    sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val searchTokensCase: SearchTokensCase,
    val search: SelectSearch,
) : ViewModel() {

    val queryState = TextFieldState()
    val selectedTag = MutableStateFlow<AssetTag?>(null)
    val chainFilter = MutableStateFlow<List<Chain>>(emptyList())
    val balanceFilter = MutableStateFlow(false)

    private val session = sessionRepository.session()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val searchState = MutableStateFlow(SearchState.Init)

    val availableChains = session
        .map { session -> session?.wallet?.accounts?.map { it.chain } ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val filters = combine(
        session,
        snapshotFlow { queryState.text.toString() },
        selectedTag,
        chainFilter,
        balanceFilter,
    ) {
        SelectAssetFilters(
            session = it[0] as Session?,
            query = it[1].toString(),
            tag = (it[2] as? AssetTag?),
            chainFilter = (it[3] as? List<Chain>) ?: emptyList(),
            hasBalance = it[4] as Boolean,
        )
    }.onEach { filters ->
        searchState.update { if (it != SearchState.Init) SearchState.Searching else it }
        request(filters.query, filters.tag, filters.session)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assets = combine(
        filters,
        search.items(filters),
    ) { filters, items ->
        val chainFilter = filters?.chainFilter
        val balanceFilter = filters?.hasBalance ?: false
        val hasChainFilter = chainFilter?.isNotEmpty() ?: false

        items.filter {
            (!hasChainFilter || chainFilter.contains(it.id().chain))
                    && (!balanceFilter || it.balance.totalAmount > 0.0)
        }
    }
    .map { items -> items.map { AssetInfoUIModel(it) } }
//    .onEach { searchState.update { SearchState.Idle } }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>())

    val popular = assets.map { items ->
        items.filter {
            listOf(
                AssetId(Chain.Ethereum),
                AssetId(Chain.Bitcoin),
                AssetId(Chain.Solana),
            ).contains(it.asset.id)
        }.toImmutableList()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>().toImmutableList())

    val pinned = assets.map { items: List<AssetItemUIModel> ->
        items.filter { it.metadata?.isPinned == true }.toImmutableList()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>().toImmutableList())

    val unpinned = assets.map { items: List<AssetItemUIModel> ->
        items.filter { it.metadata?.isPinned != true }.toImmutableList()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetItemUIModel>().toImmutableList())

    val recent = filters.flatMapLatest { filters ->
        val type = getRecentType()
        if (filters?.query?.isEmpty() != true || type == null) {
            return@flatMapLatest flow { emit(emptyList()) }
        }
        val chainFilter = filters.chainFilter
        val balanceFilter = filters.hasBalance
        val hasChainFilter = chainFilter.isNotEmpty()

        assetsRepository.getRecentActivities(RecentType.entries).map { items ->
            items.filter {
                (!hasChainFilter || chainFilter.contains(it.id().chain))
                        && (!balanceFilter || it.balance.totalAmount > 0.0)
            }
        }
        .map { items -> search.filter(items) }
    }
    .map { items -> items.map { AssetInfoUIModel(it) }.toImmutableList() }
    .flowOn(Dispatchers.IO)
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

    fun onChangeVisibility(assetId: AssetId, visible: Boolean) = viewModelScope.launch {
        val session = session.value ?: return@launch
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

    fun onTagSelect(tag: AssetTag?) {
        selectedTag.update { tag }
    }

    open fun getTags(): List<AssetTag?> = listOf(
        null,
        AssetTag.Trending,
        AssetTag.Stablecoins,
    )

    fun onBalanceFilter(onlyWithBalance: Boolean) {
        balanceFilter.update { onlyWithBalance }
    }

    fun onClearFilters() {
        chainFilter.update { emptyList() }
        balanceFilter.update { false }
    }

    fun getAccount(assetId: AssetId): Account? {
        return session.value?.wallet?.getAccount(assetId)
    }

    private fun request(query: String, tags: AssetTag?, session: Session?) = viewModelScope.launch(Dispatchers.IO) {
        delay(250)
        searchTokensCase.search(
            query = query,
            chains = session?.wallet?.accounts?.map { it.chain } ?: emptyList(),
            tags = tags?.let { listOf(it) } ?: emptyList(),
        )
        searchState.update { SearchState.Idle }
    }

    open fun getRecentType(): RecentType? = RecentType.Receive
}
