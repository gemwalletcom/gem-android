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
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import wallet.core.jni.CoinType
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale

class EvmFeeCalculator(
    private val feeService: EvmFeeService,
    callService: EvmCallService,
    coinType: CoinType
) {

    private val optimismGasOracle = OptimismGasOracle(feeService, callService, coinType)

    private val nativeGasLimit = BigInteger.valueOf(21_000L)

    suspend fun calculate(
        params: ConfirmParams,
        assetId: AssetId,
        recipient: String,
        outputAmount: BigInteger,
        payload: String?,
        chainId: String,
        nonce: BigInteger,
    ): Fee = withContext(Dispatchers.IO) {
        val isMaxAmount = params.isMax()
        val feeAssetId = AssetId(params.assetId.chain)

        val gasLimit = getGasLimit(assetId, params.from.address, recipient, outputAmount, payload)

        if (params.assetId.chain.toEVM()?.isOpStack() == true) {
            return@withContext optimismGasOracle.estimate(params, chainId, nonce, gasLimit)
        }
        val (baseFee, priorityFee) = getBasePriorityFee(params.assetId.chain, feeService)
        val maxGasPrice = baseFee.plus(priorityFee)
        val minerFee = when (params) {
            is ConfirmParams.Stake,
            is ConfirmParams.SwapParams,
            is ConfirmParams.TokenApprovalParams -> priorityFee
            is ConfirmParams.TransferParams -> if (params.assetId.type() == AssetSubtype.NATIVE && isMaxAmount) maxGasPrice else priorityFee
        }
        GasFee(feeAssetId = feeAssetId, speed = TxSpeed.Normal, limit = gasLimit, maxGasPrice = maxGasPrice, minerFee = minerFee)
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

        val gasLimit = feeService.getGasLimit(from, to, amount, data)
        return if (gasLimit == nativeGasLimit) {
            gasLimit
        } else {
            gasLimit.add(gasLimit.toBigDecimal().multiply(BigDecimal.valueOf(0.5)).toBigInteger())
        }
    }

    companion object {
        internal suspend fun getBasePriorityFee(chain: Chain, feeService: EvmFeeService): Pair<BigInteger, BigInteger> {
            val feeHistory = feeService.getFeeHistory() ?: throw Exception("Unable to calculate base fee")

            val reward = feeHistory.reward.mapNotNull { it.firstOrNull()?.hexToBigInteger() }.maxOrNull()
                ?: throw Exception("Unable to calculate priority fee")

            val baseFee = feeHistory.baseFeePerGas.mapNotNull { it.hexToBigInteger() }.maxOrNull()
                    ?: throw Exception("Unable to calculate base fee")

            val defaultPriorityFee = BigInteger(Config().getEvmChainConfig(chain.string).minPriorityFee.toString()) // Long is too small for it. Don't change to BigInteger.valueOf()
            val priorityFee = if (reward < defaultPriorityFee) defaultPriorityFee else reward
            return Pair(baseFee, priorityFee)
        }
    }
}