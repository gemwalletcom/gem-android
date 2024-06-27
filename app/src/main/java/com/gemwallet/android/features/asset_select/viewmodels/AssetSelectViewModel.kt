package com.gemwallet.android.features.asset_select.viewmodels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.forEachTextValue
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.data.tokens.TokensRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.tokenAvailableChains
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class AssetSelectViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val tokensRepository: TokensRepository,
) : ViewModel() {
    val state = MutableStateFlow(AssetSelectViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AssetSelectUIState())
    val query = TextFieldState()

    private var queryJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val list = query.textAsFlow().flatMapLatest { query ->
        val session = sessionRepository.getSession() ?: throw IllegalArgumentException("Session doesn't found")
        assetsRepository.search(session.wallet, query.toString())
            .map { assets ->
                assets.sortedByDescending {
                    it.balances.available().convert(it.asset.decimals, it.price?.price ?: 0.0).atomicValue
                }
            }
            .flowOn(Dispatchers.IO)
    }

    init {
        viewModelScope.launch {
            list.collect { assets ->
                val session = sessionRepository.getSession()
                val availableAccounts = session?.wallet?.accounts?.map { it.chain } ?: emptyList()
                val isAddAssetAvailable =
                    tokenAvailableChains.any { availableAccounts.contains(it) }
                state.update {
                    it.copy(
                        assets = assets,
                        currency = session?.currency ?: Currency.USD,
                        isAddAssetAvailable = isAddAssetAvailable,
                    )
                }
            }
        }
    }

    fun setPredicate(predicate: (AssetInfo) -> Boolean) = viewModelScope.launch {
        state.update { it.copy(predicate = predicate) }
    }

    fun onChangeVisibility(assetId: AssetId, visible: Boolean) {
        state.update { state ->
            state.copy(
                assets = state.assets.map {
                    if (it.asset.id.toIdentifier() == assetId.toIdentifier()) {
                        it.copy(
                            metadata = it.metadata?.copy(isEnabled = visible)
                                ?: AssetMetaData(
                                    isEnabled = visible,
                                    isBuyEnabled = false, isSwapEnabled = false,
                                    isStakeEnabled = false
                                )
                        )
                    } else {
                        it
                    }
                }
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val session = sessionRepository.getSession() ?: return@withContext
                val account = session.wallet.getAccount(assetId.chain) ?: return@withContext
                assetsRepository.switchVisibility(account, assetId, visible, session.currency)
            }
        }
    }

    fun onQuery() = viewModelScope.launch(Dispatchers.IO) {
        query.forEachTextValue {
            if (queryJob?.isActive == true) {
                queryJob?.cancel()
            }
            queryJob = viewModelScope.launch {
                delay(250)
                state.update { it.copy(isLoading = true) }
                tokensRepository.search(it.toString())
                state.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class AssetSelectViewModelState(
    val isLoading: Boolean = false,
    val isAddAssetAvailable: Boolean = false,
    val error: String = "",
    val assets: List<AssetInfo> = emptyList(),
    val currency: Currency = Currency.USD,
    val predicate: (AssetInfo) -> Boolean = { true },
) {
    fun toUIState(): AssetSelectUIState = AssetSelectUIState(
        isLoading = isLoading,
        isAddAssetAvailable = isAddAssetAvailable,
        error = error,
        assets = assets
            .filter(predicate)
            .map { // TODO: Remove duplicated code
                AssetUIState(
                    id = it.asset.id,
                    name = it.asset.name,
                    icon = it.asset.getIconUrl(),
                    type = it.asset.type,
                    symbol = it.asset.symbol,
                    isZeroValue = it.balances.calcTotal().atomicValue == BigInteger.ZERO,
                    value = it.balances.calcTotal().format(it.asset.decimals, it.asset.symbol, 4),
                    price = PriceUIState.create(it.price, currency),
                    fiat = if (it.price == null || it.price.price == 0.0) {
                        ""
                    } else {
                        it.balances.calcTotal().convert(it.asset.decimals, it.price.price)
                            .format(0, currency.string, 2)
                    },
                    owner = it.owner.address,
                    metadata = it.metadata,
                )
            }.toImmutableList(),
    )
}

data class AssetSelectUIState(
    val isLoading: Boolean = false,
    val isAddAssetAvailable: Boolean = false,
    val error: String = "",
    val assets: ImmutableList<AssetUIState> = listOf<AssetUIState>().toImmutableList(),
)