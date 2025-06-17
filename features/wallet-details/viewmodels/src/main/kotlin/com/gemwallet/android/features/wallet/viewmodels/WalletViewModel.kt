package com.gemwallet.android.features.wallet.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.wallet.DeleteWallet
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.wallet.core.primitives.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val passwordStore: PasswordStore,
    private val loadPrivateDataOperator: LoadPrivateDataOperator,
    private val sessionRepository: SessionRepository,
    private val deleteWallet: DeleteWallet,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val walletId = savedStateHandle.getStateFlow<String?>("walletId", null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val wallet = walletId.flatMapLatest { walletsRepository.getWallet(it ?: return@flatMapLatest flowOf(null)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val phrase = savedStateHandle.getStateFlow("inPhrase", false).combine(wallet) { inPhrase, wallet ->
        if (inPhrase) handleShowPhrase(wallet) else null
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)



    private val state = MutableStateFlow(WalletViewModelState())
    val uiState = state
        .map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WalletUIState.Success)

//    fun init(walletId: String, isPhrase: Boolean) {
//        viewModelScope.launch {
//            val wallet = walletsRepository.getWallet(walletId)
//            if (wallet == null) {
//                state.update { it.copy(error = "Wallet not found") }
//            } else {
//                state.update { it.copy(wallet = wallet) }
//            }
//            if (isPhrase) {
//                handleShowPhrase()
//            }
//        }
//    }

    fun setWalletName(name: String) {
        viewModelScope.launch {
            val wallet = wallet.value ?: return@launch
            walletsRepository.updateWallet(wallet.copy(name = name))
            if (wallet.id == sessionRepository.getSession()?.wallet?.id) {
                val newWallet = walletsRepository.getWallet(wallet.id).firstOrNull() ?: return@launch
                sessionRepository.setWallet(newWallet)
            }
        }
    }

    fun delete(onBoard: () -> Unit, onComplete: () -> Unit) {
        viewModelScope.launch {
            deleteWallet.deleteWallet(wallet.value?.id ?: return@launch, onBoard, onComplete)
        }
    }

    private suspend fun handleShowPhrase(wallet: Wallet?): String? {
        try {
            val wallet = wallet ?: return null
            val password = passwordStore.getPassword(wallet.id)
            val phrase = loadPrivateDataOperator(wallet, password)
            return phrase
        } catch (err: Throwable) {
            state.update { it.copy(error = err.message) }
            return null
        }
    }
}

data class WalletViewModelState(
    val error: String? = null,
) {
    fun toUIState() = when {
        error != null -> WalletUIState.Fatal(error)
        else -> WalletUIState.Success
    }
}

sealed interface WalletUIState {
    data class Fatal(
        val message: String,
    ) : WalletUIState

    object Success : WalletUIState
}
