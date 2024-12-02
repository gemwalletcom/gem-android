package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import java.lang.Exception
import java.math.BigInteger

class AptosTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: AptosRpcClient,
) : TransactionStatusClient {

    override suspend fun getStatus(chain: Chain, owner: String, txId: String): Result<TransactionChages> {
        val transaction = rpcClient.transactions(txId).getOrNull() ?: return Result.failure(Exception())
        val status = if (transaction.success == true) {
            TransactionState.Confirmed
        } else {
            TransactionState.Reverted
        }
        val fee = BigInteger(transaction.gas_used) * BigInteger(transaction.gas_unit_price)
        return Result.success(TransactionChages(status, fee))
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}