// TODO: Out to special module or reorganize coordinators module as example rename to application or domain-application
package com.gemwallet.android.data.coordinates.wallet_import.services

import android.util.Log
import com.gemwallet.android.application.wallet_import.coordinators.GetImportWalletState
import com.gemwallet.android.application.wallet_import.services.ImportAssets
import com.gemwallet.android.application.wallet_import.values.ImportWalletState
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.identifier
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.type
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ImportWalletService(
    private val sessionRepository: SessionRepository,
    private val gemDeviceApiClient: GemDeviceApiClient,
    private val searchTokensCase: SearchTokensCase,
    private val assetsRepository: AssetsRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler {_, _ -> }),
) : ImportAssets, GetImportWalletState {

    private val jobs = MutableStateFlow<Map<String, Job>>(mutableMapOf())

    override fun importAssets(wallet: Wallet) {
        val job = scope.launch {
            val currency = sessionRepository.getCurrentCurrency()
            try {
                val availableAssetsId = gemDeviceApiClient.getAssets(walletId = wallet.id, 0)
                val assetIds = availableAssetsId.mapNotNull { it.toAssetId() }
                val tokenIds = assetIds.filter { it.type() != AssetSubtype.NATIVE }

                searchTokensCase.search(tokenIds, currency)
                val assets = assetsRepository.getTokensInfo(assetIds.map { it.identifier }).firstOrNull()
                assets?.map { it.asset }
                    ?.map { asset ->
                    async {
                        val account = wallet.getAccount(asset.chain)?.address ?: return@async null
                        Log.d("IMPORT_ASSETS", "Asset: ${asset.id}; Account: $account")
                        assetsRepository.add(
                            walletId = wallet.id,
                            accountAddress = account,
                            asset = asset,
                            visible = true
                        )
                        asset
                    }
                }?.awaitAll()
                assetsRepository.sync()
            } catch (err: Throwable) {
                Log.d("IMPORT_ERROR", "Error:", err)
            } finally {
                jobs.update { entries -> entries.toMutableMap().apply { remove(wallet.id) } }
            }
        }
        jobs.update { it.toMutableMap().apply { put(wallet.id, job) } }
    }

    override fun getImportState(walletId: String): Flow<ImportWalletState> = jobs.mapLatest { entries ->
        when (entries[walletId]?.isActive) {
            true -> ImportWalletState.Importing
            else -> ImportWalletState.Complete
        }
    }
}