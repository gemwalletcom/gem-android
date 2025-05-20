package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node

interface SetCurrentNodeCase {
    fun setCurrentNode(chain: Chain, node: Node)
}