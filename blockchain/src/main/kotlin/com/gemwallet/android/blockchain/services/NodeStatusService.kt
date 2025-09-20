package com.gemwallet.android.blockchain.services

import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemGateway

class NodeStatusService(
    private val gateway: GemGateway,
) {

    suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? {
        return try {
            val result = gateway.getNodeStatus(chain.string, url)
            NodeStatus(
                url = url,
                chainId = result.chainId,
                blockNumber = result.latestBlockNumber,
                inSync = true,
                latency = result.latencyMs,
                loading = false
            )
        } catch (_: Throwable) {
            null
        }
    }
}