package com.gemwallet.android.data.chains

import javax.inject.Inject

class ChainInfoRepository @Inject constructor(
    private val localSource: ChainInfoLocalSource,
) {
    suspend fun getAll() = localSource.getAll()
}