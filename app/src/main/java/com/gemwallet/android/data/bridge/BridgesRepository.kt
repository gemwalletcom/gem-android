package com.gemwallet.android.data.bridge

import android.net.Uri
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.model.WalletConnectAccount
import com.gemwallet.android.services.WalletConnectDelegate
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletConnection
import com.wallet.core.primitives.WalletConnectionEvents
import com.wallet.core.primitives.WalletConnectionSession
import com.wallet.core.primitives.WalletConnectionSessionAppMetadata
import com.wallet.core.primitives.WalletConnectionState
import com.walletconnect.android.Core
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class BridgesRepository(
    private val walletsRepository: WalletsRepository,
    private val localSource: ConnectionsLocalSource,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {

    init {
        scope.launch {
            sync()
            WalletConnectDelegate.walletEvents.collectLatest { event ->
                when (event) {
                    is Wallet.Model.Session -> withContext(Dispatchers.IO) {
                        updateSession(event)
                    }
                    else -> Unit
                }
            }
        }
    }

    suspend fun getConnections(): List<WalletConnection> {
        return localSource.getAll(walletsRepository.getAll().getOrNull() ?: return emptyList())
    }

    private suspend fun sync(): List<WalletConnection> {
        val local = getConnections()
        val sessions = try {
            Web3Wallet.getListOfActiveSessions().filter { wcSession -> wcSession.metaData != null }
        } catch (err: Throwable) {
            return local
        }
        val unknownSessions = sessions.filter { session ->
            local.firstOrNull { it.session.sessionId == session.topic } == null
        }
        return if (unknownSessions.isNotEmpty()) {
            sessions.forEach { disconnect(it.topic) }
            localSource.deleteAllConnections()
            emptyList()
        } else {
            local
        }
    }

    suspend fun disconnect(id: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val connection = getConnections().firstOrNull { it.session.id == id } ?: return
        val session = try {
            Web3Wallet.getListOfActiveSessions()
                .firstOrNull { wcSession -> connection.session.sessionId == wcSession.topic }
        } catch (err: Throwable) {
            return
        } ?: return
        Web3Wallet.disconnectSession(
            params = Wallet.Params.SessionDisconnect(session.topic),
            onSuccess = {
                scope.launch {
                    localSource.deleteConnection(id)
                }
                onSuccess()
            },
            onError = {
                onError(it.throwable.message ?: "Disconnect error")
            },
        )
    }

    fun addPairing(uri: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        Web3Wallet.pair(
            params = Wallet.Params.Pair(uri),
            onSuccess = {  onSuccess() },
            onError = {
                onError(it.throwable.message ?: "Pair to ${Uri.parse(uri).host} fail")
            }
        )
    }

    suspend fun approveConnection(
        wallet: com.wallet.core.primitives.Wallet,
        proposal: Wallet.Model.SessionProposal,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (Web3Wallet.getSessionProposals().isEmpty()) {
            onSuccess()
            return
        }
        val proposalPublicKey = proposal.proposerPublicKey
        val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(Web3Wallet.getSessionProposals().find { it.proposerPublicKey == proposalPublicKey })
        val supportedNamespaces = getSupportedNamespaces(wallet)
        val sessionNamespaces = Web3Wallet.generateApprovedNamespaces(sessionProposal = sessionProposal, supportedNamespaces = supportedNamespaces)
        val approveProposal = Wallet.Params.SessionApprove(proposerPublicKey = sessionProposal.proposerPublicKey, namespaces = sessionNamespaces)

        Web3Wallet.approveSession(
            params = approveProposal,
            onError = { error ->
                onError(error.throwable.message ?: "Unknown error")
            },
            onSuccess = {
                val connection = WalletConnection(
                    wallet = wallet,
                    session = WalletConnectionSession(
                        id = UUID.randomUUID().toString(),
                        sessionId = proposal.pairingTopic,
                        state = WalletConnectionState.Active,
                        createdAt = System.currentTimeMillis(),
                        expireAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                        chains = emptyList(),
                        metadata = WalletConnectionSessionAppMetadata(
                            name = proposal.name,
                            description = proposal.description,
                            url = proposal.url,
                            icon = proposal.icons.map { it.toString() }.firstOrNull{ it.endsWith("png", ignoreCase = true) || it.endsWith("jpg", ignoreCase = true) }
                                ?: proposal.icons.map { it.toString() }.firstOrNull() ?: "",
                            redirectNative = proposal.redirect,
                        )

                    )
                )
                scope.launch {
                    localSource.addConnection(connection)
                }

                onSuccess()
            }
        )
    }

    fun rejectConnection(
        proposal: Wallet.Model.SessionProposal,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (Web3Wallet.getSessionProposals().isEmpty()) {
            onSuccess()
            return
        }
        val proposalPublicKey = proposal.proposerPublicKey
        val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(Web3Wallet.getSessionProposals().find { it.proposerPublicKey == proposalPublicKey })

        Web3Wallet.rejectSession(
            params = Wallet.Params.SessionReject(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                reason = "Reject Session"
            ),
            onSuccess = {
                onSuccess()
            },
            onError = {
                onError(it.throwable.message ?: "")
            },
        )
    }

    private suspend fun updateSession(session: Wallet.Model.Session) {
        localSource.updateConnection(
            WalletConnectionSession(
                id = "",
                sessionId = session.topic,
                state = WalletConnectionState.Active,
                createdAt = System.currentTimeMillis(),
                expireAt = System.currentTimeMillis() + session.expiry,
                chains = emptyList(),
                metadata = WalletConnectionSessionAppMetadata(
                    name = session.metaData?.name ?: "DApp",
                    description = session.metaData?.description ?: "",
                    url = session.metaData?.url ?: "",
                    icon = session.metaData?.getIcon() ?: "",
                    redirectNative = session.redirect,
                )
            )
        )
    }

    private fun getSupportedNamespaces(wallet: com.wallet.core.primitives.Wallet): Map<String, Wallet.Model.Namespace.Session> {
        return wallet.accounts
            .mapNotNull { WalletConnectAccount.create(it) }
            .groupBy { it.namespace }
            .mapValues { chain ->
                Wallet.Model.Namespace.Session(
                    chains = chain.value.map { "${it.namespace}:${it.reference}" },
                    methods = chain.value.map { it.methods }.toSet().flatten().toList(),
                    events = if (chain.key == Chain.Solana.string) emptyList() else listOf(
                        WalletConnectionEvents.connect.string,
                        WalletConnectionEvents.disconnect.string,
                        WalletConnectionEvents.chain_changed.string,
                        WalletConnectionEvents.accounts_changed.string
                    ),
                    accounts = chain.value.map { "${it.namespace}:${it.reference}:${it.address}" },
                )
            }
    }

    private fun Core.Model.AppMetaData.getIcon()
        = icons.firstOrNull{ it.endsWith("png", true) || it.endsWith("jpg", true) }
            ?: icons.firstOrNull()
            ?: ""
}