package com.gemwallet.android.blockchain.rpc.model

data class JSONRpcRequest<T>(
    val method: String,
    val params: T,
    val id: Int = 1,
    val jsonrpc: String = "2.0",
) {
    companion object {
        fun <T>create(method: JSONRpcMethod, params: T): JSONRpcRequest<T> = JSONRpcRequest(
            method = method.value(),
            params = params
        )
    }
}