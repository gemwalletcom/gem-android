package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaBalancesService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaStakeService
import com.gemwallet.android.blockchain.clients.solana.services.getBalance
import com.gemwallet.android.blockchain.clients.solana.services.getDelegationsBalance
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

class SolanaBalanceClient(
    val chain: Chain,
    val accountsService: SolanaAccountsService,
    val balancesService: SolanaBalancesService,
    val stakeService: SolanaStakeService,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? = withContext(Dispatchers.IO) {
        val available = balancesService.getBalance(address) ?: return@withContext null
        AssetBalance.create(chain.asset(), available.toString())
    }

    override suspend fun getDelegationBalances(chain: Chain, address: String): AssetBalance? {
        val staked = stakeService.getDelegationsBalance(address)
        return AssetBalance.create(chain.asset(), staked = staked.toString())
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        val accountsRequests = tokens.map {
            JSONRpcRequest.create(
                method = SolanaMethod.GetTokenAccountByOwner,
                params = listOf(
                    address,
                    mapOf("mint" to it.id.tokenId!!),
                    mapOf("encoding" to "jsonParsed"),
                )
            )
        }

        val responseAccountOwner = try {
            accountsService.batchAccount(accountsRequests)
        } catch (err: Throwable) {
            throw err
        }
        val balances = responseAccountOwner.mapIndexed { index, value ->
            val balance = value.result.value.firstOrNull()?.let { account ->
                account.account.data.parsed.info.tokenAmount.amount.toBigIntegerOrNull()
            } ?: BigInteger.ZERO
            AssetBalance.create(tokens[index], available = balance.toString())
        }
        return balances
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}