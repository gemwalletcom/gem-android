package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.aptos.services.AptosNodeStatusService
import com.gemwallet.android.blockchain.clients.aptos.services.getLedger
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AptosNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: AptosNodeStatusService,
) : NodeStatusClient {

    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val response = nodeStatusService.getLedger(url)
        val ledger = response.body() ?: return@withContext null
        NodeStatus(
            url = url,
            chainId = ledger.chain_id.toString(),
            blockNumber = ledger.ledger_version,
            inSync = true,
            latency = response.getLatency(),
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}