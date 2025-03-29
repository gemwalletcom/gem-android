package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotTransactionService
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import java.math.BigInteger

class PolkadotTransactionStatusClient(
    private val chain: Chain,
    private val transactionService: PolkadotTransactionService,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val blockNumber = try {
            request.block.toBigInteger()
        } catch (_: Throwable) {
            BigInteger.ZERO
        }
        if (blockNumber < BigInteger.ZERO) {
            throw Exception("Invalid block number")
        }
        val blockHead = transactionService.blockHead().getOrNull()?.number?.toBigInteger() ?: throw ServiceUnavailable
        val fromBlock = request.block
        val toBlock = blockHead.min(blockNumber + BigInteger.valueOf(PolkadotSignerPreloaderClient.periodLength))
        val blocks = transactionService.blocks("$fromBlock-$toBlock").getOrNull() ?: emptyList()
        for (block in blocks) {
            for (extr in block.extrinsics) {
                if (extr.hash == request.hash) {
                    val state = if (extr.success) TransactionState.Confirmed else TransactionState.Failed
                    return TransactionChages(state)
                }
            }
        }
        return TransactionChages(TransactionState.Pending)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}