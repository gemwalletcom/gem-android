package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain

interface GetCurrentBlockExplorerCase {
    fun getCurrentBlockExplorer(chain: Chain): String
}