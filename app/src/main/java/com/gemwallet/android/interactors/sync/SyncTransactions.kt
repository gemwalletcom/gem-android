package com.gemwallet.android.interactors.sync

import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.cases.transactions.PutTransactionsCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncTransactions @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val putTransactionsCase: PutTransactionsCase,
    private val getTransactionsCase: GetTransactionsCase,
    private val assetsRepository: AssetsRepository,
) {
    suspend operator fun invoke(wallet: Wallet) = withContext(Dispatchers.IO) {
        val deviceId = getDeviceIdCase.getDeviceId()
        val lastSyncTime = getTransactionsCase.getTransactions().firstOrNull()
            ?.maxByOrNull { it.transaction.createdAt }?.transaction?.createdAt ?: 0L

        val txs = try {
            gemApiClient.getTransactions(deviceId, wallet.index, lastSyncTime)
                .getOrNull() ?: return@withContext
        } catch (_: Throwable) {
            return@withContext
        }
        prefetchAssets(txs)

        putTransactionsCase.putTransactions(walletId = wallet.id, txs.toList())
    }

    private suspend fun prefetchAssets(txs: List<Transaction>) {
        val session = sessionRepository.getSession() ?: return
        val notAvailableAssetIds = txs.map {
            it.getAssociatedAssetIds().filter { assetsRepository.getAsset(it) == null }.toSet()
        }.flatten()
        assetsRepository.resolve(session.currency, session.wallet, notAvailableAssetIds)
    }
}