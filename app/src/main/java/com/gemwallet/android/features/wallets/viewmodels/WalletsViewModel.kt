package com.gemwallet.android.features.wallets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.features.assets.model.IconUrl
import com.gemwallet.android.interactors.getIconUrl
import com.wallet.core.primitives.Wallet
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
class WalletsViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val sessionRepository: SessionRepository,
    private val deleteKeyStoreOperator: DeleteKeyStoreOperator,
) : ViewModel() {
    private val state = MutableStateFlow(WalletsViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WalletsUIState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            walletsRepository.getAll().onSuccess { wallets ->
                val currentWallet = sessionRepository.session?.wallet ?: return@launch
                val other = wallets.filter { it.id != currentWallet.id }
                val watch = other.filter { it.type == WalletType.view }
                val single = other.filter { it.type == WalletType.single }
                val multi = other.filter { it.type == WalletType.multicoin }

                state.update {
                    it.copy(
                        currentWallet = currentWallet,
                        wallets = listOf(currentWallet) + multi + single + watch,
                    )
                }
            }
        }
    }

    fun handleSelectWallet(walletId: String) {
        viewModelScope.launch {
            walletsRepository.getWallet(walletId).onSuccess {
                sessionRepository.setWallet(it)
            }.onFailure {
                // TODO: Add error handle
            }
        }
    }

    fun handleDeleteWallet(walletId: String, onBoard: () -> Unit) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val session = sessionRepository.session ?: return@withContext
            val wallet = walletsRepository.getWallet(walletId).getOrNull() ?: return@withContext
            if (!walletsRepository.removeWallet(walletId = walletId).getOrElse { false }) {
                return@withContext
            }
            if (wallet.type == WalletType.multicoin) {
                if (!deleteKeyStoreOperator(walletId)) {
                    return@withContext
                }
            }
            if (session.wallet.id == walletId) {
                val wallets = walletsRepository.getAll().getOrNull() ?: emptyList()
                if (wallets.isEmpty()) {
                    sessionRepository.reset()
                    withContext(Dispatchers.Main) {
                        onBoard()
                    }
                } else {
                    sessionRepository.setWallet(wallets.first())
                }
            }
            refresh()
        }
    }
}

data class WalletsViewModelState(
    val currentWallet: Wallet? = null,
    val wallets: List<Wallet> = emptyList()
) {
    fun toUIState() = WalletsUIState(
        currentWalletId = currentWallet?.id ?: "",
        wallets = wallets.map {
            WalletItemUIState(
                id = it.id,
                name = it.name,
                type = it.type,
                typeLabel = when (it.type) {
                    WalletType.view -> it.accounts.firstOrNull()?.address?.substring(0, 10) ?: ""
                    WalletType.single -> it.accounts.firstOrNull()?.address?.substring(0, 10) ?: ""
                    WalletType.multicoin -> "Multi-coin"
                },
                icon = if (it.accounts.size > 1) {
                    ""
                } else {
                    it.accounts.firstOrNull()?.chain?.getIconUrl() ?: ""
                }
            )
        }
    )
}

data class WalletsUIState(
    val currentWalletId: String = "",
    val wallets: List<WalletItemUIState> = emptyList()
)

data class WalletItemUIState(
    val id: String,
    val name: String,
    val type: WalletType,
    val typeLabel: String,
    val icon: IconUrl = ""
)