package com.gemwallet.android.features.bridge.connections.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.bridge.WalletConnectDelegate
import com.gemwallet.android.features.bridge.connections.model.ConnectionsSceneState
import com.gemwallet.android.features.bridge.model.ConnectionUI
import com.wallet.core.primitives.WalletConnection
import com.walletconnect.web3.wallet.client.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val bridgesRepository: BridgesRepository,
) : ViewModel() {

    private val state = MutableStateFlow(ConnectionsViewModelState())
    val sceneState = state.map { it.toSceneState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectionsSceneState())

    init {
        refresh()
        viewModelScope.launch(Dispatchers.IO) {
            WalletConnectDelegate.walletEvents.collectLatest { event ->
                when (event) {
                    is Wallet.Model.ConnectionState,
                    is Wallet.Model.Session,
                    is Wallet.Model.SessionEvent,
                    is Wallet.Model.SessionDelete,
                    is Wallet.Model.SessionDelete.Error,
                    is Wallet.Model.SessionDelete.Success,
                    is Wallet.Model.SessionUpdateResponse.Error,
                    is Wallet.Model.SessionUpdateResponse.Result,
                    is Wallet.Model.SettledSessionResponse.Error,
                    is Wallet.Model.SettledSessionResponse.Result -> refresh()
                    else -> {}
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val sessions = bridgesRepository.getConnections()
            state.update { it.copy(connections = sessions) }
        }
    }

    fun addPairing(uri: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            bridgesRepository.addPairing(
                uri = uri,
                onSuccess = { refresh() },
                onError = { msg -> state.update { it.copy(pairError = msg) } }
            )
        }
    }

    fun resetErrors() {
        state.update { it.copy(error = null, pairError = null) }
    }
}

data class ConnectionsViewModelState(
    val error: String? = null,
    val pairError: String? = null,
    val connections: List<WalletConnection> = emptyList(),
    val connection: WalletConnection? = null,
) {
    fun toSceneState(): ConnectionsSceneState {
        return ConnectionsSceneState(
            error = error,
            pairError = pairError,
            connections = connections.map { it.toUI() },
        )
    }

    private fun WalletConnection.toUI() = ConnectionUI(
        icon = session.metadata.icon,
        name = session.metadata.name,
        uri = session.metadata.url,
        id = session.id,
        expire = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(session.expireAt)),
    )
}
