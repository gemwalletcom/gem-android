package com.gemwallet.android.blockchain.clients.evm

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmTransactionsService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.ethereum.models.EthereumTransactionReciept
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.theories.suppliers.TestedOn
import org.junit.runner.RunWith
import java.math.BigInteger
import kotlin.String

class TestEvmTransactionStatusClient {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testEvm_transaction_tastus_confirm() {
        var requestAddress = ""
        val  client = EvmTransactionStatusClient(
            chain = Chain.SmartChain,
            transactionsService = object : EvmTransactionsService {
                override suspend fun transaction(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EthereumTransactionReciept>> {
                    requestAddress = request.params[0]
                    return Result.success(
                        JSONRpcResponse(
                            EthereumTransactionReciept(
                                status = "0x1",
                                gasUsed = "10",
                                effectiveGasPrice = "2",
                                l1Fee = "1"
                            )
                        )
                    )
                }

            }
        )
        val resullt = runBlocking {
            client.getStatus(Chain.SmartChain, "0x502aECFE253E6AA0e8D2A06E12438FFeD0Fe16a0", "0xe84b29d6f06aeb00ba071a409ff057649f19ebbc209114a6a8135a68af589e22")
                .getOrNull()
        }
        assertNotNull(resullt)
        assertEquals(TransactionState.Confirmed, resullt!!.state)
        assertEquals(BigInteger("21"), resullt.fee)
    }

    @Test
    fun testEvm_transaction_tastus_fail() {
        var requestAddress = ""
        val  client = EvmTransactionStatusClient(
            chain = Chain.SmartChain,
            transactionsService = object : EvmTransactionsService {
                override suspend fun transaction(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EthereumTransactionReciept>> {
                    requestAddress = request.params[0]
                    return Result.success(
                        JSONRpcResponse(
                            EthereumTransactionReciept(
                                status = "0x0",
                                gasUsed = "10",
                                effectiveGasPrice = "2",
                                l1Fee = "1"
                            )
                        )
                    )
                }

            }
        )
        val resullt = runBlocking {
            client.getStatus(Chain.SmartChain, "0x502aECFE253E6AA0e8D2A06E12438FFeD0Fe16a0", "0xe84b29d6f06aeb00ba071a409ff057649f19ebbc209114a6a8135a68af589e22")
                .getOrNull()
        }
        assertNotNull(resullt)
        assertEquals(TransactionState.Reverted, resullt!!.state)
        assertEquals(BigInteger("21"), resullt.fee)
    }

    @Test
    fun testEvm_transaction_tastus_pending() {
        var requestAddress = ""
        val  client = EvmTransactionStatusClient(
            chain = Chain.SmartChain,
            transactionsService = object : EvmTransactionsService {
                override suspend fun transaction(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EthereumTransactionReciept>> {
                    requestAddress = request.params[0]
                    return Result.success(
                        JSONRpcResponse(
                            EthereumTransactionReciept(
                                status = "0x2",
                                gasUsed = "10",
                                effectiveGasPrice = "2",
                                l1Fee = "1"
                            )
                        )
                    )
                }

            }
        )
        val resullt = runBlocking {
            client.getStatus(Chain.SmartChain, "0x502aECFE253E6AA0e8D2A06E12438FFeD0Fe16a0", "0xe84b29d6f06aeb00ba071a409ff057649f19ebbc209114a6a8135a68af589e22")
                .getOrNull()
        }
        assertNotNull(resullt)
        assertEquals(TransactionState.Pending, resullt!!.state)
        assertNull(resullt.fee)
    }
}