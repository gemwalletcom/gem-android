package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmNodeStatusService
import com.gemwallet.android.blockchain.clients.ethereum.services.getChainId
import com.gemwallet.android.blockchain.clients.ethereum.services.latestBlock
import com.gemwallet.android.blockchain.clients.ethereum.services.sync
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class EvmNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: EvmNodeStatusService,
) : NodeStatusClient {

    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val getChainId = async { nodeStatusService.getChainId(url) }
        val getLatestBlock = async { nodeStatusService.latestBlock(url).getOrNull()?.result?.value?.toString() }
        val getSync = async { nodeStatusService.sync(url).getOrNull()?.result }

        val chainId = getChainId.await()
        val blockNumber = getLatestBlock.await() ?: return@withContext null
        val inSync = getSync.await() ?: return@withContext null

        NodeStatus(
            url = url,
            chainId = chainId.body()?.result?.value?.toString() ?: return@withContext null,
            blockNumber = blockNumber,
            inSync = !inSync,
            latency = chainId.getLatency(),
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}