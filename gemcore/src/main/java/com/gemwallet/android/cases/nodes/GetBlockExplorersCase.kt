package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain

interface GetBlockExplorersCase {
    fun getBlockExplorers(chain: Chain): List<String>
}