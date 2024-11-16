package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain

interface NodeStatusClient : BlockchainClient {
    suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus?
}