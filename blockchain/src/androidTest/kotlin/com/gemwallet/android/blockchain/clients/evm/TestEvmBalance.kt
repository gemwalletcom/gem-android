package com.gemwallet.android.blockchain.clients.evm

import com.gemwallet.android.blockchain.clients.ethereum.EvmBalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmBalancesService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmRpcClient
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.model.getTotalAmount
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestEvmBalance {

    companion object {
        init {
            includeLibs()
        }
    }

    class CallService : EvmCallService {
        override suspend fun callString(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String?>> {
            return Result.success(JSONRpcResponse("0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000000"))
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
            balanceClient.getNativeBalance(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
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
        val balanceService = BalanceService()
        val callService = object : EvmCallService {
            override suspend fun callString(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String?>> {
                val data = when ((request.params[0] as Map<*, *>)["data"]) {
                    "0xced0e70e000000000000000000000000ee7e9ccfb529f2c1cc02c0aea8aced7ec7e98b5e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002d" -> {
                        "0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000001000000000000000000000000ee7e9ccfb529f2c1cc02c0aea8aced7ec7e98b5e0000000000000000000000009941bce2601fc93478df9f5f6cc83f4ffc1d71d80000000000000000000000000000000000000000000000000deacafbb23d1ee90000000000000000000000000000000000000000000000000dc3088951e56b15"
                    }
                    "0xd9d4c020000000000000000000000000ee7e9ccfb529f2c1cc02c0aea8aced7ec7e98b5e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002d" -> {
                        "0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000000"
                    }
                    "0xc473318f" -> "0x000000000000000000000000000000000000000000000000000000000000002d"
                    else -> "0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000000"
                }
                return Result.success(JSONRpcResponse(data))
            }

            override suspend fun callNumber(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
                TODO("Not yet implemented")
            }
        }
        val balanceClient = EvmBalanceClient(
            Chain.SmartChain,
            callService,
            balanceService,
            SmartchainStakeClient(Chain.SmartChain, callService)
        )

        val result = runBlocking {
            balanceClient.getDelegationBalances(Chain.SmartChain, "0xEe7E9CcFb529f2c1Cc02C0Aea8aCed7Ec7e98B5e")
        }
        assertNotNull(result)
        assertEquals("0", result!!.balance.available)
        assertEquals("0", result.balance.rewards)
        assertEquals("1002837049419308777", result.balance.staked)
        assertEquals("0", result.balance.pending)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.reserved)
        assertEquals(BigInteger.valueOf(1002837049419308777), result.balance.getTotalAmount())
    }

    @Test
    fun testTokenBalance() {
        val balanceService = BalanceService()
        val callService = object : EvmCallService {
            override suspend fun callString(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String?>> {
                TODO("Not yet implemented")
            }

            override suspend fun callNumber(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
                val result = when ((request.params[0] as Map<*, *>)["to"]) {
                    "0x76A797A59Ba2C17726896976B7B3747BfD1d220f" -> "0x00000000000000000000000000000000000000000000000000000002eedef652"
                    "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c" -> "0x000000000000000000000000000000000000000000000000006a94d74f430000"
                    else -> "0x"
                }
                return Result.success(JSONRpcResponse(EvmRpcClient.EvmNumber(BigInteger(result.removePrefix("0x"), 16))))
            }
        }
        val balanceClient = EvmBalanceClient(Chain.SmartChain, callService, balanceService, SmartchainStakeClient(Chain.SmartChain, CallService()))

        val result = runBlocking {
            balanceClient.getTokenBalances(
                Chain.SmartChain,
                "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                listOf(
                    Asset(
                        AssetId(Chain.SmartChain, "0x76A797A59Ba2C17726896976B7B3747BfD1d220f"),
                        name = "ton",
                        symbol = "ton",
                        decimals = 9,
                        type = AssetType.ERC20
                    ),
                    Asset(
                        AssetId(Chain.SmartChain, "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c"),
                        name = "Wrapped BNB",
                        symbol = "WBNB",
                        decimals = 18,
                        type = AssetType.ERC20
                    ),
                )
            )
        }
        assertNotNull(result)
        assertEquals("12597524050", result[0].balance.available)
        assertEquals("0", result[0].balance.rewards)
        assertEquals("0", result[0].balance.staked)
        assertEquals("0", result[0].balance.pending)
        assertEquals("0", result[0].balance.frozen)
        assertEquals("0", result[0].balance.locked)
        assertEquals("0", result[0].balance.reserved)
        assertEquals(BigInteger.valueOf(12597524050), result[0].balance.getTotalAmount())
        assertEquals("30000000000000000", result[1].balance.available)
    }
}