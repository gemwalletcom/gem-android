package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Chain

interface BlockchainClient {
    fun isMaintain(chain: Chain): Boolean = chain == maintainChain()

    fun maintainChain(): Chain
}