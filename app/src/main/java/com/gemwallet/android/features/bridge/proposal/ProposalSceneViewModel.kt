package com.gemwallet.android.features.bridge.proposal

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.bridge.getChainNameSpace
import com.gemwallet.android.data.repositoreis.bridge.getReference
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.features.bridge.model.PeerUI
import com.reown.walletkit.client.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        PeerUI(
            peerIcon = icons
                .firstOrNull{ it.endsWith("png", ignoreCase = true) || it.endsWith("jpg", ignoreCase = true) }
                ?: icons.firstOrNull()
                ?: "",
            peerName = it.name,
            peerDescription = it.description,
            peerUri = it.url.toUri().host ?: "",
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val availableWallets = _proposal.filterNotNull().mapLatest { proposal ->
        val requiredChains = proposal.requiredNamespaces.mapNotNull { entry ->
            entry.value.chains
        }.flatten()
        val optionalChains = proposal.optionalNamespaces.mapNotNull { entry ->
            entry.value.chains
        }.flatten()
        val availableChains = (requiredChains + optionalChains).toSet()
        val availableWallets = walletsRepository.getAll()
            .filter { it.type != WalletType.view }
            .filter {
                val namespaces = it.accounts.map { "${it.chain.getChainNameSpace()}:${it.chain.getReference()}" }
                availableChains.firstOrNull { namespaces.contains(it) } != null
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
        if (wallet != null) return@combine wallet
        session?.wallet?.takeIf { availableWallets.contains(it) } ?: availableWallets.firstOrNull()
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    fun onProposal(proposal: Wallet.Model.SessionProposal) {
        _proposal.update { proposal }
    }

    fun onApprove() {
        val wallet = selectedWallet.value
        val proposal = _proposal.value

        if (wallet == null || proposal == null) {
            state.update { ProposalSceneState.Canceled }
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    bridgesRepository.approveConnection(
                        wallet = wallet,
                        proposal = proposal,
                        onSuccess = { state.update { ProposalSceneState.Canceled } },
                        onError = { message -> state.update { ProposalSceneState.Fail(message) } }
                    )
                } catch (err: Throwable) {
                    state.update { ProposalSceneState.Fail(err.message ?: "Wallet connect error") }
                }
            }
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
        _proposal.value = null
    }
}

sealed interface ProposalSceneState {
    data object Init : ProposalSceneState

    data object Canceled : ProposalSceneState

    class Fail(val message: String) : ProposalSceneState
}