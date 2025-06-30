package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain

interface GetBlockExplorers {
    fun getBlockExplorers(chain: Chain): List<String>
}