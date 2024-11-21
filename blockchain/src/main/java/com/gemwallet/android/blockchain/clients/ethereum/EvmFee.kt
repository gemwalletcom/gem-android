package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
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
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import wallet.core.jni.CoinType
import java.math.BigInteger

class EvmFee {
    suspend operator fun invoke(
        rpcClient: EvmRpcClient,
        params: ConfirmParams,
        chainId: BigInteger,
        nonce: BigInteger,
        gasLimit: BigInteger,
        coinType: CoinType,
    ): Fee = withContext(Dispatchers.IO) {
        val assetId = params.assetId
        val isMaxAmount = params.isMax()
        val feeAssetId = AssetId(assetId.chain)

        if (EVMChain.isOpStack(params.assetId.chain)) {
            return@withContext OptimismGasOracle().invoke(
                params = params,
                chainId = chainId,
                nonce = nonce,
                gasLimit = gasLimit,
                coin = coinType,
                rpcClient = rpcClient,
            )
        }
        val (baseFee, priorityFee) = getBasePriorityFee(chain = assetId.chain, rpcClient = rpcClient)

        val maxGasPrice = baseFee.plus(priorityFee)
        val minerFee = when (params.getTxType()) {
            TransactionType.Transfer -> if (assetId.type() == AssetSubtype.NATIVE && isMaxAmount) maxGasPrice else priorityFee
            TransactionType.StakeUndelegate,
            TransactionType.StakeWithdraw,
            TransactionType.StakeRedelegate,
            TransactionType.StakeDelegate,
            TransactionType.Swap,
            TransactionType.TokenApproval -> priorityFee
            else -> throw IllegalAccessException("Operation doesn't available")
        }
        GasFee(feeAssetId = feeAssetId, speed = TxSpeed.Normal, limit = gasLimit, maxGasPrice = maxGasPrice, minerFee = minerFee)
    }

    companion object {
        internal suspend fun getBasePriorityFee(
            chain: Chain,
            rpcClient: EvmRpcClient
        ): Pair<BigInteger, BigInteger> {
            val feeHistory = rpcClient.getFeeHistory(
                JSONRpcRequest.create(EvmMethod.GetFeeHistory, listOf("10", "latest", listOf(25)))
            ).getOrNull()?.result ?: throw Exception("Unable to calculate base fee")
            val reward = feeHistory.reward
                .mapNotNull { it.firstOrNull() }
                .mapNotNull { it.hexToBigInteger() }
                .maxByOrNull { it }
                ?: throw Exception("Unable to calculate priority fee")
            val baseFee =
                feeHistory.baseFeePerGas.mapNotNull { it.hexToBigInteger() }.maxByOrNull { it }
                    ?: throw Exception("Unable to calculate base fee")
            val defaultPriorityFee =
                BigInteger(Config().getEvmChainConfig(chain.string).minPriorityFee.toString())
            val priorityFee = if (reward < defaultPriorityFee) defaultPriorityFee else reward
            return Pair(baseFee, priorityFee)
        }
    }
}