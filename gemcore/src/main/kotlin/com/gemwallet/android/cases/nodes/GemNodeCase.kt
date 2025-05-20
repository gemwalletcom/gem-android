package com.gemwallet.android.cases.nodes

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState

fun getGemNodeUrl(chain: Chain) = "https://${chain.string}.gemnodes.com"

fun getGemNode(chain: Chain) = Node(
    url = getGemNodeUrl(chain),
    NodeState.Active,
    priority = 10
)