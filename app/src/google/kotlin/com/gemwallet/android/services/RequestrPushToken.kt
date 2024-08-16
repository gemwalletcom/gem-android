package com.gemwallet.android.services

import com.google.firebase.messaging.FirebaseMessaging

fun requestPushToken(onToken: (String) -> Unit) {
    try {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) { task.result } else { "" }
            onToken(token)
        }
    } catch (err: Throwable) {
        onToken("")
    }
}

fun isNotificationsAvailable() = true