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
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import uniffi.gemstone.EvmHistoryRewardPercentiles
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
        chainId: String,
        nonce: BigInteger,
    ): List<GasFee> = withContext(Dispatchers.IO) {
        val getGasLimit = async { getGasLimit(assetId, params.from.address, recipient, outputAmount, payload) } // TODO: params.from.address plain
        val getBasePriorityFees = async { getBasePriorityFee(params.assetId.chain, feeService) }    // TODO: chain is plain
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
                        0 -> TxSpeed.Slow
                        1 -> TxSpeed.Normal
                        2 -> TxSpeed.Fast
                        else -> TxSpeed.Normal
                    }
                )
            }
        }

        priorityFees.mapIndexed { index, priorityFee ->
            val maxGasPrice = baseFee.plus(priorityFee)
            val minerFee = when (params) {
                is ConfirmParams.Stake,
                is ConfirmParams.SwapParams,
                is ConfirmParams.TokenApprovalParams -> priorityFee
                is ConfirmParams.TransferParams -> if (params.assetId.type() == AssetSubtype.NATIVE && params.isMax()) { // TODO: params.assetId.type - plain, replace to type
                    maxGasPrice
                } else {
                    priorityFee
                }
            }
            GasFee(
                feeAssetId = AssetId(params.assetId.chain), // TODO: params.assetId.chain
                speed = when (index) {
                    0 -> TxSpeed.Slow
                    1 -> TxSpeed.Normal
                    2 -> TxSpeed.Fast
                    else -> TxSpeed.Normal
                },
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

    internal suspend fun getBasePriorityFee(chain: Chain, feeService: EvmFeeService): Pair<BigInteger, List<BigInteger>> {
        val config = Config().getEvmChainConfig(chain.toEVM()?.string ?: throw IllegalArgumentException())
        val rewardsPercentiles = config.rewardsPercentiles
        val minPriorityFee = config.minPriorityFee.toString().toBigInteger()

        val feeHistory = feeService.getFeeHistory(
            listOf(rewardsPercentiles.slow, rewardsPercentiles.normal, rewardsPercentiles.fast)
                .map { it.toInt() }
        ) ?: throw Exception("Unable to calculate base fee")

        val reward = feeHistory.reward.mapNotNull { it.firstOrNull()?.hexToBigInteger() }.maxOrNull()
            ?: throw Exception("Unable to calculate priority fee")

        val baseFee = feeHistory.baseFeePerGas.mapNotNull { it.hexToBigInteger() }.maxOrNull()
            ?: throw Exception("Unable to calculate base fee")

        val priorityFees = calculatePriorityFees(feeHistory.reward, rewardsPercentiles, minPriorityFee)
        return Pair(baseFee, priorityFees)
    }

    internal fun calculatePriorityFees(
        rewards: List<List<String>>,
        rewardsPercentiles: EvmHistoryRewardPercentiles,
        minPriorityFee: BigInteger
    ): List<BigInteger> {
        return TxSpeed.entries.map { speed ->
            val prices = when (speed) {
                TxSpeed.Slow -> rewards.mapNotNull { it.getOrNull(0)?.hexToBigInteger() }
                TxSpeed.Normal -> rewards.mapNotNull { it.getOrNull(1)?.hexToBigInteger() }
                TxSpeed.Fast -> rewards.mapNotNull { it.getOrNull(2)?.hexToBigInteger() }
            }
            if (prices.isEmpty()) {
                minPriorityFee
            } else {
                minPriorityFee.max(prices.fold(BigInteger.ZERO) { acc, v -> acc + v / prices.size.toBigInteger() })
            }
        }
    }

}