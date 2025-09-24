package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

class TonFeeCalculator(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) {
    val baseFee = BigInteger.valueOf(10_000_000L)
    val jettonTokenAccountCreation = BigInteger.valueOf(300_000_000L)

    fun calculateNative() = Fee(FeePriority.Normal, AssetId(chain), baseFee)

    suspend fun calculateToken(assetId: AssetId, destinationAddress: String, memo: String?): Fee = withContext(Dispatchers.IO) {
        val tokenId = assetId.tokenId!!
        val hexTokenId = uniffi.gemstone.tonBase64ToHexAddress(tokenId)
        val jetonAddress = rpcClient.getJettonWallets(destinationAddress)
            .jetton_wallets.firstOrNull { it.jetton == hexTokenId }?.address

        val state = jetonAddress?.let { true } ?: false
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
            FeePriority.Normal,
            AssetId(assetId.chain),
            baseFee,
            options = mapOf(tokenAccountCreationKey to tokenAccountFee)
        ).withOptions(tokenAccountCreationKey)
    }

    fun calculateSwap(): Fee {
//        val fee = baseFee + jettonTokenAccountCreation
        return Fee(
            FeePriority.Normal,
            AssetId(chain),
            baseFee,
            options = mapOf(tokenAccountCreationKey to jettonTokenAccountCreation)
        ).withOptions(tokenAccountCreationKey)
    }
}