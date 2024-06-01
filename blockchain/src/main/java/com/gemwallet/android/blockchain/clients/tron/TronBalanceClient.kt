package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.Balances
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import wallet.core.jni.Base58
import java.math.BigInteger

class TronBalanceClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(address: String): Balances? {
        return rpcClient.getAccount(TronAccountRequest(address, visible = true))
            .fold(
                {
                    Balances.create(AssetId(chain), BigInteger.valueOf(it.balance?.toLong() ?: 0L))
                }
            ) {
                null
            }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> {
        return tokens.mapNotNull { assetId ->
            val tokenId = assetId.tokenId ?: return@mapNotNull null
            val owner = Base58.decode(address).toHexString("")
            rpcClient.triggerSmartContract(
                contractAddress = Base58.decode(tokenId).toHexString(""),
                functionSelector = "balanceOf(address)",
                parameter = owner.padStart(64, '0'),
                feeLimit = 1_000_000L,
                callValue = 0L,
                ownerAddress = owner,
                visible = false,
            ).fold(
                {
                    val amount = BigInteger(it.constant_result?.firstOrNull() ?: "0", 16)
                    Balances.create(assetId, amount)
                }
            ) {
                null
            }
        }
    }

    override fun maintainChain(): Chain = Chain.Tron
}