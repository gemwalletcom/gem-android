package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.aptos.services.AptosBalancesService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

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
        return withContext(Dispatchers.IO) {
            tokens.map { token ->
                async {
                    val tokenId = token.id.tokenId ?: return@async null
                    try {
                        val result = balanceService.balance(address,  tokenId).string()
                        AssetBalance.create(token, available = result)
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}