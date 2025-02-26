package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetSubtype
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalancesRemoteSource @Inject constructor(
    private val balanceClients: List<BalanceClient>,
) {

    suspend fun getNativeBalances(account: Account): AssetBalance? {
        val client = balanceClients.getClient(account.chain) ?: return null
        return try {
            client.getNativeBalance(account.chain, account.address)
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getDelegationBalances(account: Account): AssetBalance? {
        val client = balanceClients.getClient(account.chain) ?: return null
        return try {
            client.getDelegationBalances(account.chain, account.address)
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getTokensBalances(account: Account, tokens: List<Asset>): List<AssetBalance> {
        val client = balanceClients.getClient(account.chain) ?: return emptyList()

        val tokens = tokens.filter { it.id.type() == AssetSubtype.TOKEN && account.chain == it.id.chain }
                .ifEmpty { return emptyList() }
        val tokensBalances = try {
            client.getTokenBalances(account.chain, account.address, tokens)
        } catch (_: Throwable) {
            emptyList()
        }
        return tokensBalances
    }
}