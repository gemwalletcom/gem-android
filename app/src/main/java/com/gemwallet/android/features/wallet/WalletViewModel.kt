package com.gemwallet.android.features.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.interactors.DeleteWalletOperator
import com.gemwallet.android.interactors.getIconUrl
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val passwordStore: PasswordStore,
    private val loadPrivateDataOperator: LoadPrivateDataOperator,
    private val sessionRepository: SessionRepository,
    private val deleteWalletOperator: DeleteWalletOperator,
) : ViewModel() {
    private val state = MutableStateFlow(WalletViewModelState())
    val uiState = state
        .map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WalletUIState.Success())

    fun init(walletId: String, isPhrase: Boolean) {
        viewModelScope.launch {
            val wallet = walletsRepository.getWallet(walletId)
            if (wallet == null) {
                state.update { it.copy(error = "Wallet not found") }
            } else {
                state.update { it.copy(wallet = wallet) }
            }
            if (isPhrase) {
                handleShowPhrase()
            }
        }
    }

    fun setWalletName(name: String) {
        viewModelScope.launch {
            val wallet = state.value.wallet ?: return@launch
            walletsRepository.updateWallet(wallet.copy(name = name))
            if (wallet.id == sessionRepository.getSession()?.wallet?.id) {
                val newWallet = walletsRepository.getWallet(wallet.id) ?: return@launch
                sessionRepository.setWallet(newWallet)
            }
        }
    }

    fun delete(onBoard: () -> Unit, onComplete: () -> Unit) {
        viewModelScope.launch {
            deleteWalletOperator(state.value.wallet?.id ?: return@launch, onBoard, onComplete)
        }
    }

    private suspend fun handleShowPhrase() {
        try {
            val wallet = state.value.wallet ?: return
            val password = passwordStore.getPassword(wallet.id)
            val phrase = loadPrivateDataOperator(wallet, password)
            state.update { it.copy(phrase = phrase) }
        } catch (err: Throwable) {
            state.update { it.copy(error = err.message) }
        }
    }
}

data class WalletViewModelState(
    val wallet: Wallet? = null,
    val phrase: String = "",
    val error: String? = null,
) {
    fun toUIState() = when {
        error != null -> WalletUIState.Fatal(error)
        wallet == null -> WalletUIState.Fatal("Wallet doesn't found")
        phrase.isNotEmpty() -> {
            WalletUIState.Phrase(
                walletName = wallet.name,
                walletType = wallet.type,
                words = if (phrase.isEmpty()) emptyList() else phrase.split(" "),
            )
        }
        else -> WalletUIState.Success(
            walletName = wallet.name,
            walletType = wallet.type,
            walletAddress = wallet.accounts.firstOrNull()?.address ?: "",
            chainIconUrl = wallet.accounts.firstOrNull()?.chain?.getIconUrl() ?: "",
        )
    }
}

sealed interface WalletUIState {
    data class Fatal(
        val message: String,
    ) : WalletUIState

    data class Success(
        val walletType: WalletType = WalletType.view,
        val walletName: String = "",
        val walletAddress: String = "",
        val chainIconUrl: String = "",
    ) : WalletUIState

    data class Phrase(
        val walletName: String,
        val walletType: WalletType,
        val words: List<String> = emptyList(),
    ) : WalletUIState
}

