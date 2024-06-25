package com.gemwallet.android.model

data class NodeStatus(
    val chainId: String,
    val blockNumber: String,
    val inSync: Boolean
)
