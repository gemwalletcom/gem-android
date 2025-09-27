package com.gemwallet.android.features.bridge.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.bridge.getNamespace
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.features.bridge.viewmodel.model.SessionUI
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.serializer.jsonEncoder
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WCSolanaSignMessageResult
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
import org.json.JSONArray
import org.json.JSONObject
import uniffi.gemstone.SignDigestType
import uniffi.gemstone.SignMessage
import uniffi.gemstone.SignMessageDecoder
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val bridgeRepository: BridgesRepository,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
    val signClient: SignClientProxy,
) : ViewModel() {

    private val state = MutableStateFlow(RequestViewModelState())
    val sceneState = state.map { it.toSceneState() }.stateIn(viewModelScope, SharingStarted.Eagerly, RequestSceneState.Loading)

    fun onRequest(request: Wallet.Model.SessionRequest, onCancel: () -> Unit) = viewModelScope.launch {
        val connection = bridgeRepository.getConnectionByTopic(request.topic)
        if (connection == null) {
            onCancel()
            return@launch
        }
        val wallet = walletsRepository.getWallet(connection.wallet.id).firstOrNull()
        if (wallet == null) {
            onCancel()
            return@launch
        }
        val chain = Chain.getNamespace(request.chainId)
        if (chain == null) {
            onCancel()
            return@launch
        }

        val params = when (request.request.method) {
            WalletConnectionMethods.SolanaSignMessage.string,
            WalletConnectionMethods.EthSign.string -> {
                val data = JSONArray(request.request.params).getString(1)
                String(data.decodeHex())
            }
            WalletConnectionMethods.EthSignTypedData.string,
            WalletConnectionMethods.EthSignTypedDataV4.string -> {
                val data = JSONArray(request.request.params).getString(1)
                data
            }
            WalletConnectionMethods.PersonalSign.string -> {
                val data = JSONArray(request.request.params).getString(0)
                try {
                    String(data.decodeHex())
                } catch (_: Throwable) {
                    data
                }
            }
            WalletConnectionMethods.EthSendTransaction.string -> request.request.params
            WalletConnectionMethods.SolanaSignAndSendTransaction.string,
            WalletConnectionMethods.SolanaSignTransaction.string -> {
                val params = JSONObject(request.request.params).getString("transaction")
                params
            }
            WalletConnectionMethods.WalletSwitchEthereumChain.string -> {
                onSwitch(request, onCancel)
                return@launch
            }
            else -> {
                state.update { it.copy(error = "Unsupported method: ${request.request.method}") }
                return@launch
            }
        }
        state.update { it.copy(sessionRequest = request, wallet = wallet, chain = chain, params = params) }
    }

    fun onSent(hash: String) {
        val sessionRequest = state.value.sessionRequest ?: return
        val data = when (state.value.sessionRequest?.request?.method) {
            WalletConnectionMethods.SolanaSignTransaction.string -> jsonEncoder.encodeToString(WCSolanaSignMessageResult(signature = hash))
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

    fun onSign() {
        val sessionRequest = state.value.sessionRequest ?: return
        val wallet = state.value.wallet ?: return
        val chain = state.value.chain ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val method = WalletConnectionMethods.entries
                .firstOrNull { it.string == sessionRequest.request.method }
            val password = passwordStore.getPassword(wallet.id)
            val privateKey = loadPrivateKeyOperator(wallet, chain, password)

            val sign = try {
                when (method) {
                    WalletConnectionMethods.EthSign,
                    WalletConnectionMethods.PersonalSign -> {
                        val data = state.value.params.toByteArray()
                        val decoder = SignMessageDecoder(SignMessage(SignDigestType.EIP191, data))

//                        val messagePrefix = "\u0019Ethereum Signed Message:\n${data.size}"
//                        val prefix = messagePrefix.toByteArray()
//                        val param = Hash.keccak256(prefix + data)
                        val param = decoder.hash()
                        signClient.signMessage(chain, param, privateKey).toHexString()
                    }
                    WalletConnectionMethods.EthSignTypedData,
                    WalletConnectionMethods.EthSignTypedDataV4 -> {
                        signClient.signTypedMessage(chain, state.value.params.toByteArray(), privateKey).toHexString()
                    }
                    WalletConnectionMethods.SolanaSignMessage -> {
                        val param = state.value.params.toByteArray()
                        signClient.signMessage(chain, param, privateKey).toHexString()
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
    val sessionRequest: Wallet.Model.SessionRequest? = null,
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
        if (sessionRequest == null) {
            return RequestSceneState.Loading
        }
        val account = wallet?.getAccount(chain!!)!!
        return when (sessionRequest.request.method) {
            WalletConnectionMethods.EthSign.string,
            WalletConnectionMethods.PersonalSign.string,
            WalletConnectionMethods.EthSignTypedData.string,
            WalletConnectionMethods.EthSignTypedDataV4.string,
            WalletConnectionMethods.SolanaSignMessage.string -> RequestSceneState.SignMessage(
                walletName = wallet.name,
                chain = chain,
                method = sessionRequest.request.method,
                session = SessionUI(
                    id = "",
                    name = sessionRequest.peerMetaData?.name ?: "",
                    icon = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    description = sessionRequest.peerMetaData?.description ?: "",
                    uri = sessionRequest.peerMetaData?.url?.toUri()?.host ?: "",
                ),
                params = params,
            )
            WalletConnectionMethods.EthSendTransaction.string -> {
                try {
                    val jObj = JSONArray(params).getJSONObject(0)
                    val to = jObj.getString("to")
                    val value = jObj.optString("value", "0x0").hexToBigInteger() ?: BigInteger.ZERO
                    val data = jObj.getString("data")
                    RequestSceneState.SendTransaction(
                        account = account,
                        to = to,
                        value = value,
                        data = data,
                    )
                } catch (err: Throwable) {
                    RequestSceneState.Error("Argument error: ${err.message}")
                }
            }
            WalletConnectionMethods.SolanaSignTransaction.string -> RequestSceneState.SignGeneric(
                    ConfirmParams.TransferParams.Generic(
                    asset = chain.asset(),
                    from = account,
                    memo = params,
                    name = sessionRequest.peerMetaData?.name ?: "",
                    description = sessionRequest.peerMetaData?.description ?: "",
                    url = sessionRequest.peerMetaData?.url ?: "",
                    icon = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    redirectNative = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    redirectUniversal = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    gasLimit = "",
                    gasPrice = "",
                    inputType = ConfirmParams.TransferParams.InputType.Signature
                )
            )
            WalletConnectionMethods.SolanaSignAndSendTransaction.string -> RequestSceneState.SignGeneric(
                ConfirmParams.TransferParams.Generic(
                    asset = chain.asset(),
                    from = account,
                    memo = params,
                    name = sessionRequest.peerMetaData?.name ?: "",
                    description = sessionRequest.peerMetaData?.description ?: "",
                    url = sessionRequest.peerMetaData?.url ?: "",
                    icon = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    redirectNative = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    redirectUniversal = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    gasLimit = "",
                    gasPrice = "",
                    inputType = ConfirmParams.TransferParams.InputType.EncodeTransaction
                )
            )
            else -> RequestSceneState.Error("Unsupported method")
        }
    }
}

sealed interface RequestSceneState {

    data object Loading : RequestSceneState

    data object Cancel : RequestSceneState

    class Error(val message: String) : RequestSceneState

    class SignMessage(
        val chain: Chain,
        val method: String,
        val walletName: String,
        val session: SessionUI,
        val params: String,
    ) : RequestSceneState

    class SendTransaction(
        val account: Account,
        val to: String,
        val value: BigInteger,
        val data: String,
    ) : RequestSceneState

    class SignGeneric(val params: ConfirmParams.TransferParams.Generic) : RequestSceneState
    class SendGeneric(val params: ConfirmParams.TransferParams.Generic) : RequestSceneState
}

