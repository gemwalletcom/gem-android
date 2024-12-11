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
import kotlinx.coroutines.async
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

    private val optimismGasOracle = OptimismGasOracle(callService, coinType)

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
        val getGasLimit = async { getGasLimit(assetId, params.from.address, recipient, outputAmount, payload) }
        val getBasePriorityFee = async { getBasePriorityFee(params.assetId.chain, feeService) }
        val gasLimit = getGasLimit.await()
        val (baseFee, priorityFee) = getBasePriorityFee.await()

        if (params.assetId.chain.toEVM()?.isOpStack() == true) {
            return@withContext optimismGasOracle.estimate(
                params = params,
                chainId = chainId,
                nonce = nonce,
                gasLimit = gasLimit,
                baseFee = baseFee,
                priorityFee = priorityFee,
            )
        }

        val maxGasPrice = baseFee.plus(priorityFee)
        val minerFee = when (params) {
            is ConfirmParams.Stake,
            is ConfirmParams.SwapParams,
            is ConfirmParams.TokenApprovalParams -> priorityFee
            is ConfirmParams.TransferParams -> if (params.assetId.type() == AssetSubtype.NATIVE && params.isMax()) {
                maxGasPrice
            } else {
                priorityFee
            }
        }
        GasFee(
            feeAssetId = AssetId(params.assetId.chain),
            speed = TxSpeed.Normal,
            limit = gasLimit,
            maxGasPrice = maxGasPrice,
            minerFee = minerFee,
        )
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

    internal suspend fun getBasePriorityFee(chain: Chain, feeService: EvmFeeService): Pair<BigInteger, BigInteger> {
        val feeHistory = feeService.getFeeHistory() ?: throw Exception("Unable to calculate base fee")

        val reward = feeHistory.reward.mapNotNull { it.firstOrNull()?.hexToBigInteger() }.maxOrNull()
            ?: throw Exception("Unable to calculate priority fee")

        val baseFee = feeHistory.baseFeePerGas.mapNotNull { it.hexToBigInteger() }.maxOrNull()
            ?: throw Exception("Unable to calculate base fee")

        val defaultPriorityFee = BigInteger(Config().getEvmChainConfig(chain.string).minPriorityFee.toString())
        val priorityFee = if (reward < defaultPriorityFee) defaultPriorityFee else reward
        return Pair(baseFee, priorityFee)
    }
}