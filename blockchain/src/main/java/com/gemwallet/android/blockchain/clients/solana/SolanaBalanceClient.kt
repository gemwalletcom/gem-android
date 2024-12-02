package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class SolanaBalanceClient(
    val chain: Chain,
    val rpcClient: SolanaRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? = withContext(Dispatchers.IO) {
        val getAvailable = async {
            rpcClient.getBalance(JSONRpcRequest.create(SolanaMethod.GetBalance, listOf(address)))
                .getOrNull()?.result?.value
        }
        val getStaked = async {
            rpcClient.delegations(address)
                .getOrNull()?.result?.map { it.account.lamports }
                ?.fold(0L) { acc, value -> acc + value } ?: 0L
        }
        val (available, staked) = Pair(getAvailable.await(), getStaked.await())
        if (available == null) {
            return@withContext null
        }
        AssetBalance.create(
            asset = chain.asset(),
            available = available.toString(),
            staked = staked.toString(),
        )
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        val result = mutableListOf<AssetBalance>()
        for (token in tokens) {
            val tokenId = token.id.tokenId ?: continue
            val balance = getTokenBalance(address, tokenId).toString()
            result.add(AssetBalance.create(token, available = balance))
        }
        return result
    }

    private suspend fun getTokenBalance(owner: String, tokenId: String): BigInteger {
        val accountRequest = JSONRpcRequest.create(
            method = SolanaMethod.GetTokenAccountByOwner,
            params = listOf(
                owner,
                mapOf("mint" to tokenId),
                mapOf("encoding" to "jsonParsed"),
            )
        )
        val tokenAccount = rpcClient.getTokenAccountByOwner(accountRequest)
            .getOrNull()?.result?.value?.firstOrNull()?.pubkey ?: return BigInteger.ZERO

        val balanceRequest = JSONRpcRequest.create(SolanaMethod.GetTokenBalance, listOf(tokenAccount))
        return rpcClient.getTokenBalance(balanceRequest).getOrNull()
            ?.result?.value?.amount?.toBigInteger() ?: return BigInteger.ZERO
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}