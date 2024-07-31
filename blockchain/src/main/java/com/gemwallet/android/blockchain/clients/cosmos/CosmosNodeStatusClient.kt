package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CosmosNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: CosmosRpcClient
) : NodeStatusClient {
    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val inSyncJob = async { rpcClient.syncing().getOrNull()?.syncing == false }
        val nodeInfoJob = async { rpcClient.getNodeInfo().getOrNull()?.block?.header }

        val inSync = inSyncJob.await()
        val nodeInfo = nodeInfoJob.await() ?: return@withContext null

        NodeStatus(
            inSync = inSync,
            blockNumber = nodeInfo.height,
            chainId = nodeInfo.chain_id,
        )
    }

    override fun maintainChain(): Chain = chain
}