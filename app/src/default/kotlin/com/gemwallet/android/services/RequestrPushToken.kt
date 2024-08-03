package com.gemwallet.android.services

fun requestPushToken(onToken: (String) -> Unit) {
    onToken("")
}

fun isNotificationsAvailable() = true