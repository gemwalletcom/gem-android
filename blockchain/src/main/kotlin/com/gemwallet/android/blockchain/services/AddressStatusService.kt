package com.gemwallet.android.blockchain.services

import com.wallet.core.primitives.AddressStatus
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemGateway

class AddressStatusService(
    private val gateway: GemGateway,
) {

    suspend fun getAddressStatus(chain: Chain, address: String): List<AddressStatus> {
        return gateway.getAddressStatus(chain.string, address)
            .map { item ->
                when (item) {
                    uniffi.gemstone.AddressStatus.MULTI_SIGNATURE -> AddressStatus.MultiSignature
                }
            }
    }
}