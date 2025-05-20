package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.cardano.services.CardanoNodeStatusService
import com.gemwallet.android.blockchain.clients.cardano.services.latestBlock
import com.gemwallet.android.blockchain.clients.cardano.services.networkMagic
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CardanoNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: CardanoNodeStatusService
) : NodeStatusClient {

    override suspend fun getNodeStatus(
        chain: Chain,
        url: String
    ): NodeStatus?  = withContext(Dispatchers.IO) {
        val getLatestBlock = async { nodeStatusService.latestBlock(url) }
        val getNetworkMagic = async { nodeStatusService.networkMagic(url) }
        val latestBlock = getLatestBlock.await()
        val networkMagic = getNetworkMagic.await()
        NodeStatus(
            url = url,
            chainId = networkMagic.body()?.data?.genesis?.shelley?.networkMagic?.toString() ?: "-",
            blockNumber = latestBlock.body()?.data?.cardano?.tip?.number?.toString() ?: return@withContext null,
            inSync = true,
            latency = latestBlock.getLatency(),
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}