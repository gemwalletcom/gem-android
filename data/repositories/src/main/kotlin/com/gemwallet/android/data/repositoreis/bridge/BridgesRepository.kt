package com.gemwallet.android.data.repositoreis.bridge

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.service.store.database.ConnectionsDao
import com.gemwallet.android.data.service.store.database.entities.DbConnection
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletConnection
import com.wallet.core.primitives.WalletConnectionEvents
import com.wallet.core.primitives.WalletConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class BridgesRepository(
    private val context: Context,
    private val walletsRepository: WalletsRepository,
    private val connectionsDao: ConnectionsDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {

    private val isWalletConnectInit = MutableStateFlow(false)
    val bridgeEvents = isWalletConnectInit.flatMapLatest {
        if (it) {
            WalletConnectDelegate.walletEvents
        } else {
            emptyFlow()
        }
    }

    init {
        scope.launch(Dispatchers.IO) {
            if ((getConnections().firstOrNull() ?: emptyList()).isNotEmpty()) {
                initWalletConnect()
                sync()
                WalletConnectDelegate.walletEvents.collectLatest { event ->
                    withContext(Dispatchers.IO) {
                        when (event) {
                            is Wallet.Model.Session -> updateSession(event)
                            is Wallet.Model.SessionDelete -> sync()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun initWalletConnect(onSuccess: () -> Unit = {}) {
        if (isWalletConnectInit.value) {
            onSuccess()
            return
        }

        val projectId = "3bc07cd7179d11ea65335fb9377702b6"
        val connectionType = ConnectionType.AUTOMATIC
        val metaData = Core.Model.AppMetaData(
            name = "Gem Wallet",
            description = "Gem Web3 Wallet",
            url = "https://gemwallet.com",
            icons = listOf("https://gemwallet.com/images/gem-logo-256x256.png"),
            redirect = "gem://wc/"
        )
        CoreClient.initialize(
            application = context as Application,
            projectId = projectId,
            metaData = metaData,
            connectionType = connectionType,
            telemetryEnabled = false,
        ) {
            Log.d("WalletConnect", "Err", it.throwable)
        }
        val initParams = Wallet.Params.Init(core = CoreClient)
        WalletKit.initialize(
            initParams,
            {
                isWalletConnectInit.update { true }
                onSuccess()
            }
        ) { _ -> }
    }

    fun getConnections(): Flow<List<WalletConnection>> {
        return walletsRepository.getAll().flatMapLatest { wallets ->
            connectionsDao.getAll().map { items ->
                items.mapNotNull { room ->
                    val wallet = wallets.firstOrNull { it.id == room.walletId } ?: return@mapNotNull null
                    room.toModel(wallet)
                }
            }
        }
    }

    suspend fun getConnectionByTopic(topic: String): WalletConnection? {
        val sessions = runCatching { WalletKit.getListOfActiveSessions().filter { wcSession -> wcSession.metaData != null } }
            .getOrNull() ?: return null
        val local = getConnections().firstOrNull() ?: emptyList()
        val sessionId = sessions.firstOrNull { it.topic == topic }?.pairingTopic
        return local.firstOrNull { it.session.sessionId == sessionId }
    }

    suspend fun getConnections(connectionId: String): Flow<WalletConnection?> {
        return walletsRepository.getAll().flatMapLatest { wallets ->
            connectionsDao.getConnection(connectionId).map { room ->
                val wallet = wallets.firstOrNull { it.id == room?.walletId } ?: return@map null
                room?.toModel(wallet)
            }
        }
    }

    private suspend fun sync() {
        val local = getConnections().firstOrNull() ?: emptyList()
        val sessions = runCatching { WalletKit.getListOfActiveSessions().filter { wcSession -> wcSession.metaData != null } }
            .getOrNull() ?: return

        val unknownSessions = local.filter { local -> !sessions.any { local.session.sessionId == it.pairingTopic } }

        if (unknownSessions.isNotEmpty()) {
            sessions.forEach { disconnect(it.pairingTopic) }
            connectionsDao.deleteAll(unknownSessions.map { it.toRecord() })
        }
    }

    suspend fun disconnect(id: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val connection = getConnections().firstOrNull()?.firstOrNull { it.session.id == id } ?: return
        val session = try {
            WalletKit.getListOfActiveSessions()
                .firstOrNull { wcSession -> connection.session.sessionId == wcSession.pairingTopic }
                    ?: throw IllegalStateException("Active sessions is null")
        } catch (err: Throwable) {
            onError(err.message ?: "Disconnect error")
            return
        }
        WalletKit.disconnectSession(
            params = Wallet.Params.SessionDisconnect(session.topic),
            onSuccess = {
                scope.launch { connectionsDao.delete(id) }
                onSuccess()
            },
            onError = {
                onError(it.throwable.message ?: "Disconnect error")
            },
        )
    }

    fun addPairing(uri: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        initWalletConnect(
            {
                try {
                    WalletKit.pair(
                        params = Wallet.Params.Pair(uri),
                        onSuccess = { onSuccess() },
                        onError = {
                            onError(it.throwable.message ?: "Pair to ${uri.toUri().host} fail")
                        }
                    )
                } catch (err: Throwable) {
                    onError("Wallet Connect unavailable: ${err.message}")
                }
            }
        )
    }

    fun approveConnection(
        wallet: com.wallet.core.primitives.Wallet,
        proposal: Wallet.Model.SessionProposal,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (WalletKit.getSessionProposals().isEmpty()) {
            onSuccess()
            return
        }
        val proposalPublicKey = proposal.proposerPublicKey
        val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(WalletKit.getSessionProposals().find { it.proposerPublicKey == proposalPublicKey })
        val supportedNamespaces = getSupportedNamespaces(wallet)
        val sessionNamespaces = WalletKit.generateApprovedNamespaces(
            sessionProposal = sessionProposal,
            supportedNamespaces = supportedNamespaces
        )
        val approveProposal = Wallet.Params.SessionApprove(proposerPublicKey = sessionProposal.proposerPublicKey, namespaces = sessionNamespaces)

        WalletKit.approveSession(
            params = approveProposal,
            onError = { error -> onError(error.throwable.message ?: "Unknown error") },
            onSuccess = {
                scope.launch(Dispatchers.IO) { addConnection(wallet, proposal) }
                onSuccess()
            }
        )
    }

    fun rejectConnection(
        proposal: Wallet.Model.SessionProposal,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (WalletKit.getSessionProposals().isEmpty()) {
            onSuccess()
            return
        }
        val proposalPublicKey = proposal.proposerPublicKey
        val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(WalletKit.getSessionProposals().find { it.proposerPublicKey == proposalPublicKey })

        WalletKit.rejectSession(
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

    private suspend fun addConnection(wallet: com.wallet.core.primitives.Wallet, proposal: Wallet.Model.SessionProposal): Boolean {
        connectionsDao.insert(
            DbConnection(
                id = UUID.randomUUID().toString(),
                walletId = wallet.id,
                sessionId = proposal.pairingTopic,
                state = WalletConnectionState.Active,
                createdAt = System.currentTimeMillis(),
                expireAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                appName = proposal.name,
                appDescription = proposal.description,
                appUrl = proposal.url,
                appIcon = proposal.icons.map { it.toString() }.firstOrNull{ it.endsWith("png", ignoreCase = true) || it.endsWith("jpg", ignoreCase = true) }
                    ?: proposal.icons.map { it.toString() }.firstOrNull() ?: "",
                redirectNative = proposal.redirect,
                redirectUniversal = proposal.redirect,
            )
        )
        return true
    }

    private suspend fun updateSession(session: Wallet.Model.Session) {
        val room = connectionsDao.getBySessionId(session.pairingTopic)
        connectionsDao.update(
            room.copy(
                expireAt = System.currentTimeMillis() + session.expiry,
                appName = session.metaData?.name ?: "DApp",
                appDescription = session.metaData?.description ?: "",
                appUrl = session.metaData?.url ?: "",
                appIcon = session.metaData?.getIcon() ?: "",
                redirectNative = session.redirect,
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