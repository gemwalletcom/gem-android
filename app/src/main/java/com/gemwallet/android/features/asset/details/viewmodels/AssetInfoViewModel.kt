package com.gemwallet.android.features.asset.details.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.isStaked
import com.gemwallet.android.ext.isSwapable
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset.details.models.AssetInfoUIModel
import com.gemwallet.android.features.asset.details.models.AssetInfoUIState
import com.gemwallet.android.features.asset.navigation.assetIdArg
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BalanceType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionExtended
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class AssetInfoViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val stakeRepository: StakeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val assetIdStr = savedStateHandle.getStateFlow(assetIdArg, "")

    val uiState = MutableStateFlow<AssetInfoUIState>(AssetInfoUIState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val model: Flow<Model> = assetIdStr.flatMapConcat {
        val assetId = it.toAssetId() ?: return@flatMapConcat emptyFlow()

        uiState.update { AssetInfoUIState.Idle(AssetInfoUIState.SyncState.Wait) }

        combine(
            assetsRepository.getAssetInfo(assetId),
            transactionsRepository.getTransactions(assetId)
        ) { assetInfo, transactions ->
            Model(assetInfo, transactions)
        }
    }

    private val sync: Flow<Unit> = combine(uiState, model) { uiState, model ->
        if (uiState is AssetInfoUIState.Idle && uiState.sync == AssetInfoUIState.SyncState.Wait) {
            syncAssetInfo(model.assetInfo.asset.id, model.assetInfo.owner, model.assetInfo.stakeApr ?: 0.0)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Unit)

    val uiModel = model.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun refresh() {
        uiState.update { AssetInfoUIState.Idle(AssetInfoUIState.SyncState.Wait) }
    }

    private fun syncAssetInfo(assetId: AssetId, owner: Account, apr: Double) {
        uiState.update { AssetInfoUIState.Idle(AssetInfoUIState.SyncState.Loading) }
        viewModelScope.launch { assetsRepository.syncAssetInfo(assetId) }
        viewModelScope.launch { stakeRepository.sync(assetId.chain, owner.address, apr) }
        viewModelScope.launch {
            delay(500)
            uiState.update { AssetInfoUIState.Idle() }
        }
    }

    private data class Model(
        val assetInfo: AssetInfo,
        val transactions: List<TransactionExtended> = emptyList(),
    ) {
        fun toUIState(): AssetInfoUIModel {
            val assetInfo = assetInfo
            val price = assetInfo.price?.price?.price ?: 0.0
            val currency = assetInfo.price?.currency ?: Currency.USD
            val asset = assetInfo.asset
            val balances = assetInfo.balances
            val total = balances.calcTotal()
            val fiatTotal = if (assetInfo.price == null) {
                null
            } else {
                total.convert(asset.decimals, assetInfo.price.price.price)
            }
            val stakeBalance = balances.items.filter {
                it.balance.type != BalanceType.available && it.balance.type != BalanceType.reserved
            }
            .map { BigInteger(it.balance.value) }
            .fold(BigInteger.ZERO) {acc, value -> acc + value }
            val reservedBalance = balances.items.filter { it.balance.type == BalanceType.reserved }
                .map { BigInteger(it.balance.value) }
                .fold(BigInteger.ZERO) {acc, value -> acc + value }

            return AssetInfoUIModel(
                asset = asset,
                name = if (asset.type == AssetType.NATIVE) {
                    asset.id.chain.asset().name
                } else {
                    asset.name
                },
                iconUrl = asset.id.getIconUrl(),
                priceValue = if (price == 0.0) "" else {
                    Fiat(price).format(0, currency.string, 2, dynamicPlace = true)
                },
                priceDayChanges = PriceUIState.formatPercentage(
                    assetInfo.price?.price?.priceChangePercentage24h ?: 0.0
                ),
                priceChangedType = PriceUIState.getState(
                    assetInfo.price?.price?.priceChangePercentage24h ?: 0.0
                ),
                tokenType = asset.type,
                networkTitle = "${asset.id.chain.asset().name} (${asset.type.string})",
                networkIcon = AssetId(asset.id.chain).getIconUrl(),
                isBuyEnabled = assetInfo.metadata?.isBuyEnabled ?: false,
                isSwapEnabled = (assetInfo.metadata?.isSwapEnabled ?: false) || asset.id.isSwapable(),
                transactions = transactions,
                account = AssetInfoUIModel.Account(
                    walletType = assetInfo.walletType,
                    totalBalance = total.format(asset.decimals, asset.symbol, 6),
                    totalFiat = fiatTotal?.format(0, currency.string, 2) ?: "",
                    owner = assetInfo.owner.address,
                    hasBalanceDetails = StakeChain.isStaked(asset.id.chain) || reservedBalance != BigInteger.ZERO,
                    available = if (balances.available().atomicValue != total.atomicValue) {
                        balances.available().format(asset.decimals, asset.symbol, 6)
                    } else {
                        ""
                    },
                    stake = if (asset.id.type() == AssetSubtype.NATIVE && StakeChain.isStaked(asset.id.chain)) {
                        if (stakeBalance == BigInteger.ZERO) {
                            "APR ${PriceUIState.formatPercentage(assetInfo.stakeApr ?: 0.0, false)}"
                        } else {
                            Crypto(stakeBalance).format(asset.decimals, asset.symbol, 6)
                        }
                    } else {
                        ""
                    },
                    reserved = if (reservedBalance != BigInteger.ZERO) {
                        Crypto(reservedBalance).format(asset.decimals, asset.symbol, 6)
                    } else {
                        ""
                    },
                ),
            )
        }
    }
}



