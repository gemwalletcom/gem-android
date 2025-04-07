package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.cardano.services.CardanoTransactionService
import com.gemwallet.android.blockchain.clients.cardano.services.transaction
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class CardanoTransactionStatusClient(
    private val chain: Chain,
    private val transactionsService: CardanoTransactionService,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val transaction = transactionsService.transaction(request.hash) ?: throw ServiceUnavailable
        return TransactionChages(
            state = TransactionState.Confirmed,
            fee = transaction.fee.toBigInteger(),
        )
//        TODO: transaction.block.number
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}