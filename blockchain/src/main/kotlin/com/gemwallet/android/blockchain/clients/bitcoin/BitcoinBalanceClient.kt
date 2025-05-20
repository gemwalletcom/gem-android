package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinBalancesService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.fullAddress
import com.gemwallet.android.ext.toBitcoinChain
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain

class BitcoinBalanceClient(
    private val chain: Chain,
    private val balanceService: BitcoinBalancesService,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        val address = chain.toBitcoinChain().fullAddress(address)
        val result = balanceService.balance(address).getOrNull()
        return try {
            AssetBalance.create(chain.asset(), available = result?.balance ?: return null)
        } catch (_: Throwable) {
            null
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}