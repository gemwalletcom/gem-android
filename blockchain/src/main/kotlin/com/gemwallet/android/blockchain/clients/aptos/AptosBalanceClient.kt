package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.aptos.services.AptosBalancesService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class AptosBalanceClient(
    private val chain: Chain,
    private val balanceService: AptosBalancesService,
) : BalanceClient {
    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        val result = balanceService.balance(address).getOrNull()?.data?.coin?.value ?: return null
        return try { // String to number
            AssetBalance.create(chain.asset(), available = result)
        } catch (err: Throwable) {
            print(err)
            null
        }
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        if (tokens.isEmpty()) {
            return emptyList()
        }
        val resources = balanceService.resources(address).getOrNull() ?: return emptyList()
        val result = mutableListOf<AssetBalance>()

        tokens.mapNotNull { token ->
            val resource = resources.firstOrNull { it.type == "0x1::coin::CoinStore<${token.id.tokenId}>" } ?: return@mapNotNull null
            val balance = resource.data.coin?.value ?: return@mapNotNull null
            result.add(AssetBalance.create(token, available = balance.toString()))
        }
        return result
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}