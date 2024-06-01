package com.gemwallet.android.data.chains

import com.wallet.core.primitives.Chain

class ChainInfoStaticSource : ChainInfoLocalSource {

    override suspend fun getAll(): List<Chain> = Chain.entries
        .filter { !ChainInfoLocalSource.exclude.contains(it) }
        .toList()
}