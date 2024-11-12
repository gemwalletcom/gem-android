package com.gemwallet.android.services

class StoreRequestPushToken : RequestPushToken {
    override suspend fun invoke(onToken: (String) -> Unit) {
        onToken("")
    }
}

fun isNotificationsAvailable() = false