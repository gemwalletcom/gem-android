package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.AddressStatus
import com.wallet.core.primitives.Chain

interface AddressStatusClient : BlockchainClient {
    suspend fun getAddressStatus(chain: Chain, address: String): List<AddressStatus>
}