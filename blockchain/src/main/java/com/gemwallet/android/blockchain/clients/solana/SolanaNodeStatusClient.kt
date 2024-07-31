package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SolanaNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: SolanaRpcClient,
) : NodeStatusClient {

    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val inSyncJob = async { rpcClient.health().getOrNull()?.result }
        val slotJob = async { rpcClient.slot().getOrNull()?.result }
        val chainIdJob = async { rpcClient.genesisHash().getOrNull()?.result }

        NodeStatus(
            inSync = inSyncJob.await() == "ok",
            blockNumber = slotJob.toString(),
            chainId = chainIdJob.await() ?: return@withContext null
        )
    }

    override fun maintainChain(): Chain = chain
}