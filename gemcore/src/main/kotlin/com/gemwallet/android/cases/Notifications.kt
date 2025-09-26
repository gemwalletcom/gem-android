package com.gemwallet.android.cases

import com.gemwallet.android.model.PushNotificationData
import com.gemwallet.android.model.PushNotificationData.*
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.PushNotificationAsset
import com.wallet.core.primitives.PushNotificationSwapAsset
import com.wallet.core.primitives.PushNotificationTransaction
import com.wallet.core.primitives.PushNotificationTypes

fun parseNotificationData(rawType: String?, rawData: String?): PushNotificationData? {
    if (rawType.isNullOrEmpty() || rawData.isNullOrEmpty()) {
        return null
    }
    val type = PushNotificationTypes.entries.firstOrNull { it.string == rawType } ?: return null
    return try {
        when (type) {
            PushNotificationTypes.Transaction -> jsonEncoder.decodeFromString<PushNotificationTransaction>(rawData).let {
                Transaction(
                    transactionId = it.transactionId,
                    assetId = it.assetId,
                    walletIndex = it.walletIndex
                )
            }
            PushNotificationTypes.PriceAlert,
            PushNotificationTypes.BuyAsset,
            PushNotificationTypes.Asset -> jsonEncoder.decodeFromString<PushNotificationAsset>(rawData).let {
                Asset(
                    assetId = it.assetId
                )
            }
            PushNotificationTypes.SwapAsset -> jsonEncoder.decodeFromString<PushNotificationSwapAsset>(rawData).let {
                Swap(
                    fromAssetId = it.fromAssetId,
                    toAssetId = it.toAssetId
                )
            }
            PushNotificationTypes.Support,
            PushNotificationTypes.Test -> null
        }
    } catch (_: Throwable) {
        null
    }
}