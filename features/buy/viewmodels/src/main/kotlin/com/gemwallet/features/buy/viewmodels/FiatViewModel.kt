package com.gemwallet.features.buy.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.math.parseNumber
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.RecentType
import com.gemwallet.android.ui.components.list_item.AssetInfoUIModel
import com.gemwallet.features.buy.viewmodels.models.AmountValidator
import com.gemwallet.features.buy.viewmodels.models.BuyError
import com.gemwallet.features.buy.viewmodels.models.FiatSceneState
import com.gemwallet.features.buy.viewmodels.models.FiatSuggestion
import com.gemwallet.features.buy.viewmodels.models.toProviderUIModel
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatProvider
import com.wallet.core.primitives.FiatQuote
import com.wallet.core.primitives.FiatQuoteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class FiatViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val buyRepository: BuyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currency = Currency.USD
    private val currencySymbol = java.util.Currency.getInstance(currency.name).symbol

    val type = MutableStateFlow(FiatQuoteType.Buy)
    val assetId = savedStateHandle.getStateFlow("assetId", "").mapNotNull { it.toAssetId() }
    val session = sessionRepository.session()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> get() = _amount

    val assetInfoUIModel = combine(session, assetId) { session, assetId ->
        Pair(session, assetId)
    }
    .flatMapLatest { data ->
        val (session, assetId) = data
        assetsRepository.getTokenInfo(assetId)
            .mapNotNull { it }
            .map { assetInfo ->
                if (assetInfo.owner == null) {
                    assetInfo.copy(owner = session?.wallet?.getAccount(assetInfo.asset.chain))
                } else {
                    assetInfo
                }
            }
    }
    .flowOn(Dispatchers.IO)
    .map {
        object : AssetInfoUIModel(it, false, 2, 4) {
            override val cryptoAmount: Double
                get() = assetInfo.balance.balanceAmount.available
        }
    }
    .flowOn(Dispatchers.Default)
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
        listOf(
            FiatSuggestion.SuggestionAmount("${currencySymbol}100", 100.0),
            FiatSuggestion.SuggestionAmount("${currencySymbol}250", 250.0),
            FiatSuggestion.RandomAmount,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val defaultAmount = type.mapLatest {
        val value = when (it) {
            FiatQuoteType.Buy -> "50"
            FiatQuoteType.Sell -> "50"
        }
        _amount.update { value }
        value
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "50")

    private val _state = MutableStateFlow<FiatSceneState?>(null)

    val state: StateFlow<FiatSceneState?> get() = _state
    private val _selectedQuote = MutableStateFlow<FiatQuote?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val quotes = combine(assetInfoUIModel, type, amount, amountValidator) { assetInfo, type, amount, validator ->
        assetInfo ?: return@combine emptyList()
        if (!validator.validate(amount)) {
            _state.value = FiatSceneState.Error(validator.error)
            return@combine emptyList()
        } else {
            _state.value = FiatSceneState.Loading
        }
        val amountParsed = amount.parseNumber().toDouble()
        val crypto = assetInfo.price.fiat?.let { Fiat(BigDecimal(amountParsed)).convert(assetInfo.asset.decimals, it).atomicValue } ?: BigInteger.ZERO
        if (type == FiatQuoteType.Sell && crypto > assetInfo.assetInfo.balance.balance.available.toBigInteger()) {
            _state.value = FiatSceneState.Error(BuyError.InsufficientBalance)
            return@combine emptyList()
        }
        val result = try {
            val quotes = buyRepository.getQuotes(
                walletId = assetInfo.assetInfo.walletId ?: return@combine emptyList(),
                asset = assetInfo.asset,
                type = type,
                fiatCurrency = currency.string,
                amount = amountParsed,
            )
            _state.value = null
            if (quotes.isEmpty()) {
                throw Exception()
            }
            quotes.sortedByDescending { quote -> quote.cryptoAmount }
        } catch (err: Exception) {
            Log.d("FIAT", "Err", err)
            _state.value = FiatSceneState.Error(BuyError.QuoteNotAvailable)
            emptyList()
        }
        result
    }
    .onEach { quotes ->
        _selectedQuote.update { quotes.firstOrNull() }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    val providers = combine(assetInfoUIModel.filterNotNull(), quotes) { asset, quotes ->
        quotes.map { quote ->
            quote.toProviderUIModel(asset.asset, currency)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedProvider = combine(assetInfoUIModel, _selectedQuote) { asset, quote ->
        return@combine asset?.let { quote?.toProviderUIModel(asset.asset, currency) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateAmount(newAmount: String) {
        _amount.value = newAmount
    }

    fun updateAmount(suggestion: FiatSuggestion) {
        _amount.value = when (suggestion) {
            FiatSuggestion.RandomAmount -> randomAmount().toString()
            is FiatSuggestion.SuggestionAmount -> suggestion.value.toInt().toString()
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
        return Random.nextInt(defaultAmount.value.toInt(), maxAmount.toInt())
    }

    fun getUrl(callback: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            addRecent()
            val url = buyRepository.getQuoteUrl(
                quoteId = _selectedQuote.value?.id ?: return@launch,
                walletId = assetInfoUIModel.value?.assetInfo?.walletId ?: return@launch,
            )
            callback(url)
        }
    }

    private fun addRecent() = viewModelScope.launch(Dispatchers.IO) {
        val assetInfo = assetInfoUIModel.value?.assetInfo ?: return@launch
        val walletId = assetInfo.walletId ?: return@launch
        assetsRepository.addRecentActivity(assetInfo.id(), walletId, RecentType.Buy)
    }

    companion object {
        const val MIN_FIAT_AMOUNT = 20.0
    }
}