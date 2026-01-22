package com.gemwallet.features.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.bridge.getNamespace
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.features.bridge.viewmodels.model.BridgeRequestError
import com.gemwallet.features.bridge.viewmodels.model.WCRequest
import com.gemwallet.features.bridge.viewmodels.model.map
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletConnectionSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.gemstone.WalletConnect
import uniffi.gemstone.WalletConnectAction
import uniffi.gemstone.WalletConnectChainOperation
import uniffi.gemstone.WalletConnectionVerificationStatus
import java.util.Arrays
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

    fun onRequest(
        sessionRequest: Wallet.Model.SessionRequest,
        verifyContext: Wallet.Model.VerifyContext,
        onCancel: (BridgeRequestError?) -> Unit
    ) = viewModelScope.launch {
        try {
            val verificationStatus = validateSession(sessionRequest, verifyContext)
            val connection = bridgeRepository.getConnectionByTopic(sessionRequest.topic)
            if (connection == null) {
                onCancel(null)
                return@launch
            }
            val wallet = walletsRepository.getWallet(connection.wallet.id).firstOrNull()
            if (wallet == null) {
                onCancel(null)
                return@launch
            }
            val chain = Chain.getNamespace(sessionRequest.chainId)
                ?: throw BridgeRequestError.ChainUnsupported

            validateChain(chain, connection.session)

            val account: Account = wallet.getAccount(chain) ?: throw BridgeRequestError.ChainUnsupported

            val action = WalletConnect().parseRequest(
                topic = sessionRequest.topic,
                method = sessionRequest.request.method,
                params = sessionRequest.request.params,
                chainId = sessionRequest.chainId ?: return@launch,
                domain = sessionRequest.peerMetaData?.url ?: ""
            )
            val request = when (action) {
                is WalletConnectAction.ChainOperation -> {
                    when (action.operation) {
                        WalletConnectChainOperation.ADD_CHAIN -> {}
                        WalletConnectChainOperation.SWITCH_CHAIN -> onSwitch(sessionRequest)
                        WalletConnectChainOperation.GET_CHAIN_ID -> {}
                    }
                    onCancel(null)
                    return@launch
                }

                is WalletConnectAction.SignMessage -> WCRequest.SignMessage(sessionRequest, account, verificationStatus, action)

                is WalletConnectAction.SendTransaction -> WCRequest.Transaction.SendTransaction(
                    sessionRequest,
                    account,
                    verificationStatus,
                    action
                )

                is WalletConnectAction.SignTransaction -> WCRequest.Transaction.SignTransaction(
                    sessionRequest,
                    account,
                    verificationStatus,
                    action
                )

                is WalletConnectAction.Unsupported -> throw BridgeRequestError.MethodUnsupported
            }
            state.update {
                it.copy(
                    request = request,
                    wallet = wallet,
                    chain = request.chain,
                )
            }
        } catch (err: BridgeRequestError.ScamSession) {
            onCancel(err)
        } catch (_: BridgeRequestError.MethodUnsupported) {
            state.update { it.copy(error = "Unsupported method: ${sessionRequest.request.method}") }
        } catch (_: BridgeRequestError.UnresolvedChainId) {
            onReject(sessionRequest)
            onCancel(null)
        } catch (_: BridgeRequestError.ChainUnsupported) {
            onCancel(null)
        } catch (_: Throwable) {
            state.update { it.copy(error = "Unsupported method: ${sessionRequest.request.method}") }
        }
    }

    fun onSent(hash: String) {
        val request = state.value.request as? WCRequest.Transaction.SendTransaction?: return
        val response = request.execute(hash)

        viewModelScope.launch(Dispatchers.IO) {
            response(request.topic, request.requestId, response)
        }
    }

    fun onSwitch(request: Wallet.Model.SessionRequest) {
        response(request.topic, request.request.id, "null")
    }

    fun onSigned(signature: String) {
        val request = (state.value.request as? WCRequest.Transaction.SignTransaction) ?: return
        val sessionRequest = state.value.request?.sessionRequest ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val response = request.execute(signature)
            response(sessionRequest.topic, sessionRequest.request.id, response)
        }
    }

    fun onSign() {
        val request = (state.value.request as? WCRequest.SignMessage) ?: return
        val wallet = state.value.wallet ?: return
        val chain = state.value.chain ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val password = passwordStore.getPassword(wallet.id)
            val privateKey = loadPrivateKeyOperator(wallet, chain, password)
            val sign = try {
                request.execute(signClient, privateKey)
            } catch (err: Throwable) {
                state.update { it.copy(error = err.message ?: "Sign error") }
                return@launch
            } finally {
                Arrays.fill(privateKey, 0)
            }
            response(request.sessionRequest.topic, request.sessionRequest.request.id, sign)
        }
    }

    private fun validateSession(
        sessionRequest: Wallet.Model.SessionRequest,
        verifyContext: Wallet.Model.VerifyContext
    ): WalletConnectionVerificationStatus {
        val validation = WalletConnect().validateOrigin(
            sessionRequest.peerMetaData?.url ?: "",
            verifyContext.origin,
            verifyContext.validation.map()
        )
        when (validation) {
            WalletConnectionVerificationStatus.UNKNOWN,
            WalletConnectionVerificationStatus.VERIFIED -> {
                return validation
            }
            WalletConnectionVerificationStatus.INVALID,
            WalletConnectionVerificationStatus.MALICIOUS -> {
                onReject(sessionRequest)
                throw BridgeRequestError.ScamSession
            }
        }
    }

    private fun response(topic: String, id: Long, payload: String) {
        WalletKit.respondSessionRequest(
            params = Wallet.Params.SessionRequestResponse(
                sessionTopic = topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(id, payload)
            ),
            onSuccess = { state.update { it.copy(canceled = true) } },
            onError = { error ->
                state.update { it.copy(error = error.throwable.message ?: "On response error") }
            }
        )
    }

    fun onReject() {
        val sessionRequest = state.value.request?.sessionRequest ?: return
        onReject(sessionRequest)
    }

    private fun onReject(sessionRequest: Wallet.Model.SessionRequest) {
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

    private fun validateChain(chain: Chain, session: WalletConnectionSession) {
        if (session.chains.isEmpty()) {
            return
        }
        if (!session.chains.contains(chain)) {
            throw IllegalAccessException("Unresolve chain id")
        }
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
            return RequestSceneState.Error(error)
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

