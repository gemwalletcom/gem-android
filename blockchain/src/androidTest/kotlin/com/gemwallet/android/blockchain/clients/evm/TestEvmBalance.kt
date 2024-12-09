package com.gemwallet.android.blockchain.clients.evm

import com.gemwallet.android.blockchain.clients.ethereum.EvmBalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmRpcClient
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmBalancesService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.model.getTotalAmount
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestEvmBalance {

    class CallService : EvmCallService {
        override suspend fun callString(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String?>> {
            return Result.success(JSONRpcResponse(null))
        }

        override suspend fun callNumber(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
            return Result.success(JSONRpcResponse(null))
        }

    }
    class BalanceService : EvmBalancesService {
        var nativeBalanceParam: String = ""

        override suspend fun getBalance(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
            nativeBalanceParam = request.params[0]
            return Result.success(JSONRpcResponse(EvmRpcClient.EvmNumber(BigInteger.TEN.pow(16))))
        }

    }

    @Test
    fun testNativeBalance() {
        val balanceService = BalanceService()
        val balanceClient = EvmBalanceClient(Chain.Ethereum, CallService(), balanceService, SmartchainStakeClient(Chain.Ethereum, CallService()))

        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
        }
        assertEquals("0x9b1DB81180c31B1b428572Be105E209b5A6222b7", balanceService.nativeBalanceParam)
        assertNotNull(result)
        assertEquals("10000000000000000", result!!.balance.available)
        assertEquals("0", result.balance.rewards)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.pending)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.reserved)
        assertEquals(BigInteger.valueOf(10_000_000_000_000_000), result.balance.getTotalAmount())
        assertEquals(0.01, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(0.01, result.balanceAmount.getTotalAmount())
    }

    @Test
    fun testNativeBalanceBadResponse() {
        var nativeBalanceParam: String = ""
        val balancesService = object : EvmBalancesService {
            override suspend fun getBalance(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
                nativeBalanceParam = request.params[0]
                return Result.success(JSONRpcResponse(EvmRpcClient.EvmNumber(null)))
            }

        }
        val balanceClient = EvmBalanceClient(Chain.Ethereum, CallService(), balancesService, SmartchainStakeClient(Chain.Ethereum, CallService()))

        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
        }
        assertEquals("0x9b1DB81180c31B1b428572Be105E209b5A6222b7", nativeBalanceParam)
        assertNull(result)
    }

    @Test
    fun test_native_balance_on_SmartChain_without_delegations() {
        val balanceService = BalanceService()
        val balanceClient = EvmBalanceClient(Chain.SmartChain, CallService(), balanceService, SmartchainStakeClient(Chain.SmartChain, CallService()))

        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
        }
        assertNotNull(result)
        assertEquals("10000000000000000", result!!.balance.available)
        assertEquals("0", result.balance.rewards)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.pending)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.reserved)
        assertEquals(BigInteger.valueOf(10_000_000_000_000_000), result.balance.getTotalAmount())
    }

    @Test
    fun test_native_balance_on_SmartChain_with_delegations() {

    }

    @Test
    fun testTokenBalance() {

    }
}