package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.AddressStatusClient
import com.gemwallet.android.model.AddressStatus
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import com.wallet.core.primitives.Chain

class TronAddressStatusClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : AddressStatusClient {

    override suspend fun getAddressStatus(chain: Chain, address: String): List<AddressStatus> {
        val activePermission = rpcClient.getAccount(TronAccountRequest(address, visible = true))
            .getOrNull()
            ?.active_permission ?: emptyList()
        return if (activePermission.filter { it.threshold > 1 }.isNotEmpty()) {
            listOf(AddressStatus.MultiSignature)
        } else emptyList()
    }

    override fun isMaintain(chain: Chain): Boolean = chain == this.chain
}