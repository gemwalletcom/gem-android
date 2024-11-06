package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TronNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : NodeStatusClient {
    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val resp = rpcClient.nowBlock("$url/wallet/getnowblock")
        NodeStatus(
            url = url,
            blockNumber = resp.body()?.block_header?.raw_data?.number?.toString() ?: return@withContext null,
            inSync = true,
            chainId = "",
            latency = resp.getLatency(),
        )
    }

    override fun maintainChain(): Chain = chain
}