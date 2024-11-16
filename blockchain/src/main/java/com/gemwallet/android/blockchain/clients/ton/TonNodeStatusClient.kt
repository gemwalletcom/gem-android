package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TonNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : NodeStatusClient {
    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val resp = rpcClient.masterChainInfo("$url/api/v2/getMasterchainInfo")
        NodeStatus(
            url = url,
            blockNumber = resp.body()?.result?.last?.seqno?.toString() ?: return@withContext null,
            inSync = true,
            chainId = "",
            latency = resp.getLatency(),
        )
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}