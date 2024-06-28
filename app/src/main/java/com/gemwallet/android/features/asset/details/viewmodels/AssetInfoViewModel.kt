package com.gemwallet.android.features.asset.details.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.isStaked
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
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BalanceType
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val stakeRepository: StakeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val assetIdStr = savedStateHandle.getStateFlow(assetIdArg, "")

    val uiState = MutableStateFlow<AssetInfoUIState>(AssetInfoUIState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val model: Flow<Model> = assetIdStr
        .flatMapConcat {
            val assetId = it.toAssetId() ?: return@flatMapConcat emptyFlow()

            val session = sessionRepository.getSession() ?: return@flatMapConcat emptyFlow()
            val account = session.wallet.accounts.firstOrNull { it.chain == assetId.chain } ?: return@flatMapConcat emptyFlow()

            val stakeApr = if (StakeChain.isStaked(assetId.chain)) {
                assetsRepository.getStakeApr(assetId)
            } else {
                null
            }
            syncAssetInfo(assetId)
            uiState.update { AssetInfoUIState.Idle() }

            combine(
                assetsRepository.getAssetInfo(assetId),
                transactionsRepository.getTransactions(assetId, account)
            ) { assetInfo, transactions ->
                Model(
                    assetInfo = assetInfo,
                    stakeApr = stakeApr,
                    transactions = transactions,
                )
            }
        }

    val uiModel = model.map {
        it.toUIState()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun refresh() {
        uiState.update { AssetInfoUIState.Idle(true) }
        syncAssetInfo(assetId = assetIdStr.value.toAssetId() ?: return)
    }

    private fun syncAssetInfo(assetId: AssetId) {
        viewModelScope.launch {
            val session = sessionRepository.getSession() ?: return@launch
            val account = session.wallet.getAccount(assetId.chain) ?: return@launch

            val syncAssetInfo = async {
                assetsRepository.syncAssetInfo(account, assetId, session.currency)
            }
            val syncStake = async {
                stakeRepository.sync(assetId.chain, account.address, assetsRepository.getStakeApr(assetId) ?: 0.0)
            }
            syncStake.await()
            syncAssetInfo.await()
            uiState.update { AssetInfoUIState.Idle() }
        }
    }

    private data class Model(
        val assetInfo: AssetInfo,
        val stakeApr: Double?,
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
                assetId = asset.id,
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
                isSwapEnabled = assetInfo.metadata?.isSwapEnabled ?: false,
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
                            "APR ${PriceUIState.formatPercentage(stakeApr ?: 0.0, false)}"
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



