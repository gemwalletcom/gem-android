package com.gemwallet.android.interactors.sync

import com.gemwallet.android.cases.transactions.PutTransactionsCase
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.tokens.TokensRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncTransactions @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val configRepository: ConfigRepository,
    private val putTransactionsCase: PutTransactionsCase,
    private val assetsRepository: AssetsRepository,
    private val tokensRepository: TokensRepository,
) {
    suspend operator fun invoke(wallet: Wallet) = withContext(Dispatchers.IO) {
        val deviceId = configRepository.getDeviceId()
        val lastSyncTime = configRepository.getTxSyncTime()
        val txs = gemApiClient.getTransactions(deviceId, wallet.index, lastSyncTime).getOrNull() ?: return@withContext
        prefetchAssets(txs)

        putTransactionsCase.putTransactions(walletId = wallet.id, txs.toList())

        configRepository.setTxSyncTime(txs.map { listOf(it.createdAt) }.flatten().maxByOrNull { it } ?: 0L)
    }

    private suspend fun prefetchAssets(txs: List<Transaction>) {
        val session = sessionRepository.getSession() ?: return
        txs.map {
            val notAvailableAssetsId = mutableListOf<AssetId>()
            if (assetsRepository.getAsset(it.assetId) == null) {
                notAvailableAssetsId.add(it.assetId)
            }
            if (assetsRepository.getAsset(it.feeAssetId) == null) {
                notAvailableAssetsId.add(it.assetId)
            }
            val swapMetadata = it.getSwapMetadata()
            if (swapMetadata != null) {
                if (assetsRepository.getAsset(swapMetadata.fromAsset) == null) {
                    notAvailableAssetsId.add(swapMetadata.fromAsset)
                }
                if (assetsRepository.getAsset(swapMetadata.toAsset) == null) {
                    notAvailableAssetsId.add(swapMetadata.toAsset)
                }
            }
            notAvailableAssetsId.toSet()
        }.flatten().forEach {  assetId ->
            tokensRepository.search(assetId)
            assetsRepository.switchVisibility(
                session.wallet.id,
                session.wallet.getAccount(assetId.chain) ?: return@forEach,
                assetId,
                false,
                session.currency,
            )
        }
    }
}