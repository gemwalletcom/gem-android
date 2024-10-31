package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node

interface GetCurrentNodeCase {
    fun getCurrentNode(chain: Chain): Node?
}