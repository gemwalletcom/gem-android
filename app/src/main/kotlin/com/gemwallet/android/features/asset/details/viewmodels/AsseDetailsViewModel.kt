package com.gemwallet.android.features.asset.details.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.nodes.GetCurrentBlockExplorer
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
import com.gemwallet.android.cases.pricealerts.GetPriceAlerts
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.isStaked
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset.details.models.AssetInfoUIModel
import com.gemwallet.android.features.asset.details.models.AssetInfoUIState
import com.gemwallet.android.features.asset.navigation.assetIdArg
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.availableFormatted
import com.gemwallet.android.model.format
import com.gemwallet.android.model.getStackedAmount
import com.gemwallet.android.model.reservedFormatted
import com.gemwallet.android.model.totalFormatted
import com.gemwallet.android.model.totalStakeFormatted
import com.gemwallet.android.ui.components.image.getIconUrl
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.StakeChain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import uniffi.gemstone.Explorer
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AsseDetailsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val getTransactions: GetTransactions,
    private val getPriceAlerts: GetPriceAlerts,
    private val enablePriceAlert: EnablePriceAlert,
    private val getCurrentBlockExplorer: GetCurrentBlockExplorer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val assetId = savedStateHandle.getStateFlow(assetIdArg, "").map { it.toAssetId() }

    val uiState = MutableStateFlow<AssetInfoUIState>(AssetInfoUIState.Idle(AssetInfoUIState.SyncState.Process))

    private val model = assetId
        .onEach { uiState.update { AssetInfoUIState.Idle(AssetInfoUIState.SyncState.Process) } }
        .flatMapLatest { assetId ->
            assetId?.let { assetsRepository.getAssetInfo(it).mapNotNull { it } } ?: emptyFlow()
        }
        .map {
            val explorerName = getCurrentBlockExplorer.getCurrentBlockExplorer(it.asset.chain())
            Model(
                assetInfo = it,
                explorerName = explorerName,
                updatedAt = System.currentTimeMillis()
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val priceAlertEnabled = assetId.flatMapLatest {
            getPriceAlerts.isAssetPriceAlertEnabled(it ?: return@flatMapLatest emptyFlow())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val transactions = assetId.flatMapLatest { getTransactions.getTransactions(it) }
        .map { it.toImmutableList() }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val sync: Flow<Unit> = combine(uiState, model) { uiState, model ->
            if (uiState is AssetInfoUIState.Idle
                    && (uiState.sync == AssetInfoUIState.SyncState.Wait || uiState.sync == AssetInfoUIState.SyncState.Process)
            ) {
                model ?: return@combine
                syncAssetInfo(model.assetInfo.asset.id)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, Unit)

    val uiModel = model.map { it?.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun refresh(state: AssetInfoUIState.SyncState = AssetInfoUIState.SyncState.Wait) {
        uiState.update { AssetInfoUIState.Idle(state) }
    }

    private fun syncAssetInfo(assetId: AssetId) {
        uiState.update {
            AssetInfoUIState.Idle(
                if ((it as? AssetInfoUIState.Idle)?.sync != AssetInfoUIState.SyncState.Process) {
                    AssetInfoUIState.SyncState.Loading
                } else {
                    AssetInfoUIState.SyncState.None
                }
            )
        }
        viewModelScope.launch {
            assetsRepository.syncAssetInfo(
                assetId = assetId,
                account = sessionRepository.getSession()?.wallet?.getAccount(assetId.chain) ?: return@launch
            )
        }
        viewModelScope.launch {
            delay(300)
            uiState.update { AssetInfoUIState.Idle() }
        }
    }

    fun enablePriceAlert(assetId: AssetId) = viewModelScope.launch {
        enablePriceAlert.setAssetPriceAlertEnabled(assetId, priceAlertEnabled.value != true)
    }

    private data class Model(
        val assetInfo: AssetInfo,
        val updatedAt: Long,
        val explorerName: String,
    ) {
        fun toUIState(): AssetInfoUIModel {
            val assetInfo = assetInfo
            val price = assetInfo.price?.price?.price ?: 0.0
            val currency = assetInfo.price?.currency ?: Currency.USD
            val asset = assetInfo.asset
            val balances = assetInfo.balance
            val total = balances.totalAmount
            val fiatTotal = currency.format(balances.fiatTotalAmount)
            val stakeBalance = balances.balanceAmount.getStackedAmount()
            return AssetInfoUIModel(
                assetInfo = assetInfo,
                name = if (asset.type == AssetType.NATIVE) {
                    asset.id.chain.asset().name
                } else {
                    asset.name
                },
                iconUrl = asset.id.getIconUrl(),
                priceValue = if (price == 0.0) "" else currency.format(price, dynamicPlace = true),
                priceDayChanges = PriceUIState.formatPercentage(
                    assetInfo.price?.price?.priceChangePercentage24h ?: 0.0
                ),
                priceChangedType = PriceUIState.getState(
                    assetInfo.price?.price?.priceChangePercentage24h ?: 0.0
                ),
                tokenType = asset.type,
                networkTitle = "${asset.id.chain.asset().name} (${asset.type.string})",
                isBuyEnabled = assetInfo.metadata?.isBuyEnabled == true,
                isSwapEnabled = assetInfo.metadata?.isSwapEnabled == true,
                explorerName = explorerName,
                explorerAddressUrl = assetInfo.owner?.address?.let {
                    Explorer(asset.chain().string).getAddressUrl(explorerName,  it)
                },
                accountInfoUIModel = AssetInfoUIModel.AccountInfoUIModel(
                    walletType = assetInfo.walletType,
                    totalBalance = balances.totalFormatted(),
                    totalFiat = fiatTotal,
                    owner = assetInfo.owner?.address ?: "",
                    hasBalanceDetails = StakeChain.isStaked(asset.id.chain) || balances.balanceAmount.reserved != 0.0,
                    available = if (balances.balanceAmount.available != total) {
                        balances.availableFormatted()
                    } else {
                        ""
                    },
                    stake = if (asset.id.type() == AssetSubtype.NATIVE && StakeChain.isStaked(asset.id.chain)) {
                        if (stakeBalance == 0.0) {
                            "APR ${PriceUIState.formatPercentage(assetInfo.stakeApr ?: 0.0, false)}"
                        } else {
                            balances.totalStakeFormatted()
                        }
                    } else {
                        ""
                    },
                    reserved = if (balances.balanceAmount.reserved != 0.0) {
                        balances.reservedFormatted()
                    } else {
                        ""
                    },
                ),
            )
        }
    }
}



