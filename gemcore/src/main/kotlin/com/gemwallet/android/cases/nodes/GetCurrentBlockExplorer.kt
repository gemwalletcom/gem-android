package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain

interface GetCurrentBlockExplorer {
    fun getCurrentBlockExplorer(chain: Chain): String
}