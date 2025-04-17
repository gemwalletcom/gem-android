package com.gemwallet.android.blockchain.clients.evm

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.ethereum.EvmFeeCalculator
import com.gemwallet.android.blockchain.clients.ethereum.StakeHub
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmFeeService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmRpcClient
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.wallet.core.blockchain.ethereum.models.EthereumFeeHistory
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import uniffi.gemstone.Config
import wallet.core.jni.CoinType
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class TestFeeCalculator {
    companion object {
        init {
            includeLibs()
        }
    }

    private class FeeService(
        private val feeHistory: EthereumFeeHistory? = null,
        private val gasLimit: BigInteger = BigInteger.valueOf(21_000),
    ) : EvmFeeService {

        var feeHistoryRequest: List<Any> = emptyList()
        var gasLimitRequest: List<Any> = emptyList()

        override suspend fun getFeeHistory(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EthereumFeeHistory>> {
            feeHistoryRequest = request.params
            return Result.success(JSONRpcResponse(feeHistory ?: throw Exception("Fee history fail")))
        }

        override suspend fun getGasLimit(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
            gasLimitRequest = request.params
            return Result.success(JSONRpcResponse(EvmRpcClient.EvmNumber(gasLimit)))
        }

        override suspend fun getNonce(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
            return Result.success(JSONRpcResponse(EvmRpcClient.EvmNumber(BigInteger.ZERO)))
        }
    }

    private class CallService : EvmCallService {
        override suspend fun callString(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String?>> {
            throw Exception("Call string fail")
        }

        override suspend fun callNumber(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
            return Result.success(JSONRpcResponse(EvmRpcClient.EvmNumber(BigInteger.ONE)))
        }

        override suspend fun callBatch(request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<String>>> {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun testEvm_transfer_fee_calculation_network_fail_gas_limit() {
        val feeService = object : EvmFeeService {
            override suspend fun getFeeHistory(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EthereumFeeHistory>> {
                return Result.failure(Exception("Fee history fail"))
            }

            override suspend fun getGasLimit(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
                return Result.failure(Exception("Gas limit fail"))
            }

            override suspend fun getNonce(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
                return Result.failure(Exception("Nonce fail"))
            }
        }
        val callService = object : EvmCallService {
            override suspend fun callString(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String?>> {
                throw Exception("Call string fail")
            }

            override suspend fun callNumber(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber?>> {
                throw Exception("Call number fail")
            }

            override suspend fun callBatch(request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<String>>> {
                throw Exception("Call number fail")
            }
        }
        try {
            runBlocking {
                EvmFeeCalculator(
                    feeService = feeService,
                    callService = callService,
                    coinType = CoinType.SMARTCHAIN,
                ).calculate(
                    ConfirmParams.Builder(
                        Chain.SmartChain.asset(),
                        Account(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                        amount = BigInteger.TEN,
                    ).transfer(
                        DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
                    ),
                    AssetId(Chain.SmartChain),
                    "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                    outputAmount = BigInteger.TEN,
                    null,
                    chainId = Config().getChainConfig(Chain.SmartChain.string).networkId,
                    BigInteger.ZERO,
                )
            }
            assertTrue(false)
        } catch (err: Throwable) {
            assertTrue("Unable to calculate base fee" == err.message || "Fail calculate gas limit" == err.message)
        }
    }

    @Test
    fun test_Evm_fee_transfer_calculation() {
        val feeService = FeeService(
            EthereumFeeHistory(
                reward = listOf(listOf("0x3b9aca00")),
                baseFeePerGas = listOf("0xa", "0xc")
            ),
            gasLimit = BigInteger.valueOf(25_000)
        )
        val feeCalculator = EvmFeeCalculator(
            feeService = feeService,
            callService = CallService(),
            coinType = CoinType.SMARTCHAIN,
        )
        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    Chain.SmartChain.asset(),
                    Account(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                    amount = BigInteger.TEN,
                ).transfer(
                    DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
                ),
                AssetId(Chain.SmartChain),
                "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                outputAmount = BigInteger.TEN,
                null,
                chainId = Config().getChainConfig(Chain.SmartChain.string).networkId,
                BigInteger.ZERO,
            )
        }
        assertEquals(FeePriority.Normal, result[1].priority)
        assertEquals(BigInteger("37500000450000"), result[1].amount)
        assertEquals(BigInteger("1000000000"), result[1].minerFee)
        assertEquals(BigInteger("1000000012"), result[1].maxGasPrice)
        assertEquals(BigInteger("37500"), result[1].limit)
    }

    @Test
    fun test_Evm_fee_transfer_max_amount_calculation() {
        val feeService = FeeService(
            EthereumFeeHistory(
                reward = listOf(listOf("0x3b9aca00")),
                baseFeePerGas = listOf("0xa", "0xc")
            ),
            gasLimit = BigInteger.valueOf(25_000)
        )
        val feeCalculator = EvmFeeCalculator(
            feeService = feeService,
            callService = CallService(),
            coinType = CoinType.SMARTCHAIN,
        )
        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    Chain.SmartChain.asset(),
                    Account(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                    amount = BigInteger.TEN,
                ).transfer(
                    destination = DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7"),
                    isMax = true
                ),
                AssetId(Chain.SmartChain),
                "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                outputAmount = BigInteger.TEN,
                null,
                chainId = Config().getChainConfig(Chain.SmartChain.string).networkId,
                BigInteger.ZERO,
            )
        }
        assertEquals(FeePriority.Normal, result[1].priority)
        assertEquals(BigInteger("37500000450000"), result[1].amount)
        assertEquals(BigInteger("1000000012"), result[1].minerFee)
        assertEquals(BigInteger("1000000012"), result[1].maxGasPrice)
        assertEquals(BigInteger("37500"), result[1].limit)
    }


    @Test
    fun test_Evm_gas_limit_native_transfer_calculation() {
        val feeService = FeeService(
            EthereumFeeHistory(
                reward = listOf(listOf("0x3b9aca00")),
                baseFeePerGas = listOf("0x0", "0x0")
            ),
            gasLimit = BigInteger.valueOf(21_000)
        )
        val feeCalculator = EvmFeeCalculator(
            feeService = feeService,
            callService = CallService(),
            coinType = CoinType.SMARTCHAIN,
        )
        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    Chain.SmartChain.asset(),
                    Account(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                    amount = BigInteger.TEN,
                ).transfer(
                    DestinationAddress("0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A")
                ),
                AssetId(Chain.SmartChain),
                "0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A",
                outputAmount = BigInteger.TEN,
                null,
                chainId = Config().getChainConfig(Chain.SmartChain.string).networkId,
                BigInteger.ZERO,
            )
        }
        assertEquals(FeePriority.Normal, result[1].priority)
        assertEquals(BigInteger("21000000000000"), result[1].amount)
        assertEquals(BigInteger.valueOf(21_000), result[1].limit)
        assertEquals("0x9b1DB81180c31B1b428572Be105E209b5A6222b7", (feeService.gasLimitRequest[0] as Map<*, *>)["from"])
        assertEquals("0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A", (feeService.gasLimitRequest[0] as Map<*, *>)["to"])
        assertEquals("0xa", (feeService.gasLimitRequest[0] as Map<*, *>)["value"])
        assertEquals("0x", (feeService.gasLimitRequest[0] as Map<*, *>)["data"])
    }

    @Test
    fun test_Evm_Oracle() {
        val feeService = FeeService(
            EthereumFeeHistory(
                reward = listOf(listOf("0x3b9aca00")),
                baseFeePerGas = listOf("0x0", "0x0")
            ),
            gasLimit = BigInteger.valueOf(21_000)
        )
        val feeCalculator = EvmFeeCalculator(
            feeService = feeService,
            callService = CallService(),
            coinType = CoinType.SMARTCHAIN,
        )
        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    Chain.OpBNB.asset(),
                    Account(Chain.OpBNB, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                    amount = BigInteger.TEN,
                ).transfer(
                    DestinationAddress("0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A")
                ),
                AssetId(Chain.OpBNB),
                "0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A",
                outputAmount = BigInteger.TEN,
                null,
                chainId = Config().getChainConfig(Chain.OpBNB.string).networkId,
                BigInteger.ZERO,
            )
        }
        assertEquals(FeePriority.Normal, result[1].priority)
        assertEquals(BigInteger("21000000001"), result[1].amount)
        assertEquals(BigInteger.valueOf(21_000), result[1].limit)
        assertEquals("0x9b1DB81180c31B1b428572Be105E209b5A6222b7", (feeService.gasLimitRequest[0] as Map<*, *>)["from"])
        assertEquals("0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A", (feeService.gasLimitRequest[0] as Map<*, *>)["to"])
        assertEquals("0xa", (feeService.gasLimitRequest[0] as Map<*, *>)["value"])
        assertEquals("0x", (feeService.gasLimitRequest[0] as Map<*, *>)["data"])
    }

    @Test
    fun test_Evm_gas_limit_token_transfer_calculation() {
        val feeService = FeeService(
            EthereumFeeHistory(
                reward = listOf(listOf("0x3b9aca00")),
                baseFeePerGas = listOf("0x0", "0x0")
            ),
            gasLimit = BigInteger.valueOf(21_000)
        )
        val feeCalculator = EvmFeeCalculator(
            feeService = feeService,
            callService = CallService(),
            coinType = CoinType.SMARTCHAIN,
        )
        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    Asset(AssetId(Chain.SmartChain, "0x2170Ed0880ac9A755fd29B2688956BD959F933F8"), name = "usdt", symbol = "usdt", decimals = 8, type = AssetType.TOKEN),
                    Account(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                    amount = BigInteger.TEN,
                ).transfer(
                    DestinationAddress("0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A")
                ),
                AssetId(Chain.SmartChain, "0x2170Ed0880ac9A755fd29B2688956BD959F933F8"),
                "0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A",
                outputAmount = BigInteger.TEN,
                null,
                chainId = Config().getChainConfig(Chain.SmartChain.string).networkId,
                BigInteger.ZERO,
            )
        }
        assertEquals(FeePriority.Normal, result[1].priority)
        assertEquals(BigInteger("21000000000000"), result[1].amount)
        assertEquals(BigInteger.valueOf(21_000), result[1].limit)
        assertEquals("0x9b1DB81180c31B1b428572Be105E209b5A6222b7", (feeService.gasLimitRequest[0] as Map<*, *>)["from"])
        assertEquals("0x2170ed0880ac9a755fd29b2688956bd959f933f8", (feeService.gasLimitRequest[0] as Map<*, *>)["to"])
        assertEquals("0x0", (feeService.gasLimitRequest[0] as Map<*, *>)["value"])
        assertEquals(
            "0xa9059cbb000000000000000000000000a857a4e4b3f7c0eb7e132a7a4abca287225ddb2a000000000000000000000000000000000000000000000000000000000000000a",
            (feeService.gasLimitRequest[0] as Map<*, *>)["data"]
        )
    }

    @Test
    fun test_Evm_gas_limit_delegate_calculation() {
        val feeService = FeeService(
            EthereumFeeHistory(
                reward = listOf(listOf("0x3b9aca00")),
                baseFeePerGas = listOf("0x0", "0x0")
            ),
            gasLimit = BigInteger.valueOf(21_000)
        )
        val feeCalculator = EvmFeeCalculator(
            feeService = feeService,
            callService = CallService(),
            coinType = CoinType.SMARTCHAIN,
        )
        val params = ConfirmParams.Builder(
            Chain.SmartChain.asset(),
            Account(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
            amount = BigInteger.TEN,
        ).delegate("0xa857a4E4B3f7C0eb7e132A7A4abcA287225dDB2A")
        val result = runBlocking {
            feeCalculator.calculate(
                params = params,
                assetId = AssetId(Chain.SmartChain),
                recipient = StakeHub.address,
                outputAmount = BigInteger.TEN,
                payload = "0xb",
                chainId = Config().getChainConfig(Chain.SmartChain.string).networkId,
                BigInteger.ZERO,
            )
        }
        assertEquals(FeePriority.Normal, result[1].priority)
        assertEquals(BigInteger("21000000000000"), result[1].amount)
        assertEquals(BigInteger.valueOf(21_000), result[1].limit)
        assertEquals("0x9b1DB81180c31B1b428572Be105E209b5A6222b7", (feeService.gasLimitRequest[0] as Map<*, *>)["from"])
        assertEquals("0x0000000000000000000000000000000000002002", (feeService.gasLimitRequest[0] as Map<*, *>)["to"])
        assertEquals("0xa", (feeService.gasLimitRequest[0] as Map<*, *>)["value"])
        assertEquals("0xb", (feeService.gasLimitRequest[0] as Map<*, *>)["data"])
    }

    @Test
    fun testEvm_transfer_fee_calculation_network_fail_fee_history() {
        val feeService = FeeService(
            gasLimit = BigInteger.valueOf(21_000)
        )
        try {
            runBlocking {
                EvmFeeCalculator(
                    feeService = feeService,
                    callService = CallService(),
                    coinType = CoinType.SMARTCHAIN,
                ).calculate(
                    ConfirmParams.Builder(
                        Chain.SmartChain.asset(),
                        Account(Chain.SmartChain, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                        amount = BigInteger.TEN,
                    ).transfer(
                        DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
                    ),
                    AssetId(Chain.SmartChain),
                    "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                    outputAmount = BigInteger.TEN,
                    null,
                    chainId = Config().getChainConfig(Chain.SmartChain.string).networkId,
                    BigInteger.ZERO,
                )
            }
            assertTrue(false)
        } catch (err: Throwable) {
            assertEquals("Fee history fail", err.message)
        }
    }

    @Test
    fun testEvm_transfer_fee_calculation_network_fail_L1Fee() {

    }
}