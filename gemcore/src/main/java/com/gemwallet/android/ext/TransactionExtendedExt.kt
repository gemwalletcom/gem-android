package com.gemwallet.android.ext

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import org.json.JSONObject

fun Transaction.getAssociatedAssetIds(): List<AssetId> {
    val swapAssets = getSwapMetadata()?.let { setOf(it.fromAsset, it.toAsset) } ?: emptySet()
    return (swapAssets + setOf(assetId, feeAssetId)).toList()
}

fun Transaction.getSwapMetadata(): TransactionSwapMetadata? {
    if (type != TransactionType.Swap ||  metadata.isNullOrEmpty()) {
        return null
    }
    val json = JSONObject(metadata)
    return try {
        TransactionSwapMetadata(
            fromAsset = with(json.getJSONObject("fromAsset")) {
                AssetId(
                    Chain.findByString(this.getString("chain")) ?: return null,
                    if (this.isNull("tokenId")) null else this.getString("tokenId"),
                )
            },
            toAsset = with(json.getJSONObject("toAsset")) {
                AssetId(
                    Chain.findByString(this.getString("chain")) ?: return null,
                    if (this.isNull("tokenId")) null else this.getString("tokenId"),
                )
            },
            fromValue = json.getString("fromValue"),
            toValue = json.getString("toValue"),
        )
    } catch (_: Throwable) {
        null
    }
}