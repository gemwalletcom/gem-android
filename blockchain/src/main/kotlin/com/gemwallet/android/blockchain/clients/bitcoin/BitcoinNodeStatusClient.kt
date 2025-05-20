package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinNodeStatusService
import com.gemwallet.android.blockchain.clients.bitcoin.services.getBlock
import com.gemwallet.android.blockchain.clients.bitcoin.services.getNodeInfo
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class BitcoinNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: BitcoinNodeStatusService,
) : NodeStatusClient {

    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val nodeInfoJob = async { nodeStatusService.getNodeInfo(url).getOrNull() }
        val chainIdJob = async { nodeStatusService.getBlock(url) }
        val nodeInfo = nodeInfoJob.await() ?: return@withContext null
        val chainId = chainIdJob.await()

        NodeStatus(
            url = url,
            chainId = chainId.body()?.previousBlockHash ?: return@withContext null,
            blockNumber = nodeInfo.blockbook.bestHeight.toString(),
            inSync = nodeInfo.blockbook.inSync,
            latency = chainId.getLatency()
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}