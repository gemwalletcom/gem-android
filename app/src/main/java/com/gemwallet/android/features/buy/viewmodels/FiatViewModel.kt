package com.gemwallet.android.features.buy.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.buy.models.AmountValidator
import com.gemwallet.android.features.buy.models.BuyError
import com.gemwallet.android.features.buy.models.FiatSceneState
import com.gemwallet.android.features.buy.models.FiatSuggestion
import com.gemwallet.android.features.buy.models.toProviderUIModel
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatProvider
import com.wallet.core.primitives.FiatQuote
import com.wallet.core.primitives.FiatQuoteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FiatViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val buyRepository: BuyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currency = Currency.USD
    private val currencySymbol = java.util.Currency.getInstance(currency.name).symbol

    val type = MutableStateFlow(FiatQuoteType.Buy)
    val assetId = savedStateHandle.getStateFlow("assetId", "").mapNotNull { it.toAssetId() }

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> get() = _amount

    val asset = assetId.flatMapLatest {
        assetsRepository.getTokenInfo(it).mapNotNull { it }
    }
    .map {
        if (it.owner == null) {
            it.copy(owner = sessionRepository.getSession()?.wallet?.getAccount(it.asset.chain()))
        } else {
            it
        }
    }
    .map { AssetInfoUIModel(it, false, 6, -1) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val amountValidator = type.mapLatest {
        AmountValidator(
            when (it) {
                FiatQuoteType.Buy -> MIN_FIAT_AMOUNT
                FiatQuoteType.Sell -> 0.0
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AmountValidator(MIN_FIAT_AMOUNT))

    val suggestedAmounts = type.mapLatest {
        when (it) {
            FiatQuoteType.Buy -> listOf(
                FiatSuggestion.SuggestionAmount("${currencySymbol}100", 100.0),
                FiatSuggestion.SuggestionAmount("${currencySymbol}250", 250.0),
                FiatSuggestion.RandomAmount,
            )

            FiatQuoteType.Sell -> listOf(
                FiatSuggestion.SuggestionPercent("25%", 25.0),
                FiatSuggestion.SuggestionPercent("50%", 50.0),
                FiatSuggestion.MaxAmount
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val defaultAmount = type.mapLatest {
        val value = when (it) {
            FiatQuoteType.Buy -> "50"
            FiatQuoteType.Sell -> "0"
        }
        _amount.update { value }
        value
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "50")

    private val _state = MutableStateFlow<FiatSceneState?>(null)

    val state: StateFlow<FiatSceneState?> get() = _state
    private val _selectedQuote = MutableStateFlow<FiatQuote?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val quotes = combine(asset, type, amount, amountValidator) { asset, type, amount, validator ->
        asset ?: return@combine emptyList()
        if (!validator.validate(amount)) {
            _state.value = FiatSceneState.Error(validator.error)
            return@combine emptyList()
        } else {
            _state.value = FiatSceneState.Loading
        }
        val result = try {
            val quotes = buyRepository.getQuotes(
                asset.asset,
                type = type,
                currency.string,
                amount.numberParse().toDouble(),
                asset.assetInfo.owner?.address ?: ""
            )
            _state.value = null
            quotes.sortedByDescending { quote -> quote.cryptoAmount }
        } catch (_: Exception) {
            _state.value = FiatSceneState.Error(BuyError.QuoteNotAvailable)
            emptyList()
        }
        result
    }
    .onEach { quotes ->
        _selectedQuote.update { quotes.firstOrNull() }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    val providers = combine(asset.filterNotNull(), quotes) { asset, quotes ->
        quotes.map { quote ->
            quote.toProviderUIModel(asset.asset, currency)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedProvider = combine(asset, _selectedQuote) { asset, quote ->
        return@combine asset?.let { quote?.toProviderUIModel(asset.asset, currency) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

            is FiatSuggestion.MaxAmount -> asset.value?.cryptoAmount?.toString() ?: ""
        }
    }

    fun setProvider(provider: FiatProvider) {
        _selectedQuote.value = quotes.value.firstOrNull { it.provider.name == provider.name }
    }

    fun setType(type: FiatQuoteType) {
        _amount.update { "0" }
        this.type.update { type }
    }

    private fun randomAmount(maxAmount: Double = 1000.0): Int {
        return when (type.value) {
            FiatQuoteType.Buy -> Random.nextInt(defaultAmount.value.toInt(), maxAmount.toInt())
            FiatQuoteType.Sell -> return 0
        }
    }

    companion object {
        const val MIN_FIAT_AMOUNT = 20.0
    }
}