package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset

interface BalanceClient : BlockchainClient {
    suspend fun getNativeBalance(address: String): AssetBalance?

    suspend fun getTokenBalances(address: String, tokens: List<Asset>): List<AssetBalance>
}