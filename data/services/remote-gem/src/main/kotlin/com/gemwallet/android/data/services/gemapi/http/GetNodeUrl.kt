package com.gemwallet.android.data.services.gemapi.http

import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

fun Chain.getNodeUrl(
    getNodesCase: GetNodesCase,
    getCurrentNodeCase: GetCurrentNodeCase,
    setCurrentNodeCase: SetCurrentNodeCase
): String? {
    val currentNode = getCurrentNodeCase.getCurrentNode(this)
    val url = if (currentNode == null) {
        val node = runBlocking { getNodesCase.getNodes(this@getNodeUrl).firstOrNull()?.firstOrNull() }
        if (node != null) {
            setCurrentNodeCase.setCurrentNode(this, node)
        }
        node
    } else {
        currentNode
    }?.url
    return url
}