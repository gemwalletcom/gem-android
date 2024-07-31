package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class NearNodeStatusClient(
    private val chain: Chain,
    private val rpcClient: NearRpcClient,
) : NodeStatusClient {

    override suspend fun getNodeStatus(url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val block = async {
            rpcClient.latestBlock(
                JSONRpcRequest(
                    NearMethod.LatestBlock.value,
                    mapOf("finality" to "final")
                )
            ).getOrNull()?.result?.header?.height?.toString()
        }
        NodeStatus(
            blockNumber = block.await() ?: return@withContext null,
            chainId = "",
            inSync = true,
        )
    }

    override fun maintainChain(): Chain = chain
}