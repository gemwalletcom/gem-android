package com.gemwallet.android.features.bridge.proposal

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.features.bridge.model.PeerUI
import com.reown.walletkit.client.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProposalSceneViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val bridgesRepository: BridgesRepository,
    private val walletsRepository: WalletsRepository,
) : ViewModel() {
    
    private val state = MutableStateFlow(ProposalViewModelState())
    val sceneState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProposalSceneState.Init)

    fun onProposal(proposal: Wallet.Model.SessionProposal, wallet: com.wallet.core.primitives.Wallet? = null) {
        state.update { it.copy(proposal = proposal, wallet = wallet ?: sessionRepository.getSession()?.wallet) }
    }

    fun onApprove() {
        val wallet = state.value.wallet
        val proposal = state.value.proposal

        if (wallet == null || proposal == null) {
            state.update { it.copy(canceled = true) }
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    bridgesRepository.approveConnection(
                        wallet = wallet,
                        proposal = proposal,
                        onSuccess = {
                            state.update { it.copy(canceled = true) }
                        },
                        onError = { message ->
                            state.update { it.copy(error = message) }
                        }
                    )
                } catch (err: Throwable) {
                    state.update { it.copy(error = err.message ?: "Wallet connect error") }
                }
            }
        }
    }

    fun onReject() = viewModelScope.launch(Dispatchers.IO) {
        val proposal = state.value.proposal ?: return@launch
        bridgesRepository.rejectConnection(
            proposal = proposal,
            onSuccess = { state.update { it.copy(canceled = true) } },
            onError = { state.update { it.copy(canceled = true) } }
        )
    }

    fun onWalletSelect() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val wallets = walletsRepository.getAll().filter { it.type != WalletType.view }
                state.update { it.copy(wallets = wallets) }
            }
        }
    }

    fun onWalletSelectCancel() {
        state.update { it.copy(wallets = null) }
    }

    fun onWalletSelected(walletId: String) {
        state.update { it.copy(wallets = null) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val wallet = walletsRepository.getAll().firstOrNull { it.id == walletId } ?: return@withContext
                onProposal(state.value.proposal ?: return@withContext, wallet)

            }
        }
    }

    fun reset() {
        state.update { ProposalViewModelState() }
    }
}

data class ProposalViewModelState(
    val error: String = "",
    val canceled: Boolean = false,
    val wallet: com.wallet.core.primitives.Wallet? = null,
    val proposal: Wallet.Model.SessionProposal? = null,
    val wallets: List<com.wallet.core.primitives.Wallet>? = null,
) {
    fun toUIState(): ProposalSceneState {
        if (error.isNotEmpty()) {
            return ProposalSceneState.Fail(error)
        }
        if (proposal == null) {
            return ProposalSceneState.Init
        }
        if (canceled) {
            return ProposalSceneState.Canceled
        }
        val icons = proposal.icons.map { it.toString() }
        return ProposalSceneState.Proposal(
            walletId = wallet?.id ?: "",
            walletName = wallet?.name ?: "",
            walletType = wallet?.type ?: WalletType.view,
            peer = PeerUI(
                peerIcon = icons
                    .firstOrNull{ it.endsWith("png", ignoreCase = true) || it.endsWith("jpg", ignoreCase = true) }
                    ?: icons.firstOrNull()
                    ?: "",
                peerName = proposal.name,
                peerDescription = proposal.description,
                peerUri = Uri.parse(proposal.url).host ?: "",
            ),
            wallets = wallets,
        )
    }
}

sealed interface ProposalSceneState {
    data object Init : ProposalSceneState

    class Proposal(
        val walletId: String,
        val walletType: WalletType,
        val walletName: String,
        val peer: PeerUI = PeerUI(),
        val wallets: List<com.wallet.core.primitives.Wallet>? = null,
    ) : ProposalSceneState

    data object Canceled : ProposalSceneState

    class Fail(val message: String) : ProposalSceneState
}