package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SolanaNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: SolanaRpcClient,
) : NodeStatusClient {

    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val inSyncJob = async { rpcClient.health(url) }
        val slotJob = async { rpcClient.slot(url).getOrNull()?.result }
        val chainIdJob = async { rpcClient.genesisHash(url).getOrNull()?.result }

        val inSync = inSyncJob.await()

        NodeStatus(
            url = url,
            inSync = inSync.body()?.result == "ok",
            blockNumber = slotJob.await().toString(),
            chainId = chainIdJob.await() ?: return@withContext null,
            latency = inSync.getLatency(),
        )
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}