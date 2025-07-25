package com.gemwallet.android.blockchain.clients.evm

import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.ethereum.EvmTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmTransactionsService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.ethereum.EthereumTransactionReciept
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestEvmTransactionStatusClient {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testEvm_transaction_tastus_confirm() {
        val  client = EvmTransactionStatusClient(
            chain = Chain.SmartChain,
            transactionsService = object : EvmTransactionsService {
                override suspend fun transaction(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EthereumTransactionReciept?>> {
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
            client.getStatus(
                TransactionStateRequest(
                    Chain.SmartChain,
                    "0xe84b29d6f06aeb00ba071a409ff057649f19ebbc209114a6a8135a68af589e22",
                    "",
                    "0x502aECFE253E6AA0e8D2A06E12438FFeD0Fe16a0",
                )
            )
        }
        assertNotNull(resullt)
        assertEquals(TransactionState.Confirmed, resullt.state)
        assertEquals(BigInteger("21"), resullt.fee)
    }

    @Test
    fun testEvm_transaction_tastus_fail() {
        val  client = EvmTransactionStatusClient(
            chain = Chain.SmartChain,
            transactionsService = object : EvmTransactionsService {
                override suspend fun transaction(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EthereumTransactionReciept?>> {
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
            client.getStatus(
                TransactionStateRequest(
                    Chain.SmartChain,
                    "0xe84b29d6f06aeb00ba071a409ff057649f19ebbc209114a6a8135a68af589e22",
                    "",
                    "0x502aECFE253E6AA0e8D2A06E12438FFeD0Fe16a0",
                )

            )
        }
        assertNotNull(resullt)
        assertEquals(TransactionState.Reverted, resullt.state)
        assertEquals(BigInteger("21"), resullt.fee)
    }

    @Test
    fun testEvm_transaction_tastus_pending() {
        val  client = EvmTransactionStatusClient(
            chain = Chain.SmartChain,
            transactionsService = object : EvmTransactionsService {
                override suspend fun transaction(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EthereumTransactionReciept?>> {
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
            client.getStatus(
                TransactionStateRequest(
                    Chain.SmartChain,
                    "0xe84b29d6f06aeb00ba071a409ff057649f19ebbc209114a6a8135a68af589e22",
                    "",
                    "0x502aECFE253E6AA0e8D2A06E12438FFeD0Fe16a0",
                )
            )
        }
        assertNotNull(resullt)
        assertEquals(TransactionState.Pending, resullt.state)
        assertNull(resullt.fee)
    }
}