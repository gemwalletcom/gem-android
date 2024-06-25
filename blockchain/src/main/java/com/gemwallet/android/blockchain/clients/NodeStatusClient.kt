package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.NodeStatus
import uniffi.Gemstone.SocialUrl

interface NodeStatusClient : BlockchainClient {
    suspend fun getNodeStatus(url: String): NodeStatus?
}