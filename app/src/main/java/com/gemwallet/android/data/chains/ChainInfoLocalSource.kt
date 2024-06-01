package com.gemwallet.android.data.chains

import com.wallet.core.primitives.Chain

interface ChainInfoLocalSource {
    suspend fun getAll(): List<Chain>

    companion object {
        val exclude = listOf(Chain.Celo)
    }
}