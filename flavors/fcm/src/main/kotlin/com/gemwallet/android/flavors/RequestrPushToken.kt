package com.gemwallet.android.flavors

import android.content.Context
import android.util.Log
import com.gemwallet.android.cases.device.RequestPushToken
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class StoreRequestPushToken : RequestPushToken {

    override fun init(context: Context) {
        FirebaseApp.initializeApp(context)
    }

    override suspend fun requestToken(callback: (String) -> Unit) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                val token = if (task.isSuccessful) { task.result } else { "" }
                callback(token)
            }
        } catch (_: Throwable) {
            callback("")
        }
    }

}


fun isNotificationsAvailable() = true