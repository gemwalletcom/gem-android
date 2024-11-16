package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

interface BalanceClient : BlockchainClient {
    suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance?

    suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> = emptyList()
}