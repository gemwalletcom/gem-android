package com.gemwallet.android.flavors

import com.gemwallet.android.cases.device.RequestPushToken

class StoreRequestPushToken : RequestPushToken {
    override suspend fun invoke(onToken: (String) -> Unit) {
        onToken("")
    }
}

fun isNotificationsAvailable() = false