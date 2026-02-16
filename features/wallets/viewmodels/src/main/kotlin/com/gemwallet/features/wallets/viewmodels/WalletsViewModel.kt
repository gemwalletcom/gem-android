package com.gemwallet.features.wallets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.wallet.coordinators.DeleteWallet
import com.gemwallet.android.application.wallet.coordinators.GetAllWallets
import com.gemwallet.android.application.wallet.coordinators.SetCurrentWallet
import com.gemwallet.android.application.wallet.coordinators.ToggleWalletPin
import com.gemwallet.android.application.wallet.coordinators.pinned
import com.gemwallet.android.application.wallet.coordinators.unpinned
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletsViewModel @Inject constructor(
    private val getAllWallets: GetAllWallets,
    private val setCurrentWallet: SetCurrentWallet,
    private val toggleWalletPin: ToggleWalletPin,
    private val deleteWallet: DeleteWallet,
) : ViewModel() {

    val wallets = getAllWallets.getAllWallets()

    val pinned = wallets.pinned()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unpinned = wallets.unpinned()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectWallet(walletId: String) = viewModelScope.launch(Dispatchers.IO) {
        setCurrentWallet.setCurrentWallet(walletId)
    }

    fun deleteWallet(walletId: String, onBoard: () -> Unit) = viewModelScope.launch {
        deleteWallet.deleteWallet(walletId, onBoard) {}
    }

    fun togglePin(walletId: String) = viewModelScope.launch(Dispatchers.IO) {
        toggleWalletPin.toggleWalletPin(walletId)
    }
}