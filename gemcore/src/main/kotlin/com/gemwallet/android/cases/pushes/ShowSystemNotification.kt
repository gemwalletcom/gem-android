package com.gemwallet.android.cases.pushes

import com.wallet.core.primitives.PushNotificationAsset
import com.wallet.core.primitives.PushNotificationTransaction

interface ShowSystemNotification {
    fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
    )

    fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        data: PushNotificationTransaction
    )

    fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        data: PushNotificationAsset
    )
}