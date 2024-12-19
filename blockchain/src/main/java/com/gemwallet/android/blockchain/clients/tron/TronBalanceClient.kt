package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.gemwallet.android.blockchain.clients.tron.services.TronCallService
import com.gemwallet.android.blockchain.clients.tron.services.TronStakeService
import com.gemwallet.android.blockchain.clients.tron.services.getAccount
import com.gemwallet.android.blockchain.clients.tron.services.staked
import com.gemwallet.android.blockchain.clients.tron.services.triggerSmartContract
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.Base58
import java.math.BigInteger

class TronBalanceClient(
    private val chain: Chain,
    private val accountsService: TronAccountsService,
    private val tronCallService: TronCallService,
    private val stakeService: TronStakeService,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? = withContext(Dispatchers.IO) {
        val getAaccount = async { accountsService.getAccount(address, true) }
        val getRewards = async { stakeService.getReward(address).getOrNull()?.reward?.toString() ?: "0" }
        val account = getAaccount.await() ?: return@withContext null
        val available = account.balance?.toString() ?: "0"
        val pending = account.unfrozenV2?.mapNotNull { unfroze ->
            unfroze.unfreeze_amount ?: return@mapNotNull null
        }?.fold(0L) { acc, amount -> acc + amount }?.toString() ?: "0"
        val rewards = getRewards.await()
        val staked = account.staked(chain).toString()

        if (available == "0" && staked == "0" && rewards == "0" && pending == "0") {
            return@withContext null
        }

        AssetBalance.create(
            chain.asset(),
            available = available.toString(),
            staked = staked,
            pending = pending,
            rewards = rewards
        )
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        return tokens.mapNotNull { token ->
            val tokenId = token.id.tokenId ?: return@mapNotNull null
            val owner = Base58.decode(address).toHexString("")
            tronCallService.triggerSmartContract(
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