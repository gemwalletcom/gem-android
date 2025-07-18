package com.gemwallet.android.cases.pushes

interface ShowSystemNotification {
    fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        walletIndex: Int?,
        assetId: String?,
    )
}