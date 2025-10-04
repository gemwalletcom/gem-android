package com.gemwallet.android.cases.pushes

import com.gemwallet.android.model.PushNotificationData

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
        data: PushNotificationData.Transaction
    )

    fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        data: PushNotificationData.Asset
    )
}