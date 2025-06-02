package com.gemwallet.android.features.wallets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.wallet.DeleteWallet
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.getIconUrl
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletsViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val sessionRepository: SessionRepository,
    private val deleteWallet: DeleteWallet,
) : ViewModel() {
    private val state = MutableStateFlow(WalletsViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WalletsUIState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val wallets = walletsRepository.getAll().firstOrNull() ?: emptyList()
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

    fun handleSelectWallet(walletId: String) {
        viewModelScope.launch {
            val wallet = walletsRepository.getWallet(walletId).firstOrNull() ?: return@launch
            sessionRepository.setWallet(wallet)
        }
    }

    fun handleDeleteWallet(walletId: String, onBoard: () -> Unit) = viewModelScope.launch {
        deleteWallet.deleteWallet(walletId, onBoard, ::refresh)
    }

    fun onTogglePin(walletId: String) = viewModelScope.launch {
        walletsRepository.togglePin(walletId)
        refresh()
    }
}

data class WalletsViewModelState(
    val currentWallet: Wallet? = null,
    val wallets: List<Wallet> = emptyList()
) {
    fun toUIState() = WalletsUIState(
        currentWalletId = currentWallet?.id ?: "",
        wallets = wallets.filter { !it.isPinned }.map { it.toUIState() },
        pinnedWallets = wallets.filter { it.isPinned }.map { it.toUIState() }
    )
}

private fun Wallet.toUIState() = WalletItemUIState(
    id = id,
    name = name,
    type = type,
    pinned = isPinned,
    typeLabel = when (type) {
        WalletType.view,
        WalletType.private_key,
        WalletType.single -> accounts.firstOrNull()?.address ?: ""
        WalletType.multicoin -> "Multi-coin"
    },
    icon = if (accounts.size > 1) {
        R.drawable.multicoin_wallet
    } else {
        accounts.firstOrNull()?.chain?.getIconUrl() ?: ""
    }
)

data class WalletsUIState(
    val currentWalletId: String = "",
    val wallets: List<WalletItemUIState> = emptyList(),
    val pinnedWallets: List<WalletItemUIState> = emptyList()
)

data class WalletItemUIState(
    val id: String,
    val name: String,
    val type: WalletType,
    val pinned: Boolean,
    val typeLabel: String,
    val icon: Any,
)