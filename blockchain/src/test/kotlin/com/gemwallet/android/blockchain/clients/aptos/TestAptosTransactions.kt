package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.aptos.services.AptosTransactionsService
import com.wallet.core.blockchain.aptos.models.AptosTransaction
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestAptosTransactions {

    @Test
    fun testAptosTransaction() {
        var requestId: String = ""
        val transactionsClient = AptosTransactionStatusClient(
            Chain.Aptos,
            object : AptosTransactionsService {
                override suspend fun transactions(txId: String): Result<AptosTransaction> {
                    requestId = txId
                    return Result.success(
                        AptosTransaction(
                            success = true,
                            gas_used = "100",
                            gas_unit_price = "10"
                        )
                    )
                }
            }
        )
        val result = runBlocking {
            transactionsClient.getStatus(
                Chain.Aptos,
                "some_address",
                "some_id"
            )
        }.getOrNull()
        assertNotNull(result)
        assertEquals("some_id", requestId)
        assertEquals(TransactionState.Confirmed, result!!.state)
        assertEquals(BigInteger("1000"), result.fee)
    }

    @Test
    fun testAptosTransactionFail() {
        var requestId: String = ""
        val transactionsClient = AptosTransactionStatusClient(
            Chain.Aptos,
            object : AptosTransactionsService {
                override suspend fun transactions(txId: String): Result<AptosTransaction> {
                    requestId = txId
                    return Result.success(
                        AptosTransaction(
                            success = false,
                            gas_used = "100",
                            gas_unit_price = "10"
                        )
                    )
                }
            }
        )
        val result = runBlocking {
            transactionsClient.getStatus(
                Chain.Aptos,
                "some_address",
                "some_id"
            )
        }.getOrNull()
        assertNotNull(result)
        assertEquals(TransactionState.Reverted, result!!.state)
    }
}