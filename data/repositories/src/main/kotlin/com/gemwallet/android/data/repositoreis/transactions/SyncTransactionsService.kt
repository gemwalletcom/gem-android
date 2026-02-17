package com.gemwallet.android.data.repositoreis.transactions

import android.util.Log
import com.gemwallet.android.cases.transactions.GetTransactionUpdateTime
import com.gemwallet.android.cases.transactions.PutTransactions
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.model.Transaction
import com.wallet.core.primitives.Wallet
import javax.inject.Inject

class SyncTransactionsService @Inject constructor(
    private val gemDeviceApiClient: GemDeviceApiClient,
    private val putTransactions: PutTransactions,
    private val getTransactionUpdateTime: GetTransactionUpdateTime,
    private val assetsRepository: AssetsRepository,
) : SyncTransactions {

    override suspend fun syncTransactions(wallet: Wallet) {
        val lastSyncTime = getTransactionUpdateTime.getTransactionUpdateTime(wallet.id) / 1000L
        val response = runCatching {
            val result: List<Transaction>? = try {
                gemDeviceApiClient.getTransactions(wallet.id, lastSyncTime).transactions
            } catch (_: Throwable) {
                null
            }
            result
        }
        val txs: List<Transaction> = response.getOrNull() ?: return
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