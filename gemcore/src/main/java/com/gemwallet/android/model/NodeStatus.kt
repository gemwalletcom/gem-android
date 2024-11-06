package com.gemwallet.android.model

data class NodeStatus(
    val url: String,
    val chainId: String,
    val blockNumber: String,
    val inSync: Boolean,
    val latency: Long,
    val loading: Boolean = false,
)