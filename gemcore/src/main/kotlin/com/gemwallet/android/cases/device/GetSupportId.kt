package com.gemwallet.android.cases.device

import kotlinx.coroutines.flow.Flow

interface GetSupportId {
    fun getSupportId(): Flow<String?>
}