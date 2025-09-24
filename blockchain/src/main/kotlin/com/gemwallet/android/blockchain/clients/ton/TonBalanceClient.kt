package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import java.math.BigInteger

class TonBalanceClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        return rpcClient.balance(address)
            .fold( { AssetBalance.create(chain.asset(), it.result) } ) { null }
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        val jettonWallets = rpcClient.getJettonWallets(address)
        return jettonWallets.jetton_wallets.mapNotNull { wallet ->
            val jettonTokenId = uniffi.gemstone.tonHexToBase64Address(wallet.jetton)
            val tokenAsset = tokens.firstOrNull { it.id.tokenId == jettonTokenId } ?: return@mapNotNull null
            AssetBalance.create(tokenAsset, available = wallet.balance)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun tokenBalance(jettonAddress: String): BigInteger {
        return BigInteger.valueOf(
            rpcClient.tokenBalance(jettonAddress).getOrNull()?.result?.balance ?: 0L
        )
    }
}