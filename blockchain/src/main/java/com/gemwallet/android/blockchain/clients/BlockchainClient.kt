package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Chain

interface BlockchainClient {
    fun isMaintain(chain: Chain): Boolean
}

fun <T: BlockchainClient> List<T>.getClient(chain: Chain) = firstOrNull { it.isMaintain(chain) }