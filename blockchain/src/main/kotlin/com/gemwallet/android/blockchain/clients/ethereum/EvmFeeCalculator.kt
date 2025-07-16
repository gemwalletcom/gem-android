package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmFeeService
import com.gemwallet.android.blockchain.clients.ethereum.services.getFeeHistory
import com.gemwallet.android.blockchain.clients.ethereum.services.getGasLimit
import com.gemwallet.android.ext.toEVM
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import uniffi.gemstone.GemEthereumFeeHistory
import uniffi.gemstone.GemFeeCalculator
import uniffi.gemstone.GemPriorityFeeRecord
import wallet.core.jni.CoinType
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale

class EvmFeeCalculator(
    private val feeService: EvmFeeService,
    callService: EvmCallService,
    coinType: CoinType
) {

    private val optimismGasOracle = OptimismGasOracle(callService, coinType)

    private val nativeGasLimit = BigInteger.valueOf(21_000L)

    suspend fun calculate(
        params: ConfirmParams, // TODO: Outpack
        assetId: AssetId,
        recipient: String,
        outputAmount: BigInteger,
        payload: String?,
        chainId: Int,
        nonce: BigInteger,
    ): List<GasFee> = withContext(Dispatchers.IO) {
        val getGasLimit = async { getGasLimit(assetId, params.from.address, recipient, outputAmount, payload) }
        val getBasePriorityFees = async { getBasePriorityFees(params) }
        val gasLimit = getGasLimit.await()
        val (baseFee, priorityFees) = getBasePriorityFees.await()

        if (params.assetId.chain.toEVM()?.isOpStack() == true) {
            return@withContext priorityFees.mapIndexed { index, priorityFee ->
                optimismGasOracle.estimate(
                    params = params,
                    chainId = chainId,
                    nonce = nonce,
                    gasLimit = gasLimit,
                    baseFee = baseFee,
                    priorityFee = priorityFee,
                    txSpeed = when (index) {
                        0 -> FeePriority.Slow
                        1 -> FeePriority.Normal
                        2 -> FeePriority.Fast
                        else -> FeePriority.Normal
                    }
                )
            }
        }

        priorityFees.map { priorityFee ->
            val value = priorityFee.value.toBigInteger()
            val maxGasPrice: BigInteger = baseFee.plus(value)
            val minerFee: BigInteger = when (params) {
                is ConfirmParams.Stake,
                is ConfirmParams.SwapParams,
                is ConfirmParams.NftParams,
                is ConfirmParams.TokenApprovalParams -> value
                is ConfirmParams.TransferParams -> if (params.assetId.type() == AssetSubtype.NATIVE && params.isMax()) {
                    maxGasPrice
                } else {
                    value
                }

                is ConfirmParams.Activate -> throw IllegalArgumentException()
            }
            GasFee(
                feeAssetId = AssetId(params.assetId.chain),
                priority = FeePriority.entries.firstOrNull { it.string == priorityFee.priority.name.lowercase() }
                    ?: FeePriority.Normal,
                limit = gasLimit,
                maxGasPrice = maxGasPrice,
                minerFee = minerFee,
            )
        }
    }

    private suspend fun getGasLimit(
        assetId: AssetId,
        from: String,
        recipient: String,
        outputAmount: BigInteger,
        payload: String?,
    ): BigInteger {
        val (amount, to, data) = when (assetId.type()) {
            AssetSubtype.NATIVE -> Triple(outputAmount, recipient, payload)
            AssetSubtype.TOKEN -> Triple(
                BigInteger.ZERO,    // Amount
                assetId.tokenId!!.lowercase(Locale.ROOT), // token
                EVMChain.encodeTransactionData(assetId, payload, outputAmount, recipient).toHexString()
            )
        }

        val gasLimit = try {
            feeService.getGasLimit(from, to, amount, data)
        } catch (err: Throwable) {
            throw err
        }
        return if (gasLimit == nativeGasLimit) {
            gasLimit
        } else {
            gasLimit.add(gasLimit.toBigDecimal().multiply(BigDecimal.valueOf(0.5)).toBigInteger())
        }
    }

    internal suspend fun getHistory(chain: Chain, feeService: EvmFeeService): Pair<BigInteger, GemEthereumFeeHistory> {
        val config = Config().getEvmChainConfig(chain.toEVM()?.string ?: throw IllegalArgumentException())
        val rewardsPercentiles = config.rewardsPercentiles
        val feeHistory = feeService.getFeeHistory(
            listOf(rewardsPercentiles.slow, rewardsPercentiles.normal, rewardsPercentiles.fast)
                .map { it.toInt() }
        ) ?: throw Exception("Unable to calculate base fee")
        val baseFee = feeHistory.baseFeePerGas.mapNotNull { it.hexToBigInteger() }.maxOrNull()
            ?: throw Exception("Unable to calculate base fee")

        return Pair(
            baseFee,
            GemEthereumFeeHistory(
                reward = feeHistory.reward,
                baseFeePerGas = feeHistory.baseFeePerGas,
                gasUsedRatio = feeHistory.gasUsedRatio,
                oldestBlock = feeHistory.oldestBlock,
            )
        )
    }

    internal suspend fun getBasePriorityFees(
        params: ConfirmParams,
    ): Pair<BigInteger, List<GemPriorityFeeRecord>> {
        val history = getHistory(params.asset.id.chain, feeService)
        val calculator = GemFeeCalculator()
        val priority = calculator.calculateBasePriorityFees(
            chain = params.from.chain.string,
            history = history.second
        )
        return Pair(history.first, priority)
    }
}