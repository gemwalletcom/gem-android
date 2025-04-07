package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

class TonFeeCalculator(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) {
    val baseFee = BigInteger.valueOf(10_000_000L)
    val jettonTokenAccountCreation = BigInteger.valueOf(300_000_000L)

    fun calculateNative() = Fee(TxSpeed.Normal, AssetId(chain), baseFee)

    suspend fun calculateToken(assetId: AssetId, destinationAddress: String, memo: String?): Fee = withContext(Dispatchers.IO) {
        val tokenId = assetId.tokenId!!
        val jetonAddress = jettonAddress(rpcClient, tokenId, destinationAddress)
            ?: throw Exception("can't get jetton address")
        val state = rpcClient.addressState(jetonAddress).getOrNull()?.result == "active"
        val tokenAccountFee = if (state) {
            if (memo.isNullOrEmpty()) {
                BigInteger.valueOf(100_000_000)
            } else {
                BigInteger.valueOf(60_000_000) // 0.06
            }
        } else {
            jettonTokenAccountCreation
        }
        Fee(
            TxSpeed.Normal,
            AssetId(assetId.chain),
            baseFee,
            options = mapOf(tokenAccountCreationKey to tokenAccountFee)
        ).withOptions(tokenAccountCreationKey)
    }

    fun calculateSwap(): Fee {
//        val fee = baseFee + jettonTokenAccountCreation
        return Fee(
            TxSpeed.Normal,
            AssetId(chain),
            baseFee,
            options = mapOf(tokenAccountCreationKey to jettonTokenAccountCreation)
        ).withOptions(tokenAccountCreationKey)
    }
}