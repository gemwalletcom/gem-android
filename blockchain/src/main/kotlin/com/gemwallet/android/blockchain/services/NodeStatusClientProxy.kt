package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.ext.toChainType
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import uniffi.gemstone.GemGateway

class NodeStatusClientProxy(
    private val gateway: GemGateway,
    private val clients: List<NodeStatusClient>,
): NodeStatusClient {

    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? {
        return try {
            if (chain.toChainType() == ChainType.Ethereum) {
                clients.getClient(chain)?.getNodeStatus(chain, url)
            } else {
                val result = gateway.getNodeStatus(chain.string, url)
                NodeStatus(
                    url = url,
                    chainId = result.chainId,
                    blockNumber = result.latestBlockNumber,
                    inSync = true,
                    latency = result.latencyMs,
                    loading = false
                )
            }
        } catch (_: Throwable) {
            null
        }
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}