package com.gemwallet.android.services

import com.gemwallet.android.cases.device.RequestPushToken
import com.google.firebase.messaging.FirebaseMessaging

class StoreRequestPushToken : RequestPushToken {
    override suspend fun invoke(onToken: (String) -> Unit) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                val token = if (task.isSuccessful) { task.result } else { "" }
                onToken(token)
            }
        } catch (_: Throwable) {
            onToken("")
        }
    }
}


fun isNotificationsAvailable() = true