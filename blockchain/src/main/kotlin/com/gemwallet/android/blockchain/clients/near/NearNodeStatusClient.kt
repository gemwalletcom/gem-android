package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.rpc.getLatency
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

    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val getBlock = async {
            rpcClient.latestBlock(
                url,
                JSONRpcRequest(
                    NearMethod.LatestBlock.value,
                    mapOf("finality" to "final")
                )
            )
        }
        val block = getBlock.await()
        NodeStatus(
            url = url,
            blockNumber = block.body()?.result?.header?.height?.toString() ?: return@withContext null,
            chainId = "",
            inSync = true,
            latency = block.getLatency(),
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}