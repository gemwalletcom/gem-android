package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetSubtype
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalancesRemoteSource @Inject constructor(
    private val balanceClients: List<BalanceClient>,
) {

    suspend fun getBalances(account: Account, tokens: List<Asset>): List<AssetBalance> = withContext(Dispatchers.IO) {
        val client = balanceClients.getClient(account.chain) ?: return@withContext emptyList()

        val nativeBalances = async {
            try {
                client.getNativeBalance(account.chain, account.address)
            } catch (_: Throwable) {
                null
            }
        }

        val tokensBalances = async {
            val tokens = tokens.filter { it.id.type() == AssetSubtype.TOKEN && account.chain == it.id.chain }
                .ifEmpty { return@async emptyList() }
            try {
                client.getTokenBalances(account.chain, account.address, tokens)
            } catch (_: Throwable) {
                emptyList()
            }
        }
        (tokensBalances.await() + nativeBalances.await()).filterNotNull()
    }
}