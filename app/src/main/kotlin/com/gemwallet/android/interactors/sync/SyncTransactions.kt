package com.gemwallet.android.interactors.sync

import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.transactions.GetTransactionUpdateTime
import com.gemwallet.android.cases.transactions.PutTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.model.Transaction
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncTransactions @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val putTransactions: PutTransactions,
    private val getTransactionUpdateTime: GetTransactionUpdateTime,
    private val assetsRepository: AssetsRepository,
) {
    suspend operator fun invoke(wallet: Wallet) = withContext(Dispatchers.IO) {
        val deviceId = getDeviceIdCase.getDeviceId()
        val lastSyncTime = getTransactionUpdateTime.getTransactionUpdateTime(wallet.id) / 1000
        val response = runCatching {
            val result: List<Transaction>? = try {
                gemApiClient.getTransactions(deviceId, wallet.index, lastSyncTime)
            } catch (_: Throwable) {
                null
            }
            result
        }
        val txs: List<Transaction> = response.getOrNull() ?: return@withContext
        prefetchAssets(txs)

        putTransactions.putTransactions(walletId = wallet.id, txs.toList())
    }

    private suspend fun prefetchAssets(txs: List<Transaction>) {
        val session = sessionRepository.getSession() ?: return
        val notAvailableAssetIds = txs.map { txs ->
            txs.getAssociatedAssetIds().filter { assetsRepository.getAsset(it) == null }.toSet()
        }.flatten()
        assetsRepository.resolve(session.wallet, notAvailableAssetIds)
    }
}