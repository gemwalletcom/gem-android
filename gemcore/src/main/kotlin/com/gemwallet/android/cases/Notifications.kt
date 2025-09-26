package com.gemwallet.android.cases

import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.PushNotificationAsset
import com.wallet.core.primitives.PushNotificationTransaction
import com.wallet.core.primitives.PushNotificationTypes

fun parseNotificatrionData(rawType: String?, rawData: String?): Any? {
    if (rawType.isNullOrEmpty() || rawData.isNullOrEmpty()) {
        return null
    }
    val type = PushNotificationTypes.entries.firstOrNull { it.string == rawType } ?: return null
    return try {
        when (type) {
            PushNotificationTypes.Transaction -> jsonEncoder.decodeFromString<PushNotificationTransaction>(rawData)
            PushNotificationTypes.PriceAlert,
            PushNotificationTypes.BuyAsset,
            PushNotificationTypes.Asset -> jsonEncoder.decodeFromString<PushNotificationAsset>(rawData).assetId
            PushNotificationTypes.Test,
            PushNotificationTypes.SwapAsset -> null
        }
    } catch (_: Throwable) {
        null
    }
}