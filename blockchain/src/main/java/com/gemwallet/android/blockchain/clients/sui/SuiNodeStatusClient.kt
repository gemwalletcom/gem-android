package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SuiNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : NodeStatusClient {
    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val chainIdJob = async { rpcClient.chainId(url) }
        val blockJob = async { rpcClient.latestBlock(url).getOrNull() }

        val chainIdResp = chainIdJob.await()
        val block = blockJob.await()
        val chainId = chainIdResp.body()?.result

        NodeStatus(
            inSync = true,
            blockNumber = block?.result?.toString() ?: return@withContext null,
            chainId = chainId ?: return@withContext null,
            latency = chainIdResp.getLatency(),
        )
    }

    override fun maintainChain(): Chain = chain
}