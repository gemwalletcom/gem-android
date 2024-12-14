package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinTransactionsService
import com.wallet.core.blockchain.bitcoin.models.BitcoinTransaction
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestBitcoinTransactions {

    @Test
    fun testBitcoinTransaction() {
        var requestId: String = ""
        val transactionsClient = BitcoinTransactionStatusClient(
            Chain.Bitcoin,
            object : BitcoinTransactionsService {
                override suspend fun transaction(txId: String): Result<BitcoinTransaction> {
                    requestId = txId
                    return Result.success(BitcoinTransaction(0))
                }
            }
        )
        val result = runBlocking {
            transactionsClient.getStatus(Chain.Bitcoin, "some_address", "some_id")
        }.getOrNull()
        assertNotNull(result)
        assertEquals("some_id", requestId)
        assertEquals(TransactionState.Pending, result!!.state)
    }

    @Test
    fun testBitcoinTransactionConfirm() {
        var requestId: String = ""
        val transactionsClient = BitcoinTransactionStatusClient(
            Chain.Bitcoin,
            object : BitcoinTransactionsService {
                override suspend fun transaction(txId: String): Result<BitcoinTransaction> {
                    requestId = txId
                    return Result.success(BitcoinTransaction(1))
                }
            }
        )
        val result = runBlocking {
            transactionsClient.getStatus(Chain.Bitcoin, "some_address", "some_id")
        }.getOrNull()
        assertNotNull(result)
        assertEquals(TransactionState.Confirmed, result!!.state)
    }
}