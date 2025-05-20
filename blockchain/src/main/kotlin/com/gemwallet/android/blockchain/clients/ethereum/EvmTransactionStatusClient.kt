package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmTransactionsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import java.math.BigInteger

class EvmTransactionStatusClient(
    private val chain: Chain,
    private val transactionsService: EvmTransactionsService,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        return getStatus(request.hash)
    }

    private suspend fun getStatus(txId: String): TransactionChages {
        val request = JSONRpcRequest.create(EvmMethod.GetTransaction, listOf(txId))
        val resp = transactionsService.transaction(request).getOrNull()?.result ?: throw ServiceUnavailable

        if (resp.status != "0x0" && resp.status != "0x1") {
            return TransactionChages(TransactionState.Pending)
        }
        val state = when (resp.status) {
            "0x0" -> TransactionState.Reverted
            "0x1" -> TransactionState.Confirmed
            else -> TransactionState.Confirmed
        }
        val gasUsed = resp.gasUsed.hexToBigInteger() ?: return TransactionChages(TransactionState.Pending)
        val effectiveGas = resp.effectiveGasPrice.hexToBigInteger() ?: return TransactionChages(TransactionState.Pending)
        val l1Fee = resp.l1Fee?.hexToBigInteger() ?: BigInteger.ZERO
        val fee = gasUsed.multiply(effectiveGas) + l1Fee

        return TransactionChages(state, fee)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}