package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.FeePriority

class SuiFeeCalculator(
    private val rpcClient: SuiRpcClient,
) {
    suspend fun calculate(account: Account, data: String): Fee {
        val chain = account.chain
        val gasUsed = rpcClient.dryRun(data)
        val computationCost = gasUsed.computationCost.toBigInteger()
        val storageCost = gasUsed.storageCost.toBigInteger()
        val storageRebate = gasUsed.storageRebate.toBigInteger()
        val fee = computationCost + storageCost - storageRebate
        return Fee(feeAssetId = AssetId(chain), priority = FeePriority.Normal, amount = fee.abs())
    }
}