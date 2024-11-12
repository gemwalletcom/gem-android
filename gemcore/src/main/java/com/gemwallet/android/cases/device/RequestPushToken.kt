package com.gemwallet.android.cases.device

fun interface RequestPushToken {
    suspend fun invoke(callback: (String) -> Unit)
}