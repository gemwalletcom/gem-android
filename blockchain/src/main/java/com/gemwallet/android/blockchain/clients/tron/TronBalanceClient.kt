package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import wallet.core.jni.Base58
import java.math.BigInteger

class TronBalanceClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        return rpcClient.getAccount(TronAccountRequest(address, visible = true))
            .fold(
                {
                    AssetBalance.create(chain.asset(), it.balance?.toLong()?.toString() ?: return null)
                }
            ) {
                null
            }
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        return tokens.mapNotNull { token ->
            val tokenId = token.id.tokenId ?: return@mapNotNull null
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
                    AssetBalance.create(token, amount.toString())
                }
            ) {
                null
            }
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}