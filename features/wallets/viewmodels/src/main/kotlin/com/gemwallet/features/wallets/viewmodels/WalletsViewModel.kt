package com.gemwallet.features.wallets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.wallet.DeleteWallet
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletsViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val sessionRepository: SessionRepository,
    private val deleteWallet: DeleteWallet,
) : ViewModel() {

    private val wallets = walletsRepository.getAll()
        .map { items ->
            val watch = items.filter { it.type == WalletType.view }
            val single = items.filter { it.type == WalletType.single }
            val privateKey = items.filter { it.type == WalletType.private_key }
            val multi = items.filter { it.type == WalletType.multicoin }
            multi + single + privateKey + watch
        }
    private val session = sessionRepository.session()

    val currentWallet = session.map { it?.wallet }.filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val pinnedWallets = wallets.map { items -> items.filter { it.isPinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val unpinnedWallets = wallets.map { items -> items.filter { !it.isPinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectWallet(walletId: String) {
        viewModelScope.launch {
            val wallet = walletsRepository.getWallet(walletId).firstOrNull() ?: return@launch
            sessionRepository.setWallet(wallet)
        }
    }

    fun deleteWallet(walletId: String, onBoard: () -> Unit) = viewModelScope.launch {
        deleteWallet.deleteWallet(walletId, onBoard) {}
    }

    fun togglePin(walletId: String) = viewModelScope.launch {
        walletsRepository.togglePin(walletId)
    }
}