package com.gemwallet.android.ext

import com.gemwallet.android.model.Transaction
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType

fun Transaction.getAssociatedAssetIds(): List<AssetId> {
    val swapAssets = getSwapMetadata()?.let { setOf(it.fromAsset, it.toAsset) } ?: emptySet()
    return (swapAssets + setOf(assetId, feeAssetId)).toList()
}

val Transaction.hash: String
    get() = id.removePrefix("${assetId.chain.string}_")

fun Transaction.getSwapMetadata(): TransactionSwapMetadata? {
    if (type != TransactionType.Swap ||  metadata.isNullOrEmpty()) {
        return null
    }
    return try {
        jsonEncoder.decodeFromString(metadata)
    } catch (_: Throwable) {
        null
    }
}

fun Transaction.getNftMetadata(): NFTAsset? {
    if (type != TransactionType.TransferNFT ||  metadata.isNullOrEmpty()) {
        return null
    }
    return try {
        jsonEncoder.decodeFromString(metadata)
    } catch (_: Throwable) {
        null
    }
}