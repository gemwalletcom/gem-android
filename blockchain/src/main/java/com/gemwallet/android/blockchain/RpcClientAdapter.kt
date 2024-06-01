package com.gemwallet.android.blockchain

import com.wallet.core.primitives.Chain

@Suppress("UNCHECKED_CAST")
class RpcClientAdapter {
    private val clients = mutableMapOf<Chain, Any>()

    fun add(chain: Chain, client: Any): RpcClientAdapter {
        clients[chain] = client
        return this
    }

    fun <T>getClient(chain: Chain): T {
        return clients[chain] as T
    }
}