package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosTransactionsService
import com.wallet.core.blockchain.cosmos.models.CosmosTransactionDataResponse
import com.wallet.core.blockchain.cosmos.models.CosmosTransactionResponse
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestCosmosTransactions {

    @Test
    fun testCosmosTransaction() {
        var requestId = ""
        val transactionsService = object : CosmosTransactionsService {
            override suspend fun transaction(txId: String): Result<CosmosTransactionResponse> {
                requestId = txId
                return Result.success(CosmosTransactionResponse(CosmosTransactionDataResponse(txId, 0)))
            }

        }
        val transactionsClient = CosmosTransactionStatusClient(Chain.Osmosis, transactionsService)
        val result = runBlocking {
            transactionsClient.getStatus(
                TransactionStateRequest(
                    Chain.Cosmos,
                    "some_id",
                    "",
                    "some_address",
                )
            )
        }
        assertNotNull(result)
        assertEquals("some_id", requestId)
        assertEquals(TransactionState.Confirmed, result.state)
    }

    @Test
    fun testCosmosTransactionPending() {
        val transactionsService = object : CosmosTransactionsService {
            override suspend fun transaction(txId: String): Result<CosmosTransactionResponse> {
                return Result.success(CosmosTransactionResponse(CosmosTransactionDataResponse("", 0)))
            }

        }
        val transactionsClient = CosmosTransactionStatusClient(Chain.Osmosis, transactionsService)
        val result = runBlocking {
            transactionsClient.getStatus(
                TransactionStateRequest(
                    Chain.Cosmos,
                    "some_id",
                    "",
                    "some_address",
                )
            )
        }
        assertNotNull(result)
        assertEquals(TransactionState.Pending, result.state)
    }

    @Test
    fun testCosmosTransactionReverted() {
        val transactionsService = object : CosmosTransactionsService {
            override suspend fun transaction(txId: String): Result<CosmosTransactionResponse> {
                return Result.success(CosmosTransactionResponse(CosmosTransactionDataResponse(txId, 1)))
            }

        }
        val transactionsClient = CosmosTransactionStatusClient(Chain.Osmosis, transactionsService)
        val result = runBlocking {
            transactionsClient.getStatus(
                TransactionStateRequest(
                    Chain.Cosmos,
                    "some_id",
                    "",
                    "some_address",
                )
            )
        }
        assertNotNull(result)
        assertEquals(TransactionState.Reverted, result.state)
    }
}