package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.aptos.services.AptosTransactionsService
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import java.math.BigInteger

class AptosTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: AptosTransactionsService,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        val transaction = rpcClient.transactions(request.hash).getOrNull() ?: throw ServiceUnavailable

        val status = if (transaction.success) TransactionState.Confirmed else TransactionState.Reverted
        val fee = BigInteger(transaction.gas_used) * BigInteger(transaction.gas_unit_price)
        return TransactionChanges(status, fee)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}