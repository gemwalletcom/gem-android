package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.ext.getReserveBalance
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import java.math.BigInteger

class XrpBalanceClient(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : BalanceClient {
    private val reservedBalance = BigInteger.valueOf(10_000_000)
    override fun maintainChain(): Chain = chain

    override suspend fun getNativeBalance(address: String): Balances {
        val amount = rpcClient.account(address).mapCatching {
            it.result.account_data.Balance.toBigInteger() - chain.getReserveBalance()
        }.getOrNull() ?: BigInteger.ZERO
        return Balances.create(AssetId(chain), amount, reserved = reservedBalance)
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> = emptyList()

}