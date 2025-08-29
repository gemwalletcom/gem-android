package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain

class HyperCoreNodeStatusClient(
    val chain: Chain,
) : NodeStatusClient {
    override suspend fun getNodeStatus(
        chain: Chain,
        url: String
    ): NodeStatus? {
        TODO("Not yet implemented")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}