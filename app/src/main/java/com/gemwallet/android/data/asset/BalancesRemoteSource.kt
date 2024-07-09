package com.gemwallet.android.data.asset

import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId

interface BalancesRemoteSource {
    suspend fun getBalances(account: Account, tokens: List<AssetId>): List<Balances>
}