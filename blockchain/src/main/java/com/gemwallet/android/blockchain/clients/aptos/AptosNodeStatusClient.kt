package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.String

class AptosNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: AptosRpcClient,
) : NodeStatusClient {
    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val response = rpcClient.getLedger(url)
        val ledger = response.body() ?: return@withContext null
        NodeStatus(
            chainId = ledger.chain_id.toString(),
            blockNumber = ledger.ledger_version,
            inSync = true,
            latency = response.getLatency(),
        )
    }

    override fun maintainChain(): Chain = chain
}