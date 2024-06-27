package com.gemwallet.android.features.bridge.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.SignTransfer
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.features.bridge.model.PeerUI
import com.gemwallet.android.features.bridge.model.findByNamespace
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletConnectionMethods
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import wallet.core.jni.EthereumAbi
import wallet.core.jni.Hash
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
    val signClient: SignTransfer,
) : ViewModel() {
    private val state = MutableStateFlow(RequestViewModelState())
    val sceneState = state.map { it.toSceneState() }.stateIn(viewModelScope, SharingStarted.Eagerly, RequestSceneState.Loading)

    fun onRequest(request: Wallet.Model.SessionRequest) {
        val wallet = sessionRepository.getSession()?.wallet
        val chainId = request.chainId?.split(":") ?: return // TODO: Cancel
        val chain = Chain.findByNamespace(chainId[0], chainId[1]) ?: return
        val params = when (request.request.method) {
            WalletConnectionMethods.solana_sign_message.string,
            WalletConnectionMethods.eth_sign.string -> {
                val data = JSONArray(request.request.params).getString(1)
                String(data.decodeHex())
            }
            WalletConnectionMethods.eth_sign_typed_data.string -> {
                JSONArray(request.request.params).getString(1)
            }
            WalletConnectionMethods.personal_sign.string -> {
                val data = JSONArray(request.request.params).getString(0)
                String(data.decodeHex())
            }
            WalletConnectionMethods.eth_send_transaction.string -> request.request.params
            else -> return // TODO: eth_sign_transaction
        }
        state.update { it.copy(sessionRequest = request, wallet = wallet, chain = chain, params = params) }
    }

    fun onSent(hash: String) {
        val sessionRequest = state.value.sessionRequest ?: return // TODO: Handle error

        viewModelScope.launch(Dispatchers.IO) {
            val response = Wallet.Params.SessionRequestResponse(
                sessionTopic = sessionRequest.topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                    sessionRequest.request.id,
                    hash,
                )
            )

            Web3Wallet.respondSessionRequest(response,
                onSuccess = {
                    state.update { it.copy(canceled = true) }
                },
                onError = { error ->
                    state.update { it.copy(error = error.throwable.message ?: "Can't sent hash to WalletConnect") }
                })
        }
    }

    fun onSign() {
        val session = sessionRepository.getSession() ?: return
        val sessionRequest = state.value.sessionRequest ?: return // TODO: Handle error
        val chain = state.value.chain ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val method = WalletConnectionMethods.entries
                .firstOrNull { it.string == sessionRequest.request.method }
            val param = when (method) {
                WalletConnectionMethods.eth_sign,
                WalletConnectionMethods.personal_sign -> {
                    val data = state.value.params.toByteArray()
                    val messagePrefix = "\u0019Ethereum Signed Message:\n${data.size}"
                    val prefix = messagePrefix.toByteArray()
                    Hash.keccak256(prefix + data)
                }
                WalletConnectionMethods.eth_sign_typed_data -> EthereumAbi.encodeTyped(state.value.params)
                WalletConnectionMethods.solana_sign_message -> state.value.params.toByteArray()
                else -> return@launch
            }

            val password = passwordStore.getPassword(session.wallet.id)
            val privateKey = loadPrivateKeyOperator(session.wallet.id, chain, password)
            val signResult = signClient(chain, param, privateKey)
            signResult.onSuccess { signedData ->
                Web3Wallet.respondSessionRequest(
                    params = Wallet.Params.SessionRequestResponse(
                        sessionTopic = sessionRequest.topic,
                        jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                            sessionRequest.request.id,
                            signedData.toHexString()
                        )
                    ),
                    onSuccess = { state.update { it.copy(canceled = true) } },
                    onError = { error ->
                        state.update { it.copy(error = error.throwable.message ?: "Can't sent sign to WalletConnect") }
                    }
                )
            }.onFailure { err ->
                state.update { it.copy(error = err.message ?: "Sign error") }
            }
        }
    }

    fun onReject() {
        val sessionRequest = state.value.sessionRequest ?: return
        val result = Wallet.Params.SessionRequestResponse(
            sessionTopic = sessionRequest.topic,
            jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                id = sessionRequest.request.id,
                code = 500,
                message = "Reject"
            )
        )

        Web3Wallet.respondSessionRequest(
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
    val sessionRequest: Wallet.Model.SessionRequest? = null,
    val chain: Chain? = null,
    val params: String = "",
) {
    fun toSceneState(): RequestSceneState {
        if (canceled) {
            return RequestSceneState.Cancel
        }
        if (sessionRequest == null) {
            return RequestSceneState.Loading
        }
        return when (sessionRequest.request.method) {
            WalletConnectionMethods.eth_sign.string,
            WalletConnectionMethods.personal_sign.string,
            WalletConnectionMethods.eth_sign_typed_data.string -> RequestSceneState.SignMessage(
                walletName = wallet?.name ?: "",
                peer = PeerUI(
                    peerName = sessionRequest.peerMetaData?.name ?: "",
                    peerIcon = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    peerDescription = sessionRequest.peerMetaData?.description ?: "",
                    peerUri = sessionRequest.peerMetaData?.description ?: "",
                ),
                params = params,

            )
            WalletConnectionMethods.eth_send_transaction.string -> {
                try {
                    val jObj = JSONArray(params).getJSONObject(0)
                    val to = jObj.getString("to")
                    val value = jObj.optString("value", "0x0").hexToBigInteger() ?: BigInteger.ZERO
                    val data = jObj.getString("data")
                    RequestSceneState.SendTransaction(
                        chain = chain!!,
                        to = to,
                        value = value,
                        data = data,
                    )
                } catch (err: Throwable) {
                    RequestSceneState.Error("Argument error: ${err.message}")
                }
            }
            else -> RequestSceneState.Error("Unsupported method")
        }
    }
}

sealed interface RequestSceneState {

    data object Loading : RequestSceneState

    data object Cancel : RequestSceneState

    class Error(val message: String) : RequestSceneState

    class SignMessage(
        val walletName: String,
        val peer: PeerUI,
        val params: String,
    ) : RequestSceneState

    class SendTransaction(
        val chain: Chain,
        val to: String,
        val value: BigInteger,
        val data: String,
    ) : RequestSceneState
}

