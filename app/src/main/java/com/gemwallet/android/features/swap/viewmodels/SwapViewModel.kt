package com.gemwallet.android.features.swap.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.swap.models.PriceImpact
import com.gemwallet.android.features.swap.models.PriceImpactType
import com.gemwallet.android.features.swap.models.SwapError
import com.gemwallet.android.features.swap.models.SwapItemModel
import com.gemwallet.android.features.swap.models.SwapItemType
import com.gemwallet.android.features.swap.models.SwapPairSelect
import com.gemwallet.android.features.swap.models.SwapPairUIModel
import com.gemwallet.android.features.swap.models.SwapProviderItem
import com.gemwallet.android.features.swap.models.SwapState
import com.gemwallet.android.features.swap.navigation.pairArg
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.availableFormatted
import com.gemwallet.android.model.format
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.gemstone.SwapProvider
import uniffi.gemstone.SwapperException
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
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val swapScreenState = MutableStateFlow<SwapState>(SwapState.None)

    val selectPair = MutableStateFlow<SwapPairSelect?>(null)

    private val approveTxHash = MutableStateFlow<String?>(null)

    private val swapPairState: StateFlow<SwapPairState?> = savedStateHandle.getStateFlow<String?>(pairArg, null)
        .mapNotNull {
            val values = it?.split("|")
            listOf(
                values?.firstOrNull()?.toAssetId(),
                values?.lastOrNull()?.toAssetId()
            )
        }
        .map {
            val fromId = it.firstOrNull()
            val toId = it.lastOrNull()
            if (fromId == null || toId == null) {
                selectPair.update { SwapPairSelect.request(fromId, toId) }
            }
            SwapPairState(fromId, toId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assetsState = swapPairState.flatMapLatest { ids ->
        if (ids?.fromId == null || ids.toId == null) {
            return@flatMapLatest emptyFlow()
        }
        assetsRepository.getAssetsInfo(listOf(ids.fromId, ids.toId))
            .map { assets ->
                SwapAssetsState(
                    from = assets.firstOrNull { ids.fromId.toIdentifier() == it.id().toIdentifier() },
                    to = assets.firstOrNull { ids.toId.toIdentifier() == it.id().toIdentifier() },
                )
            }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val swapPairUIModel = assetsState.mapNotNull { assets ->
        if (assets?.from == null || assets.to == null) {
            return@mapNotNull null
        }
        val symbolLength = max(assets.from.asset.symbol.length, assets.to.asset.symbol.length)
        val payAvailableBalance = Crypto(assets.from.balance.balance.available).value(assets.from.asset.decimals)
            .stripTrailingZeros().toPlainString()
        val receiveAvailableBalance = Crypto(assets.to.balance.balance.available).value(assets.to.asset.decimals)
            .stripTrailingZeros().toPlainString()
        SwapPairUIModel(
            from = SwapItemModel(
                asset = assets.from.asset.copy(symbol = assets.from.asset.symbol.padStart(symbolLength)),
                assetBalanceValue = payAvailableBalance,
                assetBalanceLabel = assets.from.balance.availableFormatted(4),
            ),
            to = SwapItemModel(
                asset = assets.to.asset.copy(symbol = assets.to.asset.symbol.padStart(symbolLength)),
                assetBalanceValue = receiveAvailableBalance,
                assetBalanceLabel = assets.to.balance.availableFormatted(4),
            ),
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val fromValue: TextFieldState = TextFieldState()
    private val fromValueFlow = snapshotFlow { fromValue.text }.map { it.toString() }

    private val fromEquivalent = fromValueFlow.combine(assetsState) { value, assets ->
        swapScreenState.update { SwapState.GetQuote }
        val price = assets?.from?.price ?: return@combine null
        val valueNum = try {
            value.numberParse()
        } catch (_: Throwable) {
            BigDecimal.ZERO
        }
        valueNum.toDouble() * price.price.price
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val fromEquivalentFormatted = fromEquivalent.combine(assetsState) { value, assets ->
        if (value <= 0.0) {
            return@combine ""
        }
        val price = assets?.from?.price ?: return@combine ""
        price.currency.format(value)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val refreshTimer = fromValueFlow.flatMapLatest {
        if (it.isEmpty() || (it.toDoubleOrNull() ?: 0.0) == 0.0) {
            return@flatMapLatest emptyFlow<Long>()
        }
        flow {
            while (true) {
                delay(30 * 1000)
                emit(System.currentTimeMillis())
            }
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    private val refresh = MutableStateFlow(0L)

    private val quotes = combine(
        fromValueFlow,
        assetsState,
        refreshTimer,
        refresh,
    ) { fromValue, assets, _, _ -> Pair(fromValue, assets) }
    .mapLatest { data ->
        val assets = data.second
        val fromAsset = assets?.from
        val toAsset = assets?.to

        val fromValue = try {
            data.first.numberParse()
        } catch (_: Throwable) {
            BigDecimal.ZERO
        }
        if (fromAsset == null || toAsset == null || fromAsset.id() == toAsset.id() || fromValue.compareTo(BigDecimal.ZERO) == 0) {
            withContext(Dispatchers.Main) { toValue.edit { replace(0, length, "0") } }
            swapScreenState.update { SwapState.None }
            return@mapLatest emptyList()
        }
        if (fromAsset.balance.balanceAmount.available < fromValue.toDouble()) {
            swapScreenState.update { SwapState.Error(SwapError.InsufficientBalance(data.first)) }
            return@mapLatest emptyList()
        }
        swapScreenState.update { SwapState.GetQuote }
        delay(500L) // User input type - doesn't want spam nodes
        val quotes = try {
            swapRepository.getQuotes(
                from = fromAsset.asset.id,
                to = toAsset.asset.id,
                ownerAddress = fromAsset.owner!!.address,
                destination = toAsset.owner!!.address,
                amount = Crypto(fromValue, fromAsset.asset.decimals).atomicValue.toString(),
            )
        } catch (err: Throwable) {
            swapScreenState.update {
                SwapState.Error(
                    when (err) {
                        is SwapperException.NotSupportedAsset -> SwapError.NotSupportedAsset
                        is SwapperException.NotSupportedPair -> SwapError.NotSupportedPair
                        is SwapperException.NotSupportedChain -> SwapError.NotSupportedChain
                        is SwapperException.ComputeQuoteException,
                        is SwapperException.NoQuoteAvailable -> SwapError.NoQuote
                        is SwapperException.NotImplemented -> SwapError.NotImplemented
                        is SwapperException.NetworkException -> SwapError.NetworkError
                        is SwapperException.AbiException -> SwapError.NetworkError
                        is SwapperException.InvalidAddress,
                        is SwapperException.InvalidRoute,
                        is SwapperException.InvalidAmount  -> SwapError.IncorrectInput
                        else -> SwapError.Unknown(err.localizedMessage ?: err.message ?: "")
                    }
                )
            }
            return@mapLatest emptyList()
        }
        quotes ?: emptyList()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedProvider = MutableStateFlow<SwapProvider?>(null)

    val providers = quotes.combine(assetsState) { quotes, assets ->
        val price = assets?.to?.price
        quotes.map { quote ->
            val toValue = Crypto(quote.toValue)
            val fiat = if (price?.currency != null && price.price.price > 0) {
                price.currency.format(toValue.convert(assets.to.asset.decimals, price.price.price).value(0))
            } else {
                null
            }
            SwapProviderItem(
                swapProvider = quote.data.provider,
                price = assets?.to?.asset?.format(toValue, 2, dynamicPlace = true),
                fiat = fiat,
            )
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val quote = combine(quotes, assetsState, selectedProvider) { quotes, assets, provider ->
        val quote = quotes.firstOrNull { it.data.provider.id == provider } ?: quotes.firstOrNull()
        Pair(quote, assets)
    }
    .mapLatest {
        val quote = it.first ?: return@mapLatest null
        val assets = it.second ?: return@mapLatest null
        val amount = assets.to?.asset?.format(Crypto(quote.toValue), 8, showSymbol = false) ?: ""
        withContext(Dispatchers.Main) { toValue.edit { replace(0, length, amount) } }
        swapScreenState.update { SwapState.Ready }
        quote
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val rate = quote.combine(assetsState) { quote, assets ->
        val fromAsset = assets?.from?.asset
        val toAsset = assets?.to?.asset
        if (fromAsset == null || toAsset == null || quote == null) {
            return@combine null
        }
        val fromAmount = Crypto(quote.fromValue).value(fromAsset.decimals)
        val toAmount = Crypto(quote.toValue).value(toAsset.decimals)
        val rate = toAsset.format(toAmount / fromAmount, 2, dynamicPlace = true)
        "1 ${fromAsset.symbol} \u2248 $rate"
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentProvider = quote.mapLatest { SwapProviderItem(it?.data?.provider ?: return@mapLatest null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val toValue: TextFieldState = TextFieldState()

    private val toEquivalent = quote.combine(assetsState) { quote, assets ->
        if (quote == null || assets?.to == null) {
            return@combine BigDecimal.ZERO
        }
        val price = assets.to.price
        if (price?.currency != null && price.price.price > 0) {
            Crypto(quote.toValue).convert(assets.to.asset.decimals, price.price.price).value(0)
        } else {
            BigDecimal.ZERO
        }
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    val toEquivalentFormatted = toEquivalent.combine(assetsState) { value, assets ->
        if (value <= BigDecimal.ZERO  || assets?.to == null) {
            return@combine ""
        }
        val price = assets.to.price
        if (price?.currency != null && price.price.price > 0) {
            assets.to.price!!.currency.format(value)
        } else {
            ""
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val priceImpact = combine(fromEquivalent, toEquivalent, swapScreenState) { from, to, state ->
        if (from <= 0.0 || to <= BigDecimal.ZERO || state != SwapState.Ready) {
            return@combine null
        }
        val impact = (((to.toDouble() / from) - 1.0) * 100)
        when {
            impact > 0 -> PriceImpact(impact, PriceImpactType.Positive)
            impact > -1 -> null
            impact > -5 -> PriceImpact(impact, PriceImpactType.Medium)
            else -> PriceImpact(impact, PriceImpactType.High)
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val sync = swapPairState.flatMapLatest {
        flow {
            updateBalances(it?.fromId ?: return@flow, it.toId ?: return@flow)
            emit(true)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onSelect(select: SwapPairSelect) {
        val current = if (select.fromId == null || select.toId == null) {
            when (select) {
                is SwapPairSelect.From -> SwapPairSelect.From(
                    select.fromId,
                    swapPairState.value?.toId ?: select.toId
                )

                is SwapPairSelect.To -> SwapPairSelect.To(
                    swapPairState.value?.fromId ?: select.fromId, select.toId
                )
            }
        } else {
            select
        }
        val update = if (current.sameChain()) { // TODO: Change it validation
            savedStateHandle[pairArg] = "${current.fromId?.toIdentifier()}|${current.toId?.toIdentifier()}"
            null
        } else {
            current.opposite()
        }
        selectPair.update { update }
    }

    private fun updateBalances(fromId: AssetId, toId: AssetId) {
        val session = sessionRepository.getSession() ?: return
        val fromAccount = session.wallet.getAccount(fromId.chain) ?: return
        val toAccount = session.wallet.getAccount(toId.chain) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, fromAccount, fromId, true, session.currency)
        }
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, toAccount, toId, true, session.currency)
        }
    }

    fun switchSwap() {
        onSelect(SwapPairSelect.From(swapPairState.value?.toId, swapPairState.value?.fromId))
        fromValue.clearText()
    }

    fun swap(onConfirm: (ConfirmParams) -> Unit) = viewModelScope.launch {
        if (swapScreenState.value == SwapState.Swapping) return@launch

        swapScreenState.update { SwapState.Swapping }
        val fromAmount = try {
            fromValue.text.toString().numberParse()
        } catch (_: Throwable) {
            swapScreenState.update { SwapState.Error(SwapError.IncorrectInput) }
            return@launch
        }
        val from = assetsState.value?.from ?: return@launch
        val to = assetsState.value?.to ?: return@launch
        val quote = quote.value
        if (quote == null) {
            swapScreenState.update { SwapState.Error(SwapError.NoQuote) }
            return@launch
        }
        val wallet = sessionRepository.getSession()?.wallet ?: return@launch
        val swapData = swapRepository.getQuoteData(quote, wallet)
        onConfirm(
            ConfirmParams.SwapParams(
                from = from.owner!!,
                fromAssetId = from.asset.id,
                toAssetId = to.asset.id,
                fromAmount = Crypto(fromAmount, from.asset.decimals).atomicValue,
                toAmount = BigInteger(quote.toValue),
                swapData = swapData.data,
                provider = quote.data.provider.protocol,
                to = swapData.to,
                value = swapData.value,
                approval = swapData.approval,
                gasLimit = swapData.gasLimit?.toBigIntegerOrNull(),
            )
        )
        swapScreenState.update { SwapState.Ready }
    }

    fun changePair(swapItemType: SwapItemType) {
        val fromId = if (swapItemType == SwapItemType.Pay) null else swapPairState.value?.fromId
        val toId = if (swapItemType == SwapItemType.Pay) swapPairState.value?.toId else null
        selectPair.update {
            val select = SwapPairSelect.From(fromId, toId)
            if (swapItemType == SwapItemType.Pay) select else select.opposite()
        }
    }

    fun onTxHash(hash: String) {
        approveTxHash.update { "${assetsState.value?.from?.id()?.chain?.string}_$hash" }
    }

    fun setProvider(provider: SwapProvider) {
        this.selectedProvider.update { provider }
    }

    fun refresh() {
        refresh.update { System.currentTimeMillis() }
    }

    private data class SwapPairState(
        val fromId: AssetId? = null,
        val toId: AssetId? = null,
    )

    private data class SwapAssetsState(
        val from: AssetInfo? = null,
        val to: AssetInfo? = null,
    )
}