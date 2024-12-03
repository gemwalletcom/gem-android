package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.ext.type
import com.gemwallet.android.math.hexToBigInteger
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
import java.math.BigInteger

class EvmFeeCalculator(private val rpcClient: EvmRpcClient, coinType: CoinType) {
    private val optimismGasOracle = OptimismGasOracle(rpcClient, coinType)

    suspend fun calculate(
        params: ConfirmParams,
        chainId: BigInteger,
        nonce: BigInteger,
        gasLimit: BigInteger,
    ): Fee = withContext(Dispatchers.IO) {
        val assetId = params.assetId
        val isMaxAmount = params.isMax()
        val feeAssetId = AssetId(assetId.chain)

        if (EVMChain.isOpStack(params.assetId.chain)) {
            return@withContext optimismGasOracle.estimate(params, chainId, nonce, gasLimit)
        }
        val (baseFee, priorityFee) = getBasePriorityFee(assetId.chain, rpcClient)
        val maxGasPrice = baseFee.plus(priorityFee)
        val minerFee = when (params) {
            is ConfirmParams.Stake,
            is ConfirmParams.SwapParams,
            is ConfirmParams.TokenApprovalParams -> priorityFee
            is ConfirmParams.TransferParams -> if (assetId.type() == AssetSubtype.NATIVE && isMaxAmount) maxGasPrice else priorityFee
        }

//        val minerFee = when (params.getTxType()) {
//            TransactionType.Transfer -> if (assetId.type() == AssetSubtype.NATIVE && isMaxAmount) maxGasPrice else priorityFee
//            TransactionType.StakeUndelegate,
//            TransactionType.StakeWithdraw,
//            TransactionType.StakeRedelegate,
//            TransactionType.StakeDelegate,
//            TransactionType.StakeRewards,
//            TransactionType.Swap,
//            TransactionType.TokenApproval -> priorityFee
//        }
        GasFee(feeAssetId = feeAssetId, speed = TxSpeed.Normal, limit = gasLimit, maxGasPrice = maxGasPrice, minerFee = minerFee)
    }

    companion object {
        internal suspend fun getBasePriorityFee(chain: Chain, rpcClient: EvmRpcClient): Pair<BigInteger, BigInteger> {
            val feeHistory = rpcClient.getFeeHistory() ?: throw Exception("Unable to calculate base fee")

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