package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TronNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : NodeStatusClient {
    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        NodeStatus(
            blockNumber = rpcClient.nowBlock().getOrNull()?.block_header?.raw_data?.number?.toString() ?: return@withContext null,
            inSync = true,
            chainId = "",
        )
    }

    override fun maintainChain(): Chain = chain
}