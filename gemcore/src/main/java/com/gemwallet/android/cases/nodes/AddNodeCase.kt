package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain

interface AddNodeCase {
    suspend fun addNode(chain: Chain, url: String)
}