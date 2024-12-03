package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId

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
        return Fee(feeAssetId = AssetId(chain), speed = TxSpeed.Normal, amount = fee.abs())
    }
}