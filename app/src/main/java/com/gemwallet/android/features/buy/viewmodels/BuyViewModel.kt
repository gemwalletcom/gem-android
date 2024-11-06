package com.gemwallet.android.features.buy.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.features.buy.models.BuyError
import com.gemwallet.android.features.buy.models.BuyUIState
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.SignMode
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
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
import javax.inject.Inject

@HiltViewModel
class BuyViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val buyRepository: BuyRepository,
) : ViewModel() {
    private val state = MutableStateFlow(BuyViewModelState())
    val uiState: StateFlow<BuyUIState> = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BuyUIState.Idle())

    private var queryJob: Job? = null
    var amount by mutableStateOf("")
        private set

    suspend fun init(assetId: AssetId) {
        val session = sessionRepository.getSession()
        if (session == null) {
            state.update { it.copy(fatalError = "no session") }
            return
        }
        val assetInfo = assetsRepository.getById(session.wallet, assetId).firstOrNull()
        if (assetInfo == null) {
            state.update { it.copy(fatalError = "Asset not found") }
            return
        }
        state.update { BuyViewModelState(assetInfo = assetInfo) }
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
                fiatCurrency = currency.string,
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
        state.update { it.copy(isQuoteLoading = false, error = null) }
        val newValue = try {
            input.ifEmpty { "0.0" }.numberParse().toDouble()
        } catch (_: Throwable) {
            state.update { it.copy(error = BuyError.ValueIncorrect) }
            return
        }
        if (newValue < MIN_FIAT_AMOUNT) {
            state.update { it.copy(error = BuyError.MinimumAmount) }
            return
        }
        if (state.value.fiatAmount == newValue) {
            return
        }
        state.update { it.copy(isQuoteLoading = true) }
        queryJob = viewModelScope.launch {
            delay(500)
            fiatAmount(newValue)
        }
    }

    private data class BuyViewModelState(
        val isQuoteLoading: Boolean = false,
        val assetInfo: AssetInfo? = null,
        val currency: Currency = Currency.USD,
        val fiatAmount: Double = 50.0,
        val quotes: List<FiatQuote> = emptyList(),
        val selectProvider: FiatProvider? = null,
        val error: BuyError? = null,
        val fatalError: String? = null,
    ) {
        fun toUIState(): BuyUIState = if (fatalError == null && assetInfo != null) {
            val quote = getQuote(selectProvider)
            BuyUIState.Idle(
                isQuoteLoading = isQuoteLoading,
                asset = AssetInfoUIModel(assetInfo),
                cryptoAmount = if (quote == null) {
                    " "
                } else {
                    "~${assetInfo.asset.format(quote.cryptoAmount, showSign = SignMode.NoSign, dynamicPlace = true)}"
                },
                fiatAmount = "${fiatAmount.toInt()}",
                currentProvider = if (quote != null) mapToProvider(quote, assetInfo.asset) else null,
                providers = quotes.map {
                    mapToProvider(it, assetInfo.asset)
                },
                redirectUrl = quote?.redirectUrl,
                error = error,
            )
        } else {
            BuyUIState.Fatal(message = fatalError ?: "")
        }

        private fun mapToProvider(quote: FiatQuote, asset: Asset): BuyUIState.Provider {
            return BuyUIState.Provider(
                provider = quote.provider,
                cryptoAmount = asset.format(quote.cryptoAmount, 6, SignMode.NoSign, true),
                rate = "1 ${asset.symbol} ~ ${currency.format(quote.fiatAmount / quote.cryptoAmount).format("USD", 2)}"
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

    companion object {
        const val MIN_FIAT_AMOUNT = 20.0
    }
}