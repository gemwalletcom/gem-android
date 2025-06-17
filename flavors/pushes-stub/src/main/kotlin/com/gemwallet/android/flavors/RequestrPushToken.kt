package com.gemwallet.android.flavors

import com.gemwallet.android.cases.device.RequestPushToken

class StoreRequestPushToken : RequestPushToken {
    override suspend fun invoke(callback: (String) -> Unit) {
        callback("")
    }
}

fun isNotificationsAvailable() = false