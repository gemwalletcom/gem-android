package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class EvmNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: EvmRpcClient,
) : NodeStatusClient {

    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val getChainId = async { rpcClient.getChainId(url) }
        val getLatestBlock = async { rpcClient.latestBlock(url).getOrNull()?.result?.value?.toString() }
        val getSync = async { rpcClient.sync(url).getOrNull()?.result }

        val chainId = getChainId.await()
        val blockNumber = getLatestBlock.await() ?: return@withContext null
        val inSync = getSync.await() ?: return@withContext null

        NodeStatus(
            chainId = chainId.body()?.result?.value?.toString() ?: return@withContext null,
            blockNumber = blockNumber,
            inSync = !inSync,
            latency = chainId.getLatency(),
        )
    }

    override fun maintainChain(): Chain = chain
}