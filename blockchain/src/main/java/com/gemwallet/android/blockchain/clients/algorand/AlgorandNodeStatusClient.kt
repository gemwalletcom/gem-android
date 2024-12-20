package com.gemwallet.android.blockchain.clients.algorand

import android.util.Log
import com.gemwallet.android.blockchain.clients.NodeStatusClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandNodeStatusService
import com.gemwallet.android.blockchain.clients.stellar.services.StellarNodeStatusService
import com.gemwallet.android.blockchain.rpc.getLatency
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain

class AlgorandNodeStatusClient(
    private val chain: Chain,
    private val nodeStatusService: AlgorandNodeStatusService,
) : NodeStatusClient {

    override suspend fun getNodeStatus(
        chain: Chain,
        url: String
    ): NodeStatus? {
        val resp = try {
            nodeStatusService.transactionsParams("$url/v2/transactions/params")
        } catch (err: Throwable) {
            Log.d("ALGO-NODE-STATUS", "Error: ", err)
            throw err
        }
        val result = resp.body()
        return NodeStatus(
            url = url,
            blockNumber = result?.last_round?.toString() ?: return null,
            inSync = true,
            chainId = result.genesis_id,
            latency = resp.getLatency()
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}