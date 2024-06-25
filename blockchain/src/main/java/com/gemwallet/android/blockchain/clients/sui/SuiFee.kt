package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId

class SuiFee {
    suspend operator fun invoke(
        rpcClient: SuiRpcClient,
        account: Account,
        data: String,
    ): Fee {
        val chain = account.chain
        val gasUsed = rpcClient.dryRun(JSONRpcRequest.create(SuiMethod.DryRun, listOf(data)))
            .getOrThrow().result.effects.gasUsed
        val computationCost = gasUsed.computationCost.toBigInteger()
        val storageCost = gasUsed.storageCost.toBigInteger()
        val storageRebate = gasUsed.storageRebate.toBigInteger()
        val fee = computationCost + storageCost - storageRebate
        return Fee(feeAssetId = AssetId(chain), amount = fee.abs())
    }
}