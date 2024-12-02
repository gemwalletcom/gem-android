package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.AddressStatus
import com.wallet.core.primitives.Chain

class AddressStatusClientProxy(
    private val clients: List<AddressStatusClient>,
) : AddressStatusClient {

    override suspend fun getAddressStatus(chain: Chain, address: String): List<AddressStatus> {
        return clients.getClient(chain)?.getAddressStatus(chain, address) ?: emptyList()
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}