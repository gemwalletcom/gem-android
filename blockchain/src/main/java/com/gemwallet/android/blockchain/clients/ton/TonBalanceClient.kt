package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
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

    override suspend fun getNativeBalance(address: String): Balances? {
        return rpcClient.balance(address)
            .fold( { Balances.create(AssetId(chain), it.result.toBigInteger()) } ) { null }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> {
        return tokens.asFlow()
            .mapNotNull {
                val tokenId = it.tokenId ?: return@mapNotNull null
                val jettonAddress = jettonAddress(rpcClient, tokenId, address) ?: return@mapNotNull null
                val isActive = rpcClient.addressState(jettonAddress).getOrNull()?.result == "active"

                if (isActive) {
                    Balances.create(it, tokenBalance(jettonAddress))
                } else {
                    Balances.create(it, BigInteger.ZERO)
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