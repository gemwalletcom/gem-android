package com.gemwallet.android.cases.device

interface GetPushToken {
    suspend fun getPushToken(): String
}