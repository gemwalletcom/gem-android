package com.gemwallet.android.features.assets.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.repositories.transaction.TransactionsRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.features.assets.model.WalletInfoUIState
import com.gemwallet.android.features.assets.model.toUIModel
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.interactors.sync.SyncTransactions
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.Session
import com.gemwallet.android.model.SyncState
import com.gemwallet.android.model.WalletSummary
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val syncTransactions: SyncTransactions,
    transactionsRepository: TransactionsRepository,
) : ViewModel() {
    val screenState = assetsRepository.syncState
        .flatMapLatest { state ->
            flow {
                emit(state)
                kotlinx.coroutines.delay(1000)
                emit(SyncState.Idle)
            }
        }
        .map { it == SyncState.InSync }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val assetsState: Flow<List<AssetInfo>> = assetsRepository.getAssetsInfo()

    private val assets: StateFlow<List<AssetUIState>> = assetsState
        .map { handleAssets(it) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pinnedAssets = assets.map { it.filter { asset -> asset.metadata?.isPinned ?: false }.sortedBy { it.position } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unpinnedAssets = assets.map { it.filter { asset -> !(asset.metadata?.isPinned ?: false) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val swapEnabled = assetsState.map {assets ->
        assets.any { asset ->
            EVMChain.entries.map { it.string }
                .contains(asset.owner.chain.string) || asset.owner.chain == Chain.Solana
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val txsState = transactionsRepository.getPendingTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val walletInfo: StateFlow<WalletInfoUIState> = sessionRepository.session().combine(assetsState) {session, assets ->
        if (session == null) {
            return@combine null
        }
        val walletInfo = calcWalletInfo(session.wallet, assets)
        WalletInfoUIState(
            name = walletInfo.name,
            icon = walletInfo.icon,
            totalValue = walletInfo.totalValue.format(0, session.currency.string, 2),
            changedValue = walletInfo.changedValue.format(0, session.currency.string, 2),
            changedPercentages = PriceUIState.formatPercentage(walletInfo.changedPercentages),
            priceState = PriceUIState.getState(walletInfo.changedPercentages),
            type = walletInfo.type,
        )
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, WalletInfoUIState())

    fun onRefresh() {
        val session = sessionRepository.getSession() ?: return
        updateAssetData(session)
    }

    private fun updateAssetData(session: Session) { // TODO: Out to case
        viewModelScope.launch(Dispatchers.IO) {
            val syncAssets = async {
                assetsRepository.sync(session.currency)
            }
            val syncTxs = async {
                syncTransactions(session.wallet.index)
            }
            syncAssets.await()
            syncTxs.await()
        }
    }

    private fun handleAssets(assets: List<AssetInfo>): ImmutableList<AssetUIState> {
        return assets
            .filter { asset -> asset.metadata?.isEnabled == true }
            .sortedByDescending {
                it.balances.calcTotal().convert(it.asset.decimals, it.price?.price?.price ?: 0.0).atomicValue
            }
            .map { it.toUIModel() }
            .toImmutableList()
    }

    fun hideAsset(assetId: AssetId) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getSession() ?: return@launch
            val account = session.wallet.getAccount(assetId.chain) ?: return@launch
            assetsRepository.switchVisibility(session.wallet.id, account, assetId, false, session.currency)
        }
    }

    private fun calcWalletInfo(wallet: Wallet, assets: List<AssetInfo>): WalletSummary {
        val totals = assets.map {
            val current = it.balances
                .calcTotal()
                .convert(it.asset.decimals, it.price?.price?.price ?: 0.0)
                .atomicValue.toDouble()
            val changed = current * ((it.price?.price?.priceChangePercentage24h ?: 0.0) / 100)
            Pair(current, changed)
        }.fold(Pair(0.0, 0.0)) { acc, pair ->
            Pair(acc.first + pair.first, acc.second + pair.second)
        }
        val changedPercentages = totals.second / (totals.first / 100.0)
        return WalletSummary(
            walletId = wallet.id,
            name = wallet.name,
            icon = if (wallet.type == WalletType.multicoin) {
                ""
            } else {
                wallet.accounts.firstOrNull()?.chain?.getIconUrl() ?: ""
            },
            type = wallet.type,
            totalValue = Fiat(totals.first),
            changedValue = Fiat(totals.second),
            changedPercentages = if (changedPercentages.isNaN()) {
                0.0
            } else {
                changedPercentages
            },
        )
    }

    fun togglePin(assetId: AssetId) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        assetsRepository.togglePin(
            session.wallet.id, assetId
        )
    }

    fun saveOrder(pinnedAssets: List<AssetUIState>) = viewModelScope.launch {
        assetsRepository.saveOrder(
            walletId = sessionRepository.getSession()?.wallet?.id ?: return@launch,
            order = pinnedAssets.map { it.asset.id }
        )
    }
}