package com.gemwallet.features.swap.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.swap.GetSwapQuotes
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.model.toModel
import com.gemwallet.features.swap.viewmodels.cases.calculateEquivalent
import com.gemwallet.features.swap.viewmodels.cases.formatEquivalent
import com.gemwallet.features.swap.viewmodels.cases.getProviders
import com.gemwallet.features.swap.viewmodels.cases.refreshMachine
import com.gemwallet.features.swap.viewmodels.cases.requestQuotes
import com.gemwallet.features.swap.viewmodels.models.PriceImpact
import com.gemwallet.features.swap.viewmodels.models.PriceImpactType
import com.gemwallet.features.swap.viewmodels.models.QuoteRequestParams
import com.gemwallet.features.swap.viewmodels.models.QuoteState
import com.gemwallet.features.swap.viewmodels.models.SwapError
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.gemwallet.features.swap.viewmodels.models.SwapPairSelect
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import uniffi.gemstone.SwapperProvider
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import kotlin.math.absoluteValue

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SwapViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val swapRepository: SwapRepository,
    private val getSwapQuotes: GetSwapQuotes,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val payValue: TextFieldState = TextFieldState()
    val receiveValue: TextFieldState = TextFieldState()
    private val payValueFlow = snapshotFlow { payValue.text }
        .map { it.toString() }
        .onEach { swapScreenState.update { SwapState.GetQuote } }

    val swapScreenState = MutableStateFlow<SwapState>(SwapState.None)

    val selectPair = MutableStateFlow<SwapPairSelect?>(null)

    val selectedProvider = MutableStateFlow<SwapperProvider?>(null)

