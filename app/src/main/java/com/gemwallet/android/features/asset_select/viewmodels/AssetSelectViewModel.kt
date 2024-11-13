package com.gemwallet.android.features.asset_select.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.tokenAvailableChains
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

fun interface GetExclude {
    fun invoke(): Flow<List<String>>
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetSelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
) : BaseAssetSelectViewModel(sessionRepository, assetsRepository, searchTokensCase, { MutableStateFlow(emptyList()) })

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseAssetSelectViewModel(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val searchTokensCase: SearchTokensCase,
    getExclude: GetExclude,
) : ViewModel() {

    val queryState = TextFieldState()
    private val queryFlow = snapshotFlow<String> { queryState.text.toString() }
        .onEach { searchState.update { SearchState.Searching } }
        .mapLatest { query ->
            delay(250)
            searchTokensCase.search(query)
            query
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val searchState = MutableStateFlow<SearchState>(SearchState.Searching)

    val assets = combine(sessionRepository.session(), queryFlow, getExclude.invoke()) { session, query, exclude ->
        Triple(session, query, exclude)
    }
    .flatMapLatest {
        val wallet = it.first?.wallet ?: return@flatMapLatest emptyFlow()
        assetsRepository.search(wallet, it.second, isSearchByAllWallets(), it.third)
    }
    .map {
        it.distinctBy { it.asset.id.toIdentifier() }
    }
    .map { assets -> assets.filter(::filterAsset).map { AssetInfoUIModel(it) }.toImmutableList() }
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

    val isAddAssetAvailable = sessionRepository.session().map { session ->
        val availableAccounts = session?.wallet?.accounts?.map { it.chain } ?: emptyList()
        tokenAvailableChains.any { availableAccounts.contains(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onChangeVisibility(assetId: AssetId, visible: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        val account = session.wallet.getAccount(assetId.chain) ?: return@launch
        assetsRepository.switchVisibility(session.wallet.id, account, assetId, visible, session.currency)
    }

    open fun filterAsset(assetInfo: AssetInfo): Boolean = true

    internal open fun isSearchByAllWallets() = false

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
