package com.gemwallet.android.model

data class NodeStatus(
    val url: String,
    val chainId: String,
    val blockNumber: ULong,
    val inSync: Boolean,
    val latency: ULong,
    val loading: Boolean = false,
)