package com.gemwallet.android.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.features.assets.model.WalletInfoUIState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.interactors.sync.SyncTransactions
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.Session
import com.gemwallet.android.model.WalletSummary
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val syncTransactions: SyncTransactions,
) : ViewModel() {

    private val _state = MutableStateFlow(AssetsViewModelState())
    @OptIn(ExperimentalCoroutinesApi::class)
    private val state: Flow<AssetsViewModelState> = sessionRepository.session().flatMapLatest { session ->
        session ?: return@flatMapLatest emptyFlow()
        combine(
            assetsRepository.getAllByWalletFlow(session.wallet),
            transactionsRepository.getTransactions(null, *session.wallet.accounts.toTypedArray())
                .map { txs -> txs.filter { it.transaction.state == TransactionState.Pending } }
        ) { assets, txs ->
            val swapEnabled = session.wallet.accounts.any { acc ->
                EVMChain.entries.map { it.string }.contains(acc.chain.string) || acc.chain == Chain.Solana
            }
            AssetsViewModelState(
                walletInfo = calcWalletInfo(session.wallet, assets),
                assets = handleAssets(session.currency, assets),
                pendingTransactions = txs,
                currency = session.currency,
                swapEnabled = swapEnabled,
            )
        }
    }.flowOn(Dispatchers.IO)

    val uiState = _state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AssetsUIState())

    init {
        viewModelScope.launch {
            state.collect { viewModelState ->
                _state.update { viewModelState }
            }
        }
        onRefresh()
    }

    fun onRefresh() {
        val session = sessionRepository.getSession() ?: return
        updateAssetData(session)
    }

    private fun updateAssetData(session: Session) {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                assetsRepository.syncTokens(session.wallet, session.currency)
                _state.update { it.copy(isLoading = false) }
            }
            withContext(Dispatchers.IO) {
                syncTransactions(session.wallet.index)
            }
        }
    }

    private fun handleAssets(currency: Currency, assets: List<AssetInfo>): ImmutableList<AssetUIState> {
        return assets
            .filter { asset -> asset.metadata?.isEnabled == true }
            .sortedByDescending {
                it.balances.calcTotal().convert(it.asset.decimals, it.price?.price?.price ?: 0.0).atomicValue
            }.map {
                val totalBalance = it.balances.calcTotal()
                AssetUIState(
                    id = it.asset.id,
                    name = it.asset.name,
                    icon = it.asset.getIconUrl(),
                    type = it.asset.type,
                    owner = it.owner.address,
                    value =  totalBalance.format(it.asset.decimals, it.asset.symbol, 4),
                    isZeroValue = totalBalance.atomicValue == BigInteger.ZERO,
                    fiat = if (it.price == null || it.price.price.price == 0.0) {
                        ""
                    } else {
                        totalBalance.convert(it.asset.decimals, it.price.price.price)
                            .format(0, currency.string, 2)
                    },
                    price = PriceUIState.create(it.price?.price, currency),
                    symbol = it.asset.symbol,
                )
            }.toImmutableList()
    }

    fun hideAsset(assetId: AssetId) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getSession() ?: return@launch
            val account = session.wallet.getAccount(assetId.chain) ?: return@launch
            assetsRepository.switchVisibility(account, assetId, false, session.currency)
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
}

private data class AssetsViewModelState(
    val isLoading: Boolean = false,
    val walletInfo: WalletSummary? = null,
    val currency: Currency = Currency.USD,
    val assets: ImmutableList<AssetUIState> = emptyList<AssetUIState>().toImmutableList(),
    val pendingTransactions: List<TransactionExtended> = emptyList(),
    val swapEnabled: Boolean = true,
    val error: String = "",
) {
    fun toUIState(): AssetsUIState {
        return AssetsUIState(
            isLoading = isLoading,
            walletInfo = WalletInfoUIState(
                name = walletInfo?.name ?: "",
                icon = walletInfo?.icon ?: "",
                totalValue = (walletInfo?.totalValue ?: Fiat(0.0)).format(0, currency.string, 2),
                changedValue = (walletInfo?.changedValue ?: Fiat(0.0)).format(0, currency.string, 2),
                changedPercentages = PriceUIState.formatPercentage(walletInfo?.changedPercentages ?: 0.0),
                priceState = PriceUIState.getState(walletInfo?.changedPercentages ?: 0.0),
                type = walletInfo?.type ?: WalletType.view,
            ),
            assets = assets,
            swapEnabled = swapEnabled,
            pendingTransactions = pendingTransactions.toImmutableList()
        )
    }


}

data class AssetsUIState(
    val isLoading: Boolean = false,
    val walletInfo: WalletInfoUIState = WalletInfoUIState(),
    val assets: ImmutableList<AssetUIState> = emptyList<AssetUIState>().toImmutableList(),
    val pendingTransactions: ImmutableList<TransactionExtended> = emptyList<TransactionExtended>().toImmutableList(),
    val swapEnabled: Boolean = true,
    val error: String = "",
)