//    private val approveTxHash = MutableStateFlow<String?>(null)

    private val fromAssetId = savedStateHandle.getStateFlow<String?>("from", null)
        .map { it?.toAssetId() }
        .onEach { id -> id?.let { updateBalance(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val toAssetId = savedStateHandle.getStateFlow<String?>("to", null)
        .map { it?.toAssetId() }
        .onEach { id -> id?.let { updateBalance(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val payAsset = fromAssetId.filterNotNull()
        .flatMapLatest { assetsRepository.getAssetInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val receiveAsset = toAssetId.filterNotNull()
        .flatMapLatest { assetsRepository.getAssetInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val fromEquivalent = payValueFlow.combine(payAsset) { value, assetInfo ->
        assetInfo?.calculateEquivalent(value)
    }
    .filterNotNull()
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    val fromEquivalentFormatted = payValueFlow.combine(payAsset) { value, fromAsset ->
        fromAsset?.formatEquivalent(value) ?: ""
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val refreshTimer = payValueFlow.flatMapLatest { refreshMachine(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    private val refresh = MutableStateFlow(0L)

    private val quotes = combine(
        payValueFlow,
        payAsset,
        receiveAsset,
        refreshTimer,
        refresh,
    ) { value, fromAsset, toAsset, _, _ -> QuoteRequestParams.create(value, fromAsset, toAsset) }
    .onEach { it?.let { swapScreenState.update { SwapState.GetQuote } } ?: resetReceive() }
    .filterNotNull()
    .mapLatest { it }
    .onEach { delay(500) }
    .mapLatest { it.requestQuotes(getSwapQuotes) }
    .onEach { data ->
        if (data.err != null) {
            resetReceive()
            swapScreenState.update { SwapState.Error.create(data.err) }
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val providers = quotes.mapLatest { quotes -> quotes?.getProviders() ?: emptyList() }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val quote = quotes.combine(selectedProvider) { q, p ->
        q?.getQuote(p)?.let { QuoteState(it, q.pay, q.receive) }
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
        quote?.receive
            ?.price?.takeIf { it.price.price > 0 }
            ?.currency?.format(quote.receiveEquivalent)
            ?: ""
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val priceImpact = combine(fromEquivalent, quote, swapScreenState) { from, quote, state ->
        val receiveEquivalent = quote?.receiveEquivalent ?: BigDecimal.ZERO
        if (from <= BigDecimal.ZERO || receiveEquivalent <= BigDecimal.ZERO || state != SwapState.Ready) {
            return@combine null
        }
        val impact = (((receiveEquivalent.toDouble() / from.toDouble()) - 1.0) * 100)
        val isHigh = impact.absoluteValue > Config().getSwapConfig().highPriceImpactPercent.toDouble()
        when {
            impact < 0 -> PriceImpact(impact, PriceImpactType.Positive, isHigh)
            impact < 1 -> null
            impact < 5 -> PriceImpact(impact, PriceImpactType.Medium, isHigh)
            else -> PriceImpact(impact, PriceImpactType.High, isHigh)
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onSelect(select: SwapPairSelect) {
        val current = if (select.fromId == null || select.toId == null) {
            when (select) {
                is SwapPairSelect.From -> SwapPairSelect.From(
                    select.fromId,
                    toAssetId.value ?: select.toId
                )

                is SwapPairSelect.To -> SwapPairSelect.To(
                    fromAssetId.value ?: select.fromId, select.toId
                )
            }
        } else {
            select
        }
        val update = if (current.sameChain()) { // TODO: Change it validation
            savedStateHandle["from"] = current.fromId?.toIdentifier()
            savedStateHandle["to"] = current.toId?.toIdentifier()
            null
        } else {
            current.opposite()
        }
        selectPair.update { update }
    }

    fun switchSwap() {
        onSelect(SwapPairSelect.From(toAssetId.value, fromAssetId.value))
        payValue.clearText()
    }

    fun swap(onConfirm: (ConfirmParams) -> Unit) = viewModelScope.launch {
        if (swapScreenState.value == SwapState.Swapping) return@launch

        swapScreenState.update { SwapState.Swapping }
        val fromAmount = try {
            payValue.text.toString().numberParse()
        } catch (_: Throwable) {
            swapScreenState.update { SwapState.Error(SwapError.IncorrectInput) }
            return@launch
        }
        val from = payAsset.value ?: return@launch
        val to = receiveAsset.value ?: return@launch
        val quote = quote.value
        if (quote == null) {
            swapScreenState.update { SwapState.Error(SwapError.NoQuote) }
            return@launch
        }
        val wallet = sessionRepository.getSession()?.wallet ?: return@launch
        val swapData = try {
            swapRepository.getQuoteData(quote.quote, wallet)
        } catch (_: Throwable) {
            swapScreenState.update { SwapState.Error(SwapError.NoQuote) }
            return@launch
        }
        onConfirm(
            ConfirmParams.SwapParams(
                from = from.owner!!,
                fromAsset = from.asset,
                toAssetId = to.asset.id,
                fromAmount = Crypto(fromAmount, from.asset.decimals).atomicValue,
                toAmount = BigInteger(quote.quote.toValue),
                swapData = swapData.data,
                provider = quote.quote.data.provider.protocol,
                protocolId = quote.quote.data.provider.protocolId,
                to = swapData.to,
                value = swapData.value,
                approval = swapData.approval?.toModel(),
                gasLimit = swapData.gasLimit?.toBigIntegerOrNull(),
                maxFrom = BigInteger(from.balance.balance.available) == Crypto(fromAmount, from.asset.decimals).atomicValue
            )
        )
        swapScreenState.update { SwapState.Ready }
    }

    fun changePair(swapItemType: SwapItemType) {
        val fromId = if (swapItemType == SwapItemType.Pay) null else fromAssetId.value
        val toId = if (swapItemType == SwapItemType.Pay) toAssetId.value else null
        selectPair.update {
            val select = SwapPairSelect.From(fromId, toId)
            if (swapItemType == SwapItemType.Pay) select else select.opposite()
        }
    }

//    fun onTxHash(hash: String) {
//        approveTxHash.update { "${fromAssetId.value?.chain?.string}_$hash" }
//    }

    fun setProvider(provider: SwapperProvider) {
        this.selectedProvider.update { provider }
    }

    fun refresh() {
        refresh.update { System.currentTimeMillis() }
    }

    private fun updateBalance(id: AssetId) {
        val session = sessionRepository.getSession() ?: return
        val account = session.wallet.getAccount(id.chain) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, account, id, true)
        }
    }

    private suspend fun resetReceive() {
        withContext(Dispatchers.Main) {
            receiveValue.edit { replace(0, length, "0") }
        }
        swapScreenState.update { SwapState.None }
    }

    private suspend fun setReceive(amount: String) {
        withContext(Dispatchers.Main) { receiveValue.edit { replace(0, length, amount) } }
    }
}