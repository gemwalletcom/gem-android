package com.gemwallet.android.data.repositoreis.transactions

import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.transactions.GetTransactionUpdateTime
import com.gemwallet.android.cases.transactions.PutTransactions
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.model.Transaction
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncTransactionsService @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val putTransactions: PutTransactions,
    private val getTransactionUpdateTime: GetTransactionUpdateTime,
    private val assetsRepository: AssetsRepository,
) : SyncTransactions {

    override suspend fun syncTransactions(wallet: Wallet) = withContext(Dispatchers.IO) {
        val deviceId = getDeviceIdCase.getDeviceId()
        val lastSyncTime = getTransactionUpdateTime.getTransactionUpdateTime(wallet.id) / 1000L
        val response = runCatching {
            val result: List<Transaction>? = try {
                gemApiClient.getTransactions(deviceId, wallet.index, lastSyncTime)
            } catch (_: Throwable) {
                null
            }
            result
        }
        val txs: List<Transaction> = response.getOrNull() ?: return@withContext
        prefetchAssets(wallet, txs)

        putTransactions.putTransactions(walletId = wallet.id, txs.toList())
    }

    private suspend fun prefetchAssets(wallet: Wallet, txs: List<Transaction>) {
        val notAvailableAssetIds = txs.map { txs ->
            txs.getAssociatedAssetIds().filter { assetsRepository.getAsset(it) == null }.toSet()
        }.flatten()
        assetsRepository.resolve(wallet, notAvailableAssetIds)
    }
}