package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class XrpNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : NodeStatusClient {
    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val resp = rpcClient.latestBlock(url)

        NodeStatus(
            blockNumber = resp.body()?.result?.ledger_current_index?.toString() ?: return@withContext null,
            inSync = true,
            chainId = "",
            latency = resp.getLatency(),
        )
    }

    override fun maintainChain(): Chain = chain
}