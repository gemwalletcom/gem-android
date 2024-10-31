package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain

interface SetBlockExplorerCase {
    fun setCurrentBlockExplorer(chain: Chain, name: String)
}