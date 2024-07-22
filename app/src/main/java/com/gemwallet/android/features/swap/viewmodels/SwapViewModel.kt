package com.gemwallet.android.features.swap.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.swap.SwapRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.swap.model.SwapError
import com.gemwallet.android.features.swap.model.SwapItemModel
import com.gemwallet.android.features.swap.model.SwapPairSelect
import com.gemwallet.android.features.swap.model.SwapPairUIModel
import com.gemwallet.android.features.swap.navigation.fromArg
import com.gemwallet.android.features.swap.navigation.toArg
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.format
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.SwapQuote
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

//    private val state = MutableStateFlow(State())
//    val uiState = state.map { it.toUIState() }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, SwapScreenState())

    private class SwapPairState(
        val fromId: AssetId? = null,
        val toId: AssetId? = null,
    )

    private class SwapAssetsState(
        val from: AssetInfo? = null,
        val to: AssetInfo? = null,
    )

    private val selectPair = MutableStateFlow<SwapPairSelect?>(null)
    val selectPairUiState = selectPair.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val swapPairState: StateFlow<SwapPairState?> = savedStateHandle.getStateFlow<String?>(fromArg, null)
        .combine(savedStateHandle.getStateFlow<String?>(toArg, null)) { from, to ->
            val fromId = from?.toAssetId()
            val toId = to?.toAssetId()
            /*TODO: Add sync flow*/
            /*if (fromId != null && toId != null) {
                updateBalances(fromId, toId)
            } else */if (fromId == null && toId == null) {
                selectPair.update { SwapPairSelect.request(fromId, toId) }
            }
            SwapPairState(fromId, toId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assetsState = swapPairState.flatMapLatest { ids ->
        if (ids?.fromId == null || ids.toId == null || ids.fromId.chain != ids.toId.chain || ids.fromId.toIdentifier() == ids.toId.toIdentifier()) {
            return@flatMapLatest emptyFlow()
        }
        combine(
            assetsRepository.getAssetInfo(ids.fromId),
            assetsRepository.getAssetInfo(ids.toId)
        ) { from, to ->
            SwapAssetsState(from = from, to = to)
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val swapPairUIModel = assetsState.mapNotNull { assets ->
        if (assets?.from == null || assets.to == null) {
            return@mapNotNull null
        }
        val symbolLength = max(assets.from.asset.symbol.length, assets.to.asset.symbol.length)
        SwapPairUIModel(
            from = SwapItemModel(
                asset = assets.from.asset.copy(symbol = assets.from.asset.symbol.padStart(symbolLength)),
                assetBalanceValue = assets.from.balances.calcTotal().value(assets.from.asset.decimals).stripTrailingZeros().toPlainString(),
                assetBalanceLabel = assets.from.asset.format(assets.from.balances.calcTotal(), 4),
            ),
            to = SwapItemModel(
                asset = assets.to.asset.copy(symbol = assets.to.asset.symbol.padStart(symbolLength)),
                assetBalanceValue = assets.to.balances.calcTotal().value(assets.to.asset.decimals).stripTrailingZeros().toPlainString(),
                assetBalanceLabel = assets.to.asset.format(assets.to.balances.calcTotal(), 4),
            ),
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val fromValue: TextFieldState = TextFieldState()
    private val fromValueFlow = snapshotFlow { fromValue.text }
    val fromEquivalent = fromValueFlow.combine(assetsState) { value, assets ->
        val price = assets?.from?.price ?: return@combine null
        val valueNum = try {
            value.toString().numberParse()
        } catch (err: Throwable) {
            BigDecimal.ZERO
        }
        val fiat = Fiat(valueNum.toDouble() * price.price.price)
        price.currency.format(fiat)
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val quote = fromValueFlow.combine(assetsState) { input, assets ->
        val value = try {
            input.toString().numberParse()
        } catch (err: Throwable) {
            BigDecimal.ZERO
        }
        Pair(value, assets)
    }
    .flatMapLatest {
        flow {
            if (it.second?.from == null || it.second?.to == null || it.first == BigDecimal.ZERO) {
                withContext(Dispatchers.Main) {
                    toValue.edit { replace(0, length, "0") }
                }
                emit(null)
                return@flow
            }
            calculatingQuote.update { true }
            delay(500L)
            val fromAsset = it.second?.from!!
            val toAsset = it.second?.to!!

            val quote = getQuote(fromAsset, toAsset, it.first)
            emit(quote)
            calculatingQuote.update { false }
        }
    }

    val toValue: TextFieldState = TextFieldState()
    val toEquivalent = combine(quote, assetsState) { quote, assets ->
        if (quote == null || assets == null || assets.to == null) {
            return@combine ""
        }
        val amount = assets.to.asset.format(Crypto(quote.toAmount), 8)
        toValue.edit { replace(0, length, amount) }
        val price = assets.to.price
        if (price?.currency != null && price.price.price > 0) {
            assets.to.price.currency.format(Crypto(quote.toAmount).convert(assets.to.asset.decimals, price.price.price))
        } else {
            ""
        }
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val allowance = quote.combine(assetsState) { quote, assets ->
        val spender = quote?.approval?.spender
        if (spender.isNullOrEmpty() || assets?.from == null || assets.from.asset.id.type() != AssetSubtype.TOKEN) {
            return@combine true
        }
        swapRepository.getAllowance(assets.from.id(), assets.from.owner.address, spender)
    }
    .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val sync = swapPairState.flatMapLatest {
        flow {
            updateBalances(it?.fromId ?: return@flow, it.toId ?: return@flow)
            emit(true)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val calculatingQuote = MutableStateFlow<Boolean>(false)
    val swapping = MutableStateFlow(false)
    val error = MutableStateFlow(SwapError.None)

    fun onSelect(select: SwapPairSelect) {
        if (select.sameChain()) {
            savedStateHandle[fromArg] = select.fromId?.toIdentifier()
            savedStateHandle[toArg] = select.toId?.toIdentifier()
            selectPair.update { null }
        } else {
            selectPair.update { select.opposite() }
        }
    }

    private suspend fun updateBalances(fromId: AssetId, toId: AssetId) {
        val session = sessionRepository.getSession() ?: return
        val account = session.wallet.getAccount(fromId.chain) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(account, fromId, true, session.currency)
        }
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(account, toId, true, session.currency)
        }
    }

    private suspend fun getQuote(from: AssetInfo, to: AssetInfo, amount: BigDecimal, includeData: Boolean = false): SwapQuote? {
        val quote = swapRepository.getQuote(
            from = from.asset.id,
            to = to.asset.id,
            ownerAddress = from.owner.address,
            amount = Crypto(amount, from.asset.decimals).atomicValue.toString(),
            includeData = includeData,
        )
        return quote?.quote
    }

    fun switchSwap() {
        onSelect(SwapPairSelect.From(swapPairState.value?.toId, swapPairState.value?.fromId))
        fromValue.clearText()
    }

    fun swap(onConfirm: (ConfirmParams) -> Unit) = viewModelScope.launch {
        if (swapping.value) {
            return@launch
        }
        swapping.update { true }
        val fromAmount = fromValue.text.toString().numberParse()  // TODO: Number parse could throw exception!
        val from = assetsState.value?.from ?: return@launch
        val to = assetsState.value?.to ?: return@launch
        val allowance = allowance.value
        val quote = getQuote(from, to, fromAmount, true)
        if (quote == null && allowance) {
            error.update { SwapError.NoQuote }
            return@launch
        }
        swapping.update { false }
        if (allowance) {
            if (quote == null) {
                return@launch
            }
            onConfirm(
                ConfirmParams.SwapParams(
                    fromAssetId = from.asset.id,
                    toAssetId = to.asset.id,
                    fromAmount = Crypto(fromAmount, from.asset.decimals).atomicValue,
                    toAmount = BigInteger(quote.toAmount),
                    swapData = quote.data?.data ?: return@launch,
                    provider = quote.provider.name,
                    to = quote.data?.to ?: return@launch,
                    value = quote.data?.value ?: return@launch,
                )
            )
        } else {
            val meta = swapRepository.encodeApprove(quote?.approval?.spender ?: "") // TODO: Move to special operator
            onConfirm(
                ConfirmParams.TokenApprovalParams(
                    assetId = if (from.asset.id.type() == AssetSubtype.TOKEN) from.asset.id else to.asset.id,
                    approvalData = meta.toHexString(),
                    provider = quote?.provider?.name ?: "",
                )
            )
        }
    }

//    private data class State(
//        val loading: Boolean = true,
//        val swapping: Boolean = false,
//        val error: SwapError = SwapError.None,
//        val currency: Currency = Currency.USD,
//        val payAsset: AssetInfo? = null,
//        val receiveAsset: AssetInfo? = null,
//        val quote: SwapQuote? = null,
//        val allowance: Boolean = false,
//        val payEquivalent: Fiat? = null,
//        val calculatingQuote: Boolean = false,
//        val select: SwapItemType? = null,
//        val selectedAssetId: AssetId? = null,
//    ) {
//        fun toUIState(): SwapScreenState {
//            val symbolLength = max(
//                payAsset?.asset?.symbol?.length ?: 0,
//                receiveAsset?.asset?.symbol?.length ?: 0
//            )
//            val payItem = getSwapItem(SwapItemType.Pay, payAsset, symbolLength)
//            val receiveItem = getSwapItem(SwapItemType.Receive, receiveAsset, symbolLength)
//            return SwapScreenState(
//                isLoading = loading,
//                details = when {
//                    payItem == null || receiveItem == null -> SwapDetails.None
//                    else -> SwapDetails.Quote(
//                        error = error,
//                        swaping = swapping,
//                        allowance = allowance,
//                        pay = payItem,
//                        receive = receiveItem,
//                    )
//                },
//                select = when {
//                    select == null -> null
//                    else -> SwapScreenState.Select(
//                        changeType = select,
//                        changeAssetId = when (select) {
//                            SwapItemType.Pay -> payItem?.asset?.id
//                            SwapItemType.Receive -> receiveItem?.asset?.id
//                        },
//                        oppositeAssetId = when (select) {
//                            SwapItemType.Pay -> receiveItem?.asset?.id
//                            SwapItemType.Receive -> payItem?.asset?.id
//                        },
//                        prevAssetId = selectedAssetId,
//                    )
//                }
//            )
//        }
//
//        private fun getSwapItem(itemType: SwapItemType, assetInfo: AssetInfo?, symbolLength: Int): SwapItemState? {
//            val asset = assetInfo?.asset ?: return null
//            val equivalentValue = when (itemType) {
//                SwapItemType.Pay -> (payEquivalent ?: Fiat(0.0)).format(0, currency.string, 2)
//                SwapItemType.Receive -> if ((assetInfo.price?.price?.price ?: 0.0) > 0 && quote != null) {
//                    currency.format(Crypto(quote.toAmount).convert(asset.decimals, assetInfo.price!!.price.price))
//                } else {
//                    ""
//                }
//            }
//            return SwapItemState(
//                type = itemType,
//                asset = asset.copy(symbol = asset.symbol.padStart(symbolLength)),
//                equivalentValue = equivalentValue,
//                assetBalanceValue = assetInfo.balances.calcTotal().value(asset.decimals).stripTrailingZeros().toPlainString(),
//                assetBalanceLabel = asset.format(assetInfo.balances.calcTotal(), 4),
//                calculating = itemType == SwapItemType.Receive && calculatingQuote
//            )
//        }
//    }
}