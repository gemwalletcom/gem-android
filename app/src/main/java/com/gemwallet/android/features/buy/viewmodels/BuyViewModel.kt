package com.gemwallet.android.features.buy.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.buy.BuyRepository
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.CountingUnit
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.FiatProvider
import com.wallet.core.primitives.FiatQuote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class BuyViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val buyRepository: BuyRepository,
) : ViewModel() {
    val state = MutableStateFlow(BuyViewModelState())
    val uiState: StateFlow<BuyUIState> = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BuyUIState.Success())

    private var queryJob: Job? = null
    var amount by mutableStateOf("")
        private set

    suspend fun init(assetId: AssetId) {
        val session = sessionRepository.session
        if (session == null) {
            state.update { it.copy(fatalError = "no session") }
            return
        }
        val assetInfo = assetsRepository.getById(session.wallet, assetId).getOrNull()?.firstOrNull()
        if (assetInfo == null) {
            state.update { it.copy(fatalError = "Asset not found") }
        }
        state.update {
            BuyViewModelState(
                isLoading = false,
                assetInfo = assetInfo
            )
        }
        amount = state.value.fiatAmount.toInt().toString()
        fiatAmount(state.value.fiatAmount)
    }

    private fun fiatAmount(fiatAmount: Double) {
        viewModelScope.launch {
            state.update {
                it.copy(isQuoteLoading = true, fiatAmount = fiatAmount, quotes = emptyList())
            }
            val assetSummary = state.value.assetInfo ?: return@launch
            val currency = state.value.currency

            buyRepository.getQuote(
                asset = assetSummary.asset,
                fiatCurrency = currency,
                fiatAmount = fiatAmount,
                owner = assetSummary.owner.address,
            ).onFailure {
                state.update {
                    it.copy(isQuoteLoading = false, error = BuyError.QuoteNotAvailable)
                }
            }.onSuccess { quotes ->
                state.update {
                    it.copy(
                        isQuoteLoading = false,
                        quotes = quotes.sortedByDescending { quote -> quote.cryptoAmount },
                        error = null,
                    )
                }
            }
        }
    }

    fun setProvider(provider: FiatProvider) {
        state.update { it.copy(selectProvider = provider) }
    }

    fun updateAmount(input: String) {
        amount = input
        if (queryJob?.isActive == true) {
            queryJob?.cancel()
        }
        if (input.isEmpty()) {
            state.update { it.copy(error = BuyError.MinimumAmount) }
            return
        }
        val newValue = try {
            input.numberParse().toDouble()
        } catch (err: Throwable) {
            state.update { it.copy(error = BuyError.ValueIncorrect) }
            return
        }
        if (newValue < 20) {
            state.update { it.copy(error = BuyError.MinimumAmount) }
            return
        }
        if (state.value.fiatAmount == newValue) {
            return
        }
        state.update { it.copy(isQuoteLoading = input.isNotEmpty()) }
        if (queryJob?.isActive == true) {
            queryJob?.cancel()
        }
        queryJob = viewModelScope.launch {
            delay(500)
            fiatAmount(newValue.toDouble())
        }
    }
}

data class BuyViewModelState(
    val isLoading: Boolean = true,
    val isQuoteLoading: Boolean = false,
    val assetInfo: AssetInfo? = null,
    val currency: String = "USD",
    val fiatAmount: Double = 50.0,
    val quotes: List<FiatQuote> = emptyList(),
    val selectProvider: FiatProvider? = null,
    val redirectUrl: String? = null,
    val error: BuyError? = null,
    val fatalError: String? = null,
) {
    fun toUIState(): BuyUIState = if (fatalError == null) {
        val symbol = assetInfo?.asset?.symbol ?: ""
        val decimals = assetInfo?.asset?.decimals ?: 0
        val chain = assetInfo?.asset?.id?.chain
        val quote = getQuote(selectProvider)
        BuyUIState.Success(
            isLoading = isLoading,
            isQuoteLoading = isQuoteLoading,
            asset = assetInfo?.asset,
            title = assetInfo?.asset?.name ?: "",
            assetType = chain?.asset()?.type ?: AssetType.NATIVE,
            cryptoAmount = if (quote == null) {
                " "
            } else {
                "~${
                    Crypto(quote.cryptoAmount.toBigDecimal(), decimals).format(
                        decimals,
                        symbol,
                        6,
                        CountingUnit.SignMode.NoSign,
                        true
                    )
                }"
            },
            fiatAmount = "${fiatAmount.toInt()}",
            selectProvider = if (quote != null) mapToProvider(quote, symbol, decimals) else null,
            providers = quotes.map {
                mapToProvider(it, symbol, decimals)
            },
            redirectUrl = quote?.redirectUrl,
            error = error,
        )
    } else {
        BuyUIState.Fatal(message = fatalError)
    }

    private fun mapToProvider(quote: FiatQuote, symbol: String, decimals: Int): BuyUIState.Provider {
        return BuyUIState.Provider(
            provider = quote.provider,
            cryptoAmount = Crypto(quote.cryptoAmount.toBigDecimal(), decimals).format(
                decimals,
                symbol,
                6,
                CountingUnit.SignMode.NoSign,
                true
            ),
            rate = "1 $symbol ~ ${
                Fiat(BigDecimal(quote.fiatAmount / quote.cryptoAmount)).format(
                    0,
                    "USD",
                    2
                )
            }"
        )
    }

    private fun getQuote(provider: FiatProvider?): FiatQuote? {
        if (quotes.isEmpty()) {
            return null
        }
        if (provider == null) {
            return quotes.firstOrNull()
        }
        return quotes.firstOrNull{ it.provider == provider } ?: quotes.firstOrNull()
    }
}

sealed interface BuyUIState {
    data class Success(
        val isLoading: Boolean = false,
        val isQuoteLoading: Boolean = false,
        val asset: Asset? = null,
        val title: String = "",
        val assetType: AssetType = AssetType.NATIVE,
        val cryptoAmount: String = "",
        val fiatAmount: String = "",
        val selectProvider: Provider? = null,
        val redirectUrl: String? = null,
        val providers: List<Provider> = emptyList(),
        val error: BuyError? = null,
    ) : BuyUIState

    data class Fatal(
        val message: String = "",
    ) : BuyUIState

    data class BuyLot(
        val title: String,
        val value: Double,
    )

    data class Provider(
        val provider: FiatProvider,
        val cryptoAmount: String,
        val rate: String,
    )
}

sealed interface BuyError {
    data object MinimumAmount : BuyError

    data object QuoteNotAvailable : BuyError

    data object ValueIncorrect : BuyError
}