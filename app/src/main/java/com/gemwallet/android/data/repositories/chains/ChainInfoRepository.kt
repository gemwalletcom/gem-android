package com.gemwallet.android.data.repositories.chains

import com.wallet.core.primitives.Chain
import javax.inject.Inject

class ChainInfoRepository @Inject constructor() {
    fun getAll() = Chain.entries
        .filter { !exclude.contains(it) }
        .toList()

    companion object {
        val exclude = listOf(Chain.Celo)
    }
}