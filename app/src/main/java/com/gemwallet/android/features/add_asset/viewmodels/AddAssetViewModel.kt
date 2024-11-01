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
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.filter
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.tokenAvailableChains
import com.gemwallet.android.features.add_asset.models.AddAssetError
import com.gemwallet.android.features.add_asset.models.AddAssetUIState
import com.gemwallet.android.interactors.getIconUrl
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddAssetViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AddAssetUIState())
    var input by mutableStateOf("")
    val chainFilter = TextFieldState()

    init {
        viewModelScope.launch {
            val chains = getAvailableChains()
            state.update { it.copy(chains = chains, chain = chains.firstOrNull { chain -> chain == Chain.Ethereum } ?: chains.firstOrNull() ?: Chain.Ethereum) }
        }
        viewModelScope.launch {
            snapshotFlow { chainFilter.text }.collectLatest { query ->
                state.update { it.copy(chains = getAvailableChains().filter(query.toString().lowercase())) }
            }
        }
    }

    fun onQrScan() {
        state.update { it.copy(isQrScan = true) }
    }

    fun onQuery(data: String) {
        setQrData(data)
    }

    fun cancelScan() {
        state.update {
            it.copy(
                isQrScan = false,
            )
        }
    }

    fun setQrData(data: String) {
        state.update {
            it.copy(
                isQrScan = false,
                address = data
            )
        }
        searchToken(data)
    }

    fun selectChain() {
        state.update { it.copy(isSelectChain = true) }
    }

    fun cancelSelectChain() {
        state.update { it.copy(isSelectChain = false) }
    }

    fun setChain(chain: Chain) {
        state.update { it.copy(chain = chain, isSelectChain = false, address = "", error = AddAssetError.None) }
    }

    fun addAsset(onFinish: () -> Unit) = viewModelScope.launch {
        onFinish()
        async {
            val session = sessionRepository.getSession() ?: return@async
            assetsRepository.switchVisibility(
                walletId = session.wallet.id,
                owner = session.wallet.getAccount(state.value.chain) ?: return@async,
                assetId = state.value.asset?.id ?: return@async,
                visibility = true,
                currency = session.currency
            )
        }.await()
    }

    private fun searchToken(rawAddress: String) {
        val address = rawAddress.trim()
        if (address.isEmpty()) {
            return
        }
        state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val asset = assetsRepository.getAssetByTokenId(state.value.chain, address)
                state.update {
                    it.copy(
                        asset = asset,
                        address = address,
                        error = if (asset == null) AddAssetError.TokenNotFound else AddAssetError.None,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun getAvailableChains(): List<Chain> {
        val wallet = sessionRepository.getSession()?.wallet ?: return emptyList()
        val availableAccounts = wallet.accounts.map { it.chain }
        return tokenAvailableChains.filter {
            availableAccounts.contains(it)
        }
    }

    private data class State(
        val chains: List<Chain> = emptyList(),
        val chain: Chain = Chain.Ethereum,
        val isQrScan: Boolean = false,
        val address: String = "",
        val asset: Asset? = null,
        val isLoading: Boolean = false,
        val isSelectChain: Boolean = false,
        val error: AddAssetError = AddAssetError.None,
    ) {
        fun toUIState(): AddAssetUIState {
            return AddAssetUIState(
                scene = when {
                    isQrScan -> AddAssetUIState.Scene.QrScanner
                    isSelectChain -> AddAssetUIState.Scene.SelectChain
                    else -> AddAssetUIState.Scene.Form
                },
                networkIcon = chain.getIconUrl(),
                networkTitle = chain.asset().name,
                chains = chains,
                chain = chain,
                address = address,
                asset = asset,
                isLoading = isLoading,
                error = error,
            )
        }
    }
}