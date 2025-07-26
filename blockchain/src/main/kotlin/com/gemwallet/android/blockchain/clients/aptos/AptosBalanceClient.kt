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
    private val aptosCoin = "0x1::aptos_coin::AptosCoin"

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        return try {
            val result = balanceService.balance(address, aptosCoin).string()
            AssetBalance.create(chain.asset(), available = result)
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        if (tokens.isEmpty()) {
            return emptyList()
        }
        val resources = try {
            balanceService.resources(address)
        } catch (_: Throwable) {
            return emptyList()
        }
        val result = mutableListOf<AssetBalance>()

        tokens.mapNotNull { token ->
            val resource = resources.firstOrNull { it.type == "0x1::coin::CoinStore<${token.id.tokenId}>" } ?: return@mapNotNull null
            val balance = resource.data.coin?.value ?: return@mapNotNull null
            result.add(AssetBalance.create(token, available = balance))
        }
        return result
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}