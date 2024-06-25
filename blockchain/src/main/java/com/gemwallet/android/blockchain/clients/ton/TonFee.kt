package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.ext.type
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

class TonFee {
    suspend operator fun invoke(
        rpcClient: TonRpcClient,
        assetId: AssetId,
        destinationAddress: String,
        memo: String?,
    ): Fee = withContext(Dispatchers.IO) {
        when (assetId.type()) {
            AssetSubtype.NATIVE -> Fee(AssetId(assetId.chain), BigInteger("10000000"))
            AssetSubtype.TOKEN -> {
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
                    BigInteger.valueOf(300_000_000)
                }
                Fee(AssetId(assetId.chain), BigInteger("10000000"), options = mapOf(
                    tokenAccountCreationKey to tokenAccountFee
                )).withOptions(tokenAccountCreationKey)
            }
        }
    }
}