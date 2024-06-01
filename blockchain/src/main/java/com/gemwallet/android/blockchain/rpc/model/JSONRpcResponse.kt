package com.gemwallet.android.blockchain.rpc.model

data class JSONRpcResponse<T>(
    val result: T,
    val error: JSONRpcError? = null,
)

data class JSONRpcError(
    val message: String,
)