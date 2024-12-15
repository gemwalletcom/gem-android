package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.xrp.services.XrpAccountsService
import com.gemwallet.android.blockchain.clients.xrp.services.account
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getReserveBalance
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain
import uniffi.gemstone.Config
import java.math.BigInteger

class XrpBalanceClient(
    private val chain: Chain,
    private val accountsService: XrpAccountsService,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance {
        val reserved = chain.getReserveBalance()
        val amount = accountsService.account(address).mapCatching {
            it.result.account_data.Balance.toBigInteger() - reserved
        }.getOrNull() ?: BigInteger.ZERO
        return AssetBalance.create(
            chain.asset(),
            available = amount.toString(),
            reserved = reserved.toString()
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

}