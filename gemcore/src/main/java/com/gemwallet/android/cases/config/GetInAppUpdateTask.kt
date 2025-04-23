package com.gemwallet.android.cases.config

import kotlinx.coroutines.flow.Flow

interface GetInAppUpdateTask {
    fun getInAppUpdateTask(): Flow<Long?>
}