package com.gemwallet.android.features.add_asset.viewmodels

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
import com.gemwallet.android.features.add_asset.viewmodels.models.AddAssetError
import com.gemwallet.android.features.add_asset.viewmodels.models.AddAssetUIState
import com.gemwallet.android.features.add_asset.viewmodels.models.TokenSearchState
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

    private val state = MutableStateFlow(
        State(
            onSelectChain = this::selectChain,
            isSelectChainAvailable = isSelectChainAvailable()
        )
    )
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AddAssetUIState())
    var input by mutableStateOf("")

    val chainFilter = TextFieldState()
    val chains = snapshotFlow { chainFilter.text }.mapLatest { query ->
        getAvailableChains().filter(query.toString().lowercase())
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val chain = MutableStateFlow(
        getAvailableChains().let {
            it.firstOrNull { chain -> chain == Chain.Ethereum } ?: it.firstOrNull() ?: Chain.Ethereum
        }
    )

    val addressState = mutableStateOf("")
    val addressQuery = snapshotFlow { addressState.value.toString() }

    val searchState = addressQuery.combine(chain) { address, chain ->
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

    val token = combine(addressQuery, chain) { address, chain ->
        Pair(address.toString().trim(), chain)
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

    fun addAsset(onFinish: () -> Unit) = viewModelScope.launch {
        onFinish()
        async {
            val session = sessionRepository.getSession() ?: return@async
            assetsRepository.switchVisibility(
                walletId = session.wallet.id,
                owner = session.wallet.getAccount(chain.value) ?: return@async,
                assetId = token.value?.id ?: return@async,
                visibility = true,
            )
        }.await()
    }

    private fun getAvailableChains(): List<Chain> {
        val wallet = sessionRepository.getSession()?.wallet ?: return emptyList()
        return wallet.accounts.map { it.chain }.filter { it.assetType() != null }
    }

    fun isSelectChainAvailable(): Boolean = getAvailableChains().size > 1

    private data class State(
        val isQrScan: Boolean = false,
        val isSelectChain: Boolean = false,
        val isSelectChainAvailable: Boolean = true,
        val onSelectChain: (() -> Unit)?,
    ) {
        fun toUIState(): AddAssetUIState {
            return AddAssetUIState(
                scene = when {
                    isQrScan -> AddAssetUIState.Scene.QrScanner
                    isSelectChain -> AddAssetUIState.Scene.SelectChain
                    else -> AddAssetUIState.Scene.Form
                },
                onSelectChain = if (isSelectChainAvailable) onSelectChain else null,
            )
        }
    }
}