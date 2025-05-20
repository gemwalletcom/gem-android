package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarNodeStatusService
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain

class StellarNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: StellarNodeStatusService,
) : NodeStatusClient {

    override suspend fun getNodeStatus(
        chain: Chain,
        url: String
    ): NodeStatus? {
        val resp = nodeStatusService.node("$url")
        val result = resp.body()
        return NodeStatus(
            url = url,
            blockNumber = result?.ingest_latest_ledger?.toString() ?: return null,
            inSync = true,
            chainId = result.network_passphrase,
            latency = resp.getLatency()
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}