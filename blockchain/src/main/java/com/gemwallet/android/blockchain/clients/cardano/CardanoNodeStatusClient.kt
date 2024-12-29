package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.cardano.services.CardanoNodeStatusService
import com.gemwallet.android.blockchain.clients.cardano.services.latestBlock
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain

class CardanoNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: CardanoNodeStatusService
) : NodeStatusClient {

    override suspend fun getNodeStatus(
        chain: Chain,
        url: String
    ): NodeStatus? {
        val response = nodeStatusService.latestBlock(url)
        return NodeStatus(
            url = url,
            chainId = "",
            blockNumber = response.body()?.data?.cardano?.tip?.number?.toString() ?: return null,
            inSync = true,
            latency = response.getLatency(),
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}