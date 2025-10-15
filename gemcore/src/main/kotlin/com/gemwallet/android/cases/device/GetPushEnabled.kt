package com.gemwallet.android.cases.device

import kotlinx.coroutines.flow.Flow

interface GetPushEnabled {
    fun getPushEnabled(): Flow<Boolean>
}