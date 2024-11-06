package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.AddressStatus
import com.wallet.core.primitives.Chain

interface AddressStatusClient : BlockchainClient {
    suspend fun getAddressStatus(address: String): List<AddressStatus>
}

fun List<AddressStatusClient>.getClient(chain: Chain) = firstOrNull { it.maintainChain() == chain }