package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import java.math.BigInteger

class NearBalanceClient(
    private val chain: Chain,
    private val rpcClient: NearRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(address: String): Balances? {
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
                    Balances.create(AssetId(chain), BigInteger(it.result.amount))
                } else {
                    null
                }
            }
        ) {
            null
        }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> {
        return emptyList()
    }

    override fun maintainChain(): Chain = chain
}