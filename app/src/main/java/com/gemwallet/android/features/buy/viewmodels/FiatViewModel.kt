package com.gemwallet.android.features.buy.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.buy.models.BuyError
import com.gemwallet.android.features.buy.models.toProviderUIModel
import com.gemwallet.android.math.numberParse
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

@HiltViewModel
class FiatViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val buyRepository: BuyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val type: FiatTransactionType = FiatTransactionType.Buy
    private val amountValidator = AmountValidator(MIN_FIAT_AMOUNT)
    private val currency = Currency.USD

    private val assetId = savedStateHandle.getStateFlow("assetId", "")
        .mapNotNull { it.toAssetId() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val asset = assetId.flatMapLatest { assetsRepository.getAssetInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
        _amount.value = when (type) {
            FiatTransactionType.Buy -> 50
            FiatTransactionType.Sell -> 0
        }.toString()
    }

    @OptIn(FlowPreview::class)
    private fun observeAmountUpdates() = viewModelScope.launch {
        _amount
            .debounce(500)
            .map { newAmount ->
                if (!amountValidator.validate(newAmount)) {
                    FiatSceneState.Error(amountValidator.error)
                } else {
                    FiatSceneState.Loading
                }
            }
            .collect { state ->
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
                asset.owner.address
            ).onSuccess { result ->
                _selectedQuote.update { result.firstOrNull() }
                _quotes.update { result }
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

    fun setProvider(provider: FiatProvider) {
        _selectedQuote.value = _quotes.value.firstOrNull { it.provider.name == provider.name }
    }

    companion object {
        const val MIN_FIAT_AMOUNT = 20.0
    }
}

sealed interface FiatSceneState {
    data object Loading : FiatSceneState
    data class Error(val error: BuyError?) : FiatSceneState
}

internal class AmountValidator(private val minValue: Double) {
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