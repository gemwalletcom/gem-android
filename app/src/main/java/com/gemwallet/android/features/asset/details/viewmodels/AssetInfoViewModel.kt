package com.gemwallet.android.features.asset.details.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.session.OnSessionChange
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.isStaked
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset.navigation.assetIdArg
import com.gemwallet.android.features.assets.model.PriceState
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BalanceType
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
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
) : ViewModel(), OnSessionChange {

    val assetId: StateFlow<String> = savedStateHandle.getStateFlow(assetIdArg, "")

    private val state = MutableStateFlow(AssetInfoViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AssetInfoSceneState.Loading)

    init {
        sessionRepository.subscribe(this)
        transactionsRepository.subscribe(this::onTransactions)
    }

    fun init(assetId: AssetId) {
        prepareAsset(assetId)
        syncAssetInfo(assetId)
    }

    fun refresh() {
        syncAssetInfo(state.value.assetInfo?.asset?.id ?: return)
    }

    private fun prepareAsset(assetId: AssetId) {
        val session = sessionRepository.session ?: return
        viewModelScope.launch {
            val assetInfo = assetsRepository.getById(session.wallet, assetId).getOrNull()?.firstOrNull()
            if (assetInfo == null) {
                handleFatalError(AssetStateError.AssetNotFound)
                return@launch
            }
            val transactions = transactionsRepository.getTransactions(assetId, assetInfo.owner)
                .flowOn(Dispatchers.IO)
                .firstOrNull() ?: emptyList()
            val stakeApr = if (StakeChain.isStaked(assetId.chain)) {
                 assetsRepository.getStakeApr(assetId)
            } else {
                null
            }
            state.update { state ->
                state.copy(
                    loading = false,
                    currency = session.currency,
                    walletType = session.wallet.type,
                    assetInfo = assetInfo,
                    stakeApr = stakeApr,
                    transactions = transactions,
                )
            }
        }
    }

    private fun syncAssetInfo(assetId: AssetId) {
        state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val session = sessionRepository.session ?: return@launch
            val account = session.wallet.getAccount(assetId.chain) ?: return@launch
            assetsRepository.syncAssetInfo(account, assetId, session.currency)
            launch(Dispatchers.IO) {
                stakeRepository.sync(assetId.chain, account.address, assetsRepository.getStakeApr(assetId) ?: 0.0)
            }
            prepareAsset(assetId)
        }
    }

    override fun onSessionChange(session: Session?) {
        init(state.value.assetInfo?.asset?.id ?: return)
    }

    private fun onTransactions() {
        init(state.value.assetInfo?.asset?.id ?: return)
    }

    private fun handleFatalError(error: AssetStateError) {
        state.update {
            AssetInfoViewModelState(fatalError = error)
        }
    }

    fun reset() {
        state.update { AssetInfoViewModelState() }
    }
}

data class AssetInfoViewModelState(
    val loading: Boolean = true,
    val currency: Currency = Currency.USD,
    val walletType: WalletType = WalletType.view,
    val assetInfo: AssetInfo? = null,
    val stakeApr: Double? = null,
    val transactions: List<TransactionExtended> = emptyList(),
    val fatalError: AssetStateError? = null,
) {
    fun toUIState(): AssetInfoSceneState {
        return when {
            fatalError != null -> AssetInfoSceneState.Error(fatalError)
            assetInfo == null && loading -> AssetInfoSceneState.Loading
            else -> {
                val assetInfo = assetInfo ?: return AssetInfoSceneState.Error(AssetStateError.AssetNotFound)
                val price = assetInfo.price?.price ?: 0.0
                val asset = assetInfo.asset
                val balances = assetInfo.balances
                val total = balances.calcTotal()
                val fiatTotal = if (assetInfo.price == null) {
                    null
                } else {
                    total.convert(asset.decimals, assetInfo.price.price)
                }
                val stakeBalance = balances.items.filter {
                    it.balance.type != BalanceType.available && it.balance.type != BalanceType.reserved
                }
                    .map { BigInteger(it.balance.value) }
                    .fold(BigInteger.ZERO) {acc, value -> acc + value }
                val reservedBalance = balances.items.filter { it.balance.type == BalanceType.reserved }
                    .map { BigInteger(it.balance.value) }
                    .fold(BigInteger.ZERO) {acc, value -> acc + value }

                AssetInfoSceneState.Success(
                    assetId = asset.id,
                    loading = loading,
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
                        assetInfo.price?.priceChangePercentage24h ?: 0.0
                    ),
                    priceChangedType = PriceUIState.getState(
                        assetInfo.price?.priceChangePercentage24h ?: 0.0
                    ),
                    tokenType = asset.type,
                    networkTitle = "${asset.id.chain.asset().name} (${asset.type.string})",
                    networkIcon = AssetId(asset.id.chain).getIconUrl(),
                    isBuyEnabled = assetInfo.metadata?.isBuyEnabled ?: false,
                    isSwapEnabled = assetInfo.metadata?.isSwapEnabled ?: false,
                    transactions = transactions,
                    account = AssetInfoSceneState.Account(
                        walletType = walletType,
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
}

sealed interface AssetInfoSceneState {
    data object Loading : AssetInfoSceneState

    class Error(val error: AssetStateError) : AssetInfoSceneState

    class Success(
        val assetId: AssetId,
        val loading: Boolean = false,
        val name: String = "",
        val iconUrl: String = "",
        val priceValue: String = "0",
        val priceDayChanges: String = "0",
        val priceChangedType: PriceState = PriceState.Up,
        val tokenType: AssetType = AssetType.NATIVE,
        val networkTitle: String,
        val networkIcon: String,
        val account: Account = Account(),
        val isBuyEnabled: Boolean = false,
        val isSwapEnabled: Boolean = false,
        val transactions: List<TransactionExtended> = emptyList(),
    ) : AssetInfoSceneState

    data class Account(
        val walletType: WalletType = WalletType.view,
        val totalBalance: String = "0",
        val totalFiat: String = "0",
        val owner: String = "",
        val hasBalanceDetails: Boolean = false,
        val available: String = "0",
        val stake: String = "0",
        val reserved: String = "0",
    )
}

sealed interface AssetStateError {
    data object AssetNotFound : AssetStateError
}