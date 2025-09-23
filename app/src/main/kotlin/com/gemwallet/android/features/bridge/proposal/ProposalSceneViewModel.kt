package com.gemwallet.android.features.bridge.proposal

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.bridge.getChainNameSpace
import com.gemwallet.android.data.repositoreis.bridge.getReference
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.features.bridge.model.SessionUI
import com.reown.walletkit.client.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProposalSceneViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val bridgesRepository: BridgesRepository,
    private val walletsRepository: WalletsRepository,
) : ViewModel() {
    
    val state = MutableStateFlow<ProposalSceneState>(ProposalSceneState.Init)

    private val _proposal = MutableStateFlow<Wallet.Model.SessionProposal?>(null)
    val proposal = _proposal.map {
        it ?: return@map null
        val icons = it.icons.map { it.toString() }
        SessionUI(
            id = "",
            icon = icons
                .firstOrNull{ it.endsWith("png", ignoreCase = true) || it.endsWith("jpg", ignoreCase = true) }
                ?: icons.firstOrNull()
                ?: "",
            name = it.name,
            description = it.description,
            uri = it.url.toUri().host ?: "",
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val availableWallets = _proposal.filterNotNull().mapLatest { proposal ->
        val availableChains = (proposal.requiredNamespaces.values.flatMap { it.chains.orEmpty() } +
                proposal.optionalNamespaces.values.flatMap { it.chains.orEmpty() }).toSet()
        val availableWallets = (walletsRepository.getAll().firstOrNull() ?: emptyList())
            .filter { wallet ->
                wallet.type != WalletType.view &&
                    wallet.accounts.any { "${it.chain.getChainNameSpace()}:${it.chain.getReference()}" in availableChains }
            }
            .sortedBy { it.type }
        availableWallets
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedWallet = MutableStateFlow<com.wallet.core.primitives.Wallet?>(null)
    val selectedWallet = combine(
        _selectedWallet,
        sessionRepository.session(),
        availableWallets,
    ) { wallet, session, availableWallets ->
        val current = session?.wallet
        wallet ?: availableWallets.firstOrNull { current?.id == it.id } ?: availableWallets.firstOrNull()
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    fun onProposal(proposal: Wallet.Model.SessionProposal) {
        state.update { ProposalSceneState.Init }
        _proposal.update { proposal }
    }

    fun onApprove() {
        val wallet = selectedWallet.value
        val proposal = _proposal.value

        if (wallet == null || proposal == null) {
            state.update { ProposalSceneState.Canceled }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                bridgesRepository.approveConnection(
                    wallet = wallet,
                    proposal = proposal,
                    onSuccess = { state.update { ProposalSceneState.Canceled } },
                    onError = { message -> state.update { ProposalSceneState.Fail(message) } }
                )
            }
            result.onFailure { err -> state.update { ProposalSceneState.Fail(err.message ?: "Wallet connect error") } }
        }
    }

    fun onReject() = viewModelScope.launch(Dispatchers.IO) {
        val proposal = _proposal.value ?: return@launch
        bridgesRepository.rejectConnection(
            proposal = proposal,
            onSuccess = { state.update { ProposalSceneState.Canceled } },
            onError = { state.update { ProposalSceneState.Canceled } }
        )
    }

    fun onWalletSelected(walletId: String) {
        _selectedWallet.update { availableWallets.value.firstOrNull { it.id == walletId } }
    }

    fun reset() {
        state.update { ProposalSceneState.Init }
        _proposal.value = null
    }
}

sealed interface ProposalSceneState {
    data object Init : ProposalSceneState

    data object Canceled : ProposalSceneState

    class Fail(val message: String) : ProposalSceneState
}