package com.gemwallet.android.features.asset_select.viewmodels

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.tokens.TokensRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.tokenAvailableChains
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.features.assets.model.toUIModel
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetSelectViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val tokensRepository: TokensRepository,
) : ViewModel() {
    val queryState = TextFieldState()
    private var queryJob: Job? = null
    private val queryFlow = snapshotFlow { queryState.text }

    val isLoading = MutableStateFlow(true)

    val isAddAssetAvailable = sessionRepository.session().map { session ->
        val availableAccounts = session?.wallet?.accounts?.map { it.chain } ?: emptyList()
        tokenAvailableChains.any { availableAccounts.contains(it) }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val assets = sessionRepository.session().combine(queryFlow) { session, query ->
        Pair(session, query)
    }
    .flatMapLatest { assetsRepository.search(it.first?.wallet ?: return@flatMapLatest emptyFlow(), it.second.toString()) }
    .map { assets: List<AssetInfo> ->
        assets.distinctBy { it.asset.id.toIdentifier() }.sortedByDescending {
            it.balances.available().convert(it.asset.decimals, it.price?.price?.price ?: 0.0).atomicValue
        }
    }
    .map { assets -> assets.map { it.toUIModel() }.toImmutableList() }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<AssetUIState>().toImmutableList())

    fun onChangeVisibility(assetId: AssetId, visible: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        val account = session.wallet.getAccount(assetId.chain) ?: return@launch
        assetsRepository.switchVisibility(account, assetId, visible, session.currency)
    }

    fun onQuery() = viewModelScope.launch(Dispatchers.IO) {
        queryFlow.collectLatest {
            if (queryJob?.isActive == true) {
                try {
                    queryJob?.cancel()
                } catch (err: Throwable) {
                    Log.d("ASSET_SELECT_VIEWMODEL", "Error on cancel job", err)
                }
            }
            queryJob = viewModelScope.launch {
                delay(250)
                isLoading.update { true }
                tokensRepository.search(it.toString())
                isLoading.update { false }
            }
        }
    }
}
