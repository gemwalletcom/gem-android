package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class BitcoinNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: BitcoinRpcClient
) : NodeStatusClient {

    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val nodeInfoJob = async { rpcClient.nodeInfo().getOrNull() }
        val chainIdJob = async { rpcClient.block(1).getOrNull()?.previousBlockHash }
        val nodeInfo = nodeInfoJob.await() ?: return@withContext null
        val chainId = chainIdJob.await() ?: return@withContext null

        NodeStatus(
            chainId = chainId,
            blockNumber = nodeInfo.blockbook.bestHeight.toString(),
            inSync = nodeInfo.blockbook.inSync,
        )
    }

    override fun maintainChain(): Chain = chain
}