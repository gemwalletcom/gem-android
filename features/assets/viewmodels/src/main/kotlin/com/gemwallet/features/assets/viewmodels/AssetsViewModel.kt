package com.gemwallet.features.assets.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.banners.HasMultiSign
import com.gemwallet.android.cases.swap.GetSwapSupported
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.isSwapSupport
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import com.gemwallet.android.model.SyncState
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.AssetInfoUIModel
import com.gemwallet.android.ui.models.PriceState
import com.gemwallet.features.assets.viewmodels.model.PriceUIState
import com.gemwallet.features.assets.viewmodels.model.WalletInfoUIState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSource
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val syncTransactions: SyncTransactions,
    private val getSwapSupportedChains: GetSwapSupported,
    private val userConfig: UserConfig,
    private val hasMultiSign: HasMultiSign,
) : ViewModel() {

    private val session = sessionRepository.session()

    val importInProgress = session.filterNotNull().flatMapLatest {
        assetsRepository.importInProgress(it.wallet.id)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val operationsEnabled = session.filterNotNull()
        .flatMapLatest { hasMultiSign.hasMultiSign(it.wallet) }
        .mapLatest { !it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

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

    private val assets = combine(
        assetsState,
        isHideBalances
    ) { assets, isHideBalances ->
        assets.map { AssetInfoUIModel(it, isHideBalances) }.distinctBy { it.asset.id.toIdentifier() }
    }
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pinnedAssets = assets
        .map { items -> items.filter { asset -> asset.metadata?.isPinned == true } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unpinnedAssets = assets
        .map { it.filter { asset -> asset.metadata?.isPinned != true } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val walletInfo: StateFlow<WalletInfoUIState> = combine(
        sessionRepository.session(),
        assetsState,
        isHideBalances,
        operationsEnabled,
    ) { session, assets, isHideBalances, operationsEnabled ->
        val wallet = session?.wallet ?: return@combine null
        val currency = session.currency
        calcWalletInfo(
            wallet,
            currency,
            assets,
            isHideBalances,
            operationsEnabled,
        )
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, WalletInfoUIState())

    val showWelcomeBanner = combine(
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

    private fun calcWalletInfo(
        wallet: Wallet,
        currency: Currency,
        assets: List<AssetInfo>,
        isHideBalances: Boolean,
        operationsEnabled: Boolean,
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
            else -> wallet.accounts.firstOrNull()?.chain?.asset()
        }
        val cryptoTotal = assets.fold(0.0) { acc, asset ->
            acc + asset.balance.totalAmount
        }
        val isSwapEnabled = when (wallet.type) {
            WalletType.multicoin -> true
            WalletType.single,
            WalletType.private_key -> wallet.accounts.firstOrNull()?.chain?.isSwapSupport() == true
            WalletType.view -> false
        }
        val swapPayAsset = when(wallet.type) {
            WalletType.multicoin -> (wallet.getAccount(Chain.Ethereum) ?: wallet.accounts.firstOrNull())?.chain
            WalletType.single,
            WalletType.private_key -> wallet.accounts.firstOrNull()?.chain
            WalletType.view -> null
        }?.asset()

        return if (isHideBalances) {
            WalletInfoUIState(
                name = wallet.name,
                icon = icon,
                cryptoTotalValue = cryptoTotal,
                totalValueFormatted = "✱✱✱✱✱✱",
                changedValue = "",
                changedPercentages = "",
                priceState = PriceState.None,
                type = wallet.type,
                operationsEnabled = operationsEnabled,
                isSwapEnabled = isSwapEnabled,
                swapPayAsset = swapPayAsset,
            )
        } else {
            WalletInfoUIState(
                name = wallet.name,
                icon = icon,
                cryptoTotalValue = cryptoTotal,
                totalValueFormatted = currency.format(totalValue, dynamicPlace = true),
                changedValue = currency.format(changedValue, dynamicPlace = true),
                changedPercentages = PriceUIState.formatPercentage(changedPercentages),
                priceState = PriceUIState.getState(changedPercentages),
                type = wallet.type,
                operationsEnabled = operationsEnabled,
                isSwapEnabled = isSwapEnabled,
                swapPayAsset = swapPayAsset,
            )
        }
    }

    enum class RefreshingState {
        OnOpen,
        OnForce,
    }
}