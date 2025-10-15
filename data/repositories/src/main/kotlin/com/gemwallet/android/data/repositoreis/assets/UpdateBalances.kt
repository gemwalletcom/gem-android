package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.blockchain.services.BalancesService
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.entities.DbBalance
import com.gemwallet.android.data.service.store.database.entities.mergeDelegation
import com.gemwallet.android.data.service.store.database.entities.mergeNative
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class UpdateBalances(
    private val balancesDao: BalancesDao,
    private val balancesService: BalancesService,
) {

    suspend fun updateBalances(
        walletId: String,
        account: Account,
        tokens: List<Asset>
    ): List<AssetBalance> = withContext(Dispatchers.IO) {
        val updatedAt = System.currentTimeMillis()

        val getNative = async { updateNativeBalance(walletId, account, updatedAt) }

        val getDelegation = async { balancesService.getDelegationBalances(account) }

        val getTokens = async { updateTokensBalance(walletId, account, tokens, updatedAt) }

        val native = getNative.await()
        val delegation = getDelegation.await()
        val fullNative = mergeNativeBalances(native, delegation?.toRecord(walletId, account.address, updatedAt))

        val tokens = getTokens.await()

        listOfNotNull(fullNative) + tokens
    }

    private suspend fun updateNativeBalance(walletId: String, account: Account, updatedAt: Long): DbBalance? {
        val prevBalance =
            balancesDao.getByAccount(walletId, account.address, account.chain.string)
        val nativeBalance = balancesService.getNativeBalances(account)
        val dbNativeBalance = DbBalance.mergeNative(
            prevBalance,
            nativeBalance?.toRecord(walletId, account.address, updatedAt),
        )
        dbNativeBalance?.let { runCatching { balancesDao.insert(it) } }
        return dbNativeBalance
    }

    private suspend fun updateTokensBalance(walletId: String, account: Account, tokens: List<Asset>, updatedAt: Long): List<AssetBalance> {
        val balances = balancesService.getTokensBalances(account, tokens)
        runCatching {
            val record = balances.map {
                it.toRecord(walletId, account.address, updatedAt)
            }
            balancesDao.insert(record)
        }
        return balances
    }

    private suspend fun mergeNativeBalances(native: DbBalance?, delegation: DbBalance?): AssetBalance? = withContext(Dispatchers.IO) {
        val dbFullBalance = DbBalance.mergeDelegation(native, delegation)
        dbFullBalance?.let { runCatching { balancesDao.insert(it) } }
        dbFullBalance?.toModel()
    }
}