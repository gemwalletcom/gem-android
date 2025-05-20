package com.gemwallet.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.WalletConnectDelegate
import com.reown.walletkit.client.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletConnectViewModel @Inject constructor() : ViewModel() {

    private val state = MutableStateFlow<WalletConnectIntent>(WalletConnectIntent.None)
    val uiState = state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WalletConnectIntent.None)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            WalletConnectDelegate.walletEvents.collectLatest { event ->
                state.update { event.toUIState() }
            }
        }
    }

    fun onCancel() {
        state.update { WalletConnectIntent.None }
    }
}

sealed interface WalletConnectIntent {

    data object None : WalletConnectIntent

    data object SessionDelete : WalletConnectIntent

    class SessionRequest(val request: Wallet.Model.SessionRequest) : WalletConnectIntent

    class AuthRequest(val request: Wallet.Model.SessionAuthenticate) : WalletConnectIntent

    class SessionProposal(val sessionProposal: Wallet.Model.SessionProposal) : WalletConnectIntent

    class ConnectionState(val error: String?) : WalletConnectIntent
}

private fun Wallet.Model.toUIState(): WalletConnectIntent {
    return when (this) {
        is Wallet.Model.SessionRequest -> WalletConnectIntent.SessionRequest(this)

        is Wallet.Model.SessionAuthenticate -> WalletConnectIntent.AuthRequest(this)

        is Wallet.Model.SessionDelete -> WalletConnectIntent.SessionDelete
        is Wallet.Model.SessionProposal -> WalletConnectIntent.SessionProposal(this)

        is Wallet.Model.ConnectionState -> if (this.isAvailable) {
            WalletConnectIntent.ConnectionState(null)
        } else {
            WalletConnectIntent.ConnectionState(error = "No Internet connection, please check your internet connection and try again")
        }
        else -> WalletConnectIntent.None
    }
}