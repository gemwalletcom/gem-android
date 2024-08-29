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
            ).getOrNull()?.result ?: throw Exception("Unable to calculate base fee") // TODO: Handle errors
            val reward = feeHistory.reward
                .mapNotNull { it.firstOrNull() }
                .mapNotNull { it.hexToBigInteger() }
                .maxByOrNull { it }
                ?: throw Exception("Unable to calculate priority fee")
            val baseFee = feeHistory.baseFeePerGas.mapNotNull{ it.hexToBigInteger() }.maxByOrNull { it }
                ?: throw Exception("Unable to calculate base fee")
            // Default 0.01 gwei
            val priorityFee = if (reward == BigInteger.ZERO) defaultPriorityFee(chain) else reward

            return Pair(baseFee, priorityFee)
        }

        fun defaultPriorityFee(chain: Chain) = when (chain) {
                Chain.Ethereum -> BigInteger.valueOf(1000000000)  // 1 gwei // https://etherscan.io/gastracker
                Chain.SmartChain -> BigInteger.valueOf(1000000000)  // 1 gwei
                Chain.OpBNB -> BigInteger.valueOf(1000000)     // 0.001 gwei https://opbnbscan.com/statistics
                Chain.Polygon -> BigInteger.valueOf(30000000000) // 30 gwei https://polygonscan.com/gastracker
                Chain.Optimism -> BigInteger.valueOf(10000000)    // 0.01 gwei https://optimistic.etherscan.io/chart/gasprice 
                Chain.Arbitrum -> BigInteger.valueOf(10000000) // https://arbiscan.io/address/0x000000000000000000000000000000000000006C#readContract getMinimumGasPrice
                Chain.Base -> BigInteger.valueOf(100000000)  // 0.1 gwei https://basescan.org/chart/gasprice
                Chain.AvalancheC -> BigInteger.valueOf(25000000000) // 25 nAVAX https://snowscan.xyz/gastracker
                Chain.Fantom -> BigInteger.valueOf(3500000000) // 3.5 gwei https://ftmscan.com/gastracker
                Chain.Gnosis -> BigInteger.valueOf(3000000000) // 3 gwei https://gnosisscan.io/gastracker
                Chain.Blast -> BigInteger.valueOf(200000000) // 0.2 gwei https://blastscan.io/chart/gasprice
                Chain.ZkSync -> BigInteger.valueOf(20000000) // 0.02 gwei https://era.zksync.network/chart/gasprice
                Chain.Linea -> BigInteger.valueOf(50000000) // 0.05 gwei https://lineascan.build/gastracker
                Chain.Mantle,
                Chain.Celo,
                Chain.Manta -> BigInteger.valueOf(10000000) // 0.01 gwei
            else -> BigInteger.ZERO
        }
    }
}