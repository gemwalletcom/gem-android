package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain

class NodeStatusClientsProxy(
    private val clients: List<NodeStatusClient>,
) {

    suspend operator fun invoke(chain: Chain, url: String): NodeStatus? {
        return clients.firstOrNull { it.isMaintain(chain) }?.getNodeStatus(url)
    }

    suspend fun isMaintained(chain: Chain): Boolean {
        return clients.firstOrNull { it.isMaintain(chain) } != null
    }
}