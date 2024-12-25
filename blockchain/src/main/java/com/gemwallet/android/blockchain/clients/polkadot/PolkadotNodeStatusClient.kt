package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotNodeStatusService
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.String

class PolkadotNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: PolkadotNodeStatusService,
) : NodeStatusClient {

    override suspend fun getNodeStatus(
        chain: Chain,
        url: String
    ): NodeStatus? = withContext(Dispatchers.IO) {
        val getBlock = async { nodeStatusService.blockHead("$url/blocks/head") }
        val getVersion = async { nodeStatusService.nodeVersion("$url/node/version") }

        val version = getVersion.await()
        val block = getBlock.await()

        NodeStatus(
            url = url,
            chainId = version.body()?.chain ?: return@withContext null,
            blockNumber = block.body()?.number ?: return@withContext null,
            inSync = true,
            latency = version.getLatency()
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}