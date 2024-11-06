package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId

interface BalanceClient : BlockchainClient {
    suspend fun getNativeBalance(address: String): AssetBalance?

    suspend fun getTokenBalances(address: String, tokens: List<Asset>): List<AssetBalance>
}