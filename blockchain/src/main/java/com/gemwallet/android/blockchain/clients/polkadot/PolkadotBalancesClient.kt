package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotBalancesService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain
import java.math.BigInteger

class PolkadotBalancesClient(
    private val chain: Chain,
    private val balancesService: PolkadotBalancesService
) : BalanceClient {

    override suspend fun getNativeBalance(
        chain: Chain,
        address: String
    ): AssetBalance? {
        try {
            val resp = balancesService.balance(address).getOrNull() ?: return null
            val free = resp.free.toBigInteger()
            val reserved = resp.reserved.toBigInteger()
            val available = (free - reserved).max(BigInteger.ZERO)
            return AssetBalance.create(
                chain.asset(),
                available = available.toString(),
                reserved = reserved.toString(),
            )
        } catch (_: Throwable) {
            return null
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}