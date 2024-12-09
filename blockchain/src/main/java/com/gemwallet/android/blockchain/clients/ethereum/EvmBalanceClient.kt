package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmBalancesService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.getBalance
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class EvmBalanceClient(
    private val chain: Chain,
    private val callService: EvmCallService,
    private val balancesService: EvmBalancesService,
    private val smartChainStakeClient: SmartchainStakeClient,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        val availableValue = balancesService.getBalance(address)
            .getOrNull()?.result?.value?.toString()

        return when (chain) {
            Chain.SmartChain -> smartChainStakeClient.getBalance(address, availableValue)
            else -> AssetBalance.create(chain.asset(), available = availableValue ?: return null)
        }
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        if (tokens.isEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<AssetBalance>()
        for (token in tokens) {
            val data = "0x70a08231000000000000000000000000${address.removePrefix("0x")}"
            val contract = token.id.tokenId ?: continue
            val params = mapOf(
                "to" to contract,
                "data" to data,
            )
            val balance = callService.callNumber(JSONRpcRequest.create(EvmMethod.Call, listOf(params, "latest")))
                .getOrNull()?.result?.value ?: continue
            result.add(AssetBalance.create(token, available = balance.toString()))
        }
        return result
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}