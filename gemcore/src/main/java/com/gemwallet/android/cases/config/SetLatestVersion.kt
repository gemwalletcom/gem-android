package com.gemwallet.android.cases.config

import kotlinx.coroutines.flow.Flow

interface SetLatestVersion {
    suspend fun setLatestVersion(version: String)
}