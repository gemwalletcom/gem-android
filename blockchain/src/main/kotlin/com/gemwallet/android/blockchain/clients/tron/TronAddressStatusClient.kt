package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.AddressStatusClient
import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.gemwallet.android.blockchain.clients.tron.services.getAccount
import com.gemwallet.android.model.AddressStatus
import com.wallet.core.primitives.Chain

class TronAddressStatusClient(
    private val chain: Chain,
    private val tronAccountsService: TronAccountsService,
) : AddressStatusClient {

    override suspend fun getAddressStatus(chain: Chain, address: String): List<AddressStatus> {
        val activePermission = tronAccountsService.getAccount(address, true)
            ?.active_permission ?: emptyList()
        return if (activePermission.filter { it.threshold > 1 }.isNotEmpty()) {
            listOf(AddressStatus.MultiSignature)
        } else emptyList()
    }

    override fun supported(chain: Chain): Boolean = chain == this.chain
}