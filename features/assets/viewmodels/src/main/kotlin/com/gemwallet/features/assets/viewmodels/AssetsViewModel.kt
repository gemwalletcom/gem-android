package com.gemwallet.features.assets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.assets.coordinators.GetActiveAssetsInfo
import com.gemwallet.android.application.assets.coordinators.GetWalletSummary
import com.gemwallet.android.application.wallet_import.coordinators.GetImportWalletState
import com.gemwallet.android.application.wallet_import.values.ImportWalletState
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import com.gemwallet.android.model.SyncState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.WalletSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val syncTransactions: SyncTransactions,
    private val userConfig: UserConfig,
    private val getImportWalletState: GetImportWalletState,
    getActiveAssetsInfo: GetActiveAssetsInfo,
    getWalletSummary: GetWalletSummary,
) : ViewModel() {

    private val session = sessionRepository.session()

    val importInProgress = session
        .filterNotNull()
        .flatMapLatest { session ->
            getImportWalletState
                .getImportState(session.wallet.id)
                .mapLatest { it == ImportWalletState.Importing }
        }
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val refreshingState = MutableStateFlow<RefreshingState>(RefreshingState.OnOpen)
    val screenState = refreshingState.map { refreshingState ->
            when (refreshingState) {
                RefreshingState.OnOpen -> SyncState.Idle
                RefreshingState.OnForce -> SyncState.InSync
            }
        }
        .flatMapLatest { state ->
            flow {
                emit(state)
                delay(1000)
                emit(SyncState.Idle)
            }
        }
        .map { it == SyncState.InSync }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val isHideBalances = userConfig.isHideBalances()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val assetsState: Flow<List<AssetInfo>> = assetsRepository.getAssetsInfo()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val assets = getActiveAssetsInfo.getAssetsInfo(isHideBalances)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pinnedAssets = assets
        .map { items -> items.filter { asset -> asset.pinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unpinnedAssets = assets
        .map { it.filter { asset -> !asset.pinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val walletSummary = getWalletSummary.getWalletSummary()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val showWelcomeBanner = combine( // Move to wallet summary
        sessionRepository.session().filterNotNull(),
        assetsState,
        session.filterNotNull()
            .flatMapLatest { userConfig.isWelcomeBannerHidden(it.wallet.id) }
    ) { session, assets, userConfig ->
        val empty = assets.fold(0.0) { acc, asset -> acc + asset.balance.totalAmount } == 0.0
        val created = session.wallet.source == WalletSource.Create
        empty && created && !userConfig
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onRefresh() {
        session.value?.let { session ->
            refreshingState.update { RefreshingState.OnForce }
            updateAssetData(session)
        }
    }

    private fun updateAssetData(session: Session) = viewModelScope.launch(Dispatchers.IO) {
        val syncAssets = async { assetsRepository.sync() }
        val syncTxs = async { syncTransactions.syncTransactions(session.wallet) }
        syncAssets.await()
        syncTxs.await()
    }

    fun hideAsset(assetId: AssetId) = viewModelScope.launch {
        val session = session.value ?: return@launch
        val account = session.wallet.getAccount(assetId.chain) ?: return@launch
        assetsRepository.switchVisibility(session.wallet.id, account, assetId, false)
    }

    fun togglePin(assetId: AssetId) = viewModelScope.launch {
        val session = session.value ?: return@launch
        assetsRepository.togglePin(session.wallet.id, assetId)
    }

    fun hideBalances() = viewModelScope.launch {
        userConfig.hideBalances()
    }

    fun onHideWelcomeBanner() = viewModelScope.launch {
        userConfig.hideWelcomeBanner(session.value?.wallet?.id ?: return@launch)
    }

    enum class RefreshingState {
        OnOpen,
        OnForce,
    }
}