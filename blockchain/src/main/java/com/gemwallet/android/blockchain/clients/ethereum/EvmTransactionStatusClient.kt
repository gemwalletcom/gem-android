package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmTransactionsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.eip1559Support
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import java.math.BigInteger

class EvmTransactionStatusClient(
    private val chain: Chain,
    private val transactionsService: EvmTransactionsService,
) : TransactionStatusClient {

    override suspend fun getStatus(chain: Chain, owner: String, txId: String): Result<TransactionChages> {
        return Result.success(getStatus(txId))
    }

    private suspend fun getStatus(txId: String): TransactionChages {
        return transactionsService.transaction(JSONRpcRequest.create(EvmMethod.GetTransaction, listOf(txId)))
            .fold(
                {
                    if (it.result?.status != "0x0" && it.result?.status != "0x1") {
                        return@fold TransactionChages(TransactionState.Pending)
                    }
                    val state = when (it.result.status) {
                        "0x0" -> TransactionState.Reverted
                        "0x1" -> TransactionState.Confirmed
                        else -> TransactionState.Confirmed
                    }
                    val fee = if (chain.eip1559Support()) {
                        val gasUsed = it.result.gasUsed.hexToBigInteger() ?: return@fold TransactionChages(TransactionState.Pending)
                        val effectiveGas = it.result.effectiveGasPrice.hexToBigInteger() ?: return@fold TransactionChages(TransactionState.Pending)
                        val l1Fee = it.result.l1Fee?.hexToBigInteger() ?: BigInteger.ZERO
                        gasUsed.multiply(effectiveGas) + l1Fee
                    } else {
                        null
                    }

                    TransactionChages(state, fee)
                }
            ) { TransactionChages(TransactionState.Pending) }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}