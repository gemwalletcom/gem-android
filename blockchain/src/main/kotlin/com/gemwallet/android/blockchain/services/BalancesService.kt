package com.gemwallet.android.blockchain.services

import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import uniffi.gemstone.GatewayException
import uniffi.gemstone.GemGateway

class BalancesService(
    private val gateway: GemGateway,
) {

    suspend fun getNativeBalances(account: Account): AssetBalance? {
        return try {
            val result = gateway.getBalanceCoin(account.chain.string, account.address)
            AssetBalance.Companion.create(
                asset = account.chain.asset(),
                available = result.balance.available,
                reserved = result.balance.reserved,
                isActive = result.isActive,
            )
        } catch (_: GatewayException) {
            null
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getDelegationBalances(account: Account): AssetBalance? {
        return try {
            val result = gateway.getBalanceStaking(account.chain.string, account.address)
                ?: return null
            AssetBalance.Companion.create(
                asset = account.chain.asset(),
                frozen = result.balance.frozen,
                locked = result.balance.locked,
                staked = result.balance.staked,
                pending = result.balance.pending,
                rewards = result.balance.rewards,
            )
        } catch (_: GatewayException) {
            null
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getTokensBalances(account: Account, tokens: List<Asset>): List<AssetBalance> {
        return try {
            val ids = tokens.mapNotNull { it.id.tokenId }
            val result = gateway.getBalanceTokens(
                account.chain.string,
                account.address,
                ids
            )
            val assetIndex = tokens.groupBy { it.id.toIdentifier() }
            val balances = result.mapNotNull {
                AssetBalance.Companion.create(
                    asset = assetIndex[it.assetId]?.firstOrNull() ?: return@mapNotNull null,
                    available = it.balance.available,
                )
            }
            balances
        } catch (_: GatewayException) {
            emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }
}