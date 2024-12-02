package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain

class NodeStatusClientProxy(
    private val clients: List<NodeStatusClient>,
): NodeStatusClient {

    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? {
        return try {
            clients.getClient(chain)?.getNodeStatus(chain, url)
        } catch (_: Throwable) {
            null
        }
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}