package com.gemwallet.android.model

import com.wallet.core.primitives.PushNotificationTypes
import kotlinx.serialization.Serializable

sealed interface PushNotificationData {

    data class Asset (val assetId: String): PushNotificationData

    data class PushNotificationPayloadType (
        val type: PushNotificationTypes,
    ) : PushNotificationData

    @Serializable
    data class Swap (
        val fromAssetId: String,
        val toAssetId: String,
    ): PushNotificationData

    @Serializable
    data class Transaction (
        val walletIndex: Int,
        val assetId: String,
        val transactionId: String,
    ): PushNotificationData
}