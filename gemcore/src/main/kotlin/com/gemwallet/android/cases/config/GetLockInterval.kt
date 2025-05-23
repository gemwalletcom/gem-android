package com.gemwallet.android.cases.config

import kotlinx.coroutines.flow.Flow

interface GetLockInterval {
    fun getLockInterval(): Flow<Int>
}