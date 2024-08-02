package com.gemwallet.android.features.wallets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.features.assets.model.IconUrl
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
class WalletsViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val sessionRepository: SessionRepository,
    private val deleteWalletOperator: DeleteWalletOperator,
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
                val currentWallet = sessionRepository.getSession()?.wallet ?: return@launch
                val watch = wallets.filter { it.type == WalletType.view }
                val single = wallets.filter { it.type == WalletType.single }
                val privateKey = wallets.filter { it.type == WalletType.private_key }
                val multi = wallets.filter { it.type == WalletType.multicoin }

                state.update {
                    it.copy(
                        currentWallet = currentWallet,
                        wallets = multi + single + privateKey + watch,
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
        deleteWalletOperator(walletId, onBoard, ::refresh)
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
                    WalletType.view,
                    WalletType.private_key,
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