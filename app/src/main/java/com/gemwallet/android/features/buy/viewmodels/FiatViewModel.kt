package com.gemwallet.android.features.buy.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.buy.models.BuyError
import com.gemwallet.android.features.buy.models.toProviderUIModel
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatProvider
import com.wallet.core.primitives.FiatQuote
import com.wallet.core.primitives.FiatTransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class FiatViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val buyRepository: BuyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val type: FiatTransactionType = FiatTransactionType.Buy // now always buy
    val assetId = savedStateHandle.getStateFlow("assetId", "").mapNotNull { it.toAssetId() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val asset =
        assetId.flatMapLatest { assetsRepository.getAssetInfo(it) }.map { AssetInfoUIModel(it, 6, -1) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val amountValidator = AmountValidator(
        when (type) {
            FiatTransactionType.Buy -> MIN_FIAT_AMOUNT
            FiatTransactionType.Sell -> 0.0
        }
    )
    private val currency = Currency.USD
    private val currencySymbol = java.util.Currency.getInstance(currency.name).symbol

    val suggestedAmounts: List<FiatSuggestion>
        get() = when (type) {
            FiatTransactionType.Buy -> listOf(
                FiatSuggestion.SuggestionAmount("${currencySymbol}100", 100.0),
                FiatSuggestion.SuggestionAmount("${currencySymbol}250", 250.0),
                FiatSuggestion.RandomAmount,
            )

            FiatTransactionType.Sell -> listOf(
                FiatSuggestion.SuggestionPercent("25%", 25.0),
                FiatSuggestion.SuggestionPercent("50%", 50.0),
                FiatSuggestion.MaxAmount
            )
        }

    val defaultAmount: String
        get() = when (type) {
            FiatTransactionType.Buy -> "50"
            FiatTransactionType.Sell -> "0"
        }

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> get() = _amount

    init {
        setDefaultAmount()
        observeAmountUpdates()
    }

    private val _state = MutableStateFlow<FiatSceneState?>(null)
    val state: StateFlow<FiatSceneState?> get() = _state

    private val _selectedQuote = MutableStateFlow<FiatQuote?>(null)
    val selectedQuote: StateFlow<FiatQuote?> get() = _selectedQuote

    private val _quotes = MutableStateFlow<List<FiatQuote>>(emptyList())
    val quotes: StateFlow<List<FiatQuote>> get() = _quotes

    val providers = combine(asset.filterNotNull(), quotes) { asset, quotes ->
        quotes.map { quote ->
            quote.toProviderUIModel(asset.asset, currency)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedProvider = combine(asset, _selectedQuote) { asset, quote ->
        return@combine asset?.let {
            quote?.toProviderUIModel(asset.asset, currency)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun setDefaultAmount() {
        _amount.value = defaultAmount
    }

    @OptIn(FlowPreview::class)
    private fun observeAmountUpdates() = viewModelScope.launch {
        _amount.debounce(500).map { newAmount ->
            if (!amountValidator.validate(newAmount)) {
                FiatSceneState.Error(amountValidator.error)
            } else {
                FiatSceneState.Loading
            }
        }.collect { state ->
            _state.value = state
            if (state is FiatSceneState.Loading) {
                fetchQuotes()
            }
        }
    }

    private fun fetchQuotes() = viewModelScope.launch {
        val asset = asset.value ?: return@launch
        try {
            buyRepository.getBuyQuotes(
                asset.asset,
                currency.string,
                amount.value.numberParse().toDouble(),
                asset.assetInfo.owner.address
            ).onSuccess { result ->
                val sortedResult = result.sortedByDescending { quote -> quote.cryptoAmount }
                _selectedQuote.update { sortedResult.firstOrNull() }
                _quotes.update { sortedResult }
                _state.value = null
            }.onFailure {
                _state.value = FiatSceneState.Error(BuyError.QuoteNotAvailable)
            }
        } catch (e: Exception) {
            _state.value = FiatSceneState.Error(BuyError.QuoteNotAvailable)
        }
    }

    fun updateAmount(newAmount: String) {
        _amount.value = newAmount
    }

    fun updateAmount(suggestion: FiatSuggestion) {
        _amount.value = when (suggestion) {
            FiatSuggestion.RandomAmount -> randomAmount().toString()
            is FiatSuggestion.SuggestionAmount -> suggestion.value.toInt().toString()
            is FiatSuggestion.SuggestionPercent -> {
                val amount = asset.value?.cryptoAmount ?: 0.0
                (amount * (suggestion.value / 100.0)).toString()
            }

            is FiatSuggestion.MaxAmount -> asset.value?.cryptoFormatted ?: ""
        }
    }

    fun setProvider(provider: FiatProvider) {
        _selectedQuote.value = _quotes.value.firstOrNull { it.provider.name == provider.name }
    }

    private fun randomAmount(maxAmount: Double = 1000.0): Int {
        return when (type) {
            FiatTransactionType.Buy -> Random.nextInt(defaultAmount.toInt(), maxAmount.toInt())
            FiatTransactionType.Sell -> return 0
        }
    }

    companion object {
        const val MIN_FIAT_AMOUNT = 20.0
    }
}

sealed interface FiatSceneState {
    data object Loading : FiatSceneState
    data class Error(val error: BuyError?) : FiatSceneState
}

sealed class FiatSuggestion(open val text: String, open val value: Double) {
    class SuggestionAmount(override val text: String, override val value: Double) :
        FiatSuggestion(text, value)

    data class SuggestionPercent(override val text: String, override val value: Double) :
        FiatSuggestion(text, value)

    data object RandomAmount : FiatSuggestion("Random", 0.0)

    data object MaxAmount : FiatSuggestion("Max", 100.0)
}

private class AmountValidator(private val minValue: Double) {
    var error: BuyError? = null
        private set

    fun validate(input: String): Boolean {
        error = null
        val value = try {
            input.ifEmpty { "0.0" }.numberParse().toDouble()
        } catch (_: Throwable) {
            BuyError.ValueIncorrect.also { error = it }
            return false
        }
        if (value < minValue) {
            BuyError.MinimumAmount.also { error = it }
            return false
        }
        return true
    }
}