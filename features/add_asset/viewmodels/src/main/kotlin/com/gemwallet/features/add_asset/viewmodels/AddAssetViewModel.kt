package com.gemwallet.features.add_asset.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.ext.filter
import com.gemwallet.android.ext.getAccount
import com.gemwallet.features.add_asset.viewmodels.models.AddAssetError
import com.gemwallet.features.add_asset.viewmodels.models.AddAssetUIState
import com.gemwallet.features.add_asset.viewmodels.models.TokenSearchState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddAssetViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AddAssetUIState())
//    var input by mutableStateOf("")

    val chainFilter = TextFieldState()

    val availableChains = sessionRepository.session().mapLatest { session ->
        session?.wallet?.accounts?.map { it.chain }?.filter { it.assetType() != null }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val chains = snapshotFlow { chainFilter.text }.combine(availableChains) { query, availableChains ->
        availableChains?.filter(query.toString().lowercase()) ?: emptyList()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val defaultChain = availableChains.map {
        it.let {
            it?.firstOrNull { chain -> chain == Chain.Ethereum } ?: it?.firstOrNull() ?: Chain.Ethereum
        }
    }
    private val chain = MutableStateFlow<Chain?>(null)
    val selectedChain = defaultChain.combine(chain) {defaultChain, chain ->
        chain ?: defaultChain
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, Chain.Ethereum)

    val addressState = mutableStateOf("")
    val addressQuery = snapshotFlow { addressState.value }

    val searchState = addressQuery.combine(selectedChain) { address, chain ->
        Pair(address, chain)
    }.flatMapLatest {
        val (address, chain) = it
        flow {
            if (address.isEmpty()) {
                emit(TokenSearchState.Idle)
                return@flow
            }

            emit(TokenSearchState.Loading)

            val success = assetsRepository.searchToken(AssetId(chain, address))

            emit(
                if (success) {
                    TokenSearchState.Idle
                } else {
                    TokenSearchState.Error(AddAssetError.TokenNotFound)
                }
            )
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, TokenSearchState.Idle)

    val token = combine(addressQuery, selectedChain) { address, chain ->
        Pair(address.trim(), chain)
    }
    .flatMapLatest {
        val (address, chain) = it
        if (address.isEmpty()) {
            return@flatMapLatest flowOf(null)
        }
        assetsRepository.getToken(AssetId(chain, address))
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onQrScan() {
        state.update { it.copy(isQrScan = true) }
    }

    fun cancelScan() {
        state.update {
            it.copy(
                isQrScan = false,
            )
        }
    }

    fun setQrData(data: String) {
        addressState.value = data
        state.update { it.copy(isQrScan = false) }
    }

    fun selectChain() {
        state.update { it.copy(isSelectChain = true) }
    }

    fun cancelSelectChain() {
        state.update { it.copy(isSelectChain = false) }
    }

    fun setChain(chain: Chain) {
        this.chain.update { chain }
        state.update { it.copy(isSelectChain = false) }
    }

    fun addAsset(onFinish: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        onFinish()
        async {
            val session = sessionRepository.getSession() ?: return@async
            assetsRepository.switchVisibility(
                walletId = session.wallet.id,
                owner = session.wallet.getAccount(selectedChain.value) ?: return@async,
                assetId = token.value?.id ?: return@async,
                visibility = true,
            )
        }.await()
    }

    private data class State(
        val isQrScan: Boolean = false,
        val isSelectChain: Boolean = false,
    ) {
        fun toUIState(): AddAssetUIState {
            return AddAssetUIState(
                scene = when {
                    isQrScan -> AddAssetUIState.Scene.QrScanner
                    isSelectChain -> AddAssetUIState.Scene.SelectChain
                    else -> AddAssetUIState.Scene.Form
                },
            )
        }
    }
}