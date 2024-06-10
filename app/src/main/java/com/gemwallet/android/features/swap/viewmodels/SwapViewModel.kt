package com.gemwallet.android.features.swap.viewmodels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.foundation.text2.input.forEachTextValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.data.swap.SwapRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.features.swap.model.SwapDetails
import com.gemwallet.android.features.swap.model.SwapError
import com.gemwallet.android.features.swap.model.SwapItemState
import com.gemwallet.android.features.swap.model.SwapItemType
import com.gemwallet.android.features.swap.model.SwapScreenState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.SwapQuote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class SwapViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val swapRepository: SwapRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SwapScreenState())
    val payValue: TextFieldState = TextFieldState()
    val receiveValue: TextFieldState = TextFieldState()

    private var quoteJob: Job? = null
    private var allowanceJob: Job? = null

    init {
        assetsRepository.subscribe(this::onRefreshAssets)
    }

    fun init(fromId: AssetId, toId: AssetId, onFail: () -> Unit = {}) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val session = sessionRepository.session
            val account = session?.wallet?.getAccount(fromId.chain)
            if (account == null) {
                withContext(Dispatchers.Main) {
                    onFail()
                }
                return@withContext
            }
            assetsRepository.updatePrices(session.currency, fromId, toId)
            assetsRepository.updateBalances(account, fromId, toId)
            loadData(fromId, toId)
        }
    }

    private fun loadData(fromId: AssetId, toId: AssetId) = viewModelScope.launch {
        val session = sessionRepository.session ?: return@launch
        val from = assetsRepository.getById(session.wallet, fromId).getOrNull()?.firstOrNull()
        val to = assetsRepository.getById(session.wallet, toId).getOrNull()?.firstOrNull()

        clearPayAmount()
        receiveValue.clearText()
        state.update {
            State(loading = false, payAsset = from, receiveAsset = to, quote = null)
        }
        updateAllowance(from?.owner?.address ?: return@launch, fromId)
    }

    private fun onRefreshAssets() {
        val fromAssetId = state.value.payAsset?.asset?.id ?: return
        val toAssetId = state.value.receiveAsset?.asset?.id ?: return
        loadData(fromAssetId, toAssetId)
    }

    suspend fun updateQuote() {
        payValue.forEachTextValue { newValue ->
            if (quoteJob?.isActive == true) {
                quoteJob?.cancel()
            }
            if (newValue.isEmpty() || newValue.toString() == "0") {
                receiveValue.edit {
                    replace(0, length, "0")
                }
                state.update { it.copy(quote = null, calculatingQuote = false) }
                return@forEachTextValue
            }
            val equivalent = try {
                Fiat(newValue.toString().numberParse().toDouble() * (state.value.payAsset?.price?.price ?: throw Exception()))
            } catch (err: Throwable) {
                null
            }
            state.update { it.copy(payEquivalent = equivalent, quote = null, calculatingQuote = true) }

            quoteJob = viewModelScope.launch(Dispatchers.IO) {
                delay(500L)
                val receiveAsset = state.value.receiveAsset ?: return@launch
                val quote = getQuote()
                receiveValue.edit {
                    replace(0, length, Crypto(quote?.toAmount ?: "0").format(receiveAsset.asset.decimals, "", 8))
                }
                state.update { it.copy(quote = quote, calculatingQuote = false, error = SwapError.None) }
            }
        }
    }

    private suspend fun getQuote(includeData: Boolean = false): SwapQuote? {
        val payAsset = state.value.payAsset
        val receiveAsset = state.value.receiveAsset
        if (payAsset == null || receiveAsset == null) {
            return null
        }
        val amount = try {
            getPayAmount()
        } catch (err: Throwable) {
            return null
        }

        val quote = swapRepository.getQuote(
            from = payAsset.asset.id,
            to = receiveAsset.asset.id,
            ownerAddress = payAsset.owner.address,
            amount = Crypto(amount, payAsset.asset.decimals).atomicValue.toString(),
            includeData = includeData,
        )
        return quote?.quote
    }

    fun switchSwap() {
        if (quoteJob?.isActive == true) {
            quoteJob?.cancel()
        }
        if (allowanceJob?.isActive == true) {
            allowanceJob?.cancel()
        }
        clearPayAmount()
        receiveValue.clearText()
        val receiveAsset = state.value.payAsset
        val payAsset = state.value.receiveAsset
        state.update {
            it.copy(payAsset = payAsset, receiveAsset = receiveAsset, quote = null, error = SwapError.None)
        }
        if (payAsset != null) {
            allowanceJob = updateAllowance(payAsset.owner.address, payAsset.asset.id)
        }
    }

    fun assetSelect(type: SwapItemType?) {
        state.update { it.copy(select = type) }
    }

    fun changeAsset(assetId: AssetId) {
        val prevSelected = state.value.selectedAssetId
        val fromId = when (state.value.select) {
            SwapItemType.Pay -> assetId
            SwapItemType.Receive -> prevSelected ?: state.value.payAsset?.asset?.id ?: return
            null -> return
        }
        val toId = when (state.value.select) {
            SwapItemType.Pay -> prevSelected ?: state.value.receiveAsset?.asset?.id ?: return
            SwapItemType.Receive -> assetId
            null -> return
        }
        if (fromId.chain != toId.chain) {
            state.update {
                it.copy(
                    selectedAssetId = assetId,
                    select = if (it.select == SwapItemType.Pay) SwapItemType.Receive else SwapItemType.Pay
                )
            }
        } else {
            state.update { State() }
            init(fromId, toId)
        }
    }

    private fun updateAllowance(ownerAddress: String, payAssetId: AssetId) = viewModelScope.launch {
        val allowance = swapRepository.getAllowance(assetId = payAssetId, owner = ownerAddress)
        state.update { it.copy(allowance = allowance) }
    }

    fun swap(
        onConfirm: (ConfirmParams) -> Unit,
    ) = viewModelScope.launch {
        if (state.value.swapping) {
            return@launch
        }
        state.update { it.copy(swapping = true) }
        val payAsset = state.value.payAsset ?: return@launch
        val receiveAsset = state.value.receiveAsset ?: return@launch
        val allowance = state.value.allowance
        val quote = getQuote(allowance)
        if (quote == null && allowance) {
            state.update {
                it.copy(loading = false, swapping = false, error = SwapError.NoQuote)
            }
            return@launch
        }
        state.update { it.copy(swapping = false) }
        if (allowance) {
            if (quote == null) {
                return@launch
            }
            onConfirm(
                ConfirmParams.SwapParams(
                    fromAssetId = payAsset.asset.id,
                    toAssetId = receiveAsset.asset.id,
                    fromAmount = Crypto(getPayAmount(), payAsset.asset.decimals).atomicValue,
                    toAmount = BigInteger(quote.toAmount),
                    swapData = quote.data?.data ?: return@launch,
                    provider = quote.provider.name,
                    to = quote.data?.to ?: return@launch,
                    value = quote.data?.value ?: return@launch,
                )
            )
        } else {
            val meta = swapRepository.encodeApprove() // TODO: Move to special operator
            onConfirm(
                ConfirmParams.TokenApprovalParams(
                    assetId = payAsset.asset.id,
                    approvalData = meta.toHexString(),
                    provider = quote?.provider?.name ?: "",
                )
            )
        }
    }

    private fun getPayAmount(): BigDecimal = payValue.text.toString().numberParse()

    private fun clearPayAmount() = payValue.clearText()

    private data class State(
        val loading: Boolean = true,
        val swapping: Boolean = false,
        val error: SwapError = SwapError.None,
        val currency: Currency = Currency.USD,
        val payAsset: AssetInfo? = null,
        val receiveAsset: AssetInfo? = null,
        val quote: SwapQuote? = null,
        val allowance: Boolean = false,
        val payEquivalent: Fiat? = null,
        val calculatingQuote: Boolean = false,
        val select: SwapItemType? = null,
        val selectedAssetId: AssetId? = null,
    ) {
        fun toUIState(): SwapScreenState {
            val symbolLength = max(
                payAsset?.asset?.symbol?.length ?: 0,
                receiveAsset?.asset?.symbol?.length ?: 0
            )
            val payItem = getSwapItem(SwapItemType.Pay, payAsset, symbolLength)
            val receiveItem = getSwapItem(SwapItemType.Receive, receiveAsset, symbolLength)
            return SwapScreenState(
                isLoading = loading,
                isFatal = !loading && (payItem == null || receiveItem == null),
                details = when {
                    payItem == null || receiveItem == null -> SwapDetails.None
                    else -> SwapDetails.Quote(
                        error = error,
                        swaping = swapping,
                        allowance = allowance,
                        pay = payItem,
                        receive = receiveItem,
                    )
                },
                select = when {
                    payItem == null || receiveItem == null || select == null -> null
                    else -> SwapScreenState.Select(
                        changeType = select,
                        changeAssetId = when (select) {
                            SwapItemType.Pay -> payItem.assetId
                            SwapItemType.Receive -> receiveItem.assetId
                        },
                        oppositeAssetId = when (select) {
                            SwapItemType.Pay -> receiveItem.assetId
                            SwapItemType.Receive -> payItem.assetId
                        },
                        prevAssetId = selectedAssetId,
                    )
                }
            )
        }

        private fun getSwapItem(itemType: SwapItemType, assetInfo: AssetInfo?, symbolLength: Int): SwapItemState? {
            val asset = assetInfo?.asset ?: return null
            val equivalentValue = when (itemType) {
                SwapItemType.Pay -> (payEquivalent ?: Fiat(0.0)).format(0, currency.string, 2)
                SwapItemType.Receive -> if ((assetInfo.price?.price ?: 0.0) > 0 && quote != null) {
                    Crypto(quote.toAmount).convert(asset.decimals, assetInfo.price!!.price).format(0, currency.string, 2)
                } else {
                    ""
                }
            }
            return SwapItemState(
                type = itemType,
                assetId = assetInfo.asset.id,
                assetType = asset.type,
                assetIcon = asset.getIconUrl(),
                assetSymbol = asset.symbol.padStart(symbolLength),
                equivalentValue = equivalentValue,
                assetBalanceValue = assetInfo.balances.calcTotal().value(asset.decimals).stripTrailingZeros().toPlainString(),
                assetBalanceLabel = assetInfo.balances.calcTotal().format(asset.decimals, asset.symbol, 4),
                calculating = itemType == SwapItemType.Receive && calculatingQuote
            )
        }
    }
}