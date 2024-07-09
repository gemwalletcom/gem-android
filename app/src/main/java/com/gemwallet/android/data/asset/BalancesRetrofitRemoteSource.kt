package com.gemwallet.android.data.asset

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalancesRetrofitRemoteSource @Inject constructor(
    private val balanceClients: List<BalanceClient>,
) : BalancesRemoteSource {

    override suspend fun getBalances(account: Account, tokens: List<AssetId>): List<Balances> = withContext(Dispatchers.IO) {
        val client = balanceClients.getClient(account.chain) ?: return@withContext emptyList()

        val nativeBalances = async {
            try {
                client.getNativeBalance(account.address)
            } catch (err: Throwable) {
                null
            }
        }

        val tokensBalances = async {
            val ids = tokens.filter { it.type() == AssetSubtype.TOKEN && account.chain == it.chain }
                .ifEmpty { return@async emptyList() }
            try {
                client.getTokenBalances(account.address, ids)
            } catch (err: Throwable) {
                emptyList()
            }
        }
        (tokensBalances.await() + nativeBalances.await()).filterNotNull()
    }
}