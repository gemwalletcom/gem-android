package com.gemwallet.features.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.serializer.jsonEncoder
import com.gemwallet.features.bridge.viewmodels.model.BridgeRequestError
import com.gemwallet.features.bridge.viewmodels.model.WCRequest
import com.gemwallet.features.bridge.viewmodels.model.map
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WCSolanaSignMessageResult
import com.wallet.core.primitives.WCSuiSignAndExecuteTransactionResult
import com.wallet.core.primitives.WCSuiSignTransactionResult
import com.wallet.core.primitives.WalletConnectionMethods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WCRequestViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val bridgeRepository: BridgesRepository,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
    val signClient: SignClientProxy,
) : ViewModel() {

    private val state = MutableStateFlow(RequestViewModelState())
    val sceneState = state.map { it.toSceneState() }.stateIn(viewModelScope, SharingStarted.Eagerly, RequestSceneState.Loading)

    fun onRequest(sessionRequest: Wallet.Model.SessionRequest, onCancel: () -> Unit) = viewModelScope.launch {
        val connection = bridgeRepository.getConnectionByTopic(sessionRequest.topic)
        if (connection == null) {
            onCancel()
            return@launch
        }
        val wallet = walletsRepository.getWallet(connection.wallet.id).firstOrNull()
        if (wallet == null) {
            onCancel()
            return@launch
        }

        try {
            val request = sessionRequest.map(wallet)

            if  (request is WCRequest.WalletSwitchEthereumChain) {
                onSwitch(sessionRequest, onCancel)
                return@launch
            }
            state.update {
                it.copy(
                    request = request,
                    wallet = wallet,
                    chain = request.chain,
                )
            }
        } catch (_: BridgeRequestError.MethodUnsupported) {
            state.update { it.copy(error = "Unsupported method: ${sessionRequest.request.method}") }
        } catch (_: BridgeRequestError.ChainUnsupported) {
            onCancel()
        } catch (_: Throwable) {
            state.update { it.copy(error = "Unsupported method: ${sessionRequest.request.method}") }
        }
    }

    fun onSent(hash: String) {
        val sessionRequest = state.value.request?.data ?: return
        val data = when (state.value.request?.method) {
            WalletConnectionMethods.SolanaSignTransaction.string -> jsonEncoder.encodeToString(WCSolanaSignMessageResult(signature = hash))
            WalletConnectionMethods.SuiSignAndExecuteTransaction.string -> {
                jsonEncoder.encodeToString(WCSuiSignAndExecuteTransactionResult(hash))
            }
            else -> hash
        }

        viewModelScope.launch(Dispatchers.IO) {
            WalletKit.respondSessionRequest(
                params = Wallet.Params.SessionRequestResponse(
                    sessionTopic = sessionRequest.topic,
                    jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(sessionRequest.request.id, data)
                ),
                onSuccess = { state.update { it.copy(canceled = true) } },
                onError = { error -> state.update { it.copy(error = error.throwable.message ?: "Can't sent data to WalletConnect") } }
            )
        }
    }

    fun onSwitch(request: Wallet.Model.SessionRequest, onCancel: () -> Unit) {
        WalletKit.respondSessionRequest(
            params = Wallet.Params.SessionRequestResponse(
                sessionTopic = request.topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                    request.request.id,
                    null,
                )
            ),
            onSuccess = {  },
            onError = { error -> }
        )
        onCancel()
    }

    fun onSigned(data: String) {
        val sessionRequest = state.value.request?.data ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val method = WalletConnectionMethods.entries
                .firstOrNull { it.string == sessionRequest.request.method }

            val sign = try {
                when (method) {
                    WalletConnectionMethods.SuiSignTransaction -> {
                        val (signature, bytes) = data.split("_").takeIf { it.size > 1 }?.let {
                            Pair(it[1], it[0])
                        } ?: throw IllegalStateException("Incorrect sign: not found transactionBytes")
                        val result = WCSuiSignTransactionResult(
                            signature = signature,
                            transactionBytes = bytes,
                        )
                        jsonEncoder.encodeToString(result)
                    }
                    WalletConnectionMethods.SolanaSignTransaction -> {
                        jsonEncoder.encodeToString(WCSolanaSignMessageResult(data))
                    }
                    WalletConnectionMethods.EthSignTransaction -> {
                        data
                    }
                    else -> return@launch
                }
            } catch (err: Throwable) {
                state.update { it.copy(error = err.message ?: "Sign error") }
                return@launch
            }

            WalletKit.respondSessionRequest(
                params = Wallet.Params.SessionRequestResponse(
                    sessionTopic = sessionRequest.topic,
                    jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                        sessionRequest.request.id,
                        sign
                    )
                ),
                onSuccess = { state.update { it.copy(canceled = true) } },
                onError = { error ->
                    state.update { it.copy(error = error.throwable.message ?: "Can't sent sign to WalletConnect") }
                }
            )
        }
    }

    fun onSign() {
        val request = state.value.request ?: return
        val wallet = state.value.wallet ?: return
        val chain = state.value.chain ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val password = passwordStore.getPassword(wallet.id)
            val privateKey = loadPrivateKeyOperator(wallet, chain, password)
            val sign = try {
                request.sign(signClient, privateKey)
            } catch (err: Throwable) {
                state.update { it.copy(error = err.message ?: "Sign error") }
                return@launch
            }
            WalletKit.respondSessionRequest(
                params = Wallet.Params.SessionRequestResponse(
                    sessionTopic = request.data.topic,
                    jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                        request.data.request.id,
                        sign
                    )
                ),
                onSuccess = { state.update { it.copy(canceled = true) } },
                onError = { error ->
                    state.update { it.copy(error = error.throwable.message ?: "Can't sent sign to WalletConnect") }
                }
            )
        }
    }

    fun onReject() {
        val sessionRequest = state.value.request?.data ?: return
        val result = Wallet.Params.SessionRequestResponse(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                id = sessionRequest.request.id,
                code = 500,
                message = "Reject"
            )
        )

        WalletKit.respondSessionRequest(
            result,
            onSuccess = { state.update { it.copy(canceled = true) } },
            onError = { state.update { it.copy(canceled = true) } }
        )
    }

    fun reset() {
        state.update { RequestViewModelState() }
    }
}

private data class RequestViewModelState(
    val error: String? = null,
    val canceled: Boolean = false,
    val wallet: com.wallet.core.primitives.Wallet? = null,
    val request: WCRequest? = null,
    val chain: Chain? = null,
    val params: String = "",
) {
    fun toSceneState(): RequestSceneState {
        if (canceled) {
            return RequestSceneState.Cancel
        }
        if (error != null) {
            RequestSceneState.Error(error)
        }
        if (request == null) {
            return RequestSceneState.Loading
        }
        wallet ?: return RequestSceneState.Loading

        return RequestSceneState.Request(walletName = wallet.name, request = request)
    }
}

sealed interface RequestSceneState {

    data object Loading : RequestSceneState

    data object Cancel : RequestSceneState

    class Error(val message: String) : RequestSceneState

    class Request(
        val walletName: String,
        val request: WCRequest,
    ) : RequestSceneState
}

