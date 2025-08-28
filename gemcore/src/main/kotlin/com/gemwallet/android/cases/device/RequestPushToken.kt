package com.gemwallet.android.cases.device

import android.content.Context

interface RequestPushToken {

    fun initRequester(context: Context)

    suspend fun requestToken(callback: (String) -> Unit)
}