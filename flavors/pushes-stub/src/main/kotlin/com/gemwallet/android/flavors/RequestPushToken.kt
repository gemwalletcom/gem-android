package com.gemwallet.android.flavors

import android.content.Context
import com.gemwallet.android.cases.device.RequestPushToken

class StoreRequestPushToken : RequestPushToken {

    override fun initRequester(context: Context) {}

    override suspend fun requestToken(callback: (String) -> Unit) {
        callback("")
    }
}

fun isNotificationsAvailable() = false