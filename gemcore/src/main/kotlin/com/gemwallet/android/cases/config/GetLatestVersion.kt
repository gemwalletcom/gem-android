package com.gemwallet.android.cases.config

import kotlinx.coroutines.flow.Flow

interface GetLatestVersion {
    fun getLatestVersion(): Flow<String>
}