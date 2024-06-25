package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.NodeStatus

interface NodeStatusClient : BlockchainClient {
    suspend fun getNodeStatus(url: String): NodeStatus?
}