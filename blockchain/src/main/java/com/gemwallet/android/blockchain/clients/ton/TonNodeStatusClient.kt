package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TonNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : NodeStatusClient {
    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        NodeStatus(
            blockNumber = rpcClient.masterChainInfo().getOrNull()?.result?.last?.seqno?.toString() ?: return@withContext null,
            inSync = true,
            chainId = "",
        )
    }

    override fun maintainChain(): Chain = chain
}