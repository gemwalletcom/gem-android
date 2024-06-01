package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId

interface BalanceClient : BlockchainClient {
    suspend fun getNativeBalance(address: String): Balances?

    suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances>
}