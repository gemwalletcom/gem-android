package com.gemwallet.android.features.assets.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.features.assets.model.WalletInfoUIState
import com.gemwallet.android.interactors.sync.SyncTransactions
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import com.gemwallet.android.model.SyncState
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.gemwallet.android.ui.models.PriceState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val syncTransactions: SyncTransactions,
    private val userConfig: UserConfig,
) : ViewModel() {
    val refreshingState = MutableStateFlow<RefresingState>(RefresingState.OnOpen)
    val screenState = refreshingState.map { refreshingState ->
            when (refreshingState) {
                RefresingState.OnOpen -> SyncState.Idle
                RefresingState.OnForce -> SyncState.InSync
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
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val assetsState: Flow<List<AssetInfo>> = assetsRepository.getAssetsInfo()

    private val isHideBalances = userConfig.isHideBalances()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val assets: StateFlow<List<AssetItemUIModel>> = assetsState.combine(isHideBalances) { assets, isHideBalances ->
        assets.map { AssetInfoUIModel(it, isHideBalances) }.distinctBy { it.asset.id.toIdentifier() }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pinnedAssets = assets
        .map { items -> items.filter { asset -> asset.metadata?.isPinned == true } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unpinnedAssets = assets.map { it.filter { asset -> asset.metadata?.isPinned != true } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val walletInfo: StateFlow<WalletInfoUIState> = combine(sessionRepository.session(), assetsState, isHideBalances) {session, assets, isHideBalances ->
        val wallet = session?.wallet ?: return@combine null
        val currency = session.currency
        calcWalletInfo(wallet, currency, assets, isHideBalances)
    }
    .flowOn(Dispatchers.Default)
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, WalletInfoUIState())

    fun onRefresh() {
        val session = sessionRepository.getSession() ?: return
        refreshingState.update { RefresingState.OnForce }
        updateAssetData(session)
    }

    private fun updateAssetData(session: Session) { // TODO: Out to case
        viewModelScope.launch(Dispatchers.IO) {
            val syncAssets = async { assetsRepository.sync() }
            val syncTxs = async { syncTransactions(session.wallet) }
            syncAssets.await()
            syncTxs.await()
        }
    }

    fun hideAsset(assetId: AssetId) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getSession() ?: return@launch
            val account = session.wallet.getAccount(assetId.chain) ?: return@launch
            assetsRepository.switchVisibility(session.wallet.id, account, assetId, false)
        }
    }

    private fun calcWalletInfo(
        wallet: Wallet,
        currency: Currency,
        assets: List<AssetInfo>,
        isHideBalances: Boolean
    ): WalletInfoUIState? {
        val (totalValue, changedValue) = assets.map {
            val current = it.balance.fiatTotalAmount.toBigDecimal()
            val changed = current * ((it.price?.price?.priceChangePercentage24h ?: 0.0) / 100).toBigDecimal()
            Pair(current, changed)
        }.fold(Pair(BigDecimal.ZERO, BigDecimal.ZERO)) { acc, pair ->
            Pair(acc.first + pair.first, acc.second + pair.second)
        }
        val changedPercentages = (changedValue.toDouble() / (totalValue.toDouble() / 100.0)).let {
            if (it.isNaN()) 0.0 else it
        }
        val icon = when (wallet.type) {
            WalletType.multicoin -> R.drawable.multicoin_wallet
            else -> wallet.accounts.firstOrNull()?.chain?.getIconUrl()
        }
        return if (isHideBalances) {
            WalletInfoUIState(
                name = wallet.name,
                icon = icon,
                totalValue = "*****",
                changedValue = "",
                changedPercentages = "",
                priceState = PriceState.None,
                type = wallet.type,
            )
        } else {
            WalletInfoUIState(
                name = wallet.name,
                icon = icon,
                totalValue = currency.format(totalValue),
                changedValue = currency.format(changedValue),
                changedPercentages = PriceUIState.formatPercentage(changedPercentages),
                priceState = PriceUIState.getState(changedPercentages),
                type = wallet.type,
            )
        }
    }

    fun togglePin(assetId: AssetId) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        assetsRepository.togglePin(session.wallet.id, assetId)
    }

    fun hideBalances() {
        viewModelScope.launch {
            userConfig.hideBalances()
        }
    }

    enum class RefresingState {
        OnOpen,
        OnForce,
    }
}