package com.gemwallet.android.services

import com.gemwallet.android.cases.device.RequestPushToken
import com.google.firebase.messaging.FirebaseMessaging

class StoreRequestPushToken : RequestPushToken {
    override suspend fun invoke(onToken: (String) -> Unit) {
        onToken("")
    }
}

fun isNotificationsAvailable() = false