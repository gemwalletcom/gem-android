package com.gemwallet.android.data.asset

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalancesRetrofitRemoteSource @Inject constructor(
    private val balanceClients: List<BalanceClient>,
) : BalancesRemoteSource {

    override suspend fun getBalances(account: Account, tokens: List<AssetId>): Result<List<Balances>> {
        val client = balanceClients.firstOrNull { it.isMaintain(account.chain) }
            ?: return Result.failure(Exception("Chain doesn't support"))
        val nativeBalances = try {
            client.getNativeBalance(account.address)
        } catch (err: Throwable) {
            null
        }

        val tokensBalances = if (tokens.isEmpty()) {
            emptyList()
        } else {
            try {
                client.getTokenBalances(
                    account.address,
                    tokens.filter { it.type() == AssetSubtype.TOKEN && account.chain == it.chain },
                )
            } catch (err: Throwable) {
                emptyList()
            }
        }
        val result = if (nativeBalances == null) {
            tokensBalances
        } else {
            tokensBalances + nativeBalances
        }

        return Result.success(result)
    }
}