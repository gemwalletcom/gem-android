package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.TransactionType
import java.math.BigInteger

class  CosmosFeeCalculator(
    private val chain: Chain,
) {
    fun calculate(txType: TransactionType): Fee {
        val assetId = AssetId(chain)
        val maxGasFee = when (chain) {
            Chain.Cosmos -> when (txType) {
                TransactionType.Transfer, TransactionType.Swap -> BigInteger.valueOf(3_000L)
                else -> BigInteger.valueOf(25_000L)
            }
            Chain.Osmosis -> when (txType) {
                TransactionType.Transfer, TransactionType.Swap -> BigInteger.valueOf(10_000L)
                else -> BigInteger.valueOf(100_000L)
            }

            Chain.Thorchain -> BigInteger.valueOf(2_000_000)
            Chain.Celestia -> when (txType){
                TransactionType.Transfer, TransactionType.Swap -> BigInteger.valueOf(3_000L)
                else -> BigInteger.valueOf(10_000L)
            }
            Chain.Injective -> when (txType){
                TransactionType.Transfer, TransactionType.Swap -> BigInteger.valueOf(100_000_000_000_000L)
                else -> BigInteger.valueOf(1_000_000_000_000_000L)
            }
            Chain.Sei -> when (txType){
                TransactionType.Transfer, TransactionType.Swap -> BigInteger.valueOf(100_000L)
                else -> BigInteger.valueOf(200_000L)
            }
            Chain.Noble -> BigInteger.valueOf(25_000)
            else -> throw IllegalArgumentException("Unsupported chain")
        }
        val limit = when (txType) {
            TransactionType.Transfer, TransactionType.Swap -> BigInteger.valueOf(200_000L)
            TransactionType.StakeDelegate,
            TransactionType.StakeUndelegate -> BigInteger.valueOf(1_000_000)
            TransactionType.StakeRewards -> BigInteger.valueOf(900_000)
            TransactionType.StakeRedelegate -> BigInteger.valueOf(1_250_000)
            TransactionType.StakeWithdraw,
            TransactionType.AssetActivation,
            TransactionType.TransferNFT,
            TransactionType.SmartContractCall,
            TransactionType.TokenApproval -> throw IllegalArgumentException("Chain not available: no limit fee")

        }
        return GasFee(
            feeAssetId = assetId,
            priority = FeePriority.Normal,
            maxGasPrice = maxGasFee,
            amount = maxGasFee,
            limit = limit,
        )
    }
}