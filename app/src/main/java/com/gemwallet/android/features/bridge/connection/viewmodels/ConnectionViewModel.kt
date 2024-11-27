package com.gemwallet.android.features.bridge.connection.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.bridge.WalletConnectDelegate
import com.gemwallet.android.features.bridge.connection.model.ConnectionSceneState
import com.gemwallet.android.features.bridge.model.ConnectionUI
import com.reown.walletkit.client.Wallet
import com.wallet.core.primitives.WalletConnection
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
class ConnectionViewModel @Inject constructor(
    private val bridgesRepository: BridgesRepository,
) : ViewModel() {
    private val state = MutableStateFlow(ConnectionsViewModelState())
    val sceneState = state.map { it.toSceneState() }.stateIn(viewModelScope, SharingStarted.Eagerly,
        ConnectionSceneState()
    )

    fun init(connectionId: String) {
        refresh(connectionId)
        viewModelScope.launch(Dispatchers.IO) {
            WalletConnectDelegate.walletEvents.collectLatest { event ->
                when (event) {
                    is Wallet.Model.ConnectionState,
                    is Wallet.Model.Session,
                    is Wallet.Model.SessionDelete.Error,
                    is Wallet.Model.SessionDelete.Success,
                    is Wallet.Model.SessionUpdateResponse.Error,
                    is Wallet.Model.SessionUpdateResponse.Result,
                    is Wallet.Model.SettledSessionResponse.Error,
                    is Wallet.Model.SettledSessionResponse.Result -> refresh(connectionId)
                    else -> {}
                }
            }
        }
    }

    fun refresh(connectionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val connection = bridgesRepository
                .getConnections()
                .firstOrNull { it.session.id == connectionId }
            state.update { it.copy(connection = connection) }
        }
    }

    fun disconnect(onSuccess: () -> Unit) {
        val id = state.value.connection?.session?.id
        if (id == null) {
            onSuccess()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            bridgesRepository
                .disconnect(
                    id,
                    onSuccess = {
                        viewModelScope.launch(Dispatchers.Main) {
                            onSuccess()
                        }
                    },
                ) {
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess()
                    }
                }
        }
    }
}

data class ConnectionsViewModelState(
    val error: String? = null,
    val pairError: String? = null,
    val connection: WalletConnection? = null,
) {
    fun toSceneState(): ConnectionSceneState {
        return ConnectionSceneState(
            walletName = connection?.wallet?.name ?: "",
            connection = connection?.toUI() ?: ConnectionUI(),
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
