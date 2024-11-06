package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import java.math.BigInteger

class TonBalanceClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(address: String): AssetBalance? {
        return rpcClient.balance(address)
            .fold( { AssetBalance.create(chain.asset(), it.result) } ) { null }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<Asset>): List<AssetBalance> {
        return tokens.asFlow()
            .mapNotNull {
                val tokenId = it.id.tokenId ?: return@mapNotNull null
                val jettonAddress = jettonAddress(rpcClient, tokenId, address) ?: return@mapNotNull null
                val isActive = rpcClient.addressState(jettonAddress).getOrNull()?.result == "active"

                if (isActive) {
                    AssetBalance.create(it, available = tokenBalance(jettonAddress).toString())
                } else {
                    AssetBalance.create(it)
                }
            }
            .flowOn(Dispatchers.IO)
            .toList()
    }

    override fun maintainChain(): Chain = Chain.Ton

    private suspend fun tokenBalance(jettonAddress: String): BigInteger {
        return BigInteger.valueOf(
            rpcClient.tokenBalance(jettonAddress).getOrNull()?.result?.balance ?: 0L
        )
    }
}