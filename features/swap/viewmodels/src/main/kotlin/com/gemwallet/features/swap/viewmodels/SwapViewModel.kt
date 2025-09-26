package com.gemwallet.features.swap.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
import com.gemwallet.android.domains.asset.calculateFiat
import com.gemwallet.android.domains.asset.formatFiat
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.math.parseNumberOrNull
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.model.toModel
import com.gemwallet.features.swap.viewmodels.cases.QuoteRequester
import com.gemwallet.features.swap.viewmodels.cases.calculatePriceImpact
import com.gemwallet.features.swap.viewmodels.cases.getProviders
import com.gemwallet.features.swap.viewmodels.cases.getSlippage
import com.gemwallet.features.swap.viewmodels.cases.tickerFlow
import com.gemwallet.features.swap.viewmodels.models.QuoteState
import com.gemwallet.features.swap.viewmodels.models.SwapError
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import com.gemwallet.features.swap.viewmodels.models.SwapState
import com.gemwallet.features.swap.viewmodels.models.create
import com.gemwallet.features.swap.viewmodels.models.estimateTime
import com.gemwallet.features.swap.viewmodels.models.formattedToAmount
import com.gemwallet.features.swap.viewmodels.models.getQuote
import com.gemwallet.features.swap.viewmodels.models.rates
import com.gemwallet.features.swap.viewmodels.models.receiveEquivalent
import com.gemwallet.features.swap.viewmodels.models.validate
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.gemstone.SwapperProvider
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import kotlin.math.max

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SwapViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val swapRepository: SwapRepository,
    private val quoteRequester: QuoteRequester,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val swapScreenState = MutableStateFlow<SwapState>(SwapState.None)

    val payValue: TextFieldState = TextFieldState()
    val receiveValue: TextFieldState = TextFieldState()

    private val payValueFlow = snapshotFlow { payValue.text }
        .map { it.toString() }
        .map { it.parseNumberOrNull() ?: BigDecimal.ZERO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    val selectedProvider = MutableStateFlow<SwapperProvider?>(null)

    private val refresh = MutableStateFlow(0L)
    private val ticker = payValueFlow.flatMapLatest { tickerFlow(it) }
        .flowOn(Dispatchers.Default)
    private val refreshState = combine(refresh, ticker) { user, ticker ->
        max(user, ticker)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val payAsset = savedStateHandle.getStateFlow<String?>("from", null)
        .map { it?.toAssetId() }
        .onEach { id -> id?.let { updateBalance(it) } }
        .flatMapLatest { assetId -> assetId?.let { assetsRepository.getAssetInfo(it) } ?: flow { emit(null) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val receiveAsset = savedStateHandle.getStateFlow<String?>("to", null)
        .map { it?.toAssetId() }
        .onEach { id -> id?.let { updateBalance(it) } }
        .flatMapLatest { assetId -> assetId?.let { assetsRepository.getAssetInfo(it) } ?: flow { emit(null) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val payEquivalentFormatted = combine(payValueFlow, payAsset) { input, fromAsset ->
            fromAsset?.let {
                val equivalentValue = it.calculateFiat(input)
                it.formatFiat(equivalentValue)
            } ?: ""
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val quotes = quoteRequester.requestQuotes(
        payValueFlow,
        payAsset,
        receiveAsset,
        refreshState,
         {
            if (it == null) {
                swapScreenState.update { SwapState.None }
            } else {
                swapScreenState.update { SwapState.GetQuote }
            }
        }
    ) { err ->
        resetReceive()
        swapScreenState.update { SwapState.Error.create(err) }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val providers = quotes.mapLatest { quotes -> quotes?.getProviders() ?: emptyList() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val quote = quotes.combine(selectedProvider) { quotes, provider ->
            quotes?.getQuote(provider)?.let { QuoteState(it, quotes.pay, quotes.receive) }
        }
        .onEach { state -> setReceive(state?.formattedToAmount ?: "") }
        .onEach { state -> swapScreenState.update { state?.validate() ?: SwapState.None } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val rate = quote.mapLatest { it?.rates }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val estimateTime = quote.mapLatest { it?.estimateTime }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentProvider = quote.mapLatest { SwapProviderItem(it?.quote?.data?.provider ?: return@mapLatest null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val toEquivalentFormatted = quote.mapLatest { quote ->
            quote?.receive?.formatFiat(quote.receiveEquivalent)
            quote?.receive
                ?.price?.takeIf { it.price.price > 0 }
                ?.currency?.format(quote.receiveEquivalent)
                ?: ""
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val priceImpact = combine( quote, swapScreenState) { quote, state ->
            if (quote == null || state != SwapState.Ready) {
                return@combine null
            }
            calculatePriceImpact(quote)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val minReceive = combine( quote, swapScreenState) { quote, state ->
            if (quote == null || state != SwapState.Ready) {
                return@combine null
            }
            val minReceive = Crypto(quote.quote.toValue).atomicValue.toBigDecimal().let {
                it - (it * BigDecimal.valueOf(quote.quote.data.slippageBps.toDouble() / 100.0 / 100.0))
            }.toBigInteger()
            quote.receive.asset.format(Crypto(minReceive), 2, dynamicPlace = true)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val slippage = combine( quote, swapScreenState) { quote, state ->
            if (quote == null || state != SwapState.Ready) {
                return@combine null
            }
            getSlippage(quote)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiSwapScreenState = swapScreenState
        .onEach {
            when (it) {
                SwapState.None -> resetReceive()
                is SwapState.Error,
                SwapState.Approving,
                SwapState.CheckAllowance,
                SwapState.GetQuote,
                SwapState.Ready,
                SwapState.Swapping -> {}
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SwapState.None)

    fun onSelect(type: SwapItemType, assetId: AssetId) {
        when (type) {
            SwapItemType.Pay -> {
                if (receiveAsset.value?.id() == assetId) {
                    savedStateHandle["to"] = null
                }
                savedStateHandle["from"] = assetId.toIdentifier()
            }
            SwapItemType.Receive -> {
                if (payAsset.value?.id() == assetId) {
                    savedStateHandle["from"] = null
                }
                savedStateHandle["to"] = assetId.toIdentifier()
            }
        }
    }

    fun switchSwap() = viewModelScope.launch {
        val payAssetId = payAsset.value?.id()?.toIdentifier()
        val receiveAssetId = receiveAsset.value?.id()?.toIdentifier()
        savedStateHandle["from"] = receiveAssetId
        savedStateHandle["to"] = payAssetId
        payValue.clearText()
        swapScreenState.update { SwapState.None }
    }

    fun setProvider(provider: SwapperProvider) {
        this.selectedProvider.update { provider }
    }

    fun refresh() {
        refresh.update { System.currentTimeMillis() }
    }

    fun swap(onConfirm: (ConfirmParams) -> Unit) {
        if (swapScreenState.value == SwapState.Swapping) return

        swapScreenState.update { SwapState.Swapping }

        viewModelScope.launch {
            try {
                val params = swap() ?: return@launch
                swapScreenState.update { SwapState.Ready }
                onConfirm(params)
            } catch (err: SwapError) {
                swapScreenState.update { SwapState.Error(err) }
            } catch (err: Throwable) {
                swapScreenState.update { SwapState.Error(SwapError.Unknown(err.message ?: "")) }
            }
        }
    }

    private suspend fun swap(): ConfirmParams? {
        val fromAmount = payValue.text.toString().parseNumberOrNull() ?: throw SwapError.IncorrectInput
        val quote = quote.value ?: throw SwapError.NoQuote
        val wallet = sessionRepository.getSession()?.wallet ?: return null

        val swapData = try {
            swapRepository.getQuoteData(quote.quote, wallet)
        } catch (_: Throwable) {
            throw SwapError.NoQuote
        }

        return ConfirmParams.SwapParams(
            from = quote.pay.owner!!,
            fromAsset = quote.pay.asset,
            toAsset = quote.receive.asset,
            fromAmount = Crypto(fromAmount, quote.pay.asset.decimals).atomicValue,
            toAmount = BigInteger(quote.quote.toValue),
            swapData = swapData.data,
            provider = quote.quote.data.provider.protocol,
            providerName = quote.quote.data.provider.name,
            protocolId = quote.quote.data.provider.protocolId,
            to = swapData.to,
            value = swapData.value,
            approval = swapData.approval?.toModel(),
            gasLimit = swapData.gasLimit?.toBigIntegerOrNull(),
            maxFrom = BigInteger(quote.pay.balance.balance.available) == Crypto(fromAmount, quote.pay.asset.decimals).atomicValue,
            etaInSeconds = quote.quote.etaInSeconds,
            slippageBps = quote.quote.data.slippageBps,
            walletAddress = quote.pay.owner?.address!!,

        )
    }

    private fun updateBalance(id: AssetId) {
        val session = sessionRepository.getSession() ?: return
        val account = session.wallet.getAccount(id.chain) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, account, id, true)
        }
    }

    private fun resetReceive() = viewModelScope.launch(Dispatchers.Main) {
        receiveValue.edit { replace(0, length, "0") }
    }

    private suspend fun setReceive(amount: String) {
        withContext(Dispatchers.Main) {
            receiveValue.edit { replace(0, length, amount) }
        }
    }
}