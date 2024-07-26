package com.gemwallet.android.services

import com.google.firebase.messaging.FirebaseMessaging

fun requestPushToken(onToken: (String) -> Unit) {
    onToken("")
}