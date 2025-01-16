package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarAccountService
import com.gemwallet.android.blockchain.clients.stellar.services.accounts
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getReserveBalance
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.Crypto
import com.wallet.core.primitives.Chain

class StellarBalanceClient(
    private val chain: Chain,
    private val accountService: StellarAccountService,
) : BalanceClient {

    override suspend fun getNativeBalance(
        chain: Chain,
        address: String
    ): AssetBalance? {
        val reserved = chain.getReserveBalance()
        val balance = try {
            val result = accountService.accounts(address)?.balances
                ?.firstOrNull { it.asset_type == "native" }?.balance ?: return null
            Crypto(result, chain.asset().decimals).atomicValue.max(reserved)
        } catch (_: Throwable) {
            return null
        }
        return AssetBalance.create(chain.asset(), available = balance.toString(), reserved = reserved.toString())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}