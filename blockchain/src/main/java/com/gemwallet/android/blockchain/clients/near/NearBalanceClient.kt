package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class NearBalanceClient(
    private val chain: Chain,
    private val rpcClient: NearRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        return rpcClient.account(
            JSONRpcRequest(
                method = NearMethod.Query.value,
                params = mapOf(
                    "request_type" to "view_account",
                    "finality" to "final",
                    "account_id" to address,
                )
            )
        ).fold(
            {
                if (it.error == null) {
                    AssetBalance.create(chain.asset(), it.result.amount)
                } else {
                    null
                }
            }
        ) {
            null
        }
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}